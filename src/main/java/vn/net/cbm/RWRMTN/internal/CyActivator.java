package vn.net.cbm.RWRMTN.internal;

import static org.cytoscape.work.ServiceProperties.COMMAND;
import static org.cytoscape.work.ServiceProperties.COMMAND_DESCRIPTION;
import static org.cytoscape.work.ServiceProperties.COMMAND_EXAMPLE_JSON;
import static org.cytoscape.work.ServiceProperties.COMMAND_LONG_DESCRIPTION;
import static org.cytoscape.work.ServiceProperties.COMMAND_NAMESPACE;
import static org.cytoscape.work.ServiceProperties.COMMAND_SUPPORTS_JSON;
import static org.cytoscape.work.ServiceProperties.INSERT_SEPARATOR_BEFORE;
import static org.cytoscape.work.ServiceProperties.IN_MENU_BAR;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;
import static org.cytoscape.work.ServiceProperties.TOOLTIP;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CyNodeViewContextMenuFactory;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;

import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskManager;
import org.osgi.framework.BundleContext;

import vn.net.cbm.RWRMTN.internal.RESTmodel.EvidenceResult;
import vn.net.cbm.RWRMTN.internal.RESTmodel.RankedResult;
import vn.net.cbm.RWRMTN.internal.RESTmodel.autoRWRMTN;
import vn.net.cbm.RWRMTN.internal.RESTmodel.autoRWRMTNImp;
import vn.net.cbm.RWRMTN.internal.model.MyUtils;
import vn.net.cbm.RWRMTN.internal.task.AboutActionHGPEC;
import vn.net.cbm.RWRMTN.internal.task.BuildNetworkTaskFactory;
import vn.net.cbm.RWRMTN.internal.task.ExamineRankedGenesandDiseasesTask;
import vn.net.cbm.RWRMTN.internal.task.ExamineRankedGenesandDiseasesTaskFactory;
import vn.net.cbm.RWRMTN.internal.task.HelpAction;
import vn.net.cbm.RWRMTN.internal.task.SelectDiseaseTask;
import vn.net.cbm.RWRMTN.internal.task.SelectDiseaseTaskFactory;
import vn.net.cbm.RWRMTN.internal.task.VisualizeSubNetworkTaskFactory;
import vn.net.cbm.RWRMTN.internal.ui.InfoPanel;
import vn.net.cbm.RWRMTN.internal.ui.MyNodeViewContextMenuFactory;



public class CyActivator extends AbstractCyActivator {
	public static final String MYAPP_COMMAND_NAMESPACE="RWRMTN";
	@Override
	public void start(BundleContext context) throws Exception {
		CyApplicationManager cyApplicationManager = getService(context, CyApplicationManager.class);
        TaskManager cyTaskManager = getService(context, TaskManager.class);
        SynchronousTaskManager cySynchronousTaskManager = getService(context, SynchronousTaskManager.class);
        CyNetworkManager cyNetworkManager = getService(context, CyNetworkManager.class);
        CyNetworkReaderManager cyNetworkReaderManager = getService(context, CyNetworkReaderManager.class);
        CyNetworkFactory cyNetworkFactory = getService(context, CyNetworkFactory.class);
        CyNetworkNaming cyNetworkNaming = getService(context, CyNetworkNaming.class);
        CyNetworkViewManager cyNetworkViewManager = getService(context, CyNetworkViewManager.class);
        CyNetworkViewFactory cyNetworkViewFactory = getService(context, CyNetworkViewFactory.class);
        CyLayoutAlgorithmManager layoutManager = getService(context, CyLayoutAlgorithmManager.class);
        CySwingApplication swingApplication =getService(context, CySwingApplication.class);
        VisualMappingManager visualMappingManager = getService(context, VisualMappingManager.class);
        VisualStyleFactory visualStyleFactory = getService(context,VisualStyleFactory.class);
        CyServiceRegistrar serviceRegistrar = getService(context, CyServiceRegistrar.class);
        final MyUtils myUtils = new MyUtils(serviceRegistrar);
        
        VisualMappingFunctionFactory vmfFactoryC = getService(context, VisualMappingFunctionFactory.class, "(mapping.type=continuous)");
        VisualMappingFunctionFactory vmfFactoryP = getService(context, VisualMappingFunctionFactory.class, "(mapping.type=passthrough)");
        VisualMappingFunctionFactory vmfFactoryD = getService(context, VisualMappingFunctionFactory.class, "(mapping.type=discrete)");
        {
			Properties buildProps = new Properties();
			BuildNetworkTaskFactory buildTaskFactory = new BuildNetworkTaskFactory(cyTaskManager, cyNetworkManager, cyNetworkReaderManager, cyNetworkFactory, cyNetworkNaming, cySynchronousTaskManager);
			
			String buildNetworkDescription = "Step 1: Load Datasets";
			String buildNetworkLongDescription="Step 1: Load a heterogeneous network of diseases and miRNA by selecting:"
					+ "\n\n\t+ miRNA target dataset (miRTargetDB): choose built-in dataset miRWalk (database of experimentally validated miRNA-target interactions) "
					+ "\n or TargetScan (database containing predicted interactions) or your own dataset (must follow the format as mention in the user manual)"
					+ "\n\n\t+ Disease-miRNAs dataset (miR2DiseaseDB): choose built-in dataset miR2Disease or HMDD";
					
			buildProps.setProperty(COMMAND_NAMESPACE, MYAPP_COMMAND_NAMESPACE);
			buildProps.setProperty(COMMAND, "step1_load_datasets");
			buildProps.setProperty(COMMAND_DESCRIPTION,  buildNetworkDescription);
			buildProps.setProperty(COMMAND_LONG_DESCRIPTION, buildNetworkLongDescription);
			buildProps.setProperty(COMMAND_SUPPORTS_JSON, "true");
			buildProps.setProperty(PREFERRED_MENU, "Apps."+MYAPP_COMMAND_NAMESPACE);
			buildProps.setProperty(TITLE, "Step 1: Load datasets");
			buildProps.setProperty(IN_MENU_BAR, "true");
			buildProps.setProperty(MENU_GRAVITY, "1.0");
			buildProps.setProperty(TOOLTIP,  buildNetworkDescription);
			
			registerAllServices(context, buildTaskFactory, buildProps);
			//registerService(context, buildTaskFactory, NetworkTaskFactory.class, buildProps);	
		}
        
        {
			Properties selectProps = new Properties();
			SelectDiseaseTaskFactory selectTaskFactory = new SelectDiseaseTaskFactory(cyNetworkFactory, cyNetworkManager,myUtils);

			String selectDiseaseDescription = "Step 2: Rank candidate miRNAs";
			String selectDiseaseLongDescription="Step 2: Select disease of interest and Rank associated miRNAs"
					+ "\n\nThis step is followed "
					+ "\n\n\t+ Step 1: Load Dataset at 'POST /v1/commands/"+MYAPP_COMMAND_NAMESPACE+"/step1_load_datasets)"
		
					+ "\n\nChoose a disease of interest from Heterogeneous network and input a list of miRNAs to rank. ";
	
			selectProps = new Properties();
			selectProps.setProperty(COMMAND_NAMESPACE, MYAPP_COMMAND_NAMESPACE);
			selectProps.setProperty(COMMAND, "step2_rank_miRNAs");
			selectProps.setProperty(COMMAND_DESCRIPTION,  selectDiseaseDescription);
			selectProps.setProperty(COMMAND_LONG_DESCRIPTION, selectDiseaseLongDescription);
			selectProps.setProperty(COMMAND_SUPPORTS_JSON, "true");
			selectProps.setProperty(COMMAND_EXAMPLE_JSON,  getRankExample());
			selectProps.setProperty(PREFERRED_MENU, "Apps."+MYAPP_COMMAND_NAMESPACE);		
			selectProps.setProperty(TITLE, "Step 2: Rank candidate miRNAs");
			selectProps.setProperty(MENU_GRAVITY, "3.0");
			selectProps.setProperty(TOOLTIP,  selectDiseaseDescription);
			
			registerAllServices(context, selectTaskFactory, selectProps);
		}
        

//       
        {
			Properties infoProps = new Properties();
			ExamineRankedGenesandDiseasesTaskFactory selectTaskFactory= new ExamineRankedGenesandDiseasesTaskFactory(cyNetworkFactory, cyNetworkManager);
			
			String PCG_NeighborNetworkDescription = "Step 3: Search Evidences";
			String PCG_NeighborNetworkLongDescription="Step 3: This function is to collect evidences and anotations for associations between highly ranked candidate miRNAs and the disease of interest."
					+ "\n\nThis step is followed "
					+ "\n\n\t+ Step 1: Load data at 'POST /v1/commands/"+MYAPP_COMMAND_NAMESPACE+"/step1_load_datasets)"
					+ "\n\n\t+ Step 2: Select disease and Rank associated miRNAs at 'POST /v1/commands/"+MYAPP_COMMAND_NAMESPACE+"/step2_rank_miRNAs"
					
					+ "\n\n Selecting by highlighting candidate miRNA in the ranked genes table for evidence collection. "
					+ "\n These candidate miRNAs along with disease will be looked up in PubMed database.";
			
			
			infoProps = new Properties();
			infoProps.setProperty(COMMAND_NAMESPACE, MYAPP_COMMAND_NAMESPACE);
			infoProps.setProperty(COMMAND, "step3_search_evidences");
			infoProps.setProperty(COMMAND_DESCRIPTION,  PCG_NeighborNetworkDescription);
			infoProps.setProperty(COMMAND_LONG_DESCRIPTION, PCG_NeighborNetworkLongDescription);
			infoProps.setProperty(COMMAND_SUPPORTS_JSON, "true");
			infoProps.setProperty(COMMAND_EXAMPLE_JSON,  getEvidenceExample());
			infoProps.setProperty(INSERT_SEPARATOR_BEFORE, "true");
			infoProps.setProperty(PREFERRED_MENU, "Apps."+MYAPP_COMMAND_NAMESPACE);
			infoProps.setProperty(TITLE, "Step 3: Search Evidences");
			infoProps.setProperty(MENU_GRAVITY, "21.0");
			infoProps.setProperty(TOOLTIP,  PCG_NeighborNetworkDescription);
			registerService(context, selectTaskFactory, NetworkTaskFactory.class, infoProps);
		}
        
        {
			Properties infoProps = new Properties();
			VisualizeSubNetworkTaskFactory selectTaskFactory = new VisualizeSubNetworkTaskFactory(cyNetworkFactory, cyNetworkNaming, cyNetworkManager, layoutManager, cyTaskManager, cyNetworkViewFactory, cyNetworkViewManager, visualMappingManager, visualStyleFactory, vmfFactoryP, vmfFactoryD, vmfFactoryC);
			
			String PCG_NeighborNetworkDescription = "Step 4: Visualize";
			String PCG_NeighborNetworkLongDescription="Step 4: Visualize candidate miRNAs and diseases in the heterogeneous network"
					+ "\n\nThis step is followed "
					+ "\n\n\t+ Step 1: Load data at 'POST /v1/commands/"+MYAPP_COMMAND_NAMESPACE+"/step1_load_datasets)"
					+ "\n\n\t+ Step 2: Select disease and Rank associated miRNAs at 'POST /v1/commands/"+MYAPP_COMMAND_NAMESPACE+"/step2_rank_miRNAs"
					+ "\n\n\t+ Step 3: Search Evidences at 'POST /v1/commands/"+MYAPP_COMMAND_NAMESPACE+"/step3_search_evidences"
					
					+ "\n\nRelationships between selected miRNAs and diseases in the heterogeneous network are visualized."
					+ "Highlighting the selected candidate miRNAs and candidate diseases in the table to visualize.";
			
			infoProps = new Properties();
			infoProps.setProperty(COMMAND_NAMESPACE, MYAPP_COMMAND_NAMESPACE);
			infoProps.setProperty(COMMAND, "step4_visualize");
			infoProps.setProperty(COMMAND_DESCRIPTION,  PCG_NeighborNetworkDescription);
			infoProps.setProperty(COMMAND_LONG_DESCRIPTION, PCG_NeighborNetworkLongDescription);
			infoProps.setProperty(COMMAND_SUPPORTS_JSON, "true");
			//infoProps.setProperty(COMMAND_EXAMPLE_JSON,  getRankExample());
			
			infoProps.setProperty(PREFERRED_MENU, "Apps."+MYAPP_COMMAND_NAMESPACE);
			infoProps.setProperty(TITLE, "Step 4: Visualize");
			infoProps.setProperty(MENU_GRAVITY, "23.0");
			infoProps.setProperty(TOOLTIP,  PCG_NeighborNetworkDescription);
			registerService(context, selectTaskFactory, NetworkTaskFactory.class, infoProps);
		}
        registerService(context, new autoRWRMTNImp(myUtils), autoRWRMTN.class,new Properties());
        {
        	CyNodeViewContextMenuFactory myNodeViewContextMenuFactory  = new MyNodeViewContextMenuFactory(swingApplication, cyApplicationManager, serviceRegistrar);
    		Properties myNodeViewContextMenuFactoryProps = new Properties();
       		myNodeViewContextMenuFactoryProps.put("preferredMenu", "RWRMTN");
    		registerAllServices(context, myNodeViewContextMenuFactory, myNodeViewContextMenuFactoryProps);
    		
    		
        }
        
        HelpAction helpAction = new HelpAction();
        AboutActionHGPEC aboutActionHGPEC = new AboutActionHGPEC();
        Properties helpPro=new Properties();
        helpPro.setProperty(INSERT_SEPARATOR_BEFORE, "true");

		registerService(context, helpAction, CyAction.class, helpPro);
        registerService(context, aboutActionHGPEC, CyAction.class, new Properties());

	}
	
	
	private String getEvidenceExample() {
		// TODO Auto-generated method stub
		ArrayList<EvidenceResult> arr=new ArrayList<>();
		Set<String> evi=new HashSet<>();
		evi.add("28793339");
		evi.add("29404790");
		evi.add("28440475");
		arr.add(new EvidenceResult("hsa-miR-125b",evi));
		return ExamineRankedGenesandDiseasesTask.getJson(arr);
	}


	public static final String getRankExample(){
		ArrayList<RankedResult> gf=new ArrayList<>();
		gf.add(new RankedResult("hsa-miR-124",0.01861165,1,"miRNA",true));
		gf.add(new RankedResult("hsa-miR-125a-5p",0.01572524,2,"miRNA",false));
		
		return SelectDiseaseTask.getJson(gf);
	}

}
