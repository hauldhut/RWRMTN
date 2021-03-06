/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * AboutDialog.java
 *
 * Created on Dec 3, 2010, 3:53:53 PM
 */

package vn.net.cbm.RWRMTN.internal.task;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 *
 * @author Administrator
 */
public class AboutDialogHGPEC extends javax.swing.JDialog {

    public AboutDialogHGPEC(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }

    public AboutDialogHGPEC() {
//        super(Cytoscape.getDesktop(), "About...");
        initComponents();
//        this.setBounds(0, 0, 548, 213);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        try {
            jLabel1 = new javax.swing.JLabel();
            jScrollPane1 = new javax.swing.JScrollPane();
            jTextArea1 = new javax.swing.JTextArea();
            jLabel3 = new javax.swing.JLabel();
            txtSoftwareName = new javax.swing.JLabel();
            btnOK = new javax.swing.JButton();
            jLabel5 = new javax.swing.JLabel();
            
            setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
            setTitle("About");
            
            jTextArea1.setEditable(false);
            jTextArea1.setBackground(javax.swing.UIManager.getDefaults().getColor(Color.WHITE));
            jTextArea1.setColumns(30);
            jTextArea1.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
            jTextArea1.setRows(15);
            
            jTextArea1.setText("Author:\tDuc-Hau Le and Trang T.H. Tran\n\nEmail:\thauldhut@gmail.com and trangtth@tlu.edu.vn"
                    + "\n\nTel: \t(+84)912.324564\nAffiliation: \tThuyloi University\nAddress:   \t175 Tay Son, Dong Da, Hanoi, Vietnam. \n\nDownload: \thttps://sites.google.com/site/duchaule2011/bioinformatics-tools/rwrmtn\n\nPlatform: \tCytoscape 3.7.0 and later with Automation features, Operating Systems (Windows, Linux, Mac OS X)");
            jTextArea1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
            jScrollPane1.setViewportView(jTextArea1);
            
            jLabel3.setText("For more information, please contact:");
            
            txtSoftwareName.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
            txtSoftwareName.setText("RWRMTN Ver 1.0");
            
            btnOK.setText("OK");
            btnOK.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    btnOKActionPerformed(evt);
                }
            });
            URL url = this.getClass().getClassLoader().getResource("icon.png");
            File file=new File(url.toURI());
            BufferedImage imgIcon=ImageIO.read(file);
            Image dimg=imgIcon.getScaledInstance(jLabel5.getWidth(), jLabel5.getHeight(), Image.SCALE_SMOOTH);
            jLabel5.setIcon(new ImageIcon(dimg)); // NOI18N
            
            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
            getContentPane().setLayout(layout);
            layout.setHorizontalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                    .addContainerGap()
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                    .addComponent(jLabel1)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(jLabel5)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                            .addGroup(layout.createSequentialGroup()
                                                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                            .addComponent(txtSoftwareName)
                                                                            .addComponent(jLabel3))
                                                                    .addGap(0, 253, Short.MAX_VALUE))
                                                            .addComponent(jScrollPane1)))
                                            .addGroup(layout.createSequentialGroup()
                                                    .addGap(0, 0, Short.MAX_VALUE)
                                                    .addComponent(btnOK, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addContainerGap())
            );
            layout.setVerticalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                    .addContainerGap()
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(jLabel1)
                                            .addGroup(layout.createSequentialGroup()
                                                    .addComponent(txtSoftwareName)
                                                    .addGap(11, 11, 11)
                                                    .addComponent(jLabel3)
                                                    .addGap(5, 5, 5)
                                                    .addComponent(jScrollPane1))
                                            .addComponent(jLabel5))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(btnOK)
                                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );
            
            pack();
        } // </editor-fold>//GEN-END:initComponents
        catch (IOException ex) {
            Logger.getLogger(AboutDialogHGPEC.class.getName()).log(Level.SEVERE, null, ex);
        } catch (URISyntaxException ex) {
            Logger.getLogger(AboutDialogHGPEC.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void btnOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOKActionPerformed
        // TODO add your handling code here:
        this.dispose();
    }//GEN-LAST:event_btnOKActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnOK;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JLabel txtSoftwareName;
    // End of variables declaration//GEN-END:variables

}
