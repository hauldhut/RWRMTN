package vn.net.cbm.RWRMTN.internal.task;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNode;
import org.cytoscape.task.AbstractNodeViewTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TunableSetter;

public class PMInfoContextmenuTaskFactory extends AbstractNodeViewTaskFactory implements NetworkViewTaskFactory{
	
//	private final CySwingApplication swingApplication;
//	private final CyApplicationManager appManager;
//	private final TunableSetter setter;
	
	
	@Override
	public TaskIterator createTaskIterator(View<CyNode> arg0, CyNetworkView arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TaskIterator createTaskIterator(CyNetworkView arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isReady(CyNetworkView arg0) {
		// TODO Auto-generated method stub
		return false;
	}

}
