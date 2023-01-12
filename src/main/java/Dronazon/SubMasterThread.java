package Dronazon;

import Network.OrderThread;
import Network.GlobalStatsThread;
import Network.Node;
import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.*;
import java.sql.Timestamp;

// master
public class SubMasterThread extends Thread {
    Node n;

    public SubMasterThread(Node n) {
        this.n = n;
    }

    @Override
    public void run() {
        MqttClient client;
        String broker = "tcp://localhost:1883";
        String clientId = MqttClient.generateClientId();
        String topic = "dronazon/smartcity/orders";
        int qos = 2;

        try {
            client = new MqttClient(broker, clientId);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);

            // connect the client
            System.out.println("\nConnecting Broker " + broker);
            client.connect(connOpts);
            System.out.println("Connected - Thread PID: " + Thread.currentThread().getId());

            Gson gson = new Gson();
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    System.out.println(clientId + " Connection lost! cause:" + cause.getMessage()+ "-  Thread PID: " + Thread.currentThread().getId());
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    long time = new Timestamp(System.currentTimeMillis()).getTime();
                    String receivedMessage = new String(message.getPayload());
                    /*
                    System.out.println(clientId +" Received a Message! - Callback - Thread PID: " + Thread.currentThread().getId() +
                            "\n\tTime:    " + time +
                            "\n\tTopic:   " + topic +
                            "\n\tMessage: " + receivedMessage +
                            "\n\tQoS:     " + message.getQos() + "\n");

                    System.out.println("\n ***  Press a random key to exit *** \n");
                    */
                    Order o = gson.fromJson(receivedMessage, Order.class);
                    n.addOrder(o);
                    synchronized (n.getLockWaitOrders()) {
                        n.getLockWaitOrders().notifyAll();
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                }
            });

            System.out.println(("Subscribing ... - Thread PID: " + Thread.currentThread().getId()));
            client.subscribe(topic,qos);
            System.out.println("Subscribed to topic: " + topic + "\n");

            synchronized (n.getLockWaitOrders()) {
                n.getLockWaitOrders().wait();
            }
            OrderThread delThread = new OrderThread(n);
            delThread.start();

            GlobalStatsThread globalStats = new GlobalStatsThread(n);
            globalStats.start();
            
            synchronized (n.getLockMqtt()) {
                n.getLockMqtt().wait();
                client.disconnect();
            }

        } catch (MqttException me ) {
            System.out.println("reason " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
