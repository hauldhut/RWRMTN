/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package vn.net.cbm.RWRMTN.internal.Base;

/**
 *
 * @author Administrator
 */
public class NodeInteraction {
    public String Node;
    public double Weight;

    public NodeInteraction(){
        this.Node="";
        this.Weight=0.0;
    }

    public NodeInteraction(String Node, double Weight){
        this.Node=Node;
        this.Weight=Weight;
    }

}
