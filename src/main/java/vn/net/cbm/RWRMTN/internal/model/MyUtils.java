package vn.net.cbm.RWRMTN.internal.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskObserver;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import vn.net.cbm.RWRMTN.internal.RESTmodel.ErrorMessage;
import vn.net.cbm.RWRMTN.internal.RESTmodel.EvidenceResult;
import vn.net.cbm.RWRMTN.internal.RESTmodel.PMInfo;
import vn.net.cbm.RWRMTN.internal.RESTmodel.RankedResult;

public class MyUtils implements TaskObserver {
	final CyServiceRegistrar serviceRegistrar;
	CommandExecutorTaskFactory commandTaskFactory = null;
	SynchronousTaskManager<?> taskManager = null;

	Map<String, Double> NodeScore;
	Map<Integer, String> RankNode;
	ArrayList<String> arrUserMiRNA;

	public MyUtils(CyServiceRegistrar serviceRegistrar) {
		super();
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void allFinished(FinishStatus arg0) {
		// TODO Auto-generated method stub

	}

	public Map<String, Double> getNodeScore() {
		return NodeScore;
	}

	public Map<Integer, String> getRankNode() {
		return RankNode;
	}

	public void rankDisease(String DiseaseName, float backProb, float subnetWeight, ArrayList<String> arrUserMiRNA) {
		if (Common.ID2NameMap == null) {
			Map<String, Object> m = new HashMap<>();
			m.put("diseaseNetwork", "Phenotype2miRNAs");
			m.put("miRNANetwork", "HetermiRWalkNet (mutual)");

			executeCommand("RWRMTN", "step1_load_network", m, null);
		}
		this.arrUserMiRNA = arrUserMiRNA;
		String DiseaseID = Common.getKeyByValue(Common.ID2NameMap, DiseaseName);
		System.out.println(DiseaseID);

		Set<String> AssociatedmiRNAOnNetworkSet = new TreeSet<String>();
		AssociatedmiRNAOnNetworkSet.addAll(Common.Disease2miRNA2WeightMapMap.get(DiseaseID).keySet());
		AssociatedmiRNAOnNetworkSet.retainAll(Common.NetworkNodeSet);

		if (AssociatedmiRNAOnNetworkSet.size() <= 1) {// if Number of
														// training genes is
														// less than or
														// equal to 1, we
														// can not use leave
														// one out cross
														// validation
			throw new NotFoundException("Associated RNA not found",
					Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON)
							.entity(new ErrorMessage("Cannot find associated RNA of disease")).build());
		}

		int MaxRank = Common.miRNA2Gene2WeightMapMap.size() - AssociatedmiRNAOnNetworkSet.size() + 1;

		System.out.println("-> AssociatedmiRNAOnNetworkSet.toString(): " + AssociatedmiRNAOnNetworkSet.toString());

		// ArrayList<Integer> HoldoutRanks = new ArrayList<Integer>();
		NodeScore = new TreeMap<String, Double>();

		RankNode = Common.evaluateByRWRMTN(NodeScore, AssociatedmiRNAOnNetworkSet, Common.miRNA2Gene2WeightMapMap,
				Common.IncomingNodeTable, backProb, subnetWeight,arrUserMiRNA);
		System.out.println("\t- Finish prioritization process!");
	}

	public ArrayList<RankedResult> fillPhenotypeTable(String diseaseName, CyNetworkFactory networkFactory,
			CyNetworkManager networkManager) {
		// TODO Auto-generated method stub
		CyNetwork mynetwork = networkFactory.createNetwork();
		mynetwork.getRow(mynetwork).set(CyNetwork.NAME, diseaseName);

		CyTable nodeTable = mynetwork.getDefaultNodeTable();

		if (nodeTable.getColumn("Score") == null) {
			nodeTable.createColumn("Score", Double.class, false);
		}

		if (nodeTable.getColumn("Rank") == null) {
			nodeTable.createColumn("Rank", Integer.class, false);
		}

		if (nodeTable.getColumn("Type") == null) {
			nodeTable.createColumn("Type", String.class, false);
		}
		//
		if (nodeTable.getColumn("Known") == null) {
			nodeTable.createColumn("Known", Boolean.class, false);
		}

		// Vector<Object> RankedGene= new Vector<Object>();
		// System.out.println("PhenotypeData.size(): " + PhenotypeData.size());
		String DiseaseID = Common.getKeyByValue(Common.ID2NameMap, diseaseName);
		Set<String> AssRnaList = Common.Disease2miRNA2WeightMapMap.get(DiseaseID).keySet();

		// System.out.println(AssRnaList.size());
		ArrayList<RankedResult> result = new ArrayList<>();

		if (arrUserMiRNA.size()==0) {
			for (Entry<Integer, String> e : RankNode.entrySet()) {
				RankedResult rr = new RankedResult();

				CyNode node = mynetwork.addNode();
				CyRow row = nodeTable.getRow(node.getSUID());

				String rnaName = e.getValue();
				row.set("shared name", rnaName);
				row.set("Rank", e.getKey());
				row.set("Score", NodeScore.get(rnaName));

				rr.setName(rnaName);
				rr.setRank(e.getKey());
				rr.setScore(NodeScore.get(rnaName));

				if (rnaName.startsWith("hsa-")) {
					row.set("Type", "miRNA");
					rr.setType("miRNA");
				} else {
					row.set("Type", "Gene");
					rr.setType("Gene");
				}

				if (AssRnaList.contains(rnaName)) {
					row.set("Known", true);
					rr.setKnown(true);
				} else {
					row.set("Known", false);
					rr.setKnown(false);
				}

				result.add(rr);
			}
		} else {
			int count=0;
			for (Entry<Integer, String> e : RankNode.entrySet()) {
				RankedResult rr = new RankedResult();
				
				CyNode node = mynetwork.addNode();
				CyRow row = nodeTable.getRow(node.getSUID());

				String rnaName = e.getValue();
				row.set("shared name", rnaName);
				rr.setName(rnaName);
				count++;
				row.set("Rank", count);
				row.set("Score", NodeScore.get(rnaName));

				
				rr.setRank(count);
				rr.setScore(NodeScore.get(rnaName));

				if (rnaName.startsWith("hsa-")) {
					row.set("Type", "miRNA");
					rr.setType("miRNA");
				} else {
					row.set("Type", "Gene");
					rr.setType("Gene");
				}

				if (AssRnaList.contains(rnaName)) {
					row.set("Known", true);
					rr.setKnown(true);
				} else {
					row.set("Known", false);
					rr.setKnown(false);
				} 
				result.add(rr);
			}
			
			for (String UserMiRNA : arrUserMiRNA) {
				if (!RankNode.containsValue(UserMiRNA)){
					RankedResult rr = new RankedResult();
					
					CyNode node = mynetwork.addNode();
					CyRow row = nodeTable.getRow(node.getSUID());
					row.set("shared name", UserMiRNA);
					rr.setName(UserMiRNA);
					row.set("Type", "miRNA");
					rr.setType("miRNA");
					rr.setKnown(false);
					row.set("Known", false);
					result.add(rr);
				}
				
			}
			
		}
		networkManager.addNetwork(mynetwork);
		return result;
	}

	@Override
	public void taskFinished(ObservableTask arg0) {
		// TODO Auto-generated method stub

	}

	public void executeCommand(String namespace, String command, Map<String, Object> args, TaskObserver observer) {
		if (commandTaskFactory == null)
			commandTaskFactory = getService(CommandExecutorTaskFactory.class);

		if (taskManager == null)
			taskManager = getService(SynchronousTaskManager.class);
		TaskIterator ti = commandTaskFactory.createTaskIterator(namespace, command, args, observer);
		taskManager.execute(ti);
	}

	public <S> S getService(Class<S> serviceClass) {
		return serviceRegistrar.getService(serviceClass);
	}

}
