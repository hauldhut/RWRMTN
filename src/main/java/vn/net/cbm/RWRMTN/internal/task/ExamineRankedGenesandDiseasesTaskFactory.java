package vn.net.cbm.RWRMTN.internal.task;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.work.TaskIterator;

public class ExamineRankedGenesandDiseasesTaskFactory implements NetworkTaskFactory {
	CyNetworkFactory networkFactory;
	CyNetworkManager networkManager;
	
	
	public ExamineRankedGenesandDiseasesTaskFactory(CyNetworkFactory networkFactory, CyNetworkManager networkManager) {
		super();
		this.networkFactory = networkFactory;
		this.networkManager = networkManager;
	}

	@Override
	public TaskIterator createTaskIterator(CyNetwork arg0) {
		// TODO Auto-generated method stub
		return new TaskIterator(new ExamineRankedGenesandDiseasesTask(networkFactory, networkManager, arg0, null, null));
	}

	@Override
	public boolean isReady(CyNetwork arg0) {
		// TODO Auto-generated method stub
		return true;
	}

}
