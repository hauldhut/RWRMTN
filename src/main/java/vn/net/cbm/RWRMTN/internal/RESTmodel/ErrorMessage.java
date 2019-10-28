package vn.net.cbm.RWRMTN.internal.RESTmodel;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="CyREST Best Practice Error Message",description="Simple message object to relay error messages from endpoints")
public class ErrorMessage {
	@ApiModelProperty(value="The message string",example="Error processing the request right now")
	public String message;

	public ErrorMessage(String message) {
		super();
		this.message = message;
	}

	public ErrorMessage() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}
