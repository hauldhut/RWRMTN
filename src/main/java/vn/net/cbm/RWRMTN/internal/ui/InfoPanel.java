package vn.net.cbm.RWRMTN.internal.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent2;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.events.RowSetRecord;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;

import vn.net.cbm.RWRMTN.internal.RESTmodel.PMInfo;
import vn.net.cbm.RWRMTN.internal.model.Common;
import vn.net.cbm.RWRMTN.internal.task.ExamineRankedGenesandDiseasesTask;

public class InfoPanel extends JPanel implements CytoPanelComponent2, RowsSetListener {

    CyApplicationManager appManager;
    CySwingApplication swingApplication;

    public static JTextArea lbl;

    public InfoPanel(CyApplicationManager appManager, CySwingApplication swingApplication) {
        super();
        this.appManager = appManager;
        this.swingApplication = swingApplication;

        initPanel();
    }

    private void initPanel() {
        // TODO Auto-generated method stub
        this.setBackground(Color.WHITE);
        this.setOpaque(false);
        this.setLayout(new BorderLayout());
        this.setMinimumSize(new Dimension(380, 155));
        this.setMaximumSize(new Dimension(5000, 155));
        this.setBorder(BorderFactory.createTitledBorder("Node information"));

        final Border padding = BorderFactory.createEmptyBorder(0, 5, 0, 5);
        this.setBorder(padding);

        lbl = new JTextArea("No result yet");
        lbl.setBackground(Color.WHITE);
        lbl.setOpaque(false);
        lbl.setLineWrap(true);
        lbl.setAutoscrolls(true);

        lbl.setFont(new Font("sansserif", Font.PLAIN, 18));

        this.add(lbl, BorderLayout.CENTER);
        this.setVisible(true);
        this.repaint();

        this.updateUI();
    }

    @Override
    public Component getComponent() {
        // TODO Auto-generated method stub
        return this;
    }

    @Override
    public CytoPanelName getCytoPanelName() {
        // TODO Auto-generated method stub
        return CytoPanelName.EAST;
    }

    @Override
    public Icon getIcon() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getTitle() {
        // TODO Auto-generated method stub
        return "More Information";
    }

    @Override
    public String getIdentifier() {
        // TODO Auto-generated method stub
        return "RWRMTN";
    }

    public void setLbl(String txt) {
        InfoPanel.lbl.setText(txt);
    }

    @Override
    public void handleEvent(RowsSetEvent e) {
        // TODO Auto-generated method stub
        if (!e.containsColumn(CyNetwork.SELECTED)) {
            return;
        }

        CyNetworkView currentNetworkView = appManager.getCurrentNetworkView();
        CyNetwork currentNetwork = currentNetworkView.getModel();

        CytoPanel panel = swingApplication.getCytoPanel(CytoPanelName.EAST);
        if (panel.getState() != CytoPanelState.DOCK) {
            return;
        }
//        if (e.getSource() != currentNetwork.getDefaultNodeTable()) {
//            return;
//        }

        for (RowSetRecord record : e.getColumnRecords(CyNetwork.SELECTED)) {
            Long suid = record.getRow().get(CyIdentifiable.SUID, Long.class);
            Boolean value = (Boolean) record.getValue();
            if (value.equals(Boolean.TRUE)) {
                CyNode node = currentNetwork.getNode(suid);
                String id = currentNetwork.getRow(node).get("shared name", String.class);

                if (!id.startsWith("hsa")) {
                    PMInfo info = Common.PMID_info.get(id);
                    lbl.setText(info.toString());
                } else {
                    lbl.setText(id);
                }

            }
        }

    }

}
