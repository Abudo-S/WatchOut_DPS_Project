package services;

import beans.*;
import com.google.gson.Gson;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import manager.PlayersRegistryManager;


@Path("registration")
public class RegistrationRestService 
{

    @Path("add_player")
    @POST
    @Produces({"application/json"})
    @Consumes({"application/json"})
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
                ArrayList<String> playerEPs = registry.getPlayersEndpoints();
                playerEPs.remove(endpoint);
                
                AddPlayerResponse response = new AddPlayerResponse(player.getId(), player.getPosition(), playerEPs);
                return Response.ok(new Gson().toJson(response)).build();
            }
        }
        catch(Exception e)
        {
            System.out.println("In addPlayer: " + e.getMessage());
        }
        
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    
    @Path("get_total_players_number")
    @GET
    @Produces({"application/json"})
    public Response getTotalPlayersNumber()
    {
        try
        {
            System.out.println("Invoked getTotalPlayersNumber");
            
            PlayersRegistryManager registry = PlayersRegistryManager.getInstance();
            
            return Response.ok(new Gson().toJson(new TotalPlayersNumberResponse(registry.getTotalPlayersNumber()))).build();
        }
        catch(Exception e)
        {
            System.out.println("In getTotalPlayersNumber: " + e.getMessage());
        }
        
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    
    @Path("get_all_players")
    @GET
    @Produces({"application/json"})
    public Response getAllPlayers()
    {
        try
        {
            System.out.println("Invoked getAllPlayers");
            
            PlayersRegistryManager registry = PlayersRegistryManager.getInstance();
            
            HashMap<Integer, AbstractMap.SimpleEntry<String, Integer[]>> compliantAllPlayers = new HashMap();
            
            ArrayList<Player> allPlayers = registry.getAllPlayers();
            
            for(Player p : allPlayers)
            {
                compliantAllPlayers.put(p.getId(), new AbstractMap.SimpleEntry(p.getEndpoint(), p.getPosition()));
            }
            
            return Response.ok(new Gson().toJson(new GetAllPlayersResponse(compliantAllPlayers))).build();
        }
        catch(Exception e)
        {
            System.out.println("In getAllPlayers: " + e.getMessage());
        }
        
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
}
