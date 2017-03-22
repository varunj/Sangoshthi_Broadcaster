package io.github.varunj.sangoshthi_broadcaster;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by Varun on 22-Mar-17.
 */

public class AMQPPublishFanOut {

    public static Thread publishThread;
    public static JSONObject messagePresent;
    public static String QUEUE_NAME = "amq.fanout";
    public static BlockingDeque<JSONObject> queue = new LinkedBlockingDeque<>();

    public static ConnectionFactory factory = new ConnectionFactory();
    public static  void setupConnectionFactory() {
        try {
            factory.setUsername(StarterActivity.SERVER_USERNAME);
            factory.setPassword(StarterActivity.SERVER_PASS);
            factory.setHost(StarterActivity.IP_ADDR);
            factory.setPort(StarterActivity.SERVER_PORT);
            factory.setAutomaticRecoveryEnabled(true);
            factory.setNetworkRecoveryInterval(10000);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void publishToAMQP() {
        publishThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        Connection connection = factory.newConnection();
                        Channel channel = connection.createChannel();
                        channel.confirmSelect();
                        while (true) {
                            messagePresent = queue.takeFirst();
                            try {
                                // xxx: read http://www.rabbitmq.com/tutorials/tutorial-three-python.html, http://stackoverflow.com/questions/10620976/rabbitmq-amqp-single-queue-multiple-consumers-for-same-message
                                // channel.basicPublish(exchangeName, routingKey, null, messageBodyBytes);
                                channel.basicPublish(QUEUE_NAME, messagePresent.getString("show_name"), null, messagePresent.toString().getBytes());
                                displayMessage(messagePresent);
                                channel.waitForConfirmsOrDie();
                            } catch (Exception e) {
                                queue.putFirst(messagePresent);
                                throw e;
                            }
                        }
                    } catch (InterruptedException e) {
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                        try {
                            Thread.sleep(5000); //sleep and then try again
                        } catch (InterruptedException e1) {
                            break;
                        }
                    }
                }
            }
        });
        publishThread.start();
    }

    public static void displayMessage(JSONObject message) {
        try {
            System.out.println("xxx:" + " " + message.getString("objective") + ":   " +
                    message.getString("broadcaster") + "->" + message.getString("show_name") +
                    " " + message.getString("message") + "   @" + message.getString("timestamp"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
