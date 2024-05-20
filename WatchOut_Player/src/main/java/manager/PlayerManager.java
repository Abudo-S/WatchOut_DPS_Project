/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

import beans.Player;
import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import java.sql.Timestamp;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import threads.CheckToSendHrAvgsThread;
import threads.PersistentPlayerRegistrationThread;


/**
 * responsible of starting & stopping its smartWatchthrough MQTT and asking for game registration through REST.
 * responsible of receiving custom messages through MQTT
 * @author Admin
 */
public class PlayerManager 
{
    private static final int DELAY_COORDINATION_MILLISECONDS = 0;
    private static final int DELAY_SEEKING_MILLISECONDS = 60000;
    private static final int DELAY_HIDER_IN_HB_MILLISECONDS = 0;
    private static final String START_GAME = "START_GAME";
    private static final String STOP_GAME = "STOP_GAME";
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 1337;
    
    private String broker;
    private String clientId;
    private String gameSubTopic;
    private String customSubTopic;
    private int subQos;
    
    private MqttClient client;
    private PersistentPlayerRegistrationThread persistentPlayerReg_thread;
    private SmartWatch smartWatch;
    
    private static PlayerManager instance;
    
    public PlayerManager(String grpcServiceEndpoint)
    {
        if(grpcServiceEndpoint == null)
            throw new NullPointerException("Can't initialize PlayerManager with null grpcServiceEndpoint!");
        
        this.broker = "tcp://localhost:1883";
        this.clientId = MqttClient.generateClientId();
        this.gameSubTopic = "home/game/state";
        this.customSubTopic = "home/game/custom";
        this.subQos = 2;
        
        Gson jsonSerializer = new Gson();
        Client client = Client.create();
        String serverAddress = "http://" + SERVER_HOST + ":" + SERVER_PORT + "/";
        
        this.persistentPlayerReg_thread = new PersistentPlayerRegistrationThread(client, serverAddress, jsonSerializer, 0, grpcServiceEndpoint);
        this.persistentPlayerReg_thread.start();
        
        checkAndStartSmartWatch(grpcServiceEndpoint, client, serverAddress, jsonSerializer);
        new Thread(() -> connectMqttBroker()).start();
    }
    
    /**
     * should wait until player registration is completed successfully, then initialize the smart watch
     * @param client
     * @param serverAddress
     * @param jsonSerializer 
     */
    private void checkAndStartSmartWatch(String grpcServiceEndpoint, Client client, String serverAddress, Gson jsonSerializer)
    {
        try
        {
            while(!this.persistentPlayerReg_thread.checkIsCompleted())
                Thread.sleep(1000);
            
            Player player = this.persistentPlayerReg_thread.getBuiltPlayer();
            
            if (player == null)
            {
                throw new NullPointerException("Can't get builtPlayer!");
            }
            
            this.smartWatch = SmartWatch.getInstance(player, 
                                                     grpcServiceEndpoint,
                                                     new CheckToSendHrAvgsThread(client, serverAddress, jsonSerializer, 0, player.getId()),
                                                     DELAY_SEEKING_MILLISECONDS,
                                                     DELAY_HIDER_IN_HB_MILLISECONDS);
        }
        catch (Exception e)
        {
            System.err.println("In checkAndStartSmartWatch: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void connectMqttBroker()
    {
        try
        {
            boolean isConnected = false;
            
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
            
            client.setCallback(new MqttCallback()
            {
                @Override
                public void messageArrived(String topic, MqttMessage message) 
                {
                    String time = new Timestamp(System.currentTimeMillis()).toString();
                    String receivedMessage = new String(message.getPayload());
                    System.out.println(clientId + " - Received a Message -" +
                                        "\n\tTime:    " + time +
                                        "\n\tTopic:   " + topic +
                                        "\n\tMessage: " + receivedMessage +
                                        "\n\tQoS:     " + message.getQos() + "\n");
                    
                    if(topic.equals(gameSubTopic))
                    {
                        if(receivedMessage.equals(START_GAME))
                        {
                            smartWatch.startGameCoordination(DELAY_COORDINATION_MILLISECONDS);
                        }
                        else if(receivedMessage.equals(STOP_GAME))
                        {
                            smartWatch.stopSmartWatch();
                        }
                        else
                        {
                            System.out.println(clientId + " - Unhandled custom message on topic: " + gameSubTopic);
                        }
                    }
                    else //customSubTopic
                    {
                        System.out.println(clientId + " - Handled custom message!");
                    } 
                }

                @Override
                public void connectionLost(Throwable cause)
                {
                    System.out.println(clientId + " - Connectionlost! cause:" + cause.getMessage());
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token)
                {
                    //Not used here
                }

            });

            System.out.println(clientId + " - Subscribing ...");
            client.subscribe(this.gameSubTopic, this.subQos);
            client.subscribe(this.customSubTopic, this.subQos);
            System.out.println(clientId + " - Subscribed to topics: " + this.gameSubTopic + ", " + this.customSubTopic);
        }
        catch (MqttException mqttE) 
        {
            System.out.println("In connectMqttBroker: reason " + mqttE.getReasonCode() + ", cause: " + mqttE.getCause() + ", msg: " + mqttE.getMessage());
        } 
        catch (Exception e) 
        {
            System.err.println("In connectMqttBroker: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void StopAll()
    {
        System.out.println("Invoked StopAll!");
        this.smartWatch.stopSmartWatch();
    }
    
    /**
     * singleton pattern
     * @return instance
     */
    public static PlayerManager getInstance(String grpcServiceEndpoint)
    {
        if(instance == null)
            instance = new PlayerManager(grpcServiceEndpoint);
        
        return instance;
    }
}
