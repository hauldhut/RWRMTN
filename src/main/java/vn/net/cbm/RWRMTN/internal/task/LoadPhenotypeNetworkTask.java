/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.net.cbm.RWRMTN.internal.task;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.swing.JOptionPane;
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
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;

import vn.net.cbm.RWRMTN.internal.Base.Disease;
import vn.net.cbm.RWRMTN.internal.RESTmodel.ErrorMessage;
import vn.net.cbm.RWRMTN.internal.model.Common;




/**
 *
 * @author ""
 */
public class LoadPhenotypeNetworkTask implements Task {

    private volatile boolean interrupted = false;
    private CyNetworkFactory cyNetworkFactory;
    private CyNetworkNaming cyNetworkNaming;
    CyNetworkManager cyNetworkManager;
    public static CyNetwork curNet;

    public LoadPhenotypeNetworkTask(CyNetworkFactory cyNetworkFactory, CyNetworkNaming cyNetworkNaming, CyNetworkManager cyNetworkManager) {
        this.cyNetworkFactory = cyNetworkFactory;
        this.cyNetworkNaming = cyNetworkNaming;
        this.cyNetworkManager = cyNetworkManager;
    }

    
    
    @Override
    public void run(TaskMonitor taskMonitor) {
        try {
        	 //========Load Diseases and associated miRNAs
            
            CyNetwork PhenoTypeNetwork = cyNetworkFactory.createNetwork() ;
            Map<String,Long> allDiseaseNodes=new TreeMap<String,Long>();
            Map<String,Long> allRNANodes=new TreeMap<String,Long>();
            
            PhenoTypeNetwork.getRow(PhenoTypeNetwork).set("name",Common.DiseaseFileName);
            
            PhenoTypeNetwork.getDefaultNetworkTable().createColumn("Type", String.class, false);
            PhenoTypeNetwork.getRow(PhenoTypeNetwork).set("Type", "Disease/miRNA Network");
            PhenoTypeNetwork.getDefaultNetworkTable().getRow(PhenoTypeNetwork.getSUID()).set("Type", "Disease Network");
            CyTable nodeTable = PhenoTypeNetwork.getDefaultNodeTable();
            CyTable edgeTable = PhenoTypeNetwork.getDefaultEdgeTable();
            
            if (nodeTable.getColumn("Type") == null) {
    			nodeTable.createColumn("Type", String.class, false);
    		}
            if (edgeTable.getColumn("Weight") == null) {
    			edgeTable.createColumn("Weight", Double.class, false);
    		}

    		Map<String, Map<String, Double>> arr=Common.Disease2miRNA2WeightMapMap;
    		for (Entry<String, Map<String, Double>> d:arr.entrySet()) {
    			String src=d.getKey();
    			CyNode node = PhenoTypeNetwork.addNode();
    			CyRow row = nodeTable.getRow(node.getSUID());
    			allDiseaseNodes.put(src, node.getSUID());
    		
    			row.set("shared name", src);    
    			row.set(CyNetwork.NAME, Common.ID2NameMap.get(src));
    			row.set("Type", "Disease");

    			for (Entry<String, Double> e:d.getValue().entrySet()){
    				String dest=e.getKey();
    				double weight=e.getValue();
    				if (allRNANodes.containsKey(dest)){
    					CyEdge edge=PhenoTypeNetwork.addEdge(node, PhenoTypeNetwork.getNode(allRNANodes.get(dest)), false);
	                	PhenoTypeNetwork.getRow(edge).set("shared name", src+" (interacts with) "+dest);
	                	PhenoTypeNetwork.getRow(edge).set("Weight",weight);
    				}else{
	        			CyNode RNAnode = PhenoTypeNetwork.addNode();
	        			CyRow RNArow = nodeTable.getRow(RNAnode.getSUID());
	        			allRNANodes.put(dest, RNAnode.getSUID());
	        			RNArow.set("shared name", dest);
	        			RNArow.set(CyNetwork.NAME, dest);
	        			RNArow.set("Type", "miRNA");
	        			
	        			CyEdge edge=PhenoTypeNetwork.addEdge(node, RNAnode, false);
	                	PhenoTypeNetwork.getRow(edge).set("shared name", src+" (interacts with) "+dest);
	                	PhenoTypeNetwork.getRow(edge).set("Weight",weight);
    				}
        		}
    		}
    		
    		
    		
//    		if (edgeTable.getColumn("Source Node") == null) {
//    			edgeTable.createColumn("Source Node", String.class, false);
//    		}
//
//    		if (edgeTable.getColumn("Destination Node") == null) {
//    			edgeTable.createColumn("Destination Node", String.class, false);
//    		}
    		cyNetworkManager.addNetwork(PhenoTypeNetwork);
    		
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error while loading Disease Network: " + e.toString());
            throw new NotFoundException("Data resources not found",Response.status(Response.Status.NOT_FOUND)
            		.type(MediaType.APPLICATION_JSON).entity(new ErrorMessage("Error while loading data resources")).build()); 
       
        }
    }

    @Override
    public void cancel() {
        this.interrupted = true;
    }

}
