/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package vn.net.cbm.RWRMTN.internal.model;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 *
 * @author Le Duc Hau
 */
public class Evaluation {
    public ArrayList<Double> TPFs; //True Positive Fraction (Vertical axis)
    public ArrayList<Double> Precisions;
    public ArrayList<Double> FPFs; //False Positive Fraction (Horizontal axis)
    public double AUC;
    
    public int MaxRank;
    public ArrayList<Integer> HoldoutRanks;

    public Evaluation(){
        HoldoutRanks=new ArrayList<Integer>();
        TPFs = new ArrayList<Double>();
        FPFs = new ArrayList<Double>();
        //RandomRanks=new ArrayList<Integer>();
    }
 
    
    public void calTPFs_FPFs(){
        
        int threshold,i;
        TPFs = new ArrayList<Double>();
        FPFs = new ArrayList<Double>();
        Precisions = new ArrayList<Double>();
        for(threshold=1;threshold<=MaxRank;threshold++){
            int numTP=0;
            int numFN=0;
            int numFP=0;
            int numTN=0;
            for(i=0;i<HoldoutRanks.size();i++){
                if(HoldoutRanks.get(i)<=threshold){
                    numTP++;
                }else{
                    numFN++;
                }
            }
            numTN=(MaxRank-threshold)*HoldoutRanks.size()-numFN;
            numFP=threshold*HoldoutRanks.size()-numTP;
            TPFs.add((double)numTP/(numTP+numFN));//Recall
            FPFs.add((double)numFP/(numFP+numTN));
            Precisions.add((double)numTP/(numTP+numFP));
            //TPFs.add((double)numTP/HoldoutRanks.size());

            //FPFs.add((double)(threshold*HoldoutRanks.size()-numTP)/((MaxRank-1)*HoldoutRanks.size()));//MaxRank-1 equal to Number of genes in Common.AllRankedTestGene
        }
        
    }
    
    public void calcAUC(){
        double temp1=0;
        double temp2=0;
        int i;
        for(i=0;i<MaxRank-1;i++){
            temp1+=(FPFs.get(i+1)-FPFs.get(i))*((TPFs.get(i+1)+TPFs.get(i))/2);
            temp2+=(Precisions.get(i+1)+Precisions.get(i))*((TPFs.get(i+1)-TPFs.get(i))/2);
        }
        this.AUC = temp1;
        
    }
    
    public static void calculateOverallAUCROC(String ROCFileName, int MaxRank, ArrayList<ArrayList<Double>> AllFPFs, ArrayList<ArrayList<Double>> AllTPFs, ArrayList<ArrayList<Double>> AllPrecisions) throws Exception{
        double OverallAUC;
        
        int i,j;
        ArrayList<ArrayList<Double>> aTPFs = new ArrayList<ArrayList<Double>>();
        ArrayList<ArrayList<Double>> aFPFs = new ArrayList<ArrayList<Double>>();
        ArrayList<ArrayList<Double>> aPrecisions = new ArrayList<ArrayList<Double>>();
        for(j=0;j<MaxRank;j++){
            aTPFs.add(new ArrayList<Double>());
            aFPFs.add(new ArrayList<Double>());
            aPrecisions.add(new ArrayList<Double>());
        }
        for(i=0;i<AllTPFs.size();i++){
            for(j=0;j<MaxRank;j++){
                if(j<AllFPFs.get(i).size()){
                    aFPFs.get(j).add(AllFPFs.get(i).get(j));
                    aTPFs.get(j).add(AllTPFs.get(i).get(j));
                    aPrecisions.get(j).add(AllPrecisions.get(i).get(j));
                }else{
                    aFPFs.get(j).add(-1.0);
                    aTPFs.get(j).add(-1.0);
                    aPrecisions.get(j).add(-1.0);
                }
            }
        }
        
        
        int k;
        double[] sumFPFs = new double[MaxRank];
        double[] sumTPFs = new double[MaxRank];
        double[] sumPrecisions = new double[MaxRank];
        int[] count = new int[MaxRank];
        for(j=0;j<MaxRank;j++){
            sumFPFs[j]=0.0;
            sumTPFs[j]=0.0;
            sumPrecisions[j]=0.0;
            count[j]=0;
        }
        for(j=0;j<MaxRank;j++){
            for(k=0;k<aFPFs.get(j).size();k++){
                if(aFPFs.get(j).get(k)!=-1.0 && aTPFs.get(j).get(k)!=-1.0){
                    sumFPFs[j]+=aFPFs.get(j).get(k);
                    sumTPFs[j]+=aTPFs.get(j).get(k);
                    sumPrecisions[j]+=aPrecisions.get(j).get(k);
                    count[j]++;
                }
            }
        }
        
        ArrayList<Double> AvgTPFs = new ArrayList<Double>(); 
        ArrayList<Double> AvgFPFs = new ArrayList<Double>();
        ArrayList<Double> AvgPrecisions = new ArrayList<Double>();
        for(j=0;j<MaxRank;j++){
            if(count[j]>0){
                AvgFPFs.add(sumFPFs[j]/count[j]);
                AvgTPFs.add(sumTPFs[j]/count[j]);
                AvgPrecisions.add(sumPrecisions[j]/count[j]);
            }
        }
        
        //Draw and Store ROC        
        double temp1=0;
        double temp2=0;
        System.out.println("AvgTPFs.size(): " + AvgTPFs.size());
        System.out.println("AvgFPFs.size(): " + AvgFPFs.size());
        System.out.println("AvgPrecisions.size(): " + AvgPrecisions.size());
        for(i=0;i<AvgTPFs.size()-1;i++){
            temp1+=(AvgFPFs.get(i+1)-AvgFPFs.get(i))*(AvgTPFs.get(i+1)+AvgTPFs.get(i))/2;
            temp2+=(AvgPrecisions.get(i+1)+AvgPrecisions.get(i))*(AvgTPFs.get(i+1)-AvgTPFs.get(i))/2;
        }
        
        OverallAUC=temp1;
        
        System.out.println("OverallAUC: " + OverallAUC);
        
        
        PrintWriter pw1 = new PrintWriter(new FileOutputStream(ROCFileName + "_ROC.txt"), true);
        
        
        for(i=0;i<AvgTPFs.size();i++){
            pw1.println(AvgFPFs.get(i) + "\t" + AvgTPFs.get(i));
            
        }
        pw1.close();

    }
}
