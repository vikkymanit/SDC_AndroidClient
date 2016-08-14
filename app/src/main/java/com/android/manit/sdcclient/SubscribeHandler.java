//package com.android.manit.sdcclient;
//
//import android.os.Environment;
//import android.util.Log;
//import android.widget.Toast;
//
//import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
//import org.eclipse.paho.client.mqttv3.MqttCallback;
//import org.eclipse.paho.client.mqttv3.MqttClient;
//import org.eclipse.paho.client.mqttv3.MqttException;
//import org.eclipse.paho.client.mqttv3.MqttMessage;
//import org.toubassi.femtozip.models.FemtoZipCompressionModel;
//import org.w3c.dom.Document;
//
//import java.io.BufferedWriter;
//import java.io.ByteArrayInputStream;
//import java.io.File;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.util.Arrays;
//import java.util.Dictionary;
//import java.util.Hashtable;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//
//
//public class SubscribeHandler implements MqttCallback {
//    private Logger logger = Logger.getLogger("SubscribeHandler");
//
//    private static final String TAG = SubscribeHandler.class.getSimpleName();
//
//    private final MqttClient cl;
//    private MainActivity.SubscribeDataAsyncTask subscribeDataAsyncTask;
//    private final Dictionary<Byte, FemtoZipCompressionModel> dictionaries;
//    private Integer msgCount;
//    private Long uncompressStop;
//    private Long compressedStop;
//    private Long beginUnCompressed;
//    private Long endUnCompressed;
//    private Long beginCompressed;
//    private Long endCompressed;
//
//    private long compressedByteCnt;
//    private long uncompressedByteCnt;
//    private long uncompressionCPUTime;
//    private Long dictReceiveTime;
//
//    private int count = 0;
//    private int dictCount = 0;
//
////    private final BufferedWriter df;
//    private DocumentBuilder documentBuilder;
//    private FemtoZipCompressionModel femtoZipCompressionModelXml;
//    private boolean timerStarted = false;
//    private long lastTime;
//
//    public SubscribeHandler(MqttClient cl) throws Exception {
//        this.msgCount = 0;
//        this.cl = cl;
//
//        dictionaries = new Hashtable<>();
//        documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
//
//        compressedByteCnt = 0;
//        uncompressedByteCnt = 0;
//        uncompressionCPUTime = 0;
//
//        getDictionaries();
//
////        File resultFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/sdc_bandwidth/");
////
////        if (!resultFolder.exists())
////            resultFolder.mkdir();
////
////        File resultFile = new File(resultFolder, "results.csv");
////        boolean fileExists = resultFile.exists();
////
////        df = new BufferedWriter(new FileWriter(resultFile, true));
////        if (!fileExists) {
////            df.write("uncompressedTime,compressedTime,dictRcvMinusEndCompressed");
////            df.newLine();
////            df.flush();
////        }
//    }
//
//    public void startSubscribing() throws MqttException {
//        this.cl.subscribe(Const.DICT_TOPIC_NAME);
//    }
//
//    public void subscribeToTopic(String topic) throws MqttException {
//        this.cl.subscribe(topic);
//    }
//
//    public MqttClient getMqttClient() {
//        return cl;
//    }
//
//
//    @Override
//    public void connectionLost(Throwable throwable) {
//
//    }
//
//    private void printTimeVariables() {
//        subscribeDataAsyncTask.pbProgress(
//                "#beginUncompressed:" + beginUnCompressed +
//                        " #endUnCompressed:" + endUnCompressed +
//                        " #beginCompressed:" + beginCompressed +
//                        " #endCompressed:" + endCompressed +
//                        " #compressedByteCnt:" + compressedByteCnt +
//                        " #uncompressedByteCnt:" + uncompressedByteCnt +
//                        " #uncompressionCPUTime:" + uncompressionCPUTime);
//
//        if ((beginUnCompressed != null) && (endUnCompressed != null)) {
//            subscribeDataAsyncTask.pbProgress("#durationUnCompressed:" + (endUnCompressed - beginUnCompressed));
//        }
//        if ((beginCompressed != null) && (endCompressed != null)) {
//            subscribeDataAsyncTask.pbProgress("#durationCompressed:" + (endCompressed - beginCompressed));
//        }
//        if ((dictReceiveTime != null) && (endCompressed != null)) {
//            subscribeDataAsyncTask.pbProgress("#durationdicttillendcompressed:" + (endCompressed - dictReceiveTime));
//        }
//
//    }
//
//    @Override
//    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
//
//        if(!timerStarted) {
//            lastTime = System.currentTimeMillis();
//            timerStarted = true;
//        }
//        msgCount++;
////        logger.log(Level.INFO,"Count" + msgCount);
//
//        if(System.currentTimeMillis()-lastTime > 1000){
//            double timeElapsed = (System.currentTimeMillis()-lastTime)/1000.0;
//            double throughput = msgCount/timeElapsed;
//            timerStarted=false;
//            msgCount=0;
//            subscribeDataAsyncTask.pbProgress(Double.toString(throughput));
//        }
//
//        byte[] msgPayload = mqttMessage.getPayload();
//        byte id = msgPayload[0];
//
//        byte[] payload = Arrays.copyOfRange(msgPayload, 1, msgPayload.length);
//
//        if (s.equalsIgnoreCase(Const.DICT_TOPIC_NAME)) {
//            FemtoZipCompressionModel femtoZipCompressionModel = FemtoFactory.fromDictionary(payload);
//            dictionaries.put(id, femtoZipCompressionModel);
//
//            //Write to file
//            if(id==1) {
//                FemtoFactory.toCache(femtoZipCompressionModel, "xmlDict");
//                dictCount++;
//                logger.log(Level.INFO,"#received xml dictionary:" + payload.length);
//
//            }
//
//            if(id==2) {
//                FemtoFactory.toCache(femtoZipCompressionModel, "jsonDict");
//                dictCount++;
//                logger.log(Level.INFO,"#received json dictionary:" + payload.length);
//
//            }
//
//            if(id==3) {
//                FemtoFactory.toCache(femtoZipCompressionModel, "csvDict");
//                dictCount++;
//                logger.log(Level.INFO,"#received csv dictionary:" + payload.length);
//
//            }
//
//            if(dictCount==3){
//                femtoZipCompressionModelXml = dictionaries.get((byte)1);
//            }
//
//        }
//
//
//        if (s.equalsIgnoreCase(Const.XML_UNCOMPRESSED_TOPIC_NAME)) {
//
//            Document xmlMsg = documentBuilder.parse(new ByteArrayInputStream(payload));
//            String latitude = xmlMsg.getElementsByTagName("pickup_latitude").item(0).getTextContent();
//            String longitude = xmlMsg.getElementsByTagName("pickup_longitude").item(0).getTextContent();
//
//            subscribeDataAsyncTask.pbProgress(latitude, longitude);
////            subscribeDataAsyncTask.pbProgress(Integer.toString(msgPayload.length));
//        }
//
//        if (s.equalsIgnoreCase(Const.XML_COMPRESSED_TOPIC_NAME)) {
//
//            byte[] uncompressedMessage = femtoZipCompressionModelXml.decompress(payload);
//
//            Document xmlMsg = documentBuilder.parse(new ByteArrayInputStream(uncompressedMessage));
//            String latitude = xmlMsg.getElementsByTagName("pickup_latitude").item(0).getTextContent();
//            String longitude = xmlMsg.getElementsByTagName("pickup_longitude").item(0).getTextContent();
//            subscribeDataAsyncTask.pbProgress(latitude, longitude);
//
////            subscribeDataAsyncTask.pbProgress("#compressedMessage:" + count + ":" + new String(uncompressedMessage));
////            subscribeDataAsyncTask.pbProgress(Integer.toString(msgPayload.length));
//        }
//
//    }
//
//    @Override
//    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
//
//    }
//
//    public void getDictionaries() throws Exception {
//        dictionaries.put((byte) 1, FemtoFactory.fromDictionary(FemtoFactory.fromCacheXml()));
//        dictionaries.put((byte) 2, FemtoFactory.fromDictionary(FemtoFactory.fromCacheJson()));
//        dictionaries.put((byte) 3, FemtoFactory.fromDictionary(FemtoFactory.fromCacheCsv()));
//        logger.log(Level.INFO, "Finished loading dictionaries");
//
//        femtoZipCompressionModelXml = dictionaries.get((byte)1);
//    }
//
//
//}
