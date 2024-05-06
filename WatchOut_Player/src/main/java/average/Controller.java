package average;

import org.eclipse.paho.client.mqttv3.*;

import java.sql.Timestamp;

public class Controller {

    private static double temperaturesCurrentSum;
    private static int temperaturesCurrent;

    private static int subQos = 2;
    private static int pubQos = 2;

    private static String clientId;

    private static MqttClient client;
    private static String broker;
    private static String subTopic;
    private static String pubTopic;

    public static void main(String[] argv) {
        broker = "tcp://localhost:1883";
        clientId = MqttClient.generateClientId();
        subTopic = "home/sensors/temp";
        pubTopic = "home/controllers/temp";

        temperaturesCurrentSum = 0D;
        temperaturesCurrent = 0;

        try {
            client = new MqttClient(broker, clientId);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);

            System.out.println(clientId + " Connecting Broker " + broker);
            client.connect(connOpts);
            System.out.println(clientId + " Connected " + Thread.currentThread().getId());

            client.setCallback(new MqttCallback() {

                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    String time = new Timestamp(System.currentTimeMillis()).toString();
                    String receivedMessage = new String(message.getPayload());
                    System.out.println(clientId + " Received a Message! - Callback - Thread PID: " + Thread.currentThread().getId() +
                            "\n\tTime:    " + time +
                            "\n\tTopic:   " + topic +
                            "\n\tMessage: " + receivedMessage);
                    if (topic.equals("home/sensors/temp")) {
                        updateTemperatureAverage(Double.parseDouble(receivedMessage));
                    }
                }

                public void connectionLost(Throwable cause) {
                    System.out.println(clientId + " Connectionlost! cause:" + cause.getMessage() + "-  Thread PID: " + Thread.currentThread().getId());
                }

                public void deliveryComplete(IMqttDeliveryToken token) {
                    //Not used here
                }
            });

            System.out.println(clientId + " Subscribing ... - Thread PID: " + Thread.currentThread().getId());
            client.subscribe(subTopic,subQos);
            System.out.println(clientId + " Subscribed to topics : " + subTopic);


        } catch (MqttException me) {
            System.out.println("reason " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
        }


    }

    private static void updateTemperatureAverage(double newTemperature) {
        synchronized (Controller.class) {
            temperaturesCurrent += 1;
            temperaturesCurrentSum += newTemperature;
            if (temperaturesCurrent == 5) {
                temperaturesCurrent = 0;
                double avg = temperaturesCurrentSum / 5.0;
                String payload;
                if (avg >= 20) {
                    payload = "off";
                } else {
                    payload = "on";
                }
                MqttMessage message = new MqttMessage(payload.getBytes());
                message.setQos(pubQos);
                System.out.println(clientId + " Average: " + avg + "; Publishing message about what to do with the heater: " + payload);
                try {
                    client.publish(pubTopic, message);
                } catch (MqttException e) {
                    e.printStackTrace();
                }

                temperaturesCurrentSum = 0D;
            }
        }
    }
}