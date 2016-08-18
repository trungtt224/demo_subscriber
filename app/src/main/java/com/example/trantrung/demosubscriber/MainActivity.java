package com.example.trantrung.demosubscriber;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText txtUsername;
    private EditText txtPassword;
    private Button btnConnect;
    private MqttClient mqttClient;
    private ListView lvDevice;
    private ArrayList<String> listMsg = new ArrayList<>();
    private ArrayAdapter<String> adapMsg = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtUsername = (EditText) findViewById(R.id.txtUsername);
        txtPassword = (EditText) findViewById(R.id.txtPassword);
        btnConnect = (Button) findViewById(R.id.btnConnect);
        lvDevice  = (ListView) findViewById(R.id.lvDevice);

        adapMsg = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listMsg);
        lvDevice.setAdapter(adapMsg);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                adapMsg.notifyDataSetChanged();
                handler.postDelayed(this, 1000);
            }
        }, 1000);

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                    if (mqttClient.isConnected()) {
                        System.out.println("Subscriber");
                        mqttClient.subscribe("sub");
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
                                listMsg.add(new String(message.getPayload()));
                            }

                            @Override
                            public void deliveryComplete(IMqttDeliveryToken token) {

                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }
}
