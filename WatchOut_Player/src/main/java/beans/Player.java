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
     * volatile enables us to have an instantaneous visibility on size and keys
     * <player's endpoint, player>
     */
    private volatile HashMap<String, Player> otherPlayers;
    
    /**
     * used to synchronize operations on otherPlayers.
     * it's reasonable that a lock on a player's op shouldn't influence other players ops
     */
    private volatile HashMap<String, CustomLock> otherPlayersLocks;
    
    public Player(int id, int[] position, PlayerStatus status)
    {
        this.id = id;
        this.position = position;
        this.status = status;
        this.otherPlayers = new HashMap();
        this.otherPlayersLocks = new HashMap();
    }
    
    public void addInitialOtherPlayers(List<String> playersEndpoints)
    {
        for (String playerEndpoint : playersEndpoints)
        {
            //suppose intially that other players are active
            this.otherPlayers.put(playerEndpoint, new Player(0, new int[] {0, 0}, PlayerStatus.Active));
            //add related lock
            this.otherPlayersLocks.put(playerEndpoint, new CustomLock());
        }
    }
    
    /**
     * otherPlayer's lock should be handled by method's caller
     * @param endpoint
     * @param player
     * @throws KeyException 
     */
    public void upsertOtherPlayer(String endpoint, Player player) throws KeyException
    {        
        this.otherPlayers.put(endpoint, player);
        
        if(!this.otherPlayersLocks.containsKey(endpoint))
            this.otherPlayersLocks.put(endpoint, new CustomLock());
    }
    
    public void setPosition(int[] position)
    {
       this.position = position;
    }
    
    /**{
     * used to update player's status subsequently
     * @param status 
     */
    public void setStatus(PlayerStatus status)
    {
       this.status = status;
    }
    
    /**
     * otherPlayer's lock should be handled by method's caller
     * @param endpoint
     * @return
     * @throws KeyException 
     */
    public Player getOtherPlayer(String endpoint) throws KeyException
    {
        if(!this.otherPlayers.containsKey(endpoint))
            throw new KeyException("Can't find player with endpoint: " + endpoint);
        
        Player otherPlayer = this.otherPlayers.get(endpoint);
        
        return otherPlayer;
    }
    
    public HashMap<String, Player> getOtherPlayers()
    {
        return this.otherPlayers;
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
     * set the player's position on the first point of H.B.
     */
    public void setPositionOnReachedHB()
    {
        this.position = HomeBaseCoordinates[0];
    }
    
    public void AcquireOtherPlayerLock(String otherPlayerEndpoint) throws KeyException
    {
        if(!this.otherPlayersLocks.containsKey(otherPlayerEndpoint))
            throw new KeyException("Can't find player lock with endpoint: " + otherPlayerEndpoint);
        
        this.otherPlayersLocks.get(otherPlayerEndpoint).Acquire();
    }
    
    public void ReleaseOtherPlayerLock(String otherPlayerEndpoint) throws KeyException
    {
        if(!this.otherPlayersLocks.containsKey(otherPlayerEndpoint))
            throw new KeyException("Can't find player lock with endpoint: " + otherPlayerEndpoint);
        
        this.otherPlayersLocks.get(otherPlayerEndpoint).Release();
    }
     
     /**
     * essentially used by the seeker.
     * it calculates the euclidean distance between a player's position and another position.
     * @param anotherPosition
     * @return 
     */
    public double getDistanceToPosition(int[] anotherPosition)
    {
        if(anotherPosition.length != 2)
            throw new NumberFormatException("Position's length sould be equal to 2!");
        
        double x =  Math.sqrt((Math.pow(this.position[0] - anotherPosition[0], 2) + Math.pow(this.position[1] - anotherPosition[1], 2)));
        return x;
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
            
            if(playerMinDistance < anotherPlayerMinDistance)
                return true;
            else if(playerMinDistance > anotherPlayerMinDistance)
                return false;
            else //==
                return this.id > anotherPlayerId;
        }
        catch(Exception e)
        {
            System.err.println("In CompareCloserDistanceToHB: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Can be used to calculated min distance to H.B from a player position.
     * it calculates the minimum euclidean distance between a player's position and H.B. all points.
     * @param playerPosition
     * @return 
     */
    public static double getMinDistanceToHB(int[] playerPosition)
    {
        int[][] playerDiffPoints = IntStream.range(0, HomeBaseCoordinates.length)
                                        .mapToObj(m -> 
                                            IntStream.range(0, HomeBaseCoordinates[m].length)
                                            .map(m1 -> (int)Math.pow(HomeBaseCoordinates[m][m1] - playerPosition[m1], 2))
                                            .toArray()
                                        ).toArray(int[][]::new);
        
        double playerMinDistance = Arrays.stream(IntStream.range(0, playerDiffPoints.length)
                                        .mapToDouble(m -> 
                                            Math.sqrt(Arrays.stream(playerDiffPoints[m]).sum())
                                        ).toArray()).min().getAsDouble();
        
        return playerMinDistance;
    }
    
    @Override
    public String toString()
    {
        return "id: " + this.id + ", status : " + this.status + ", position: " + Arrays.toString(this.position) + ", otherPlayers: " + String.join(", ", this.otherPlayers.keySet());
    }

}
