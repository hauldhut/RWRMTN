package vn.net.cbm.RWRMTN.internal.task;

import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class LoadPhenotypeNetworkTaskFactory extends AbstractTaskFactory {
	private CyNetworkFactory cyNetworkFactory;
    private CyNetworkNaming cyNetworkNaming;
    CyNetworkManager cyNetworkManager;
    
    
	public LoadPhenotypeNetworkTaskFactory(CyNetworkFactory cyNetworkFactory, CyNetworkNaming cyNetworkNaming,
			CyNetworkManager cyNetworkManager) {
		super();
		this.cyNetworkFactory = cyNetworkFactory;
		this.cyNetworkNaming = cyNetworkNaming;
		this.cyNetworkManager = cyNetworkManager;
	}


	@Override
	public TaskIterator createTaskIterator() {
		// TODO Auto-generated method stub
		return new TaskIterator(new LoadPhenotypeNetworkTask(cyNetworkFactory, cyNetworkNaming, cyNetworkManager));
	}

}
