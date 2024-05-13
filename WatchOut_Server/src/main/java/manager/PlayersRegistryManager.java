package manager;

import beans.Player;
import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.stream.Collectors;

public class PlayersRegistryManager 
{
    private static final int[][] HomeBaseCoordinates = {{4, 4}, {4, 5}, {5, 5}, {5, 4}};
    private static final int PitchLengthX = 10;
    private static final int PitchLengthY = 10;
    
    private CustomLock players_lock;
    private CustomLock playersHR_lock;
    
    /**
     * <playerId, <tsDuration, <HR_Avgs>>>
     */
    private volatile HashMap<Integer, HashMap<Long, ArrayList<Double>>> registry;
    private ArrayList<Player> players;

    private static PlayersRegistryManager instance;

    public PlayersRegistryManager()
    {
        this.registry = new HashMap<>();
        this.players = new ArrayList<>();
        this.players_lock = new CustomLock();
        this.playersHR_lock = new CustomLock();
    }

    /**
     * 
     * @param player
     * @return true if added, false otherwise
     */
    public boolean addPlayer(Player player)
    {
        playersHR_lock.Acquire();
        if (this.registry.containsKey(player.getId()))
            return false;

        this.registry.put(player.getId(), new HashMap<>());
        playersHR_lock.Release();
        
        players_lock.Acquire();
        this.players.add(player);
        players_lock.Release();
        
        System.out.println("Added player: " + player.getId());

        return true;
    }

    /**
     * appends a received player's HR
     * @param playerId
     * @param timestampedHR
     * @return true if added, false otherwise
     */
    public boolean addPlayerHR(int playerId, SimpleEntry<Double, Double> timestampedHR)
    {
        playersHR_lock.Acquire();
        if (this.registry.containsKey(playerId))
            return false;
        
        HashMap playerTimeStampedHR = this.registry.get(playerId);
        playerTimeStampedHR.put(timestampedHR.getKey(), timestampedHR.getValue());
        
        this.registry.put(playerId, playerTimeStampedHR);
        playersHR_lock.Release();
        
        System.out.println("Added player HR, playerId: " + playerId + ", timestampedHH: " + timestampedHR);
         
        return true;
    }
    
    /**
     * appends a received player's HRs
     * @param playerId
     * @param timestampedHRs
     * @return true if added, false otherwise
     */
    public boolean addPlayerHRs(int playerId, SimpleEntry<Long, ArrayList<Double>> timestampedHRs)
    {
        playersHR_lock.Acquire();
        if (this.registry.containsKey(playerId))
            return false;
        
        HashMap playerTimeStampedHR = this.registry.get(playerId);
        playerTimeStampedHR.put(timestampedHRs.getKey(), timestampedHRs.getValue());
        
        this.registry.put(playerId, playerTimeStampedHR);
        playersHR_lock.Release();
        
        System.out.println("Added player HR, playerId: " + playerId);
         
        return true;
    }
    
    /**
     * 
     * @param playerId
     * @return single player HRs
     */
    public HashMap<Long, ArrayList<Double>> getPlayerHRs(int playerId)
    {
        playersHR_lock.Acquire();
        if (!this.registry.containsKey(playerId)) 
            return null;
        
        HashMap playerHRs = this.registry.get(playerId);
        playersHR_lock.Release();
        
        return playerHRs;
    }
    
    /**
     * 
     * @return all registered players HRs
     */
    public HashMap<Integer, HashMap<Long, ArrayList<Double>>> getAllPlayerHRs()
    {
        playersHR_lock.Acquire();
        HashMap reg = this.registry;
        playersHR_lock.Release();
        
        return reg;
    }
    
    public double getPlayerAvgNHrs(int playerId, int n)
    {
        double avg = 0;
        
        try
        {
            ArrayList<Long> sortedTimeStamps;
            HashMap<Long, ArrayList<Double>> playerTimestampedHrs;
            LinkedHashMap<Long, ArrayList<Double>> orderedTimestampedHrs = new LinkedHashMap();
            
            //take a copy of timeStamps
            playersHR_lock.Acquire();
            sortedTimeStamps = new ArrayList(this.registry.get(playerId).keySet());
            playerTimestampedHrs = new HashMap(this.registry.get(playerId));
            playersHR_lock.Release();
            
            int consideredSize = ((n) > sortedTimeStamps.size())? sortedTimeStamps.size() : n;  
            Collections.sort(sortedTimeStamps, Collections.reverseOrder());
            
            playerTimestampedHrs.keySet()
                                .stream()
                                .forEach(k -> orderedTimestampedHrs.put(k, playerTimestampedHrs.get(k)));
            
            List<Double> limitedHrs = orderedTimestampedHrs.values()
                                                           .stream()
                                                           .flatMap(List::stream)
                                                           .collect(Collectors.toList())
                                                           .subList(0, consideredSize);
            avg = limitedHrs.stream()
                            .mapToDouble(m -> (double)m)
                            .sum() / consideredSize;
        }
        catch (Exception e) 
        {
            System.out.println("In getPlayerAvgNHrs: " + e.getMessage());
        }
        
        return avg;
    }
    
    /**
     * 
     * @param playerId
     * @param ts1 lower bound
     * @param ts2 upper bound
     * @return 
     */
    public double getPlayerAvgTimestampedHrs(int playerId, long ts1, long ts2)
    {
        double avg = 0;
        
        try
        {
            ArrayList<Long> timeStamps;
            
            //take a copy of timeStamps
            playersHR_lock.Acquire();
            timeStamps = new ArrayList(this.registry.get(playerId).keySet());
            playersHR_lock.Release();
            
            List<Double> boundedHrs = timeStamps.stream()
                                                .filter(f -> f >= ts1 && f <= ts2)
                                                .map(m -> this.registry.get(m))
                                                .collect(Collectors.toList())
                                                .stream()
                                                .flatMap(List<Double>::stream)
                                                .collect(Collectors.toList());
              
            avg = boundedHrs.stream()
                            .mapToDouble(m -> (double)m)
                            .sum() / boundedHrs.size();
        }
        catch (Exception e) 
        {
            System.out.println("In getPlayerAvgNHrs: " + e.getMessage());
        }
        
        return avg;
    }
    
    /**
     * 
     * @return the length of List<players>
     */
    public int getTotalPlayersNumber()
    {
        players_lock.Acquire();
        int player_num = this.players.size();
        players_lock.Release();
        
        return player_num;
    }
    
    /**
     * 
     * @return all current player
     */
    public ArrayList<Player> getAllPlayers()
    {
        ArrayList<Player> playerEP;
        players_lock.Acquire();
        playerEP = this.players;
        players_lock.Release();
        
        return playerEP;
    }
    
    /**
     * 
     * @return all current player endpoints
     */
    public ArrayList<String> getPlayersEndpoints()
    {
        players_lock.Acquire();
        ArrayList playerEP = new ArrayList<String>(
                                    this.players.stream()
                                                .map(player -> player.getEndpoint())
                                                .collect(Collectors.toList())
                            );
        players_lock.Release();
        
        return playerEP;
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
            int x = r.nextInt(PitchLengthX);
            int y = r.nextInt(PitchLengthY);
            boolean isValid = Arrays.stream(HomeBaseCoordinates)
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
