package io.github.varunj.sangoshthi_broadcaster;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Varun on 13-Apr-17.
 */

public class StartShowCallsActivity extends AppCompatActivity  {
    private String showName, ashalist;
    private String VIDEO_URI = "/";
    private int totalASHAs = 0, activeASHAs = 0;
    Thread subscribeThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startshowcalls);

        // to pass on data to next activity
        Intent i = getIntent();
        showName = i.getStringExtra("showName");
        VIDEO_URI = "/" + i.getStringExtra("videoname");
        ashalist = i.getStringExtra("ashalist");

        // calculate nos
        totalASHAs = ashalist.split(",").length;
        final TextView startshowcalls_participants = (TextView) findViewById(R.id.startshowcalls_participants);
        startshowcalls_participants.setText("0/" + totalASHAs);

        // AMQP stuff
        AMQPPublishFreeSwitch.setupConnectionFactory();
        AMQPPublishFreeSwitch.publishToAMQP();
        setupConnectionFactory();
        subscribe();

        // button listeners
        final Button startshowcalls_initSelf = (Button) findViewById(R.id.startshowcalls_initSelf);
        startshowcalls_initSelf.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    final JSONObject jsonObject = new JSONObject();
                    //primary key: <, >
                    jsonObject.put("objective", "start_show");
                    jsonObject.put("show_name", showName);
                    jsonObject.put("video_name", VIDEO_URI);
                    jsonObject.put("timestamp", DateFormat.getDateTimeInstance().format(new Date()));
                    AMQPPublishFreeSwitch.queue.putLast(jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        final Button startshowcalls_initASHA = (Button) findViewById(R.id.startshowcalls_initASHA);
        startshowcalls_initASHA.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    final JSONObject jsonObject = new JSONObject();
                    //primary key: <, >
                    jsonObject.put("objective", "dial_listeners");
                    jsonObject.put("show_name", showName);
                    jsonObject.put("video_name", VIDEO_URI);
                    jsonObject.put("timestamp", DateFormat.getDateTimeInstance().format(new Date()));
                    AMQPPublishFreeSwitch.queue.putLast(jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
              }
        });
    }

    @Override
    protected void onDestroy() {
        if (AMQPPublishFreeSwitch.publishThreadFreeSwitch != null)
            AMQPPublishFreeSwitch.publishThreadFreeSwitch.interrupt();
        if (subscribeThread != null)
            subscribeThread.interrupt();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (AMQPPublishFreeSwitch.publishThreadFreeSwitch != null)
            AMQPPublishFreeSwitch.publishThreadFreeSwitch.interrupt();
        if (subscribeThread != null)
            subscribeThread.interrupt();
        super.onBackPressed();
    }


    // subscribe to RabbitMQ
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

    void subscribe() {
        subscribeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        Connection connection = factory.newConnection();
                        Channel channel = connection.createChannel();

                        // xxx: read http://www.rabbitmq.com/tutorials/tutorial-three-python.html, http://stackoverflow.com/questions/10620976/rabbitmq-amqp-single-queue-multiple-consumers-for-same-message
                        channel.queueDeclare("freeswitch_server_to_broadcaster", false, false, false, null);
                        QueueingConsumer consumer = new QueueingConsumer(channel);
                        channel.basicConsume("freeswitch_server_to_broadcaster", true, consumer);

                        while (true) {
                            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                            final JSONObject message = new JSONObject(new String(delivery.getBody()));
                            System.out.println("xxx:" + message.toString());

                            if (message.getString("objective").equals("nos_connected_ashas") && message.getString("show_name").equals(showName)) {
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        final TextView startshowcalls_participants = (TextView) findViewById(R.id.startshowcalls_participants);
                                        try {
                                            startshowcalls_participants.setText(message.getString("connected_ashas") + "/" + totalASHAs);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }

                            else if (message.getString("objective").equals("flag_begin_show")) {
                                // start new Activity
                                if (message.getString("show_name").equals(showName)) {
                                    Intent iNew = new Intent(getApplicationContext(), GroupVideoActivity.class);
                                    iNew.putExtra("showName", showName);
                                    iNew.putExtra("VIDEO_URI", VIDEO_URI);
                                    iNew.putExtra("ashalist", ashalist);
                                    startActivity(iNew);
                                }
                                // xxx: onDestroy?
                            }
                        }
                    } catch (InterruptedException e) {
                        break;
                    } catch (Exception e1) {
                        e1.printStackTrace();
                        try {
                            Thread.sleep(4000); //sleep and then try again
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            break;
                        }
                    }
                }
            }
        });
        subscribeThread.start();
    }

}
