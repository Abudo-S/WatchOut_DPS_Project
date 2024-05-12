package beans;

import java.security.KeyException;
import java.util.*;
import java.util.stream.IntStream;
import manager.CustomLock;

public class Player
{
    private static final int[][] HomeBaseCoordinates = {{4, 4}, {4, 5}, {5, 5}, {5, 4}};
    private static final int PitchLengthX = 10;
    private static final int PitchLengthY = 10;
    
    private int id;
    private int[] position;
    private PlayerStatus status;
    
    /**
     * Note that other players ids aren't available.
     * volatile enables us to have a instantaneous visibility on size and keys
     * <player's endpoint, player>
     */
    private volatile HashMap<String, Player> otherPlayers;
    private CustomLock otherPlayersLock;
    
    public Player(int id, int[] position, PlayerStatus status)
    {
        this.id = id;
        this.position = position;
        this.status = status;
        this.otherPlayersLock = new CustomLock();
    }
    
    public void addInitialOtherPlayers(List<String> playersEndpoints)
    {
        for (String playerEndpoint : playersEndpoints)
        {
            //suppose intially that other players are active
            this.otherPlayers.put(playerEndpoint, new Player(0, new int[] {0, 0}, PlayerStatus.Active));
        }
    }
    
    /**
     * More correct approach is to have a lock for each otherPlayer, so updating two or more different players can occur concurrently.
     * But for simplicity we can just use a lock for all otherPlayers
     * @param endpoint
     * @param player
     * @throws KeyException 
     */
    public void upsertOtherPlayer(String endpoint, Player player) throws KeyException
    {        
        this.otherPlayersLock.Acquire();
        this.otherPlayers.put(endpoint, player);
        this.otherPlayersLock.Release();
    }
    
    /**
     * used to update player's status subsequently
     * @param status 
     */
    public void setStatus(PlayerStatus status)
    {
       this.status = status;
    }
    
    public Player getOtherPlayer(String endpoint) throws KeyException
    {
        if(!this.otherPlayers.containsKey(endpoint))
            throw new KeyException("Can't find player with endpoint: " + endpoint);
        
        Player otherPlayer = null;

        this.otherPlayersLock.Acquire();
        otherPlayer = this.otherPlayers.get(endpoint);
        this.otherPlayersLock.Release();
        
        return otherPlayer;
    }
    
    public HashMap<String, Player> getOtherPlayers()
    {
        HashMap<String, Player> otherPlayers = null;
        this.otherPlayersLock.Acquire();
        otherPlayers = this.otherPlayers;
        this.otherPlayersLock.Release();
        
        return otherPlayers;
    }
    
    public PlayerStatus getStatus()
    {
        return this.status;
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
    
    /**
     * true if this player's position is closer to the H.B.
     * if the two distances in question are equal then the player with the highest id is considered closer to the H.B.
     * @return 
     */
    public boolean compareCloserDistanceToHB(int[] anotherPlayerPosition, int anotherPlayerId)
    {
        try
        {
            double playerMinDistance = Player.getMinDistanceToHB(this.position);
            double anotherPlayerMinDistance = Player.getMinDistanceToHB(anotherPlayerPosition);
            
            if(playerMinDistance > anotherPlayerMinDistance)
                return true;
            else if(playerMinDistance < anotherPlayerMinDistance)
                return false;
            else //==
                return this.id > anotherPlayerId;
        }
        catch(Exception e)
        {
            System.err.println("In CompareCloserDistanceToHB: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Can be used to calculated min distance to H.B from a player position.
     * it gets the euclidean distance between a player's position and H.B. all points.
     * @param playerPosition
     * @return 
     */
    public static double getMinDistanceToHB(int[] playerPosition)
    {
        int[][] playerDiffPoints = IntStream.range(0, HomeBaseCoordinates.length)
                                        .mapToObj(m -> 
                                            IntStream.range(0, HomeBaseCoordinates[m].length)
                                            .map(m1 -> (HomeBaseCoordinates[m][m1] - playerPosition[m1]) ^ 2)
                                            .toArray()
                                        ).toArray(int[][]::new);
        
        double playerMinDistance = Arrays.stream(IntStream.range(0, playerDiffPoints.length)
                                        .mapToDouble(m -> 
                                            (Arrays.stream(playerDiffPoints[m]).sum() * 0.5)
                                        ).toArray()).min().getAsDouble();
        
        return playerMinDistance;
    }
    
    @Override
    public String toString()
    {
        return "id: " + this.id + ", status : " + this.status + ", position: " + Arrays.toString(this.position);
    }

}
