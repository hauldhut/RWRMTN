
package vn.net.cbm.RWRMTN.internal.task;



import java.awt.Desktop;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;


public class HelpAction extends AbstractCyAction {
    
    public HelpAction() {

        super("Help...");
        setPreferredMenu("Apps.RWRMTN");

    }
    public void actionPerformed(ActionEvent ae){
        
        Desktop desktop = Desktop.getDesktop();
        try{
            desktop.browse(new URI("https://sites.google.com/site/duchaule2011/bioinformatics-tools/rwrmtn"));
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
        }

//        if(Config.HelpLoaded==false){
//            Config.dlg = new HelpFrame();
//            //HelpPanel dlg = new HelpPanel();
//
//            Config.dlg.setLocationRelativeTo(null); //should center on screen
//
//            Config.dlg.setVisible(true);
//            Config.HelpLoaded=true;
//        }else{
//            if(Config.HelpHidden==true){
//                Config.dlg.setVisible(true);
//            }
//        }
    }

}
