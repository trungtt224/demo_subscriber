package com.example.trantrung.subscriberdemo;

import android.app.Activity;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.ArrayList;

public class MainActivity extends Activity {

    ListView lvTopic;
    ListView lvDevice;

    Handler handler = new Handler();
    Handler handler2 = new Handler();

    MqttClient mqttClient;
    MqttClient mqttClient2;

    ArrayList<String> listMsgDevice = new ArrayList<>();
    ArrayAdapter<String> adapDevice = null;
    ArrayList<String> listMsgTopic = new ArrayList<>();
    ArrayAdapter<String> adapTopic = null;
    String payload = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lvTopic = (ListView) findViewById(R.id.lvTopic);
        lvDevice = (ListView) findViewById(R.id.lvDevice);

        adapDevice = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listMsgDevice);
        lvTopic.setAdapter(adapDevice);

        adapTopic = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listMsgTopic);
        lvDevice.setAdapter(adapTopic);

        final Handler autoDevice = new Handler();
        autoDevice.postDelayed( new Runnable() {
            Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALL);
            Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), ringtoneUri);
            @Override
            public void run() {
                adapDevice.notifyDataSetChanged();
                autoDevice.postDelayed( this, 1 * 1000 );

//                    Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALL);
//                     Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), ringtoneUri);
               String p = "";
                if (!listMsgDevice.isEmpty()) {
                   p = listMsgDevice.get(listMsgDevice.size() - 1);
                    if (p.contains("{\"ring\":\"on\"}")) {
                        ringtone.play();
                    }

                    if (p.contains("{\"ring\":\"off\"}")) {
                        ringtone.stop();
                    }
                }
                System.out.println(p);

            }
        }, 5 *1000 );

        final Handler autoTopic = new Handler();
        autoTopic.postDelayed( new Runnable() {

            @Override
            public void run() {
                adapTopic.notifyDataSetChanged();
                autoTopic.postDelayed( this, 1 * 1000 );
            }
        }, 5 *1000 );

        final Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(1000);
                String broker = "tcp://10.22.20.164:1888";
                String clientId = "sub device 002";
                MemoryPersistence persistence = new MemoryPersistence();

                try {
                    mqttClient = new MqttClient(broker, clientId, persistence);
                    MqttConnectOptions mqttConnection = new MqttConnectOptions();
                    mqttConnection.setUserName("sub/1/control_device/dev_001");
                    mqttConnection.setPassword("dd181ff3cc3e946ef9e4ddf81e07b9a85ff73be2e8cf557de57104ecd52f5a389".toCharArray());
                    mqttConnection.setCleanSession(true);
                    System.out.println("Connecting to broker: " + broker);
                    mqttClient.connect(mqttConnection);
                    System.out.println("Connected");
                    mqttClient.subscribe("sub");
                } catch (MqttException e) {
                    e.printStackTrace();
                }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            mqttClient.setCallback(new MqttCallback() {
                                @Override
                                public void connectionLost(Throwable cause) {

                                }

                                @Override
                                public void messageArrived(String topic, MqttMessage message) throws Exception {
                                    System.out.println("\nReceived a Message!" +
                                            "\n\tTopic:   " + topic +
                                            "\n\tMessage: " + new String(message.getPayload()) +
                                            "\n\tQoS:     " + message.getQos() + "\n");
                                    listMsgDevice.add(new String(message.getPayload()));
                                }

                                @Override
                                public void deliveryComplete(IMqttDeliveryToken token) {

                                }
                            });

                        }
                    });
                }

        });

        final Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(1000);
                String broker = "tcp://10.22.20.164:1888";
                String clientId = "sub DTopic";
                MemoryPersistence persistence = new MemoryPersistence();


                try {
                    mqttClient2 = new MqttClient(broker, clientId, persistence);
                    MqttConnectOptions mqttConnection = new MqttConnectOptions();
                    mqttConnection.setUserName("sub/1/control_device");
                    mqttConnection.setPassword("dd181ff3cc3e946ef9e4ddf81e07b9a85ff73be2e8cf557de57104ecd52f5a389".toCharArray());
                    mqttConnection.setCleanSession(true);
                    System.out.println("Connecting to broker: " + broker);
                    mqttClient2.connect(mqttConnection);
                    System.out.println("Connected");
                    mqttClient2.subscribe("1/control_temperature");
                } catch (MqttException e) {
                    e.printStackTrace();
                }
                handler2.post(new Runnable() {
                    @Override
                    public void run() {
                        mqttClient2.setCallback(new MqttCallback() {
                            @Override
                            public void connectionLost(Throwable cause) {

                            }

                            @Override
                            public void messageArrived(String topic, MqttMessage message) throws Exception {
                                System.out.println("\nReceived a Message!" +
                                        "\n\tTopic:   " + topic +
                                        "\n\tMessage: " + new String(message.getPayload()) +
                                        "\n\tQoS:     " + message.getQos() + "\n");
                                listMsgTopic.add(new String(message.getPayload()));
                            }

                            @Override
                            public void deliveryComplete(IMqttDeliveryToken token) {

                            }
                        });

                    }
                });
            }

        });

        thread1.start();
        thread2.start();

    }
}
