package vn.net.cbm.RWRMTN.internal.task;

import java.awt.event.ActionEvent;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;

public class AboutActionHGPEC extends AbstractCyAction {

    public AboutActionHGPEC() {

        super("About...");
        setPreferredMenu("Apps.RWRMTN");

    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        AboutDialog aboutActionPanel = new AboutDialog(null, true);
        aboutActionPanel.setLocationRelativeTo(null); //should center on screen

        aboutActionPanel.setVisible(true);
    }

}
