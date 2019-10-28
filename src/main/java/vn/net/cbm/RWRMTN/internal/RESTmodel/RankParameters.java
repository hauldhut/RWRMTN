package vn.net.cbm.RWRMTN.internal.RESTmodel;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="RankParameters",description="Input parameters to rank RNA")
public class RankParameters {
	@ApiModelProperty(value="Disease OMIM ID",example="MIM114480",required=true)
	public String diseaseOMIMID;

	@ApiModelProperty(value="List of miRNA to rank",example="All miRNAs",required=true)
	public String listOfmiRNAs;
	
	@ApiModelProperty(value="MicroRNA Dataset",example="TargetScan",required=false)
	public String miRTargetDB;

	@ApiModelProperty(value="Disease-miRNA Dataset",example="miR2Disease",required=false)
	public String miR2DiseaseDB;
	
	@ApiModelProperty(value="Back-probability",example="0.5",required=false)
	public float backProb;
	
	@ApiModelProperty(value="Sub-network importance weight",example="0.5",required=false)
	public float subnetWeight;
	
}
