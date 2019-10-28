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
import java.util.List;
import java.util.Map;
import java.util.Set;
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

import vn.net.cbm.RWRMTN.internal.RESTmodel.ErrorMessage;
import vn.net.cbm.RWRMTN.internal.model.Common;




/**
 *
 * @author "MinhDA"
 */
public class LoadmiRNANetworkTask implements Task {

    private volatile boolean interrupted = false;
    private CyNetworkFactory cyNetworkFactory;
    private CyNetworkNaming cyNetworkNaming;
    CyNetworkManager cyNetworkManager;
    public static CyNetwork curNet;

    public LoadmiRNANetworkTask(CyNetworkFactory cyNetworkFactory, CyNetworkNaming cyNetworkNaming, CyNetworkManager cyNetworkManager) {
        this.cyNetworkFactory = cyNetworkFactory;
        this.cyNetworkNaming = cyNetworkNaming;
        this.cyNetworkManager = cyNetworkManager;
    }

    
    
    @Override
    public void run(TaskMonitor taskMonitor) {
        try {
            CyNetwork miRNANet = cyNetworkFactory.createNetwork() ;
            miRNANet.getRow(miRNANet).set("name", Common.NetworkFileName);
            
            miRNANet.getDefaultNetworkTable().createColumn("Type", String.class, false);
            miRNANet.getRow(miRNANet).set("Type", "Micro RNA/Protein Network");
            miRNANet.getDefaultNetworkTable().getRow(miRNANet.getSUID()).set("Type", "MicroRNA Network");
            CyTable nodeTable = miRNANet.getDefaultNodeTable();
            if (nodeTable.getColumn("Type") == null) {
    			nodeTable.createColumn("Type", String.class, false);
    		}
            
    		ArrayList<String> arr=Common.NetworkNodeArray;
    		Map<String,Long> allNodes=new HashMap<String,Long>();
    		for (int i = 0; i < arr.size(); i++) {
    			CyNode node = miRNANet.addNode();
    			CyRow row = nodeTable.getRow(node.getSUID());
    			allNodes.put(arr.get(i), node.getSUID());
    			row.set("shared name", arr.get(i));
    			row.set("Type", "Gene");
    			row.set("selected", true);
    		}
    		
    		CyTable edgeTable = miRNANet.getDefaultEdgeTable();
    		


//    		if (edgeTable.getColumn("Source Node") == null) {
//    			edgeTable.createColumn("Source Node", String.class, false);
//    		}
//
//    		if (edgeTable.getColumn("Destination Node") == null) {
//    			edgeTable.createColumn("Destination Node", String.class, false);
//    		}
    		
    		if (edgeTable.getColumn("Weight") == null) {
    			edgeTable.createColumn("Weight", Double.class, false);
    		}
    		
    		for(Entry<String, Map<String,Double>> e: Common.miRNA2Gene2WeightMapMap.entrySet()){
                
                CyNode oriNode=miRNANet.getNode(allNodes.get(e.getKey()));
                nodeTable.getRow(oriNode.getSUID()).set("Type", "miRNA");
                for (Entry<String, Double> e2:e.getValue().entrySet()){
                	CyNode destNode=miRNANet.getNode(allNodes.get(e2.getKey()));
                	CyEdge edge=miRNANet.addEdge(oriNode, destNode, false);
                	miRNANet.getRow(edge).set("shared name", e.getKey()+" (interacts with) "+e2.getKey());
//                	miRNANet.getRow(edge).set("Source Node", e.getKey());
//                	miRNANet.getRow(edge).set("Destination Node", str);
                	miRNANet.getRow(edge).set("Weight", e2.getValue());
                }
            } 
    		
    		cyNetworkManager.addNetwork(miRNANet);
   

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
