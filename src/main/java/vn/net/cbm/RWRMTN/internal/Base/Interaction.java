/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package vn.net.cbm.RWRMTN.internal.Base;

/**
 *
 * @author Le Duc Hau
 */
public class Interaction {
    public String Index;//Common field to store field by which Interaction list will be sorted
    public String NodeSrc;
    public String NodeDst;
    public int Type;
    public double Weight;
    public double WeightOriginal;

    public Interaction(){
        this.Type=0;
        this.NodeSrc="";
        this.NodeDst="";
        this.Weight=0;
        this.WeightOriginal=0;
    }
     
    
}
