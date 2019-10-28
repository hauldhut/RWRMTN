/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package vn.net.cbm.RWRMTN.internal.Base;

import java.util.ArrayList;

/**
 *
 * @author Le Duc Hau
 */
public class Node {
    public String NodeID;
    public String Name;
    public double Score;
    public int Rank;

    public Node(){
        this.NodeID="";
        this.Name="";
        this.Score=0.0;
        this.Rank=0;
    }

}
