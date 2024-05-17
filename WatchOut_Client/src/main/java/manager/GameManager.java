package manager;

import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import threads.CheckToStartGameThread;
import threads.CheckToStopGameThread;
import threads.PersistentGetAllPlayersThread;
import threads.PersistentGetAvgNHrsThread;
import threads.PersistentGetTotalAvgTsHrsThread;


public class GameManager
{
    private static final int MinimumPlayersNum_toStart = 2;
    private static final double MinimumPlayerHRRatio_toStop = 0.05;
    private static final String START_GAME = "START_GAME";
    private static final String STOP_GAME = "STOP_GAME";
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 1337;
    
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
    private HashMap<Integer, HashMap<Double, ArrayList<Double>>> allPlayerHistoricalHrs;

    private static GameManager instance;

    public GameManager()
    {
        this.broker = "tcp://localhost:1883";
        this.clientId = MqttClient.generateClientId();
        this.gamePubTopic = "home/game/state";
        this.customPubTopic = "home/game/custom";
        this.pubQos = 2;
        this.allPlayerHistoricalHrs = new HashMap();
        
        Gson jsonSerializer = new Gson();
        Client client = Client.create();
        String serverAddress = "http://" + SERVER_HOST + ":" + SERVER_PORT + "/";
        
        connectMqttBroker();
        
        CheckToStopGameThread checkToStop_thread = new CheckToStopGameThread(client, serverAddress, jsonSerializer, 0);
        CheckToStartGameThread checkToStart_thread = new CheckToStartGameThread(client, serverAddress, jsonSerializer, 0, checkToStop_thread);
        
        //SendCustomMsgToPlayersThread custom_thread = new SendCustomMsgToPlayersThread("Hello, this is custom!", false, 0);
        
        //start rest thread(s)
        checkToStart_thread.start();
        
        //start custom thread
        //custom_thread.start();
    }

    private void connectMqttBroker()
    {
        boolean isConnected = false;
        
        try
        {
            while(!isConnected)
            {
                try
                {
                    this.client = new MqttClient(broker, clientId);
                    MqttConnectOptions connOpts = new MqttConnectOptions();
                    connOpts.setCleanSession(true);

                    System.out.println(clientId + " - Connecting Broker " + broker);
                    client.connect(connOpts);
                    System.out.println(clientId + " - Connected");
                    isConnected = true;
                }
                catch (MqttException mqttE) 
                {
                    System.err.println("In connectMqttBroker: reason " + mqttE.getReasonCode() + ", cause: " + mqttE.getCause() + ", msg: " + mqttE.getMessage());
                    mqttE.printStackTrace();
                    System.out.println("Retrying after 10s ...");
                    Thread.sleep(10000);
                } 
            }
        }
        catch (Exception e) 
        {
            System.err.println("In connectMqttBroker: " + e.getMessage());
            e.printStackTrace();
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
                Thread.sleep(5000); //wait for new unsubscribed registered players
                
                //start the game
                sendMqttMessage(this.gamePubTopic, this.pubQos, START_GAME);
                
                System.out.println("Game started with playersNum: " + playersNum + ", timestamp: " + System.currentTimeMillis());
                
                return true;
            }
        }
        catch (Exception e) 
        {
            System.err.println("In checkToStart: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * checks if a player's new-HR avg has exceeded the avg of its historical HR by the ratio of MinimumPlayerHRRatio_toStop.if so, then publish stop-game message through MQTT.
     * @param allPlayerHrs
     * @return true if game is stopped, so this method shouldn't be called again
     */
    public boolean checkToStop(HashMap<Integer, HashMap<Double, ArrayList<Double>>> allPlayerHrs)
    {
        try
        {
            for (int playerId : allPlayerHrs.keySet())
            {
                HashMap<Double, ArrayList<Double>> timestampedHrs = allPlayerHrs.get(playerId);
                
                if (allPlayerHistoricalHrs.containsKey(playerId)) //check the two averages with respect to MinimumPlayerHRRatio_toStop
                {
                    if(timestampedHrs.isEmpty())
                        continue;
                    
                    HashMap<Double, ArrayList<Double>> historicalTimestampedHrs = this.allPlayerHistoricalHrs.get(playerId);
                    
                    double historicalHrRatio = historicalTimestampedHrs.values()
                                                .stream()
                                                .flatMap(List::stream)
                                                .mapToDouble(hr -> hr).sum() / historicalTimestampedHrs.values().size();
                    
                    double currentHrRatio = timestampedHrs.values()
                                             .stream()
                                             .flatMap(List::stream)
                                             .mapToDouble(hr -> hr).sum() / timestampedHrs.values().size();
                    
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
            System.err.println("In checkToStop: " + e.getMessage());
            e.printStackTrace();
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
            System.err.println("In sendCustomMsg: " + e.getMessage());
            e.printStackTrace();
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
        
    public PersistentGetAllPlayersThread preparePersistentGetAllPlayersThread()
    {
        Gson jsonSerializer = new Gson();
        Client client = Client.create();
        String serverAddress = "http://" + SERVER_HOST + ":" + SERVER_PORT + "/";
        
        return new PersistentGetAllPlayersThread(client, serverAddress, jsonSerializer, 0);
    }
    
    public PersistentGetAvgNHrsThread preparePersistentGetAvgNHrsThread(int playerId, int n)
    {
        Gson jsonSerializer = new Gson();
        Client client = Client.create();
        String serverAddress = "http://" + SERVER_HOST + ":" + SERVER_PORT + "/";
        
        return new PersistentGetAvgNHrsThread(client, serverAddress, jsonSerializer, 0, playerId, n);
    }
    
    public PersistentGetTotalAvgTsHrsThread preparePersistentGetTotalAvgTsHrsThread(long ts1, long ts2)
    {
        Gson jsonSerializer = new Gson();
        Client client = Client.create();
        String serverAddress = "http://" + SERVER_HOST + ":" + SERVER_PORT + "/";
        
        return new PersistentGetTotalAvgTsHrsThread(client, serverAddress, jsonSerializer, 0, ts1, ts2);
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
