package vn.net.cbm.RWRMTN.internal.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.Map.Entry;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;
import org.cytoscape.work.util.ListSingleSelection;

import com.google.gson.Gson;

import vn.net.cbm.RWRMTN.internal.RESTmodel.ErrorMessage;
import vn.net.cbm.RWRMTN.internal.RESTmodel.RankedResult;
import vn.net.cbm.RWRMTN.internal.RESTmodel.rDisease;
import vn.net.cbm.RWRMTN.internal.model.Common;
import vn.net.cbm.RWRMTN.internal.model.MyUtils;

public class SelectDiseaseTask extends AbstractTask implements ObservableTask {
	CyNetwork network;
	CyNetworkFactory networkFactory;
	CyNetworkManager networkManager;
	public ArrayList<RankedResult> result;
	MyUtils myUtils;
	static final String READ_FROM_FILE="List of miRNA from file";
	static final String READ_FROM_LISTRNA="Manual input list of miRNA";
	

	@Tunable(description = "Input disease name", longDescription = "The disease to filter from Heterogeneous Network", exampleStringValue = "BREAST CANCER", groups = {
			"Select Disease"},required=true)
	public ListSingleSelection<String> diseaseName;
	@Tunable(
			description="Choose list of miRNAs to rank",longDescription = "Select to rank all miRNAs or a list of miRNA of interest", 
			exampleStringValue = "All miRNA", groups={"Input candidate miRNAs to rank"},required=true)
	public ListSingleSelection<String> candidateOption=new ListSingleSelection<>("All miRNAs in the miRNA-target network",READ_FROM_FILE,READ_FROM_LISTRNA);
	
	@Tunable(description="MiRNA file name",
			 groups={"Input candidate miRNAs to rank"},
			 params="input=true", 
			 dependsOn="candidateOption=" + READ_FROM_FILE)
	public File miRNAFile;
	

	@Tunable(description="Manual input list of miRNA", exampleStringValue="hsa-miR-107,hsa-miR-146a,hsa-miR-298",
			 groups={"Input candidate miRNAs to rank"},
			 params="input=true",
			 dependsOn="candidateOption=" + READ_FROM_LISTRNA)
	public String miRNAList;
	
	@Tunable(description = "Back probability", longDescription = "back-probability (alpha) of RWRH algorithm ", exampleStringValue = "0.5", groups = {
	"Parameters setting (for advanced users)"},required=false)
public ListSingleSelection<Float> backProb = new ListSingleSelection<Float>(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f,
	0.7f, 0.8f, 0.9f);

// @Tunable(description = "Jumping prob",longDescription="jumping
// probability (lamda) of RWRH algorithm ",
// exampleStringValue="0.6",
// groups = { "Step 3: Rank candidate genes and diseases in the
// heterogeneous network " })
// public ListSingleSelection<Float> jumpProb = new
// ListSingleSelection<Float>(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f,
// 0.8f, 0.9f);

@Tunable(description = "Sub-network importance weight", longDescription = "subnetwork importance (eta) of RWRH algorithm ", exampleStringValue = "0.5", groups = {
	"Parameters setting (for advanced users)"},required=false)
public ListSingleSelection<Float> subnetWeight = new ListSingleSelection<Float>(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f,
	0.7f, 0.8f, 0.9f);
	
	public SelectDiseaseTask(CyNetworkFactory networkFactory, CyNetworkManager networkManager, CyNetwork network, MyUtils myUtils) {
		super();
		this.network = network;
		this.networkFactory = networkFactory;
		this.networkManager = networkManager;
		this.myUtils=myUtils;
		
		List<String> diseaseList = new ArrayList<>();
		if (Common.ID2NameMap !=null){
			for (Entry<String, String> e:Common.ID2NameMap.entrySet()){
				diseaseList.add(e.getValue());
			}
			
			Collections.sort(diseaseList);
		}else{
			diseaseList.add("BREAST CANCER");
		}
		//diseaseName="BREAST CANCER";
		diseaseName=new ListSingleSelection<>(diseaseList);
		diseaseName.setSelectedValue("BREAST CANCER");
		backProb.setSelectedValue(0.5f);
		subnetWeight.setSelectedValue(0.5f);
	}

	@Override
	public void run(TaskMonitor arg0) throws IOException {
		// TODO Auto-generated method stub
//		try {
			if (Common.ID2NameMap==null){
				Map<String,Object> m=new HashMap<>();
				m.put("diseaseNetwork", "Phenotype2miRNAs");
				m.put("miRNANetwork", "HetermiRWalkNet (mutual)");
				
				myUtils.executeCommand("RWRMTN", "step1_load_network", m, null);
			}
			
			String DiseaseName = diseaseName.getSelectedValue();
			Common.DiseaseTerm = DiseaseName;

			if (!Common.ID2NameMap.containsValue(DiseaseName.toUpperCase())) {
				arg0.setStatusMessage(DiseaseName + "is not existed in the network");
				return;
			}
			
			ArrayList<String> arrUserMiRNA=new ArrayList<>();
			if (candidateOption.getSelectedValue().equals(READ_FROM_LISTRNA)){
				String[] listMiRNA=miRNAList.trim().split(",");
				if(listMiRNA.length>0){
					for (String str:listMiRNA){
						arrUserMiRNA.add(str.trim());
					}
				}
			}else if (candidateOption.getSelectedValue().equals(READ_FROM_FILE)){
				BufferedReader reader=new BufferedReader(new FileReader(miRNAFile));
				String line="";
				int count=0;
				while ((line=reader.readLine()) != null){
					count++;
					arrUserMiRNA.add(line.trim());
					System.out.println(count);
				}
				if (count==0){
					arg0.setStatusMessage("Textfile is empty. Check text file");
					return;
				}
			}
			
			float bP=(backProb.getSelectedValue()==0?0.5f:backProb.getSelectedValue());
			float sW=subnetWeight.getSelectedValue()==0?0.5f:subnetWeight.getSelectedValue();
			
			myUtils.rankDisease(DiseaseName, bP, sW,arrUserMiRNA);
			result=myUtils.fillPhenotypeTable(DiseaseName, networkFactory, networkManager);

//		} catch (Exception e) {
//			throw new NotFoundException("Disease not found", Response.status(Response.Status.NOT_FOUND)
//					.type(MediaType.APPLICATION_JSON).entity(new ErrorMessage("Cannot find disease")).build());
//
//		}
	}

	
	@ProvidesTitle
	public String getTitle() {
		return "Rank candidate miRNAs";
	}

	public static String getJson(ArrayList<RankedResult> listresult) {
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
