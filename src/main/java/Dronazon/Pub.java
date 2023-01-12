package Dronazon;

import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.IOException;
import java.util.Random;

// client MQTT
public class Pub {
    public static void main(String[] args) {
        MqttClient client;
        String broker = "tcp://localhost:1883";
        String clientId = MqttClient.generateClientId();
        String topic = "dronazon/smartcity/orders";
        int qos = 2;

        int id = 0;
        int[] pickupP = new int[2];
        int[] deliveryP = new int[2];

        Random r = new Random();
        try {
            client = new MqttClient(broker, clientId);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);

            // connect the client
            System.out.println(clientId + " Connecting Broker " + broker);
            client.connect(connOpts);
            System.out.println(clientId + " Connected");

            while(System.in.available() == 0) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // generate a new order using gson
                id += 1;
                pickupP[0] = r.nextInt(10);
                pickupP[1] = r.nextInt(10);
                deliveryP[0] = r.nextInt(10);
                deliveryP[1] = r.nextInt(10);

                Order o = new Order(id, pickupP, deliveryP);

                Gson gson = new Gson();
                String payload = gson.toJson(o);
                MqttMessage message = new MqttMessage(payload.getBytes());

                // set the QoS on the message
                message.setQos(qos);
                System.out.println(clientId + " Publishing message: " + payload + " ...");
                client.publish(topic, message);
                System.out.println(clientId + " Message published");
            }

            // disconnect the client
            if (client.isConnected())
                client.disconnect();
            System.out.println("Publisher " + clientId + " disconnected");

        } catch (MqttException me) {
            System.out.println("reason " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
