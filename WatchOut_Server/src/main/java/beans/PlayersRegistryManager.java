package beans;

import java.util.ArrayList;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;

public class PlayersRegistryManager {

    /**
     * <playerId, <tsDuration, <H.R. avgs>>>
     */
    @XmlElement(name = "registry")
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

    public HashMap<Integer, HashMap<Double, ArrayList<Double>>> addPlayerHR()
    {
        return this.registry;
    }
    
    public boolean getAllPlayersHR(int playerId, HashMap<Double, ArrayList<Double>> timestampedHR)
    {
        if (!this.registry.containsKey(playerId)) 
            return false;  //Key is not present

        this.registry.replace(playerId, timestampedHR);
        
        return true;
    }
    
        
    /**
     * singleton pattern
     * @return instance
     */
    public synchronized static PlayersRegistryManager getInstance()
    {
        if(instance == null)
            instance = new PlayersRegistryManager();
        
        return instance;
    }
}
