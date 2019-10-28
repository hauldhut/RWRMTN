package vn.net.cbm.RWRMTN.internal.RESTmodel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.Map.Entry;

import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;

import vn.net.cbm.RWRMTN.internal.Base.Disease;
import vn.net.cbm.RWRMTN.internal.model.Common;
import vn.net.cbm.RWRMTN.internal.model.MyUtils;

public class autoRWRMTNImp implements autoRWRMTN {
	
	MyUtils mu;
	Set<String> AssRnaList;
	public autoRWRMTNImp(MyUtils mu) {
		super();
		this.mu = mu;
	}
         
	@Override
	public ArrayList<RankOutput> selectRankDisease(RankParameters rp) {
		// TODO Auto-generated method stub
		Map<String,Object> m=new HashMap<>();
//		String diseaseNetwork=null;
//		String miRNetwork=null;
//		if (rp.miRTargetDB==null){
//			miRNetwork="HetermiRWalkNet (mutual)";
//		}else	if (rp.miRTargetDB.equals("miRWalk")) {
//			miRNetwork = "HetermiRWalkNet (mutual)";
//		} else if (rp.miRTargetDB.equals("TargetScan")) {
//			miRNetwork = "HeterTargetScanNet_mutual";
//		}
//		
//		
//		if (rp.miR2DiseaseDB==null){
//			diseaseNetwork="Phenotype2miRNAs";
//		} else if (rp.miR2DiseaseDB.equals("miR2Disease")) {
//			diseaseNetwork = "Phenotype2miRNAs";
//		} else if (rp.miR2DiseaseDB.equals("HMDD")) {
//			diseaseNetwork = "Phenotype2miRNAs_HMDD";
//		}
		
		m.put("miR2DiseaseDB", rp.miR2DiseaseDB);
		m.put("miRTargetDB", rp.miRTargetDB);
			
		mu.executeCommand("RWRMTN", "step1_load_datasets", m, null);
		
		String DiseaseID = rp.diseaseOMIMID;
		String DiseaseName = Common.ID2NameMap.get(DiseaseID);
		Common.DiseaseTerm = DiseaseName;
		
		
		AssRnaList = Common.Disease2miRNA2WeightMapMap.get(DiseaseID).keySet();
		ArrayList<String> arrUserMiRNA=new ArrayList<>();
		if (!rp.listOfmiRNAs.equals("All miRNAs")){		
			File f=new File(rp.listOfmiRNAs);
			if (f.exists() && f.isFile()){
				BufferedReader reader;
				try {
					reader = new BufferedReader(new FileReader(f));
					String line="";
					int count=0;
					while ((line=reader.readLine()) != null){
						count++;
						arrUserMiRNA.add(line.trim());
						System.out.println(count);
					}
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}else{
				String[] listMiRNA=rp.listOfmiRNAs.trim().split(",");
				if(listMiRNA.length>0){
					for (String str:listMiRNA){
						arrUserMiRNA.add(str.trim());
					}
				}
			}
		}
		System.out.println(arrUserMiRNA.size());
		
		float bP=(rp.backProb==0?0.5f:rp.backProb);
		float sW=rp.subnetWeight==0?0.5f:rp.subnetWeight;
		mu.rankDisease(DiseaseName, bP, sW,arrUserMiRNA);
		
		Map<String, Double> NodeScore=mu.getNodeScore();
		Map<Integer, String> RankNode=mu.getRankNode();
		
		ArrayList<RankOutput> arr=new ArrayList<>();
		if (arrUserMiRNA.size()==0) {
			for (Entry<Integer, String> e : RankNode.entrySet()) {
				RankOutput rr = new RankOutput();

				String rnaName = e.getValue();

				rr.rnaName=rnaName;
				rr.rnaRank=e.getKey();
				rr.rnaScore=NodeScore.get(rnaName);

				if (rnaName.startsWith("hsa-")) {
					rr.type="miRNA";
				} else {
					rr.type="Gene";
				}

				if (AssRnaList.contains(rnaName)) {
					rr.known=true;
				} else {
					rr.known=false;
				}
				arr.add(rr);
			}
		} else {
			int count=0;
			for (Entry<Integer, String> e : RankNode.entrySet()) {
				RankOutput rr = new RankOutput();
				
				String rnaName = e.getValue();
				rr.rnaName=rnaName;	
				count++;
				rr.rnaRank=count;
				rr.rnaScore=NodeScore.get(rnaName);

				if (rnaName.startsWith("hsa-")) {
					rr.type="miRNA";
				} else {
					rr.type="Gene";
				}

				if (AssRnaList.contains(rnaName)) {
					rr.known=true;
				} else {
					rr.known=false;
				} 
				arr.add(rr);
			}
			
			for (String UserMiRNA : arrUserMiRNA) {
				if (!RankNode.containsValue(UserMiRNA)){
					RankOutput rr = new RankOutput();
					rr.rnaName=UserMiRNA;
					rr.type="miRNA";
					rr.known=false;
					arr.add(rr);
				}
				
			}
		}
		return arr;
	}

	@Override
	public List<rDisease> getDiseaseList() {
		// TODO Auto-generated method stub
		if (Common.ID2NameMap==null){
			Map<String,Object> m=new HashMap<>();
			m.put("diseaseNetwork", "Phenotype2miRNAs");
			m.put("miRNANetwork", "HetermiRWalkNet (mutual)");
			
			mu.executeCommand("RWRMTN", "step1_load_network", m, null);
		}
		List<rDisease> arr=new ArrayList<>();
		for (Entry<String, String> e:Common.ID2NameMap.entrySet()){
			rDisease d=new rDisease();
			d.diseaseID=e.getKey();
			d.diseaseName=e.getValue();
			
			arr.add(d);
		}
		return arr;
	}


	@Override
	public List<RankOutput> getRankedGenes(int limit) {
		// TODO Auto-generated method stub
		ArrayList<RankOutput> arr=new ArrayList<>();
		Map<String, Double> NodeScore=mu.getNodeScore();
		Map<Integer, String> RankNode=mu.getRankNode();
		int i=0;
		for (Entry<Integer, String> e:RankNode.entrySet()){
			i++;
			RankOutput r=new RankOutput();
			String rnaName = e.getValue();
			r.rnaRank=e.getKey();
			r.rnaName=rnaName;
			r.rnaScore=NodeScore.get(rnaName);		
			r.type="miRNA";
			
			if (AssRnaList.contains(rnaName)) {
				r.known=true;
			} else {
				r.known=false;
			}
			
			arr.add(r);
			if (i==limit){
				break;
			}
		}
		return arr;
	}

	@Override
	public ArrayList<rDisease> getDiseaseId(String diseaseName) {
		// TODO Auto-generated method stub
		if (Common.ID2NameMap==null){
			Map<String,Object> m=new HashMap<>();
			m.put("diseaseNetwork", "Phenotype2miRNAs");
			m.put("miRNANetwork", "HetermiRWalkNet (mutual)");
			
			mu.executeCommand("RWRMTN", "step1_load_network", m, null);
		}
		ArrayList<rDisease> arr=new ArrayList<>();
		String[] DiseaseNameToken = diseaseName.split(" ");

		
		for (Entry<String, String> e:Common.ID2NameMap.entrySet()){
			for (String str:DiseaseNameToken){
				if(e.getValue().toLowerCase().contains(str.toLowerCase())){
					rDisease d=new rDisease();
					d.diseaseID=e.getKey();
					d.diseaseName=e.getValue();	
					arr.add(d);
					break;
				}
			}	
		}
		return arr;
	}


}
