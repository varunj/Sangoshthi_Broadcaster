package io.github.varunj.sangoshthi_broadcaster;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import org.apache.commons.lang3.SerializationUtils;

import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by Varun on 04-03-2017.
 */

public class GroupVideo extends AppCompatActivity {
    private String receiverGroupName , senderPhoneNum;
    private EditText messageToSend;
    private Button sendMessageButton;
    private Message message1;
    Thread publishThread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groupvideo);
        messageToSend = (EditText) findViewById(R.id.messageEdit);
        sendMessageButton = (Button) findViewById(R.id.chatSendButton);

        // get groupName and senderPhoneNumber
        Intent i = getIntent();
        receiverGroupName = i.getStringExtra("groupName");
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        senderPhoneNum = pref.getString("phoneNum", "0000000000");

        setupConnectionFactory();
        publishToAMQP();
        setupPubButton();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        publishThread.interrupt();
    }

    ConnectionFactory factory = new ConnectionFactory();
    private void setupConnectionFactory() {
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

    public void publishToAMQP() {
        publishThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        Connection connection = factory.newConnection();
                        Channel channel = connection.createChannel();
                        channel.confirmSelect();
                        while (true) {
                            message1 = queue.takeFirst();
                            try {
                                // xxx: read http://www.rabbitmq.com/tutorials/tutorial-three-python.html, http://stackoverflow.com/questions/10620976/rabbitmq-amqp-single-queue-multiple-consumers-for-same-message
                                // channel.basicPublish(exchangeName, routingKey, null, messageBodyBytes);
                                channel.basicPublish("amq.fanout", message1.getReceiver(), null, SerializationUtils.serialize(message1));

                                displayMessage(message1, 1);
                                channel.waitForConfirmsOrDie();
                            } catch (Exception e) {
                                queue.putFirst(message1);
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

    void setupPubButton() {
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String messageText = messageToSend.getText().toString();
                if (TextUtils.isEmpty(messageText)) {
                    return;
                }
                publishMessage(messageToSend.getText().toString());
                messageToSend.setText("");
            }
        });
    }

    private BlockingDeque<Message> queue = new LinkedBlockingDeque<Message>();
    void publishMessage(String message) {
        try {
            Message chatMessage = new Message();
            chatMessage.setMessage(message);
            chatMessage.setTimestamp(DateFormat.getDateTimeInstance().format(new Date()));
            chatMessage.setSender(senderPhoneNum);
            chatMessage.setReciever(receiverGroupName);
            queue.putLast(chatMessage);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void displayMessage(Message message, int x) {
        System.out.println("xxx:" + x + "   " + message.getSender() + "->" + message.getReceiver() + "   " + message.getMessage() + "   " + message.getTimestamp());
    }
}
