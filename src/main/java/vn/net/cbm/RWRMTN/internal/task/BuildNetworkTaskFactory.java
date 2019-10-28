package vn.net.cbm.RWRMTN.internal.task;

import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;

public class BuildNetworkTaskFactory implements NetworkTaskFactory {
    private CyNetworkManager cyNetworkManager;
    private CyNetworkReaderManager cyNetworkReaderManager;
    private CyNetworkFactory cyNetworkFactory;
    private CyNetworkNaming namingUtil;
    private TaskManager cyTaskManager;
    private SynchronousTaskManager cySynchronousTaskManager; 
    
	public BuildNetworkTaskFactory(TaskManager cyTaskManager,CyNetworkManager cyNetworkManager, CyNetworkReaderManager cyNetworkReaderManager,
			CyNetworkFactory cyNetworkFactory, CyNetworkNaming namingUtil,SynchronousTaskManager cySynchronousTaskManager) {
		super();
		this.cyNetworkManager = cyNetworkManager;
		this.cyNetworkReaderManager = cyNetworkReaderManager;
		this.cyNetworkFactory = cyNetworkFactory;
		this.namingUtil = namingUtil;
		this.cyTaskManager=cyTaskManager;
		this.cySynchronousTaskManager=cySynchronousTaskManager;
	}

	@Override
	public TaskIterator createTaskIterator(CyNetwork arg0) {
		// TODO Auto-generated method stub
		return new TaskIterator(new BuildNetworkTask(cySynchronousTaskManager, cyTaskManager, arg0, cyNetworkManager, cyNetworkReaderManager, cyNetworkFactory, namingUtil));
	}

	@Override
	public boolean isReady(CyNetwork arg0) {
		// TODO Auto-generated method stub
		return true;
	}

}
