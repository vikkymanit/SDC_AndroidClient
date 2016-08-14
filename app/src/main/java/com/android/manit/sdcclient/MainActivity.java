package com.android.manit.sdcclient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;
import org.toubassi.femtozip.models.FemtoZipCompressionModel;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private Logger logger = Logger.getLogger("MainActivity");

    private static MqttClient cl;

    private TextView throughput;

    private String ip;

    private ProgressBar msgSizeBar;

    private Integer msgCount = 0;

    private boolean clientConnected = false;

    private SubscriberDataThread subscriberDataThread;

    private List<Float> throughputValues = new ArrayList<>();

    private List<Marker> markers = new ArrayList<>();

    private Dictionary<Byte, FemtoZipCompressionModel> dictionaries;

    private FemtoZipCompressionModel femtoZipCompressionModelXml;

    private FemtoZipCompressionModel femtoZipCompressionModelCsv;

    private FemtoZipCompressionModel femtoZipCompressionModelJson;

    private ProgressDialog progressDialog;

    private Switch swtSubUncompressed;

    public static Context context;

    private String currentTopic;

    private boolean switchReset = false;

    private boolean firstBtnClick = false;

    private MqttConnectOptions mqttConnectOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        throughput = (TextView) findViewById(R.id.throughput);

        msgSizeBar = (ProgressBar) findViewById(R.id.msgSizeBar);

        dictionaries = new Hashtable<>();

        context = MainActivity.this;

        progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Please wait");

        mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setUserName("manit");
        mqttConnectOptions.setPassword("manit".toCharArray());

        Button btnGetDict = (Button) findViewById(R.id.btnGetDict);
        btnGetDict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
//                    getDictionaries();
                    try {
                        subscriberDataThread.subscribeToTopic(Const.DICT_TOPIC_NAME);
                        firstBtnClick = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(MainActivity.this, "Finsihed Loading Dictionaries", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        Button btnSubXml = (Button) findViewById(R.id.btnSubXml);
        btnSubXml.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {

                    if (firstBtnClick)
                        reset();

                    subscriberDataThread.subscribeToTopic(Const.XML_COMPRESSED_TOPIC_NAME);
                    Toast.makeText(MainActivity.this, "XML compressed messages", Toast.LENGTH_SHORT).show();
                    currentTopic = Const.XML_COMPRESSED_TOPIC_NAME;
                    System.out.println("xmlCompressedTopic");
                    firstBtnClick = true;

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Button btnSubCsv = (Button) findViewById(R.id.btnSubCsv);
        btnSubCsv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {

                    if (firstBtnClick)
                        reset();

                    subscriberDataThread.subscribeToTopic(Const.CSV_COMPRESSED_TOPIC_NAME);
                    Toast.makeText(MainActivity.this, "CSV compressed messages", Toast.LENGTH_SHORT).show();
                    currentTopic = Const.CSV_COMPRESSED_TOPIC_NAME;
                    System.out.println("csvCompressedTopic");
                    firstBtnClick = true;

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Button btnSubJson = (Button) findViewById(R.id.btnSubJson);
        btnSubJson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {

                    if (firstBtnClick)
                        reset();

                    subscriberDataThread.subscribeToTopic(Const.JSON_COMPRESSED_TOPIC_NAME);
                    Toast.makeText(MainActivity.this, "JSON compressed messages", Toast.LENGTH_SHORT).show();
                    currentTopic = Const.JSON_COMPRESSED_TOPIC_NAME;
                    System.out.println("jsonCompressedTopic");
                    firstBtnClick = true;

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        swtSubUncompressed = (Switch) findViewById(R.id.swtSubUncompressed);
        swtSubUncompressed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                try {

                    if (switchReset)
                        return;

                    subscriberDataThread.interrupt();

                    if (cl.isConnected())
                        cl.disconnect();

                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            throughput.setText("0");
                            msgSizeBar.setProgress(0);
                            throughputValues.clear();
                        }
                    });


                    mqttSubscribe();

                    if (b) {
                        switch (currentTopic) {
                            case Const.XML_COMPRESSED_TOPIC_NAME:
                                subscriberDataThread.subscribeToTopic(Const.XML_UNCOMPRESSED_TOPIC_NAME);
                                Toast.makeText(MainActivity.this, "XML uncompressed messages", Toast.LENGTH_SHORT).show();
                                break;

                            case Const.CSV_COMPRESSED_TOPIC_NAME:
                                subscriberDataThread.subscribeToTopic(Const.CSV_UNCOMPRESSED_TOPIC_NAME);
                                Toast.makeText(MainActivity.this, "CSV uncompressed messages", Toast.LENGTH_SHORT).show();
                                break;

                            case Const.JSON_COMPRESSED_TOPIC_NAME:
                                subscriberDataThread.subscribeToTopic(Const.JSON_UNCOMPRESSED_TOPIC_NAME);
                                Toast.makeText(MainActivity.this, "JSON uncompressed messages", Toast.LENGTH_SHORT).show();
                                break;

                        }
                    } else {
                        switch (currentTopic) {
                            case Const.XML_COMPRESSED_TOPIC_NAME:
                                subscriberDataThread.subscribeToTopic(Const.XML_COMPRESSED_TOPIC_NAME);
                                Toast.makeText(MainActivity.this, "XML compressed messages", Toast.LENGTH_SHORT).show();
                                break;

                            case Const.CSV_COMPRESSED_TOPIC_NAME:
                                subscriberDataThread.subscribeToTopic(Const.CSV_COMPRESSED_TOPIC_NAME);
                                Toast.makeText(MainActivity.this, "CSV compressed messages", Toast.LENGTH_SHORT).show();
                                break;

                            case Const.JSON_COMPRESSED_TOPIC_NAME:
                                subscriberDataThread.subscribeToTopic(Const.JSON_COMPRESSED_TOPIC_NAME);
                                Toast.makeText(MainActivity.this, "JSON compressed messages", Toast.LENGTH_SHORT).show();
                                break;

                        }

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            cl.connect(mqttConnectOptions);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        final EditText txtIp = new EditText(this);
//        txtIp.setText("tcp://10.149.52.82:1883");
        txtIp.setText("tcp://131.159.52.29:1883");
        if (id == R.id.stop) {
            try {
                if (cl.isConnected())
                    cl.disconnect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
            mMap.clear();
            throughput.setText("0");
            msgSizeBar.setProgress(0);
        } else if (id == R.id.enter_ip) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setTitle("Enter IP")
                    .setMessage("Enter the IP address of the broker")
                    .setView(txtIp)
                    .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ip = txtIp.getText().toString().trim();
                            logger.log(Level.INFO, "IP" + ip);
                            try {
                                mqttSubscribe();
                                if(clientConnected)
                                    Toast.makeText(MainActivity.this, "client connected", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
            AlertDialog dialogGetIp = dialogBuilder.create();
            dialogGetIp.show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Set location to new york, and move the camera.
        LatLng newyork = new LatLng(40.712784, -74.005941);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newyork, 12.0f));
    }

    private void mqttSubscribe() throws Exception {
//        String broker = "tcp://131.159.207.159:1883";
        try {
            cl = new MqttClient(ip, "AndroidSubscriber", new MemoryPersistence());
            cl.connect(mqttConnectOptions);
            clientConnected = true;
        } catch (Exception e) {
            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
            alertDialog.setTitle("Error!");
            alertDialog.setMessage("Unable to connect. Please check the ip or try again");
            alertDialog.setCancelable(true);
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }
        subscriberDataThread = new SubscriberDataThread(this, ip);
        subscriberDataThread.start();
    }

    public void getDictionaries() throws Exception {
        dictionaries.put((byte) 1, FemtoFactory.fromDictionary(FemtoFactory.fromCacheXml()));
        dictionaries.put((byte) 2, FemtoFactory.fromDictionary(FemtoFactory.fromCacheJson()));
        dictionaries.put((byte) 3, FemtoFactory.fromDictionary(FemtoFactory.fromCacheCsv()));
        logger.log(Level.INFO, "Finished loading dictionaries");

        femtoZipCompressionModelXml = dictionaries.get((byte) 1);
        femtoZipCompressionModelJson = dictionaries.get((byte) 2);
        femtoZipCompressionModelCsv = dictionaries.get((byte) 3);

        mqttSubscribe();

    }

    public void reset() throws Exception {

        if (subscriberDataThread.isAlive())
            subscriberDataThread.interrupt();

        if (cl.isConnected())
            cl.disconnect();

        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                throughput.setText("0");
                msgSizeBar.setProgress(0);
                switchReset = true;
                swtSubUncompressed.setChecked(false);
                switchReset = false;
                throughputValues.clear();
            }
        });

        mqttSubscribe();

    }

    public class SubscriberDataThread extends Thread implements MqttCallback {

        private Activity activity;
        private String topic;
        private String broker;

        private DocumentBuilder documentBuilder;

        private boolean timerStarted = false;
        private long lastTime;
        private int dictCount = 0;


        public SubscriberDataThread(Activity activity, String broker) throws Exception {
            this.activity = activity;
            this.broker = broker;
            documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        }

        @Override
        public void run() {
            try {
                while (!Thread.interrupted())
                    cl.setCallback(this);
            } catch (Exception ex) {
                logger.log(Level.INFO, ex.getMessage());
            }
        }

        @Override
        public void connectionLost(Throwable cause) {

        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            if (!timerStarted) {
                lastTime = System.currentTimeMillis();
                timerStarted = true;
            }
            msgCount++;

            if (System.currentTimeMillis() - lastTime > 1000) {
                float timeElapsed = (System.currentTimeMillis() - lastTime) / 1000;
                float throughput = msgCount / timeElapsed;
                timerStarted = false;
                msgCount = 0;
                throughputValues.add(throughput);
                if (throughputValues.size() == 5) {
                    float val = 0;
                    for (float v : throughputValues)
                        val += v;
                    final float weightedThroughput = val / 5;

                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            publishThroughput(weightedThroughput);
                        }
                    });
                    throughputValues.remove(0);
                }
            }

            byte[] msgPayload = message.getPayload();
            byte id = msgPayload[0];

            byte[] payload = Arrays.copyOfRange(msgPayload, 1, msgPayload.length);

//            if (topic.equalsIgnoreCase(Const.DICT_TOPIC_NAME)) {
//                FemtoZipCompressionModel femtoZipCompressionModel = FemtoFactory.fromDictionary(payload);
//                dictionaries.put(id, femtoZipCompressionModel);
//                dictCount++;

//                //Write to file
//                if(id==1) {
//                    FemtoFactory.toCache(femtoZipCompressionModel, "xmlDict");
//                    dictCount++;
//                    logger.log(Level.INFO,"#received xml dictionary:" + payload.length);
//
//                }
//
//                if(id==2) {
//                    FemtoFactory.toCache(femtoZipCompressionModel, "jsonDict");
//                    dictCount++;
//                    logger.log(Level.INFO,"#received json dictionary:" + payload.length);
//
//                }
//
//                if(id==3) {
//                    FemtoFactory.toCache(femtoZipCompressionModel, "csvDict");
//                    dictCount++;
//                    logger.log(Level.INFO,"#received csv dictionary:" + payload.length);
//
//                }

//                if (dictCount == 3) {
//                    femtoZipCompressionModelXml = dictionaries.get((byte) 1);
//                    logger.log(Level.INFO, "Finished loading xml dictionary");
////                    cl.unsubscribe(Const.DICT_TOPIC_NAME);
//                }

//            }


            switch (topic) {

                case Const.XML_UNCOMPRESSED_TOPIC_NAME: {
                    Document xmlMsg = documentBuilder.parse(new ByteArrayInputStream(payload));
                    final String latitude = xmlMsg.getElementsByTagName("pickup_latitude").item(0).getTextContent();
                    final String longitude = xmlMsg.getElementsByTagName("pickup_longitude").item(0).getTextContent();

                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            publishCoordinates(latitude, longitude);
                        }
                    });

                    break;
                }

                case Const.XML_COMPRESSED_TOPIC_NAME: {
                    byte[] uncompressedMessage = femtoZipCompressionModelXml.decompress(payload);

                    Document xmlMsg = documentBuilder.parse(new ByteArrayInputStream(uncompressedMessage));
                    final String latitude = xmlMsg.getElementsByTagName("pickup_latitude").item(0).getTextContent();
                    final String longitude = xmlMsg.getElementsByTagName("pickup_longitude").item(0).getTextContent();

                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            publishCoordinates(latitude, longitude);
                        }
                    });

                    break;
                }

                case Const.CSV_UNCOMPRESSED_TOPIC_NAME: {
                    String csvMsg = new String(payload);
                    final String[] csvMsgArray = csvMsg.split(",");

                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            publishCoordinates(csvMsgArray[7], csvMsgArray[6]);
                        }
                    });

                    break;
                }

                case Const.CSV_COMPRESSED_TOPIC_NAME: {
                    byte[] uncompressedMessage = femtoZipCompressionModelCsv.decompress(payload);
                    String csvMsg = new String(uncompressedMessage);
                    final String[] csvMsgArray = csvMsg.split(",");

                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            publishCoordinates(csvMsgArray[7], csvMsgArray[6]);
                        }
                    });

                    break;
                }

                case Const.JSON_UNCOMPRESSED_TOPIC_NAME: {

                    final JSONObject jsonObject = new JSONObject(new String(payload));

                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                publishCoordinates(jsonObject.getString("pickup_latitude"), jsonObject.getString("pickup_longitude"));
                            } catch (Exception e) {

                            }
                        }
                    });
                    break;
                }

                case Const.JSON_COMPRESSED_TOPIC_NAME: {
                    byte[] uncompressedMessage = femtoZipCompressionModelJson.decompress(payload);
                    final JSONObject jsonObject = new JSONObject(new String(uncompressedMessage));

                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                publishCoordinates(jsonObject.getString("pickup_latitude"), jsonObject.getString("pickup_longitude"));
                            } catch (Exception e) {

                            }
                        }
                    });

                    break;
                }

                case Const.DICT_TOPIC_NAME: {
                    FemtoZipCompressionModel femtoZipCompressionModel = FemtoFactory.fromDictionary(payload);
                    dictionaries.put(id, femtoZipCompressionModel);
                    dictCount++;

                    if (dictCount == 3) {
                        femtoZipCompressionModelXml = dictionaries.get((byte) 1);
                        femtoZipCompressionModelJson = dictionaries.get((byte) 2);
                        femtoZipCompressionModelCsv = dictionaries.get((byte) 3);
                        logger.log(Level.INFO, "Finished loading dictionaries");
                    }
                }

            }

        }

        private void publishCoordinates(String latitude, String longitude) {
            LatLng location = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
            Marker marker = mMap.addMarker(new MarkerOptions().position(location));
            markers.add(marker);
            if (markers.size() == 100) {
                markers.get(0).setVisible(false);
                markers.remove(0);
            }
        }

        private void publishThroughput(Float throughput) {
            MainActivity.this.throughput.setText(throughput.toString());
            MainActivity.this.msgSizeBar.setProgress(throughput.intValue());
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {

        }

        public void subscribeToTopic(String topic) throws MqttException {
            cl.subscribe(topic, 0);
        }

    }

}
