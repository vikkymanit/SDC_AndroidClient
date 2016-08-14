package com.android.manit.sdcclient;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import org.toubassi.femtozip.models.FemtoZipCompressionModel;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import java.io.*;


public class FemtoFactory {
    public static FemtoZipCompressionModel fromDictionary(byte[] dict) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(dict);
        DataInputStream dis = new DataInputStream(bis);

        FemtoZipCompressionModel compressionModel = new FemtoZipCompressionModel();
        compressionModel.load(dis);

        dis.close();

        return compressionModel;
    }

    public static byte[] getDictionary(FemtoZipCompressionModel compressionModel) throws IOException {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        compressionModel.save(dos);
        dos.flush();

        return bos.toByteArray();
    }

    public static File getDictCacheFile(String fileName) {
//        return new File("dict");
        File resultFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/sdc_bandwidth/");

        if (!resultFolder.exists())
            resultFolder.mkdir();

        File dictionaryFile = new File(resultFolder, fileName);
        return dictionaryFile;
    }

    public static boolean isCachedDictionaryAvailable(String fileName) {
        File dictFile = getDictCacheFile(fileName);
        return dictFile.exists() && !dictFile.isDirectory();
    }

    public static byte[] fromCache(String fileName) throws IOException {
        return IOUtils.toByteArray(new FileInputStream(getDictCacheFile(fileName)));
    }

    public static byte[] fromCacheXml() throws IOException {
        return IOUtils.toByteArray(new FileInputStream(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/sdc_bandwidth/" + "dictXml")));
    }

    public static byte[] fromCacheCsv() throws IOException {
        return IOUtils.toByteArray(new FileInputStream(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/sdc_bandwidth/" + "dictCSV")));
    }

    public static byte[] fromCacheJson() throws IOException {
        return IOUtils.toByteArray(new FileInputStream(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/sdc_bandwidth/" + "dictJson")));
    }

    public static void toCache(FemtoZipCompressionModel compressionModel, String fileName) throws IOException {
            FileUtils.writeByteArrayToFile(getDictCacheFile(fileName), getDictionary(compressionModel));

    }

}
