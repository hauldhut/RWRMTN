package vn.net.cbm.RWRMTN.internal.task;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;

public class VisualizeSubNetworkTaskFactory implements NetworkTaskFactory {
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
	
	


	public VisualizeSubNetworkTaskFactory(CyNetworkFactory cyNetworkFactory, CyNetworkNaming cyNetworkNaming,
			CyNetworkManager cyNetworkManager, CyLayoutAlgorithmManager layoutManager, TaskManager taskManager,
			CyNetworkViewFactory cyNetworkViewFactory, CyNetworkViewManager cyNetworkViewManager,
			VisualMappingManager vmm, VisualStyleFactory visualStyleFactory, VisualMappingFunctionFactory vmfFactoryP,
			VisualMappingFunctionFactory vmfFactoryD, VisualMappingFunctionFactory vmfFactoryC) {
		super();
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
	}

	@Override
	public TaskIterator createTaskIterator(CyNetwork arg0) {
		// TODO Auto-generated method stub
		return new TaskIterator(new VisualizeSubNetworkTask(cyNetworkFactory, cyNetworkNaming, cyNetworkManager, layoutManager, taskManager, cyNetworkViewFactory, cyNetworkViewManager, vmm, visualStyleFactory, vmfFactoryP, vmfFactoryD, vmfFactoryC));
	}

	@Override
	public boolean isReady(CyNetwork arg0) {
		// TODO Auto-generated method stub
		return true;
	}

}
