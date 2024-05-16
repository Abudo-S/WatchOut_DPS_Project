package services;

import beans.*;
import com.google.gson.Gson;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import manager.PlayersRegistryManager;


@Path("heart_rate")
public class HeartRateRestService
{
    @Path("get_player_avg_n_hrs/{playerId}/{n}")
    @GET
    @Produces({"application/json"})
    public Response getPlayerAvgNHrs(@PathParam("playerId") int playerId, @PathParam("n") int n)
    {
        try
        {
            //System.out.println("Invoked getPlayerAvgNHrs with playerId: " + playerId + ", n: " + n);
            
            PlayersRegistryManager registry = PlayersRegistryManager.getInstance();

            return Response.ok(new Gson().toJson(registry.getPlayerAvgNHrs(playerId, n))).build();
        }
        catch(Exception e)
        {
            System.err.println("In getPlayerAvgNHrs: " + e.getMessage());
            e.printStackTrace();
        }
        
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    
    
    @Path("get_player_avg_hrs/{ts1}/{ts2}")
    @GET
    @Produces({"application/json"})
    public Response getPlayerAvgTimestampedHrs(@PathParam("ts1") long ts1,@PathParam("ts2") long ts2)
    {
        try
        {
            //System.out.println("Invoked getPlayerAvgTimestampedHrs with t1: " + ts1 + ", t2: " + ts2);
            
            PlayersRegistryManager registry = PlayersRegistryManager.getInstance();

            return Response.ok(new Gson().toJson(registry.getTotalAvgTimestampedHrs(ts1, ts2))).build();
        }
        catch(Exception e)
        {
            System.err.println("In getPlayerAvgTimestampedHrs: " + e.getMessage());
            e.printStackTrace();
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
            //System.out.println("Invoked getPlayersHRs");
        
            PlayersRegistryManager registry = PlayersRegistryManager.getInstance();
            
            return Response.ok(new Gson().toJson(new AllPlayerHRsResponse(registry.getAllPlayerHRs()))).build();
        }
        catch(Exception e)
        {
            System.err.println("In getPlayersHRs: " + e.getMessage());
            e.printStackTrace();
        }
        
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    
    @Path("add_player_hrs")
    @POST
    @Consumes({"application/json"})
    public Response addPlayerHrs(String request)
    {
        try
        {
            AddPlayerHrsRequest playerHrs = new Gson().fromJson(request.replaceAll("\\\\\"","\"").replaceAll("\"\"", "\""), AddPlayerHrsRequest.class);

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
            System.err.println("In addPlayerHrs: " + e.getMessage());
            e.printStackTrace();
        }
        
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    
}
