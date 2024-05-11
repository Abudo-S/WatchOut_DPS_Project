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
    private static final String START_GAME = "START_GAME";
    private static final String STOP_GAME = "STOP_GAME";
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 1337;
    private static String GrpcServiceEndpoint;
    
    private String broker;
    private String clientId;
    private String gameSubTopic;
    private String customSubTopic;
    private int subQos;
    
    private MqttClient client;
    private PersistentPlayerRegistrationThread persistentPlayerReg_thread;
    private SmartWatch smartWatch;
    
    private static PlayerManager instance;
    
    public PlayerManager ()
    {
        if(GrpcServiceEndpoint == null)
            throw new NullPointerException("Can't initialize PlayerManager with null GrpcServiceEndpoint!");
        
        this.broker = "tcp://localhost:1883";
        this.clientId = MqttClient.generateClientId();
        this.gameSubTopic = "home/state/game";
        this.customSubTopic = "home/custom/game";
        this.subQos = 2;
        
        Gson jsonSerializer = new Gson();
        Client client = Client.create();
        String serverAddress = "http://" + SERVER_HOST + ":" + SERVER_PORT + "/";
        
        this.persistentPlayerReg_thread = new PersistentPlayerRegistrationThread(client, serverAddress, jsonSerializer, 0, GrpcServiceEndpoint);
        this.persistentPlayerReg_thread.start();
        
        checkAndStartSmartWatch(client, serverAddress, jsonSerializer);
        connectMqttBroker();
    }
    
    /**
     * should wait until player registration is completed successfully, then initialize the smart watch
     * @param client
     * @param serverAddress
     * @param jsonSerializer 
     */
    private synchronized void checkAndStartSmartWatch(Client client, String serverAddress, Gson jsonSerializer)
    {
        try
        {
            while(!this.persistentPlayerReg_thread.checkIsCompleted())
                wait(1000);
            
            Player player = this.persistentPlayerReg_thread.getBuiltPlayer();
            this.smartWatch = new SmartWatch(player, new CheckToSendHrAvgsThread(client, serverAddress, jsonSerializer, 0, player.getId()));
        }
        catch (Exception e)
        {
            System.err.println("In checkAndStartSmartWatch: " + e.getMessage());
        }
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
                            smartWatch.startGameCoordination();
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
            System.out.println("In connectMqttBroker: " + e.getMessage());
        }
    }
    
    public void StopAll()
    {
        this.smartWatch.stopSmartWatch();
    }
    
    /**
     * singleton pattern
     * @return instance
     */
    public static PlayerManager getInstance()
    {
        if(instance == null)
            instance = new PlayerManager();
        
        return instance;
    }
}
