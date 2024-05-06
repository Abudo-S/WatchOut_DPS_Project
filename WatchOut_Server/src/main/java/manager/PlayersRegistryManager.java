package manager;

import beans.Player;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.stream.Collectors;

public class PlayersRegistryManager 
{
    private static final int[][] excludedHomeBaseCoordinates = {{4, 4}, {4, 5}, {5, 5}, {5, 4}};
    private static final int pitchLengthX = 10;
    private static final int pitchLengthY = 10;
    
    /**
     * <playerId, <tsDuration, <H.R. avgs>>>
     */
    private HashMap<Integer, HashMap<Double, ArrayList<Double>>> registry;
    private ArrayList<Player> players;

    private static PlayersRegistryManager instance;

    public PlayersRegistryManager()
    {
        this.registry = new HashMap<>();
        this.players = new ArrayList<>();
    }

    /**
     * 
     * @param player
     * @return true if added, false otherwise
     */
    public boolean addPlayer(Player player)
    {
        if (this.registry.containsKey(player.getId()))
            return false;

        this.registry.put(player.getId(), new HashMap<>());
        this.players.add(player);
        
        System.out.println("Added player: " + player.getId());

        return true;
    }

    /**
     * appends a received player's HR
     * @param playerId
     * @param timestampedHR
     * @return true if added, false otherwise
     */
    public boolean addPlayerHR(int playerId, SimpleEntry<Double, ArrayList<Double>> timestampedHR)
    {
        if (this.registry.containsKey(playerId))
            return false;
        
        HashMap playerTimeStampedHR = this.registry.get(playerId);
        playerTimeStampedHR.put(timestampedHR.getKey(), timestampedHR.getValue());
        
        this.registry.put(playerId, playerTimeStampedHR);
        
        System.out.println("Added player HR: " + playerId + ", timestamp: " + timestampedHR);
         
        return true;
    }
    
    /**
     * 
     * @param playerId
     * @return single player HRs
     */
    public HashMap<Double, ArrayList<Double>> getPlayerHRs(int playerId)
    {
        if (!this.registry.containsKey(playerId)) 
            return null;

        return this.registry.get(playerId);
    }
    
    /**
     * 
     * @return all registered players HRs
     */
    public HashMap<Integer, HashMap<Double, ArrayList<Double>>> getAllPlayerHRs()
    {
        return this.registry;
    }
    
    /**
     * 
     * @return all current player endpoints
     */
    public ArrayList<String> getPlayersEndpoints()
    {
        return new ArrayList<String>(
                this.players.stream()
                            .map(player -> player.getEndpoint())
                            .collect(Collectors.toList())
        );
    }
    
    /**
     * generate 2d point with respect to the pitch limit.
     * the generated point shouldn't be reserved by the home base
     * @return 
     */
    public static int[] generateRandomValidCoordinates()
    {
        Random r = new Random();

        while(true)
        {
            int x = r.nextInt(pitchLengthX);
            int y = r.nextInt(pitchLengthY);
            boolean isValid = Arrays.stream(excludedHomeBaseCoordinates)
                            .anyMatch(m -> !Arrays.equals(m, new int[] {x, y}));
            
            if(isValid)
                return new int[] {x, y};
        }
    }
        
    /**
     * singleton pattern
     * @return instance
     */
    public static PlayersRegistryManager getInstance()
    {
        if(instance == null)
            instance = new PlayersRegistryManager();
        
        return instance;
    }
}
