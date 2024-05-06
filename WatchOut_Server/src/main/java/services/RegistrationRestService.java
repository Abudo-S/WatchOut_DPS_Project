package services;

import beans.*;
import com.google.gson.Gson;
import java.util.ArrayList;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;


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
            
            Player player = new Player(endpoint);
            
            if(!registry.addPlayer(player))
            {
                return Response.status(Response.Status.NOT_ACCEPTABLE).build();
            }
            else
            {
                //set player initial coordinates
                //to be completed
                AddPlayerResponse response = new AddPlayerResponse(0, new int[] {0, 0}, new ArrayList<String>());
                return Response.ok(new Gson().toJson(response)).build();
            }
        }
        catch(Exception e)
        {
            System.out.println("In addPlayer: " + e.getMessage());
        }
        
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    @Path("get_players_HR")
    @GET
    @Produces({"application/json", "application/xml"})
    public Response getPlayersHR()
    {
        try
        {
            System.out.println("Invoked getPlayersHR");
        
            //to be implemented
        }
        catch(Exception e)
        {
            System.out.println("In getPlayersHR: " + e.getMessage());
        }
        
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    
    @Path("get_players")
    @GET
    @Produces({"application/json", "application/xml"})
    public Response getPlayers()
    {
        try
        {
            System.out.println("Invoked getPlayers");
            
            //to be implemented
        }
        catch(Exception e)
        {
            System.out.println("In getPlayers: " + e.getMessage());
        }
        
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
}
