package vn.net.cbm.RWRMTN.internal.task;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.concurrent.atomic.LongAdder;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;
import org.cytoscape.work.util.ListSingleSelection;

import com.google.gson.Gson;

import vn.net.cbm.RWRMTN.internal.Base.Disease;
import vn.net.cbm.RWRMTN.internal.Base.Interaction;
import vn.net.cbm.RWRMTN.internal.Base.NodeInteraction;
import vn.net.cbm.RWRMTN.internal.Base.Resource;
import vn.net.cbm.RWRMTN.internal.RESTmodel.ErrorMessage;
import vn.net.cbm.RWRMTN.internal.model.Common;
import vn.net.cbm.RWRMTN.internal.model.Messages;

public class BuildNetworkTask implements ObservableTask {

    CyNetwork network;
    private volatile boolean interrupted;
    private TaskManager cyTaskManager;
    private CyNetworkManager cyNetworkManager;
    private CyNetworkReaderManager cyNetworkReaderManager;
    private CyNetworkFactory cyNetworkFactory;
    private CyNetworkNaming namingUtil;
    private SynchronousTaskManager cySynchronousTaskManager;

    @Tunable(description = "Choose miRNA-target interaction dataset", exampleStringValue = "TargetScan", context = Tunable.BOTH_CONTEXT)
    public ListSingleSelection<String> miRTargetDB;

    @Tunable(description = "Choose known disease-microRNA association dataset", exampleStringValue = "miR2Disease", context = Tunable.BOTH_CONTEXT)
    public ListSingleSelection<String> miR2DiseaseDB;

    public BuildNetworkTask(SynchronousTaskManager cySynchronousTaskManager, TaskManager cyTaskManager, CyNetwork arg0,
            CyNetworkManager cyNetworkManager, CyNetworkReaderManager cyNetworkReaderManager,
            CyNetworkFactory cyNetworkFactory, CyNetworkNaming namingUtil) {
        super();
        this.cyTaskManager = cyTaskManager;
        this.cyNetworkManager = cyNetworkManager;
        this.cyNetworkReaderManager = cyNetworkReaderManager;
        this.cyNetworkFactory = cyNetworkFactory;
        this.namingUtil = namingUtil;
        this.cySynchronousTaskManager = cySynchronousTaskManager;
        network = arg0;

        List<String> RNAList = new ArrayList<>();
        List<String> diseaseList = new ArrayList<>();
        RNAList.add("TargetScan");
        RNAList.add("miRWalk");
        diseaseList.add("miR2Disease");
        diseaseList.add("HMDD");

        // miRWalk - TargetScan
        // miR2Disease - HMDD
        for (CyNetwork net : cyNetworkManager.getNetworkSet()) {
            RNAList.add(net.getRow(net).get("name", String.class));
        }

        miRTargetDB = new ListSingleSelection<>(RNAList);
        miR2DiseaseDB = new ListSingleSelection<>(diseaseList);

        miRTargetDB.setSelectedValue("TargetScan");
        miR2DiseaseDB.setSelectedValue("miR2Disease");
    }

    @Override
    public void run(TaskMonitor taskMonitor) throws Exception {
        try {

            boolean directed = false;
            Common.NetworkFileName = miRTargetDB.getSelectedValue();

            taskMonitor.setStatusMessage("Network data file is being loaded...");

            // ========Load Network===============
            if (Common.NetworkFileName.equals("miRWalk") || Common.NetworkFileName.equals("TargetScan")) {

                // NetworkFileName = "Data\\Networks\\HeterNet\\HetermiRWalkNet
                // (mutual).txt";
                // Common.NetworkFileName = "HeterTargetScanNet_mutual.txt";
                new Resource().loadNetwork();
            } else {
                new Resource().readUserRNANetwork(cyNetworkManager);

            }

            if (interrupted) {
                return;
            }

            System.out.println("Number of Nodes: " + Common.NetworkNodeSet.size());
            System.out.println("Number of Links: " + Common.NetworkInteractionArray.size());
            // ========Load miRNA 2 target genes=============
            taskMonitor.setStatusMessage("Loading miRNA to target genes...");
            // Common.miRNA2GeneMap = new TreeMap<String,Set<String>> ();
            Common.miRNA2Gene2WeightMapMap = new TreeMap<String, Map<String, Double>>();
            for (int i = 0; i < Common.NetworkInteractionArray.size(); i++) {

                Interaction ina = Common.NetworkInteractionArray.get(i);

                if (Common.miRNA2Gene2WeightMapMap.containsKey(ina.NodeSrc)) {
                    // Common.miRNA2GeneMap.get(ina.NodeSrc).add(ina.NodeDst);
                    Common.miRNA2Gene2WeightMapMap.get(ina.NodeSrc).put(ina.NodeDst, ina.Weight);
                } else {
                    Map<String, Double> Gene2WeightMap = new TreeMap<>();
                    Gene2WeightMap.put(ina.NodeDst, ina.Weight);
                    Common.miRNA2Gene2WeightMapMap.put(ina.NodeSrc, Gene2WeightMap);

                    // Set<String> TargetGeneSet = new TreeSet<String>();
                    // TargetGeneSet.add(ina.NodeDst);
                    // Common.miRNA2GeneMap.put(ina.NodeSrc, TargetGeneSet);
                }
            }

            if (interrupted) {
                return;
            }

            System.out.println("miRNA2GeneMap.size(): " + Common.miRNA2Gene2WeightMapMap.size());

            // for(Entry<String, Set<String>> e:
            // Common.miRNA2GeneMap.entrySet()){
            // System.out.println(e.getKey() + "\t" + e.getValue().toString());
            // }
            // ========Normalize network
            taskMonitor.setStatusMessage("Normalizing network...");
            ArrayList<Interaction> DirectedNetwork = new ArrayList<Interaction>();
            if (!directed) {
                for (int i = 0; i < Common.NetworkInteractionArray.size(); i++) {
                    Interaction ina = new Interaction();
                    ina.NodeSrc = Common.NetworkInteractionArray.get(i).NodeDst;
                    ina.NodeDst = Common.NetworkInteractionArray.get(i).NodeSrc;

                    ina.Weight = Common.NetworkInteractionArray.get(i).Weight;
                    ina.Type = Common.NetworkInteractionArray.get(i).Type;
                    DirectedNetwork.add(Common.NetworkInteractionArray.get(i));
                    DirectedNetwork.add(ina);
                }
            } else {
                for (int i = 0; i < Common.NetworkInteractionArray.size(); i++) {
                    DirectedNetwork.add(Common.NetworkInteractionArray.get(i));
                }
            }
            System.out.println("DirectedNetwork.size(): " + DirectedNetwork.size());

            if (interrupted) {
                return;
            }

            ArrayList<Interaction> NormalizedNetwork = new ArrayList<Interaction>();
            Map<String, ArrayList<NodeInteraction>> OutgoingNodeTable = Common
                    .calculateOutgoingNeighbors(DirectedNetwork);

            System.out.println("OutgoingNodeTable.size(): " + OutgoingNodeTable.size());
            for (Map.Entry<String, ArrayList<NodeInteraction>> e : OutgoingNodeTable.entrySet()) {
                double totaloutweight = 0.0;
                ArrayList<NodeInteraction> OutNodeInteractionList = e.getValue();
                for (int i = 0; i < OutNodeInteractionList.size(); i++) {
                    totaloutweight += OutNodeInteractionList.get(i).Weight;
                }

                for (int i = 0; i < OutNodeInteractionList.size(); i++) {
                    Interaction ina = new Interaction();
                    ina.NodeSrc = e.getKey();
                    ina.NodeDst = OutNodeInteractionList.get(i).Node;
                    ina.Weight = OutNodeInteractionList.get(i).Weight / totaloutweight;
                    // System.out.println(ina.NodeSrc + "\t" + ina.Weight + "\t"
                    // + ina.NodeDst);
                    NormalizedNetwork.add(ina);
                }
            }

            if (interrupted) {
                return;
            }

            Common.IncomingNodeTable = Common.calculateIncomingNeighbors(NormalizedNetwork);

            System.out.println("IncomingNodeTable.size(): " + Common.IncomingNodeTable.size());

            // ========Load Diseases and associated miRNAs
            taskMonitor.setStatusMessage("Loading Diseases and associated miRNAs...");
//			if (miR2DiseaseDB.getSelectedValue().equals("miR2Disease")) {
//				Common.DiseaseFileName = "Phenotype2miRNAs";
//			} else if (miR2DiseaseDB.getSelectedValue().equals("HMDD")) {
//				Common.DiseaseFileName = "Phenotype2miRNAs_HMDD";
//			} else{
//				Common.DiseaseFileName = "Phenotype2miRNAs";
//			}
            Common.DiseaseFileName = miR2DiseaseDB.getSelectedValue().toString();
            Map<String, Map<String, Double>> Disease2miRNA2WeightMap = new Resource().loadDisease2Nodes();
            Common.Disease2miRNA2WeightMapMap = Disease2miRNA2WeightMap;
            Common.ID2NameMap = new Resource().loadAllLowerNodeInfo("Phenotype2Genes_Full.txt");
            for (String str : Disease2miRNA2WeightMap.keySet()) {//
                System.out.println("----------" + str + "-----------");
                for (Entry<String, Double> str1 : Disease2miRNA2WeightMap.get(str).entrySet()) {
                    System.out.println("\t" + str1.getKey() + "\t" + str1.getValue());
                }
            }
            System.out.println("Total Diseases: " + Disease2miRNA2WeightMap.size());

            if (interrupted) {
                return;
            }

            if (Common.NetworkFileName.equals("miRWalk") || Common.NetworkFileName.equals("TargetScan")) {
                LoadmiRNANetworkTaskFactory loadmiRNANetworkTaskFactory = new LoadmiRNANetworkTaskFactory(
                        cyNetworkFactory, namingUtil, cyNetworkManager);
                cySynchronousTaskManager.execute(loadmiRNANetworkTaskFactory.createTaskIterator());
            }

            if (interrupted) {
                return;
            }

            if (Common.DiseaseFileName.equals("miR2Disease") || Common.DiseaseFileName.equals("HMDD")) {
                LoadPhenotypeNetworkTaskFactory loadPhenotypeNetworkTaskFactory = new LoadPhenotypeNetworkTaskFactory(
                        cyNetworkFactory, namingUtil, cyNetworkManager);
                cySynchronousTaskManager.execute(loadPhenotypeNetworkTaskFactory.createTaskIterator());
            }

        } catch (Exception e) {
            throw new NotFoundException("Network not found", Response.status(Response.Status.NOT_FOUND)
                    .type(MediaType.APPLICATION_JSON).entity(new ErrorMessage("Cannot find network")).build());

        }
    }

    @ProvidesTitle
    public String getTitle() {
        return "Load Datasets";
    }

    @Override
    public void cancel() {
        // TODO Auto-generated method stub
        interrupted = true;

    }

    @SuppressWarnings("unchecked")
    @Override
    public <R> R getResults(Class<? extends R> type) {
        // TODO Auto-generated method stub
        return (R) new JSONResult() {

            @Override
            public String getJSON() {
                // TODO Auto-generated method stub
                Messages m = new Messages("Load Heterogeneous Network successfully");
                return new Gson().toJson(m);
            }
        };

    }

    @Override
    public List<Class<?>> getResultClasses() {
        // TODO Auto-generated method stub
        return Collections.unmodifiableList(Arrays.asList(String.class, JSONResult.class));
    }
}
