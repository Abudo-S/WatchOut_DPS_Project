package beans;

import java.util.*;

public class Player
{
    private int id;
    private int[] position;
    private PlayerStatus status;
    
    /**
     * Note that other players ids aren't available
     * <player's endpoint, player>
     */
    private HashMap<String, Player> otherPlayers;
    
    public Player(int id, int[] position, PlayerStatus status)
    {
        this.id = id;
        this.position = position;
        this.status = status;
    }
    
    public void addInitialOtherPlayers(List<String> playersEndpoints)
    {
        for (String playerEndpoint : playersEndpoints)
        {
            //suppose intially that other players are active
            this.otherPlayers.put(playerEndpoint, new Player(0, new int[] {0, 0}, PlayerStatus.Active));
        }
    }
    
    public void updateOtherPlayerStatus()
    {
        
    }
    
    /**
     * used to update player's status subsequently
     * @param status 
     */
    public void setStatus(PlayerStatus status)
    {
       this.status = status;
    }
    
    public int getId()
    {
        if(this.id == 0)
            throw new IllegalAccessError("Can't retreive player's id, may be it's a remote player!");
            
        return this.id;
    }
    
    public int[] getPosition()
    {
        return this.position;
    }
    
    @Override
    public String toString()
    {
        return "id: " + this.id + ", status : " + this.status + ", position: " + Arrays.toString(this.position);
    }

}
