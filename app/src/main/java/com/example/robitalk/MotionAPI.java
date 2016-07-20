package com.example.robitalk;

import android.os.AsyncTask;
import android.util.Log;

import java.util.Locale;
import java.util.Random;


/**
 * Created by hiratsukamichihisa on 08/08/2015.
 */
public class MotionAPI {

    private static String REMOTE_FILENAME = "REMOTE.LOG";
    private static String REMOTE_FOLDERNAME = "ADDON";
    private static int REMOTE_PROGRAM_ID = 904;
    private static int REMOTE_SIZE = 16;
    protected boolean mEnable = true;
    private static String UPLOAD_URL = "http://flashair/upload.cgi";
    private String textForSpeech;

    public MotionAPI(String str){
        textForSpeech = str;
    }


    private class MotionAsyncTask extends AsyncTask<byte[], Void, Integer> {

        @Override
        protected Integer doInBackground(byte[]... paramAnonymousVarArgs) {
            // check send message
            String str = "?WRITEPROTECT=OFF&UPDIR=/" + REMOTE_FOLDERNAME;
            Log.d("FlashAir", "parameter=" + str);
            // check connection
            str = FlashAirRequest.getString(UPLOAD_URL + str);
            Log.d("FlashAir", "getString rtnStr=" + str);
            // if connection failed
            if (!str.toUpperCase(Locale.getDefault()).equals("SUCCESS")) {
                Log.d("FlashAir", "Cannot rtnStr=" + str);
                return Integer.valueOf(-1);
            }

            // if upload failed
            if (!FlashAirRequest.upload(UPLOAD_URL,
                    new String(paramAnonymousVarArgs[0]), paramAnonymousVarArgs[1])) {
                Log.d("FlashAir", "upload rtnStr=" + str);
                return Integer.valueOf(-2);
            }

            // if message is too long
            if (paramAnonymousVarArgs[1].length > REMOTE_SIZE) {
                return Integer.valueOf(2);
            }

            return Integer.valueOf(1);
        }

        @Override
        protected void onPostExecute(Integer paramAnonymousInteger) {
            if (paramAnonymousInteger.intValue() < 0) {
                ResultFlashAir(paramAnonymousInteger);
            }
            while (paramAnonymousInteger.intValue() <= 1) {
                return;
            }
            start(REMOTE_PROGRAM_ID);
        }
    }

    /**
     * motion開始
     */
    public void start(int paramInt) {
        if(paramInt != REMOTE_PROGRAM_ID){
            paramInt = selectMotion(textForSpeech);
//            paramInt = selectMotion(feeling, seconds, motionType);
        }
        byte[] arrayOfByte = new byte[REMOTE_SIZE];
        arrayOfByte[0] = -2;
        arrayOfByte[1] = -1;
        arrayOfByte[2] = ((byte) (paramInt % 256));
        arrayOfByte[3] = ((byte) (paramInt / 256));
        Log.d("RBCon", "SendFlashAir:id=" + String.valueOf(paramInt));

        // 実行
        byte[][] params = new byte[][]{REMOTE_FILENAME.getBytes(), arrayOfByte};
//        new MotionAsyncTask().execute(params);
        new MotionAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,params);
        return;
    }

    public int selectMotion(String str){
        int returnID=30;
        int textLength = str.length();
        double speechDuration = textLength * 0.2;
        Random rnd = new Random();
        int ran = rnd.nextInt(3);
        if(speechDuration <= 2){
            switch (ran) {
                case 0:returnID = 22;
                    break;
                case 1:returnID = 26;
                    break;
                case 2:returnID = 27;
                    break;
            }
        }
        else if(speechDuration < 3){
            switch (ran) {
                case 0:returnID = 17;
                    break;
                case 1:returnID = 19;
                    break;
                case 2:returnID = 20;
                    break;
            }
        }
        else if(speechDuration < 5){
            switch (ran) {
                case 0:returnID = 21;
                    break;
                case 1:returnID = 21;
                    break;
                case 2:returnID = 21;
                    break;
            }
        }
        else if(speechDuration >= 5){
            switch (ran) {
                case 0:returnID = 210;
                    break;
                case 1:returnID = 211;
                    break;
                case 2:returnID = 211;
                    break;
            }
        }
//        Random rnd = new Random();
//        int ran = rnd.nextInt(4);
//        switch (ran){
//            case 0: returnID = 17;break;
//            case 1: returnID = 19;break;
//            case 2: returnID = 23;break;
//            case 3: returnID = 108;break;
//        }
        return returnID;
    }

    public static void ResultFlashAir(Integer paramInteger) {
        // miss connection
        if (paramInteger.intValue() < 0) {
            // miss upload
            if (paramInteger.intValue() != -2) {
                Log.d("connection failed", "check wifi");
            }
        }
        return;
    }
}
