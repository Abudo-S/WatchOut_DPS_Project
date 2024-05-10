package services;

import beans.*;
import com.google.gson.Gson;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import manager.PlayersRegistryManager;


@Path("heart_rate")
public class HeartRateRestService 
{
    @Path("add_player_hrs")
    @POST
    @Consumes({"application/json"})
    public Response addPlayerHrs(AddPlayerHrsRequest playerHrs)
    {
        try
        {
            System.out.println("Invoked addPlayerHrs with : " + playerHrs.toString());
            
            PlayersRegistryManager registry = PlayersRegistryManager.getInstance();
            
            if(!registry.addPlayerHRs(playerHrs.getPlayerId(), playerHrs.getHrs()))
            {
                return Response.status(Response.Status.NOT_ACCEPTABLE).build();
            }
            else
            {
                return Response.ok().build();
            }
        }
        catch(Exception e)
        {
            System.out.println("In addPlayerHrs: " + e.getMessage());
        }
        
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    
    
    @Path("get_players_hrs")
    @GET
    @Produces({"application/json"})
    public Response getPlayersHRs()
    {
        try
        {
            System.out.println("Invoked getPlayersHRs");
        
            PlayersRegistryManager registry = PlayersRegistryManager.getInstance();
            
            return Response.ok(new Gson().toJson(new AllPlayerHRsResponse(registry.getAllPlayerHRs()))).build();
        }
        catch(Exception e)
        {
            System.out.println("In getPlayersHRs: " + e.getMessage());
        }
        
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
}
