package com.example.robitalk;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;


public class FlashAirRequest {

    public static boolean InitFlashAir()
    {
        return true;
    }

    static public String getString(String command) {
        String result = "";
        try{
            URL url = new URL(command);
            URLConnection urlCon = url.openConnection();
            urlCon.connect();
            InputStream inputStream = urlCon.getInputStream();
            BufferedReader bufreader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            StringBuffer strbuf = new StringBuffer();
            String str;
            while ((str = bufreader.readLine()) != null) {
                if(strbuf.toString() != "") strbuf.append("\n");
                strbuf.append(str);
            }
            result =  strbuf.toString();
        }catch(MalformedURLException e) {
            Log.e("ERROR", "ERROR: " + e.toString());
            e.printStackTrace();
        }
        catch(IOException e) {
            Log.e("ERROR", "ERROR: " + e.toString());
            e.printStackTrace();
        }
        return result;
    }

    public static boolean upload(String s, String s1, byte abyte0[])
    {
        String result="";
        try {
            byte abyte1[];
            URL url = new URL(s);
            HttpURLConnection httpUrlCon = (HttpURLConnection) url.openConnection();

            httpUrlCon.setDoInput(true);
            httpUrlCon.setDoOutput(true);
            httpUrlCon.setUseCaches(false);
            httpUrlCon.setRequestMethod("POST");
            httpUrlCon.setRequestProperty("Charset", "UTF-8");
            httpUrlCon.setRequestProperty("Content-Type", "multipart/form-data;boundary=========================");
            DataOutputStream dataoutputstream = new DataOutputStream(httpUrlCon.getOutputStream());
            dataoutputstream.writeBytes("--========================\r\n");
            dataoutputstream.writeBytes((new StringBuilder("Content-Disposition: form-data; name=\"upload.cgi\";filename=\"")).append(s1).append("\"").append("\r\n").toString());
            dataoutputstream.writeBytes("\r\n");
            dataoutputstream.write(abyte0);
            dataoutputstream.writeBytes("\r\n");
            dataoutputstream.writeBytes("--========================--\r\n");
            dataoutputstream.flush();
            dataoutputstream.close();
            if (httpUrlCon.getResponseCode() == HttpURLConnection.HTTP_OK){
                StringBuffer sb = new StringBuffer();
                InputStream is = httpUrlCon.getInputStream();
                abyte1 = new byte[1024];
                int leng = -1;
                while((leng = is.read(abyte1)) != -1) {
                    sb.append(new String(abyte1, 0, leng));
                }
                result = sb.toString();
            }
        }
        catch (MalformedURLException e) {
            Log.e("ERROR", "ERROR: " + e.toString());
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("ERROR", "ERROR: " + e.toString());
            e.printStackTrace();
        }
        return true;
    }

}