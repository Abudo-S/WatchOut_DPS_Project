package services;

import beans.*;
import com.google.gson.Gson;
import java.util.ArrayList;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import manager.PlayersRegistryManager;


@Path("registration")
public class RegistrationRestService 
{

    @Path("add_player")
    @POST
    @Consumes({"application/json", "application/xml"})
    public Response addPlayer(String endpoint)
    {
        try
        {
            System.out.println("Invoked addPlayer with endpoint: " + endpoint);
        
            PlayersRegistryManager registry = PlayersRegistryManager.getInstance();
            
            Player player = new Player(endpoint, PlayersRegistryManager.generateRandomValidCoordinates());
            
            if(!registry.addPlayer(player))
            {
                return Response.status(Response.Status.NOT_ACCEPTABLE).build();
            }
            else
            {
                AddPlayerResponse response = new AddPlayerResponse(player.getId(), player.getPosition(), registry.getPlayersEndpoints());
                return Response.ok(new Gson().toJson(response)).build();
            }
        }
        catch(Exception e)
        {
            System.out.println("In addPlayer: " + e.getMessage());
        }
        
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    @Path("get_players_hrs")
    @GET
    @Produces({"application/json", "application/xml"})
    public Response getPlayersHRs()
    {
        try
        {
            System.out.println("Invoked getPlayersHRs");
        
            PlayersRegistryManager registry = PlayersRegistryManager.getInstance();
            
            return Response.ok(new Gson().toJson(registry.getAllPlayerHRs())).build();
        }
        catch(Exception e)
        {
            System.out.println("In getPlayersHRs: " + e.getMessage());
        }
        
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    
    @Path("get_players_endpoints")
    @GET
    @Produces({"application/json", "application/xml"})
    public Response getPlayersEndpoints()
    {
        try
        {
            System.out.println("Invoked getPlayersEndpoints");
            
            PlayersRegistryManager registry = PlayersRegistryManager.getInstance();
            
            return Response.ok(new Gson().toJson(registry.getPlayersEndpoints())).build();
        }
        catch(Exception e)
        {
            System.out.println("In getPlayersEndpoints: " + e.getMessage());
        }
        
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
}
