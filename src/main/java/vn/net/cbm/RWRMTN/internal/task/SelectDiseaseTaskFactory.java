package vn.net.cbm.RWRMTN.internal.task;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.work.TaskIterator;

import vn.net.cbm.RWRMTN.internal.model.MyUtils;

public class SelectDiseaseTaskFactory implements NetworkTaskFactory{
	CyNetworkFactory networkFactory;
	CyNetworkManager networkManager;
	MyUtils myUtils;
	
	public SelectDiseaseTaskFactory(CyNetworkFactory networkFactory, CyNetworkManager networkManager, MyUtils myUtils) {
		super();
		this.networkFactory = networkFactory;
		this.networkManager = networkManager;
		this.myUtils=myUtils;
	}

	@Override
	public TaskIterator createTaskIterator(CyNetwork arg0) {
		// TODO Auto-generated method stub
		return new TaskIterator(new SelectDiseaseTask(networkFactory, networkManager, arg0,myUtils));
	}

	@Override
	public boolean isReady(CyNetwork arg0) {
		// TODO Auto-generated method stub
		return true;
	}
}
