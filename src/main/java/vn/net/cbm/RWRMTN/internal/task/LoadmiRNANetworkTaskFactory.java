/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.net.cbm.RWRMTN.internal.task;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import javax.swing.JOptionPane;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import vn.net.cbm.RWRMTN.internal.model.Common;




/**
 *
 * @author "trangtth"
 */
public class LoadmiRNANetworkTaskFactory extends AbstractTaskFactory  {

	private CyNetworkFactory cyNetworkFactory;
    private CyNetworkNaming cyNetworkNaming;
    CyNetworkManager cyNetworkManager;
    
    
	public LoadmiRNANetworkTaskFactory(CyNetworkFactory cyNetworkFactory, CyNetworkNaming cyNetworkNaming,CyNetworkManager cyNetworkManager) {
		super();
		this.cyNetworkFactory = cyNetworkFactory;
		this.cyNetworkNaming = cyNetworkNaming;
		this.cyNetworkManager = cyNetworkManager;
	}


	@Override
	public TaskIterator createTaskIterator() {
		// TODO Auto-generated method stub
		return new TaskIterator(new LoadmiRNANetworkTask(cyNetworkFactory, cyNetworkNaming, cyNetworkManager));
	}

}
