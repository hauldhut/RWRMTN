/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.net.cbm.RWRMTN.internal.Base;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.swing.JOptionPane;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.session.CyNetworkNaming;

import vn.net.cbm.RWRMTN.internal.model.Common;

/**
 *
 * @author "trangtth"
 */
public class Resource {
	public Resource() {

	}

	public void loadNetwork() {
		try {   
                        String filename="";
                        if (Common.NetworkFileName.equals("miRWalk")){
                            filename="HetermiRWalkNet (mutual)";
                        }else{
                            filename="HeterTargetScanNet_mutual";
                        }
			InputStream is = getClass().getResourceAsStream("/" + filename + ".txt");
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String str = null;

			Common.NetworkInteractionArray = new ArrayList<Interaction>();
			Common.NetworkNodeArray = new ArrayList<String>();
			Common.NetworkNodeSet = new TreeSet<String>();
			Interaction inatemp;
			String srcnode = "";
			String dstnode = "";
			double weight = 0.0;
			System.out.println("Network data file is being loaded...!");

			while ((str = br.readLine()) != null) {
				String[] st = str.split("\t");
				// System.out.println(st.nextToken());

				srcnode = st[0].trim();
				String TypeOrWeight = st[1].trim();
				weight = Double.parseDouble(TypeOrWeight);
				dstnode = st[2].trim();

				inatemp = new Interaction();

				inatemp.NodeSrc = srcnode;
				inatemp.NodeDst = dstnode;
				inatemp.Weight = weight;

				if (weight > 0) {
					Common.NetworkInteractionArray.add(inatemp);
					Common.NetworkNodeSet.add(srcnode);
					Common.NetworkNodeSet.add(dstnode);
				}

			}

			Common.NetworkNodeArray.addAll(Common.NetworkNodeSet);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void readUserRNANetwork(CyNetworkManager cyNetworkManager) {
		CyNetwork rnaNet = Common.getNetworkByName(cyNetworkManager, Common.NetworkFileName);
		CyTable nodetable = rnaNet.getDefaultNodeTable();
		CyTable edgetable = rnaNet.getDefaultEdgeTable();

		Common.NetworkInteractionArray = new ArrayList<Interaction>();
		Common.NetworkNodeArray = new ArrayList<String>();
		Common.NetworkNodeSet = new TreeSet<String>();
		Interaction inatemp;

		for (CyEdge e : rnaNet.getEdgeList()) {
			inatemp = new Interaction();

			inatemp.NodeSrc = nodetable.getRow(e.getSource().getSUID()).get("shared name", String.class);
			inatemp.NodeDst = nodetable.getRow(e.getTarget().getSUID()).get("shared name", String.class);
			inatemp.Weight = edgetable.getRow(e.getSUID()).get("Weight", Double.class);

			if (inatemp.Weight > 0) {
				Common.NetworkInteractionArray.add(inatemp);
				Common.NetworkNodeSet.add(inatemp.NodeSrc);
				Common.NetworkNodeSet.add(inatemp.NodeDst);
			}
		}

		Common.NetworkNodeArray.addAll(Common.NetworkNodeSet);
	}

	public Map<String, Map<String, Double>> loadDisease2Nodes() {

		Map<String, Map<String, Double>> Disease2miRNA2WeightMapMap = new TreeMap<>();

		try {   
                        String filename="";
                        if (Common.DiseaseFileName.equals("miR2Disease")){
                            filename="Phenotype2miRNAs";
                        }else{
                            filename="Phenotype2miRNAs_HMDD";
                        }
			InputStream is = getClass().getResourceAsStream("/" + filename + ".txt");
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String str = "";

			ArrayList<Interaction> DiseaseInteractionArray = new ArrayList<Interaction>();

			Interaction inatemp;
			String srcnode = "";
			String dstnode = "";
			double weight = 0.0;

			Set<String> AllAssocmiRNASet = new TreeSet<>();
			while ((str = br.readLine()) != null) {
				String[] st = str.split("\t");
				// System.out.println(st.nextToken());

				srcnode = st[0].trim();
				String TypeOrWeight = st[1].trim();
				weight = Double.parseDouble(TypeOrWeight);
				dstnode = st[2].trim();

				AllAssocmiRNASet.add(dstnode);
				if (!Disease2miRNA2WeightMapMap.containsKey(srcnode)) {
					Map<String, Double> miRNA2WeightMap = new TreeMap<>();
					miRNA2WeightMap.put(dstnode, weight);
					Disease2miRNA2WeightMapMap.put(srcnode, miRNA2WeightMap);
				} else {
					Disease2miRNA2WeightMapMap.get(srcnode).put(dstnode, weight);
				}
			}
			br.close();

			System.out.println("Total Diseases: " + Disease2miRNA2WeightMapMap.size());
			System.out.println("AllAssocmiRNASet.size(): " + AllAssocmiRNASet.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Disease2miRNA2WeightMapMap;
	}

	public Map<String, String> loadAllLowerNodeInfo(String FileName) {
		Map<String, String> ID2NameMap = new TreeMap<String, String>();
		try {
			InputStream is = getClass().getResourceAsStream("/" + FileName);
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String str = "";

			while ((str = br.readLine()) != null) {
				// System.out.println(numofina + ": " + str);
				String[] st = str.split("\t");
				// System.out.println(st.nextToken());
				if (Common.Disease2miRNA2WeightMapMap.containsKey(st[0])) {
					ID2NameMap.put(st[0], st[3]);
				}
			}
			br.close();
			System.out.println("ID2NameMap.size(): " + ID2NameMap.size());

		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Error while loading " + " Database: " + e.toString());
			e.printStackTrace();
		}
		return ID2NameMap;
	}
}
