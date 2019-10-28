package vn.net.cbm.RWRMTN.internal.task;

import java.awt.Color;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
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
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;
import org.cytoscape.work.util.ListSingleSelection;

import com.google.gson.Gson;

import vn.net.cbm.RWRMTN.internal.model.Common;
import vn.net.cbm.RWRMTN.internal.model.Messages;

/**
 *
 * @author suvl_000
 */
public class VisualizeSubNetworkTask implements ObservableTask {

    private boolean interrupted = false;
    private CyNetworkFactory cyNetworkFactory;
    private CyNetworkNaming cyNetworkNaming;
    private CyNetworkManager cyNetworkManager;
    private CyLayoutAlgorithmManager layoutManager;
    private TaskManager taskManager;
    private CyNetworkViewFactory cyNetworkViewFactory;
    private CyNetworkViewManager cyNetworkViewManager;
    private VisualMappingManager vmm;
    private VisualStyleFactory visualStyleFactory;
    private VisualMappingFunctionFactory vmfFactoryP;
    private VisualMappingFunctionFactory vmfFactoryD;
    private VisualMappingFunctionFactory vmfFactoryC;
    private HashMap<String, CyNode> nodeIdMap;

    @Tunable(description = "Select sub network to visualize", longDescription = "Visualize network with/without targeted genes", exampleStringValue = "without targeted genes")
    public ListSingleSelection<String> visualizeOptions;

    public VisualizeSubNetworkTask(CyNetworkFactory cyNetworkFactory, CyNetworkNaming cyNetworkNaming,
            CyNetworkManager cyNetworkManager, CyLayoutAlgorithmManager layoutManager, TaskManager taskManager,
            CyNetworkViewFactory cyNetworkViewFactory, CyNetworkViewManager cyNetworkViewManager,
            VisualMappingManager vmm, VisualStyleFactory visualStyleFactory, VisualMappingFunctionFactory vmfFactoryP,
            VisualMappingFunctionFactory vmfFactoryD, VisualMappingFunctionFactory vmfFactoryC) {
        this.cyNetworkFactory = cyNetworkFactory;
        this.cyNetworkNaming = cyNetworkNaming;
        this.cyNetworkManager = cyNetworkManager;
        this.layoutManager = layoutManager;
        this.taskManager = taskManager;
        this.cyNetworkViewFactory = cyNetworkViewFactory;
        this.cyNetworkViewManager = cyNetworkViewManager;
        this.vmm = vmm;
        this.visualStyleFactory = visualStyleFactory;
        this.vmfFactoryP = vmfFactoryP;
        this.vmfFactoryD = vmfFactoryD;
        this.vmfFactoryC = vmfFactoryC;
        this.nodeIdMap = new HashMap<>();
        visualizeOptions = new ListSingleSelection<>("without targeted genes", "with targeted genes");
        visualizeOptions.setSelectedValue("without targeted genes");
    }

    @Override
    public void run(TaskMonitor taskMonitor) {
        taskMonitor.setTitle("Visualizing Sub-network");
        taskMonitor.setProgress(0.1);
        try {

            System.out.println("Showing network of selected genes and phenotypes from whole network...");
            taskMonitor.setStatusMessage("Showing network of selected genes and phenotypes from whole network...");

            // CyNetwork curNetwork = Cytoscape.getCurrentNetwork();
            CyNetwork separateNetwork = cyNetworkFactory.createNetwork();
            String networkName = "Sub-Network";
            separateNetwork.getRow(separateNetwork).set(CyNetwork.NAME,
                    cyNetworkNaming.getSuggestedNetworkTitle(networkName));
            // node's attributes

            if (interrupted) {
                return;
            }
            separateNetwork.getDefaultNodeTable().createColumn("Type", String.class, false);
            separateNetwork.getDefaultNodeTable().createColumn("Rank", Integer.class, false);
            separateNetwork.getDefaultNodeTable().createColumn("Score", Double.class, false);
            separateNetwork.getDefaultNodeTable().createColumn("Role", String.class, false);
            separateNetwork.getDefaultEdgeTable().createColumn("Weight", Double.class, false);
            separateNetwork.getDefaultEdgeTable().createColumn("Known", Boolean.class, false);


            // separateNetwork.getDefaultNodeTable().createColumn("Id",
            // String.class, false);
            // edge's attributes
            // separateNetwork.getDefaultEdgeTable().createColumn("NodeSrc",
            // Long.class, false);
            // separateNetwork.getDefaultEdgeTable().createColumn("NodeDst",
            // Long.class, false);
            // network_gene and network_disease
            CyNetwork RNAnetwork = Common.getNetworkByName(cyNetworkManager, Common.NetworkFileName);
            CyNetwork DiseaseNetwork = Common.getNetworkByName(cyNetworkManager, Common.DiseaseTerm);

            if (DiseaseNetwork.getDefaultNodeTable().getColumn("PubMed (PudMedIDs)") != null) {
                separateNetwork.getDefaultNodeTable().createColumn("PubMed (PudMedIDs)", String.class, false);
            }

            List<CyNode> selected_rna = CyTableUtil.getNodesInState(DiseaseNetwork, "selected", true);
            if (interrupted) {
                return;
            }

            int i, j;
            System.out.println(selected_rna.size());

            if (selected_rna.size() == 0) {
                taskMonitor.setStatusMessage("You should select at least one row to visualize");
                //JOptionPane.showMessageDialog(null, "You should select at least one row to visualize");			       
                return;
            }

            // JOptionPane.showMessageDialog(Cytoscape.getDesktop(), ri.length +
            // ":" + this.tblResult.getSelectedRows().length);
            // Create nodes
            taskMonitor.setStatusMessage("Creating nodes of Gen-Graph...!");
            // curNetwork.setSelectedNodeState(curNetwork.nodesList(), false);
            CyNode diseaseNode = separateNetwork.addNode();
            CyRow diseaseRow = separateNetwork.getDefaultNodeTable().getRow(diseaseNode.getSUID());
            diseaseRow.set("shared name", Common.DiseaseTerm);
            diseaseRow.set("Type", "Disease");
            diseaseRow.set("Role", "Disease");
            diseaseRow.set("Rank", 1000);
            int high = 0;

            if (interrupted) {
                return;
            }

            for (CyNode node : selected_rna) {
                CyNode n = separateNetwork.addNode();
                CyRow cyRow = separateNetwork.getDefaultNodeTable().getRow(n.getSUID());
                String RID = DiseaseNetwork.getRow(node).get("shared name", String.class);

                if (interrupted) {
                    return;
                }
                // System.out.println(RID);
                cyRow.set("shared name", RID);

                if (DiseaseNetwork.getRow(node).get("Rank", Integer.class) != null) {
                    cyRow.set("Score", DiseaseNetwork.getRow(node).get("Score", Double.class));
                    if (high < DiseaseNetwork.getRow(node).get("Rank", Integer.class)) {
                        high = DiseaseNetwork.getRow(node).get("Rank", Integer.class);
                    }
                    cyRow.set("Rank", DiseaseNetwork.getRow(node).get("Rank", Integer.class));
                    String PIDstr = null;
                    if (DiseaseNetwork.getDefaultNodeTable().getColumn("PubMed (PudMedIDs)") != null) {
                        PIDstr = DiseaseNetwork.getRow(node).get("PubMed (PudMedIDs)", String.class);
                        if (PIDstr != null) {
                            cyRow.set("PubMed (PudMedIDs)", PIDstr);

                        }
                    }

                    CyEdge MainEdge = separateNetwork.addEdge(diseaseNode, n, false);
                    if (!RID.startsWith("hsa-")) {
                        cyRow.set("Role", "Gene-Candidate");
                        cyRow.set("Type", "Gene");
                        continue;
                    } else {
                        if (DiseaseNetwork.getRow(node).get("Known", Boolean.class)) {
                            cyRow.set("Role", "miRNA-Known");
                            separateNetwork.getRow(MainEdge).set("Known", true);
                        } else {
                            cyRow.set("Role", "miRNA-Candidate");
                            separateNetwork.getRow(MainEdge).set("Known", false);
                        }
                        cyRow.set("Type", "miRNA");
                    }

                    System.out.println(PIDstr);
                    if (visualizeOptions.getSelectedValue().equals("without targeted genes") && PIDstr != null) {
                        String[] PIDs = PIDstr.split(", ");
                        for (String PID : PIDs) {
                            PID = PID.trim();
                            CyNode PMnode = separateNetwork.addNode();
                            CyRow PMrow = separateNetwork.getDefaultNodeTable().getRow(PMnode.getSUID());
                            PMrow.set("shared name", PID);
                            PMrow.set("Type", "PubMedID");
                            PMrow.set("Role", "PubMedID");
                            PMrow.set("Rank", 10000);

                            CyEdge edg = separateNetwork.addEdge(n, PMnode, false);
                            separateNetwork.getRow(edg).set("Known", null);
                        }
                    }

                    // separateNetwork.getRow(edge).set("shared name", RID + "
                    // (interacts with) " + e.getKey());
                    // separateNetwork.getRow(edge).set("Weight", e.getValue());
                    if (visualizeOptions.getSelectedValue().equals("with targeted genes")) {
                        for (Entry<String, Double> e : Common.miRNA2Gene2WeightMapMap.get(RID).entrySet()) {
                            if (interrupted) {
                                return;
                            }

                            CyNode destNode = separateNetwork.addNode();
                            CyRow destRow = separateNetwork.getDefaultNodeTable().getRow(destNode.getSUID());
                            destRow.set("shared name", e.getKey());
                            destRow.set("Role", "Gene");

                            CyEdge edge = separateNetwork.addEdge(n, destNode, false);
                            separateNetwork.getRow(edge).set("shared name", RID + " (interacts with) " + e.getKey());
                            separateNetwork.getRow(edge).set("Weight", e.getValue());
                            separateNetwork.getRow(edge).set("Known", true);
                        }
                    }
                } else {
                    cyRow.set("Type", "Unknown miRNA");
                    cyRow.set("Role", "Unknown miRNA");
                }

            }

            // Add network properties
            separateNetwork.getDefaultNetworkTable().createColumn("Type", String.class, false);
            separateNetwork.getRow(separateNetwork).set("Type", "Sub network");

            cyNetworkManager.addNetwork(separateNetwork);

            // If the style already existed, remove it first
            Iterator it = vmm.getAllVisualStyles().iterator();
            while (it.hasNext()) {
                VisualStyle curVS = (VisualStyle) it.next();
                if (curVS.getTitle().startsWith("HaTrangStyle")) {
                    vmm.removeVisualStyle(curVS);
                    break;
                }
            }

            if (interrupted) {
                return;
            }

            // Create a new Visual style
            VisualStyle vs = visualStyleFactory.createVisualStyle("HaTrangStyle");
            vmm.addVisualStyle(vs);

            // 1. pass-through mapping
            PassthroughMapping pMapping = (PassthroughMapping) vmfFactoryP.createVisualMappingFunction("shared name",
                    String.class, BasicVisualLexicon.NODE_LABEL);
            // 2. DiscreteMapping - Set node shape based on attribute value
            DiscreteMapping dMapping = (DiscreteMapping) vmfFactoryD.createVisualMappingFunction("Role", String.class,
                    BasicVisualLexicon.NODE_SHAPE);

            // If attribute value is "diamond", map the nodeShape to DIAMOND
            if (interrupted) {
                return;
            }

            dMapping.putMapValue("Disease", NodeShapeVisualProperty.OCTAGON);
            dMapping.putMapValue("miRNA-Known", NodeShapeVisualProperty.TRIANGLE);
            dMapping.putMapValue("miRNA-Candidate", NodeShapeVisualProperty.ELLIPSE);
            dMapping.putMapValue("Gene-Candidate", NodeShapeVisualProperty.HEXAGON);
            dMapping.putMapValue("Gene", NodeShapeVisualProperty.DIAMOND);
            dMapping.putMapValue("PubMedID", NodeShapeVisualProperty.PARALLELOGRAM);

            DiscreteMapping dEdgeMapping = (DiscreteMapping) vmfFactoryD.createVisualMappingFunction("Known", Boolean.class,
                    BasicVisualLexicon.EDGE_LINE_TYPE);

            dEdgeMapping.putMapValue(false, LineTypeVisualProperty.LONG_DASH);
            dEdgeMapping.putMapValue(true, LineTypeVisualProperty.SOLID);
            dEdgeMapping.putMapValue(null, LineTypeVisualProperty.DOT);
            
            DiscreteMapping<Boolean,Paint> dEdgeMappingColor = (DiscreteMapping) vmfFactoryD.createVisualMappingFunction("Known", Boolean.class,
                    BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT);

            dEdgeMappingColor.putMapValue(false, Color.ORANGE);
            dEdgeMappingColor.putMapValue(true, Color.BLUE);
            dEdgeMappingColor.putMapValue(null, Color.GRAY);

            // 3. continous mapping.
            // Set node color map to attribute "Degree"
            ContinuousMapping cMapping = (ContinuousMapping) vmfFactoryC.createVisualMappingFunction("Rank",
                    Integer.class, BasicVisualLexicon.NODE_FILL_COLOR);
            // set the points
            Double val1 = (double)(high)/4;
            BoundaryRangeValues<Paint> brv1 = new BoundaryRangeValues<Paint>(Color.decode("#ff0000"), Color.decode("#ff4c4c"), Color.decode("#ff7f7f"));
            Double val2 = (double)(high+1)/2;
            BoundaryRangeValues<Paint> brv2 = new BoundaryRangeValues<Paint>(Color.decode("#ff7f7f"), Color.decode("#ff9999"), Color.decode("#ffb2b2"));
            Double val3 = (double)(high+1)/1.2d;
            BoundaryRangeValues<Paint> brv3 = new BoundaryRangeValues<Paint>(Color.decode("#ffb2b2"), Color.decode("#ffcccc"), Color.decode("#ffe5e5"));
            Double val4 = 10000d;
            BoundaryRangeValues<Paint> brv4 = new BoundaryRangeValues<Paint>(Color.WHITE, Color.CYAN, Color.MAGENTA);
            if (interrupted) {
                return;
            }

            cMapping.addPoint(val1, brv1);
            cMapping.addPoint(val2, brv2);
            cMapping.addPoint(val3, brv3);
            cMapping.addPoint(val4, brv4);

            vs.addVisualMappingFunction(pMapping);
            vs.addVisualMappingFunction(dMapping);
            vs.addVisualMappingFunction(cMapping);
            vs.addVisualMappingFunction(dEdgeMapping);
            vs.addVisualMappingFunction(dEdgeMappingColor);

            vmm.addVisualStyle(vs);

            if (interrupted) {
                return;
            }
            for (VisualPropertyDependency<?> visualPropertyDependency : vs.getAllVisualPropertyDependencies()) {
                if (visualPropertyDependency.getIdString().equals("nodeSizeLocked")) {
                    visualPropertyDependency.setDependency(false);
                    break;
                }
            }
            CyNetworkView view = cyNetworkViewFactory.createNetworkView(separateNetwork);
            cyNetworkViewManager.addNetworkView(view);
            vmm.setVisualStyle(vs, view);

            CyLayoutAlgorithm layoutAlgorithm = layoutManager.getLayout("attributes-layout");
            final Collection<CyNetworkView> views = cyNetworkViewManager.getNetworkViews(separateNetwork);
            CyNetworkView PheGenNetworkView = null;
            if (views.size() != 0) {
                PheGenNetworkView = views.iterator().next();
                this.taskManager.execute(layoutAlgorithm.createTaskIterator(PheGenNetworkView, layoutAlgorithm.createLayoutContext(), CyLayoutAlgorithm.ALL_NODE_VIEWS, "Role"));
            }
            if (PheGenNetworkView == null) {
                // create a new view for my network
                PheGenNetworkView = cyNetworkViewFactory.createNetworkView(separateNetwork);
                // Apply the visual style to a NetwokView
//                this.vs.apply(PheGenNetworkView);
                PheGenNetworkView.updateView();
                this.taskManager.execute(layoutAlgorithm.createTaskIterator(PheGenNetworkView, layoutAlgorithm.createLayoutContext(), CyLayoutAlgorithm.ALL_NODE_VIEWS, "Role"));
                cyNetworkViewManager.addNetworkView(PheGenNetworkView);

            } else {
                System.out.println("networkView already existed.");
            }
            //

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        taskMonitor.setProgress(0.1);
    }

    @Override
    public void cancel() {
        this.interrupted = true;
    }

    @ProvidesTitle
    public String getTitle() {
        return "Visualize";
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R> R getResults(Class<? extends R> type) {
        // TODO Auto-generated method stub
        return (R) new JSONResult() {

            @Override
            public String getJSON() {
                // TODO Auto-generated method stub
                Messages m = new Messages("Visualize sub-network successfully");
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
