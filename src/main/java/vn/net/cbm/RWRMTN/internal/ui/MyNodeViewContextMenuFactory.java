package vn.net.cbm.RWRMTN.internal.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CyMenuItem;
import org.cytoscape.application.swing.CyNodeViewContextMenuFactory;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent2;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import vn.net.cbm.RWRMTN.internal.RESTmodel.PMInfo;
import vn.net.cbm.RWRMTN.internal.model.Common;

public class MyNodeViewContextMenuFactory implements CyNodeViewContextMenuFactory, ActionListener {

    CySwingApplication swingApplication;
    CyApplicationManager cyApplicationManager;
    CyServiceRegistrar serviceRegistrar;

    public MyNodeViewContextMenuFactory(CySwingApplication swingApplication, CyApplicationManager cyApplicationManager,
            CyServiceRegistrar serviceRegistrar) {
        super();
        this.swingApplication = swingApplication;
        this.cyApplicationManager = cyApplicationManager;
        this.serviceRegistrar = serviceRegistrar;
    }

    @Override
    public CyMenuItem createMenuItem(CyNetworkView netView,
            View<CyNode> nodeView) {
        JMenuItem menuItem = new JMenuItem("Check Information");
        menuItem.addActionListener(this);
        CyMenuItem cyMenuItem = new CyMenuItem(menuItem, 0);
        return cyMenuItem;
    }

    public void actionPerformed(ActionEvent e) {
        // Write your own function here.
//		JOptionPane.showMessageDialog(null, "MyNodeViewContextMenuFactory action worked.");

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                final CytoPanel panel = swingApplication.getCytoPanel(CytoPanelName.EAST);
                panel.setState(CytoPanelState.DOCK);
                int ind = panel.getCytoPanelComponentCount();
                System.out.println(ind);
                int targerPanelId = 0;

                if (ind == 0) {
                    System.out.println("not created");
                    InfoPanel pnl = new InfoPanel(cyApplicationManager, swingApplication);
                    serviceRegistrar.registerAllServices(pnl, new Properties());

                    for (int i = 0; i < panel.getCytoPanelComponentCount(); i++) {
                        final Component panelComponent = panel.getComponentAt(i);
                        if (panelComponent instanceof CytoPanelComponent2) {
                            CytoPanelComponent2 cp = (CytoPanelComponent2) panelComponent;
                            String panelId = cp.getIdentifier();
                            if (panelId != null && panelId.equals("RWRMTN")) {
                                panelComponent.setPreferredSize(new Dimension(300, 400));
                                targerPanelId = i;
                                break;
                            }
                        }
                    }

                    panel.setSelectedIndex(targerPanelId);
                    CyNetworkView currentNetworkView = cyApplicationManager.getCurrentNetworkView();
                    CyNetwork currentNetwork = currentNetworkView.getModel();
                    final List<CyNode> nodes = CyTableUtil.getNodesInState(currentNetwork, CyNetwork.SELECTED, true);
                    for (CyNode node : nodes) {
                        String id = currentNetwork.getRow(node).get("shared name", String.class);
                        if (!id.startsWith("hsa")) {
                            PMInfo info = Common.PMID_info.get(id);
                            pnl.setLbl(info.toString());
                        } else {
                            pnl.setLbl(id);
                        }

                        if (!id.startsWith("hsa")) {
                            PMInfo info = Common.PMID_info.get(id);
                            pnl.setLbl(info.toString());
                        } else {
                            pnl.setLbl(id);
                        }

                    }
                    panel.getThisComponent().repaint();
                } else {

                    for (int i = 0; i < ind; i++) {
                        final Component panelComponent = panel.getComponentAt(i);
                        if (panelComponent instanceof CytoPanelComponent2) {
                            CytoPanelComponent2 cp = (CytoPanelComponent2) panelComponent;
                            String panelId = cp.getIdentifier();
                            if (panelId != null && panelId.equals("RWRMTN")) {
                                panelComponent.setPreferredSize(new Dimension(300, 400));
                                targerPanelId = i;
                                break;
                            }
                        }
                    }

                }
            }
        }
        );
    }

}
