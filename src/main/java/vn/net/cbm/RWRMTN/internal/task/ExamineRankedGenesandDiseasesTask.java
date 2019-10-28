package vn.net.cbm.RWRMTN.internal.task;

import java.awt.Color;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.swing.JOptionPane;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.json.JSONResult;

import com.google.gson.Gson;

import vn.net.cbm.RWRMTN.internal.RESTmodel.EvidenceResult;
import vn.net.cbm.RWRMTN.internal.RESTmodel.RankedResult;
import vn.net.cbm.RWRMTN.internal.model.Common;

public class ExamineRankedGenesandDiseasesTask implements ObservableTask {
	CyNetworkFactory networkFactory;
	CyNetworkManager networkManager;
	CyNetwork network;
	TaskManager cyTaskManager;
	SynchronousTaskManager cySynchronousTaskManager;
	public static ArrayList<EvidenceResult> result;

	private volatile boolean interrupted = false;

	public ExamineRankedGenesandDiseasesTask(CyNetworkFactory networkFactory, CyNetworkManager networkManager,
			CyNetwork network, TaskManager cyTaskManager, SynchronousTaskManager cySynchronousTaskManager) {
		super();
		this.networkFactory = networkFactory;
		this.networkManager = networkManager;
		this.network = network;
		this.cyTaskManager = cyTaskManager;
		this.cySynchronousTaskManager = cySynchronousTaskManager;
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub
		interrupted = true;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		// TODO Auto-generated method stu
		// network_gene and network_disease
		CyNetwork network_rna = Common.getNetworkByName(networkManager, Common.DiseaseTerm);
		if (interrupted){
			return;
		}
		
		List<CyNode> selected_rnas = CyTableUtil.getNodesInState(network_rna, "selected", true);
		System.out.println(selected_rnas.size());
		if (selected_rnas.size()==0){
			taskMonitor.setStatusMessage("You should select at least one row in node table");
			//JOptionPane.showMessageDialog(null, "You should select at least one row to find evidence");
			return;
		}

		CyTable nodeTable = network_rna.getDefaultNodeTable();
		if (nodeTable.getColumn("PubMed (PudMedIDs)") == null) {
			nodeTable.createColumn("PubMed (PudMedIDs)", String.class, false);
		}
		
		result=new ArrayList<>();
		for (CyNode node : selected_rnas) {
			if (interrupted){
				return;
			}
			
			String RID = network_rna.getRow(node).get("shared name", String.class);

			taskMonitor.setStatusMessage("Finding evidences of association of Keyword \"" + Common.DiseaseTerm
					+ "\" and miRNA " + RID);
			System.out.println("Finding evidences of association of Keyword \"" + Common.DiseaseTerm + "\" and miRNA "
					+ RID);

			String searchterm = "(" + Common.DiseaseTerm + "[Title/Abstract] AND " + RID + "[Title/Abstract]) OR ("
					+ Common.DiseaseTerm + "[MeSH%20Terms] AND " + RID + ")";
			Set<String> IDs = new TreeSet<String>();
			System.out.println("PubMed query: " + searchterm);
			taskMonitor.setStatusMessage("Finding evidences of association of Keyword \"" + Common.DiseaseTerm
					+ "\" and miRNA " + RID + ". Currently found " + Common.numOfEvidence);
			
			EvidenceResult er=new EvidenceResult();
			er.setRnaName(RID);
			
			
			IDs = Common.getPubMedIDFromPubMedSearch(searchterm,er.getInfo());
			er.setPubMedIds(IDs);		
			result.add(er);
			if (IDs.size() > 0) {
				if (interrupted){
					return;
				}
				
				String IDsStr = IDs.toString().substring(1, IDs.toString().length() - 1);
				nodeTable.getRow(node.getSUID()).set("PubMed (PudMedIDs)", IDsStr);
				
			}
		}
		
	}
	
	public static String getJson(ArrayList<EvidenceResult> listresult) {
		return new Gson().toJson(listresult);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <R> R getResults(Class<? extends R> type) {
		// TODO Auto-generated method stub
		if (type.equals(String.class)) {
			return (R) getJson(result);
		} else if (type.equals(JSONResult.class)) {
			JSONResult res = () -> {
				return getJson(result);
			};
			return (R) (res);
		} else {
			return null;
		}
	}

	@Override
	public List<Class<?>> getResultClasses() {
		// TODO Auto-generated method stub
		return Arrays.asList(String.class, JSONResult.class);
	}
}
