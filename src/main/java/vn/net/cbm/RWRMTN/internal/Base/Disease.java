/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package vn.net.cbm.RWRMTN.internal.Base;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Administrator
 */
public class Disease {
    
    public String DiseaseID;//Common ID (stand for ICD9CM, MIMID, etc...)
    public String Name;

    public Set<String> AssociatedNodeSet;
    public ArrayList<String> AssociatedNodeArray;
    public Disease(){
        
        this.DiseaseID="";
        this.Name="";

        this.AssociatedNodeSet = new TreeSet<String>();
        this.AssociatedNodeArray = new ArrayList<String>();
    }

}
