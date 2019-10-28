package vn.net.cbm.RWRMTN.internal.RESTmodel;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="RankOutput",description="Output of rank algorithm")
public class RankOutput {
	@ApiModelProperty(value="Name",example="hsa-miR-124")
	public String rnaName;
	
	@ApiModelProperty(value="Score",example="0.0186532")
	public double rnaScore;
	
	@ApiModelProperty(value="Rank",example="1")
	public int rnaRank;
	@ApiModelProperty(value="Type",example="miRNA")
	public String type;
	@ApiModelProperty(value="Known",example="true")
	public boolean known;
}
