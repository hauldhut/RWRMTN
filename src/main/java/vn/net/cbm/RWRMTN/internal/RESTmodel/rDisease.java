package vn.net.cbm.RWRMTN.internal.RESTmodel;

import io.swagger.annotations.ApiModelProperty;

public class rDisease {
	@ApiModelProperty(value="Disease ID",example="MIM104300")
	public String diseaseID;
	
	@ApiModelProperty(value="Disease Name",example="ALZHEIMER DISEASE; AD")
	public String diseaseName;
}
