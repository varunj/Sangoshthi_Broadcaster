package io.github.varunj.sangoshthi_broadcaster;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaCodec;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecSelector;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.extractor.ExtractorSampleSource;
import com.google.android.exoplayer.upstream.Allocator;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultAllocator;
import com.google.android.exoplayer.upstream.DefaultUriDataSource;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

import org.apache.commons.lang3.SerializationUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by Varun on 04-03-2017.
 */

public class GroupVideoActivity extends AppCompatActivity {
    private String showName, senderPhoneNum;
    private SurfaceView surfaceView;
    private SeekBar seekPlayerProgress;
    private TextView txtCurrentTime;
    private TextView txtEndTime;
    private ImageButton btnRew;
    private ImageButton btnPlay;
    private ImageButton btnFwd;
    private LinearLayout mediaController;
    private ExoPlayer exoPlayer;
    private Handler handler;
    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;
    private boolean bAutoplay=false;
    private boolean bIsPlaying=false;
    private boolean bControlsActive=true;
    private int RENDERER_COUNT = 300000;
    private int minBufferMs =    250000;
    private final int BUFFER_SEGMENT_SIZE = 64 * 1024;
    private final int BUFFER_SEGMENT_COUNT = 256;
    private String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:40.0) Gecko/20100101 Firefox/40.0";
    private String VIDEO_URI = "/";
    Thread subscribeThread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_player_layout);

        // Hide the status bar.
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        // get showName and senderPhoneNumber and videoname
        Intent i = getIntent();
        showName = i.getStringExtra("showName");
        VIDEO_URI = "/" + i.getStringExtra("videoname");
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        senderPhoneNum = pref.getString("phoneNum", "0000000000");

        // AMQP stuff
        AMQPPublishFanOut.setupConnectionFactory();
        AMQPPublishFanOut.publishToAMQP();
        AMQPPublish.setupConnectionFactory();
        AMQPPublish.publishToAMQP();
        setupConnectionFactory();
        subscribe();

        // Video Player Stuff
        setContentView(R.layout.video_player_layout);
        surfaceView = (SurfaceView) findViewById(R.id.sv_player);
        mediaController = (LinearLayout) findViewById(R.id.lin_media_controller);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initPlayer(0);
        if(bAutoplay){
            if(exoPlayer!=null){
                exoPlayer.setPlayWhenReady(true);
                bIsPlaying=true;
                setProgress();
            }
        }

        final Button button_groupvideo_flushQueries = (Button) findViewById(R.id.flushQueries);
        button_groupvideo_flushQueries.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                final TextView text_groupvideo_nosLikesQuery = (TextView) findViewById(R.id.nosLikesQuery);
                String temp = text_groupvideo_nosLikesQuery.getText().toString();
                System.out.println("xxx: " + temp);
                text_groupvideo_nosLikesQuery.setText("Likes: 0, Queries: 0/0");
                try {
                    final JSONObject jsonObject = new JSONObject();
                    //primary key: <broadcaster, show_name>
                    jsonObject.put("objective", "control_show_flush");
                    jsonObject.put("broadcaster", senderPhoneNum);
                    jsonObject.put("show_name", showName);
                    jsonObject.put("timestamp", DateFormat.getDateTimeInstance().format(new Date()));
                    AMQPPublish.queue.putLast(jsonObject);
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
        super.onDestroy();
        if (AMQPPublishFanOut.publishThread != null)
            AMQPPublishFanOut.publishThread.interrupt();
        if (AMQPPublish.publishThread != null)
            AMQPPublish.publishThread.interrupt();
        if (subscribeThread != null)
            subscribeThread.interrupt();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Closing Activity")
                .setMessage("Sure you don't want to continue?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
        if (AMQPPublishFanOut.publishThread != null)
            AMQPPublishFanOut.publishThread.interrupt();
        if (AMQPPublish.publishThread != null)
            AMQPPublish.publishThread.interrupt();
        if (subscribeThread != null)
            subscribeThread.interrupt();
    }

    void publishMessage(String message) {
        try {
            final JSONObject jsonObject = new JSONObject();
            //primary key: <broadcaster, show_name>
            jsonObject.put("objective", "control_show_flush");
            jsonObject.put("broadcaster", senderPhoneNum);
            jsonObject.put("show_name", showName);
            jsonObject.put("timestamp", DateFormat.getDateTimeInstance().format(new Date()));
            jsonObject.put("message", message);
            AMQPPublish.queue.putLast(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            final JSONObject jsonObject = new JSONObject();
            //primary key: <broadcaster, show_name>
            jsonObject.put("objective", "control_show");
            jsonObject.put("broadcaster", senderPhoneNum);
            jsonObject.put("show_name", showName);
            jsonObject.put("timestamp", DateFormat.getDateTimeInstance().format(new Date()));
            jsonObject.put("message", message);
            AMQPPublishFanOut.queue.putLast(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
                        channel.queueDeclare("server_to_broadcaster", false, false, false, null);
                        QueueingConsumer consumer = new QueueingConsumer(channel);
                        channel.basicConsume("server_to_broadcaster", true, consumer);

                        while (true) {
                            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                            final JSONObject message = new JSONObject(new String(delivery.getBody()));

                            try {
                                System.out.println("xxx:" + " " + message.getString("objective") + ":" +
                                        message.getString("sender") + "->" + message.getString("location") +
                                        " " + message.getString("show_name"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            if (message.getString("objective").equals("like")) {
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        final TextView text_groupvideo_nosLikesQuery = (TextView) findViewById(R.id.nosLikesQuery);
                                        String temp = text_groupvideo_nosLikesQuery.getText().toString();
                                        System.out.println("xxx: " + temp);
                                        int likes = Integer.parseInt(temp.split(",")[0].split(":")[1].trim());
                                        int queries = Integer.parseInt(temp.split(",")[1].trim().split(":")[1].trim().split("/")[0]);
                                        int total = Integer.parseInt(temp.split(",")[1].split(":")[1].trim().split("/")[1]);
                                        text_groupvideo_nosLikesQuery.setText("Likes: " + (likes+1) + ", Queries:" + queries + "/" + total);
                                    }
                                });

                            }
                            else if (message.getString("objective").equals("query")) {
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        final TextView text_groupvideo_nosLikesQuery = (TextView) findViewById(R.id.nosLikesQuery);
                                        String temp = text_groupvideo_nosLikesQuery.getText().toString();
                                        System.out.println("xxx: " + temp);
                                        int likes = Integer.parseInt(temp.split(",")[0].split(":")[1].trim());
                                        int queries = Integer.parseInt(temp.split(",")[1].trim().split(":")[1].trim().split("/")[0]);
                                        int total = Integer.parseInt(temp.split(",")[1].split(":")[1].trim().split("/")[1]);
                                        text_groupvideo_nosLikesQuery.setText("Likes: " + likes + ", Queries:" + (queries+1) + "/" + total);
                                    }
                                });

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


    // initialising media control
    private void initMediaControls() {
        initSurfaceView();
        initSeekBar();
        initTxtTime();
        initBtnRew();
        initBtnPlay();
        initBtnFwd();
    }

    private void initSurfaceView() {
        surfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleMediaControls();
            }
        });
    }

    private void initSeekBar() {
        seekPlayerProgress = (SeekBar) findViewById(R.id.mediacontroller_progress);
        seekPlayerProgress.requestFocus();
        seekPlayerProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) {
                    // We're not interested in programmatically generated changes to the progress bar's position.
                    return;
                }
                exoPlayer.seekTo(progress*1000);
                publishMessage("seek:" + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        seekPlayerProgress.setMax(0);
        seekPlayerProgress.setMax((int) exoPlayer.getDuration()/1000);
    }

    private void initTxtTime() {
        txtCurrentTime = (TextView) findViewById(R.id.time_current);
        txtEndTime = (TextView) findViewById(R.id.player_end_time);
    }

    private void initBtnRew() {
        btnRew = (ImageButton) findViewById(R.id.rew);
        btnRew.requestFocus();
        btnRew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exoPlayer.seekTo(exoPlayer.getCurrentPosition()-3000);
                publishMessage("seek:" + exoPlayer.getCurrentPosition());
            }
        });
    }

    private void initBtnPlay() {
        btnPlay = (ImageButton) findViewById(R.id.btnPlay);
        btnPlay.requestFocus();
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bIsPlaying){
                    exoPlayer.setPlayWhenReady(false);
                    bIsPlaying=false;
                    publishMessage("pause:" + exoPlayer.getCurrentPosition());
                }
                else {
                    exoPlayer.setPlayWhenReady(true);
                    bIsPlaying=true;
                    setProgress();
                    publishMessage("play:" + exoPlayer.getCurrentPosition());
                }
            }
        });
    }

    private void initBtnFwd() {
        btnFwd = (ImageButton) findViewById(R.id.ffwd);
        btnFwd.requestFocus();
        btnFwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exoPlayer.seekTo(exoPlayer.getCurrentPosition()+3000);
                publishMessage("seek:" + exoPlayer.getCurrentPosition());
            }
        });
    }

    private String stringForTime(int timeMs) {
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
        int totalSeconds =  timeMs / 1000;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours   = totalSeconds / 3600;
        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        }
        else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    private void setProgress() {
        seekPlayerProgress.setProgress(0);
        seekPlayerProgress.setMax(0);
        seekPlayerProgress.setMax((int) exoPlayer.getDuration()/1000);
        handler = new Handler();
        //Make sure Seekbar is updated only on UI thread
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (exoPlayer != null && bIsPlaying ) {
                    seekPlayerProgress.setMax(0);
                    seekPlayerProgress.setMax((int) exoPlayer.getDuration()/1000);
                    int mCurrentPosition = (int) exoPlayer.getCurrentPosition() / 1000;
                    seekPlayerProgress.setProgress(mCurrentPosition);
                    txtCurrentTime.setText(stringForTime((int)exoPlayer.getCurrentPosition()));
                    txtEndTime.setText(stringForTime((int)exoPlayer.getDuration()));
                    handler.postDelayed(this, 1000);
                }
            }
        });
    }

    private void toggleMediaControls() {
        if(bControlsActive){
            hideMediaController();
            bControlsActive=false;
        }
        else {
            showController();
            bControlsActive=true;
            setProgress();
        }
    }

    private void showController() {
        mediaController.setVisibility(View.VISIBLE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void hideMediaController() {
        mediaController.setVisibility(View.GONE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void initPlayer(int position) {
        Allocator allocator = new DefaultAllocator(minBufferMs);
        DataSource dataSource = new DefaultUriDataSource(this, null, userAgent);
        ExtractorSampleSource sampleSource = new ExtractorSampleSource(
                Uri.fromFile(
                        new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "/Sangoshthi_Broadcaster/" + VIDEO_URI)
                ),
                dataSource, allocator, BUFFER_SEGMENT_COUNT * BUFFER_SEGMENT_SIZE);

        MediaCodecVideoTrackRenderer videoRenderer = new MediaCodecVideoTrackRenderer(this, sampleSource, MediaCodecSelector.DEFAULT,
                MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT);

        MediaCodecAudioTrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource, MediaCodecSelector.DEFAULT);
        exoPlayer = ExoPlayer.Factory.newInstance(RENDERER_COUNT);
        exoPlayer.prepare(videoRenderer, audioRenderer);
        exoPlayer.sendMessage(videoRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, surfaceView.getHolder().getSurface());
        exoPlayer.seekTo(position);
        initMediaControls();
    }
}
