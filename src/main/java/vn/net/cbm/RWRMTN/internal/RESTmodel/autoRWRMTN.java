package vn.net.cbm.RWRMTN.internal.RESTmodel;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import vn.net.cbm.RWRMTN.internal.Base.Disease;

@Api(tags = "Apps: RWRMTN")
@Path("/RWRMTN/v1/")
public interface autoRWRMTN {
	public static final String DISEASE_LIST = "For a list of all disease names, see 'GET /RWRMTN/v1/diseaseList'";
        
	

	// ------- select disease to rank----------
	@ApiOperation(value = "Rank candidate miRNAs", notes = "Select disease from Heterogeneous network to rank associated miRNAs.\n\n"
			+ DISEASE_LIST, response = RankOutput.class, responseContainer = "List")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successful rank RNA", response = RankOutput.class, responseContainer = "List"),
			@ApiResponse(code = 404, message = "Unsuccessful rank RNA", response = ErrorMessage.class) })
	@Path("rank")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public ArrayList<RankOutput> selectRankDisease(
			@ApiParam(value = "Required parameters for rank algorithm", required = true) RankParameters rp);

	// ---------- get list of Disease name---------
	@ApiOperation(value = "List all diseases", notes = "Returns a list of ID and Disease Name for all Disease in the network.", response = rDisease.class, responseContainer = "List")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successful retrieve list of Disease", response = Disease.class, responseContainer = "List"),
			@ApiResponse(code = 404, message = "Disease network is not loaded", response = ErrorMessage.class) })
	@Path("diseaseList/")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<rDisease> getDiseaseList();


	// ------------get Disease ID of Disease name ------------
	@ApiOperation(value = "List the diseases match keyword search", notes = "Returns a list of disease ID which matches the keyword search.", response = String.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful found", response = String.class),
			@ApiResponse(code = 404, message = "DiseaseID not found", response = ErrorMessage.class) })
	@Path("diseaseList/{diseaseName}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public ArrayList<rDisease> getDiseaseId(
			@ApiParam(value = "Disease name " + DISEASE_LIST, required = true) @PathParam("diseaseName") String diseaseName);

	// ------- get ranked disease ----------
	@ApiOperation(value = "Return top ranked miRNAs", notes = "Return rank top of disease of users' interest.\n\n")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Sucess", response = RankOutput.class, responseContainer = "List"),
			@ApiResponse(code = 404, message = "Unsucess. Complete all the steps in CyREST command for result", response = ErrorMessage.class)

	})
	@Path("getRank/{limit}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<RankOutput> getRankedGenes(
			@ApiParam(value = "Select top number:", required = true) @PathParam("limit") int limit);

}
