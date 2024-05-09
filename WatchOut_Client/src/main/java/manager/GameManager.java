package manager;

import java.util.ArrayList;
import java.util.HashMap;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;


public class GameManager
{
    private static final int MinimumPlayersNum_toStart = 4;
    private static final double MinimumPlayerHRRatio_toStop = 0.05;
    private static final String START_GAME = "START_GAME";
    private static final String STOP_GAME = "STOP_GAME";
    
    private String broker;
    private String clientId;
    private String gamePubTopic;
    private String customPubTopic;
    private int pubQos;
    
    private MqttClient client;
    
    /**
     * used to maintain all player's historical Health rate average
     * <playerId, <timestamp, HR>>
     */
    private HashMap<Integer, HashMap<Double, Double>> allPlayerHistoricalHrs;

    private static GameManager instance;

    public GameManager()
    {
        this.broker = "tcp://localhost:1883";
        this.clientId = MqttClient.generateClientId();
        this.gamePubTopic = "home/state/game";
        this.customPubTopic = "home/custom/game";
        this.pubQos = 2;
        
        connectMqttBroker();
    }

    private void connectMqttBroker()
    {
        try
        {
            this.client = new MqttClient(broker, clientId);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);

            System.out.println(clientId + " - Connecting Broker " + broker);
            client.connect(connOpts);
            System.out.println(clientId + " - Connected");
        }
        catch (MqttException mqttE) 
        {
            System.out.println("In connectMqttBroker: reason " + mqttE.getReasonCode() + ", cause: " + mqttE.getCause() + ", msg: " + mqttE.getMessage());
        } 
        catch (Exception e) 
        {
            System.out.println("In connectMqttBroker: " + e.getMessage());
        }
    }
    
    /**
     * check if the minimum number of players is satisfied
     * if so, publish start-game message through MQTT.
     * @param playersNum
     * @return true if game is started, so this method shouldn't be called again
     */
    public boolean checkToStart(int playersNum)
    {
        try
        {
            if (playersNum >= MinimumPlayersNum_toStart)
            {
                //start the game
                sendMqttMessage(this.gamePubTopic, this.pubQos, START_GAME);
                
                System.out.println("Game started with playersNum: " + playersNum);
            }
        }
        catch (Exception e) 
        {
            System.out.println("In checkToStart: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * checks if a player's new-HR avg has exceeded the avg of its historical HR by the ratio of MinimumPlayerHRRatio_toStop.if so, then publish stop-game message through MQTT.
     * @param allPlayerHrs
     * @return true if game is stopped, so this method shouldn't be called again
     */
    public boolean checkToStop(HashMap<Integer, HashMap<Double, Double>> allPlayerHrs)
    {
        try
        {
            for (int playerId : allPlayerHrs.keySet())
            {
                HashMap timestampedHrs = allPlayerHrs.get(playerId);
                
                if (allPlayerHistoricalHrs.containsKey(playerId)) //check the two averages with respect to MinimumPlayerHRRatio_toStop
                {
                    HashMap historicalHrs = this.allPlayerHistoricalHrs.get(playerId);
                    
                    double historicalHrRatio = (new ArrayList<Double>(historicalHrs.values())
                                             .stream()
                                             .mapToDouble(hr -> hr).sum()) / historicalHrs.values().size();
                    
                    double currentHrRatio = (new ArrayList<Double>(timestampedHrs.values())
                                             .stream()
                                             .mapToDouble(hr -> hr).sum()) / timestampedHrs.values().size();
                    
                    double diffHrRatio = currentHrRatio - historicalHrRatio;
                    
                    if (diffHrRatio >= MinimumPlayerHRRatio_toStop)
                    {
                        System.out.println("Found player: " + playerId + "historicalHrRatio: " + historicalHrRatio + ", currentHrRatio: " + currentHrRatio);
                        System.out.println("Found player: " + playerId + ", diffHrRatio: " + diffHrRatio);
                                
                        //stop the game
                        sendMqttMessage(this.gamePubTopic, this.pubQos, STOP_GAME);

                        System.out.println("Game stopped with playersNum: " + allPlayerHrs.keySet().size());

                        return true;
                    }
                }
                else //insert new player and calculate the average associated to last timestamp
                {
                    this.allPlayerHistoricalHrs.put(playerId, timestampedHrs);
                }
            }
        }
        catch (Exception e) 
        {
            System.out.println("In checkToStop: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * publish custom message through MQTT
     * @param customMsg
     * @return 
     */
    public boolean sendCustomMsg(String customMsg)
    {
        try
        {
            //send custom message
            sendMqttMessage(this.customPubTopic, this.pubQos, customMsg);

            return true;
        }
        catch (Exception e) 
        {
            System.out.println("In sendCustomMsg: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * sends/published MQTT message to the broker, using the following parameters
     * @param topic
     * @param qos
     * @param payload 
     */
    private void sendMqttMessage(String topic, int qos, String payload)
    {
        try 
        {
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(qos);

            System.out.println(clientId + " - Publishing message: " + payload + " ...");
            client.publish(topic, message);
            System.out.println(clientId + " - Message published");
        }
        catch (MqttException mqttE) 
        {
            System.out.println("In sendMqttMessage: reason " + mqttE.getReasonCode() + ", cause: " + mqttE.getCause() + ", msg: " + mqttE.getMessage());
        } 
    }
        
    /**
     * singleton pattern
     * @return instance
     */
    public static GameManager getInstance()
    {
        if(instance == null)
            instance = new GameManager();
        
        return instance;
    }
}
