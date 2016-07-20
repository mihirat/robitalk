/*
 * 2015 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package com.example.robitalk;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import jp.co.nttit.EnterVoiceSP.service.helper.SpeechRecServiceHelper;
import jp.co.nttit.EnterVoiceSP.service.helper.VoiceRecognitionEventListener;
import jp.co.nttit.EnterVoiceSP.service.util.DivideFileManager;
import speechrec.client.ModelGroup;
import speechrec.client.Nbest;
import speechrec.client.Sentence;
import speechrec.client.SpeechRecException;
import speechrec.client.Word;


public class SpeechRecognitionNTT implements VoiceRecognitionEventListener {

    private Context mContext = null;        // Context
    private String mApiKey = "";            // APIキー
    private Handler mHandler;               // UIスレッドに通知するためのHandler

    /** Logcat用のタグ */
    private final static String TAG = SpeechRecognitionNTT.class.getSimpleName();

    /** 起動パラメータ（SBM_MODE） */
    public static final String KEY_SBM_MODE = "sbm_mode";
    /** 起動パラメータ（API_KEY） */
    public static final String KEY_API_KEY = "api_key";
    /** 起動パラメータ（VAD_MODEL） */
    public static final String KEY_VAD_MODEL = "vad_model";

    /** Nベスト表示モード */
    private boolean nbest = false;
    /** 音声認識結果出力モードのセパレータ */
    private String separator = "";

    /** 音声認識サービスのヘルパー */
    private SpeechRecServiceHelper helper;
    /** 音声認識サービスのパラメータ */
    private Bundle bundle;
    /** 音声認識結果のリスト */
    private LinkedList<StringBuilder> resultList = null;

    /**
     * コンストラクタ
     */
    public SpeechRecognitionNTT(Context context, String apiKey, Handler handler) {
        mContext = context;
        mApiKey = apiKey;
        mHandler = handler;
        helper = new SpeechRecServiceHelper();
    }

    private final class NttControlThread extends Thread {

        private final VoiceRecognitionEventListener mVRElsr;
        private NttControlThread(final VoiceRecognitionEventListener VRElsr) {
            this.mVRElsr = VRElsr;
        }

        @Override
        public void run() {
            /**
             * Divide関連ファイルを端末内に展開し、音声認識サービスと接続する。
             *
             * @see android.support.v4.app.FragmentActivity#onStart()
             */
            // Divide関連ファイルを端末内に展開
            DivideFileManager divideFileManager = new DivideFileManager(mContext);
            if (!divideFileManager.isExtracted()) {
                try {
                    divideFileManager.extract();
                } catch (IOException e) {
                    sendMessage("devidefile loading failed", Uti.API_NOTIFY_ERROR, 0);
                    return;
                }
            }

            bundle = new Bundle();
            // Bundle にインテントの値（SBMモード、APIキー）を追加
            int sbm_mode = 0;
            Intent intent = new Intent();
            intent.putExtra(SpeechRecognitionNTT.KEY_SBM_MODE, sbm_mode);
            // APIキーを設定
            intent.putExtra(SpeechRecognitionNTT.KEY_API_KEY, mApiKey);
            bundle.putAll(intent.getExtras());
            // Bundle に区間検出モデルファイルを追加
            bundle.putString(KEY_VAD_MODEL, divideFileManager.getDivideModelPath());

            Log.d(TAG, "helper.connect()");
            // 音声認識サービスと接続
            try {
                helper.connect(mContext, mVRElsr);
            }catch(IllegalStateException | NullPointerException e){
                Log.d(TAG, "connection failed");
                sendMessage("connection failed", Uti.API_NOTIFY_ERROR, 0);
            }
        }

    }

    /**
     * メッセージ送信
     */
    private void sendMessage(Object obj, int arg2, int what) {
        Message msg = new Message();
        msg.obj = obj;
        msg.arg1 = Uti.API_SPEECHRECOGNITION;
        msg.arg2 = arg2;
        msg.what = what;
        mHandler.sendMessage(msg);
    }

    /**
     * 音声認識開始
     */
    public void start() {
        // 認識スレッドの開始
        new NttControlThread(this).start();
    }

    /**
     * キャンセル
     */
    public void cancel() {
        helper.stopRecognition();
        sendMessage(null, Uti.API_NOTIFY_EVENT, Uti.NOTIFY_CANCEL);
        helper.close();

    }

    /**
     * 終了ボタンを活性に変更し、音声認識処理を開始を開始する。
     *
     * @see jp.co.nttit.EnterVoiceSP.service.helper.VoiceRecognitionEventListener#onServiceConnected()
     */
    @Override
    public void onServiceConnected() {
        sendMessage(null, Uti.API_NOTIFY_EVENT, Uti.STATE_READY_FOR_SPEECH);

        Log.d(TAG, "onServiceConnected()");
        Log.d(TAG, "helper.startRecognition()");

        // 音声認識処理を開始
        sendMessage(null, Uti.API_NOTIFY_EVENT, Uti.STATE_BEGINNING_OF_SPEECH);

        helper.startRecognition(bundle);
    }
    /**
     * 音声認識結果の値を保持する。
     *
     * @see jp.co.nttit.EnterVoiceSP.service.helper.VoiceRecognitionEventListener#onResult(speechrec.client.Nbest)
     */
    @Override
    public void onResult(Nbest result) {
        Log.d(TAG, "onResult()");

        List<Sentence> sentenceList = result.getSentenceList();

        if (resultList == null) {
            resultList = new LinkedList<StringBuilder>();
            for (int i = 0; i < sentenceList.size(); i++) {
                resultList.add(new StringBuilder());
            }
        } else {
            while (resultList.size() > sentenceList.size()) {
                resultList.removeLast();
            }
        }

        for (int i = 0; i < resultList.size(); i++) {
            StringBuilder sb = resultList.get(i);
            Sentence sentence = sentenceList.get(i);
            for (Word word : sentence.getWordList()) {
                String label = word.getLabel();
                // ラベルがnullは無視
                if (label == null) {
                    continue;
                }
                // セミコロン以降、空白のみは無視
                label = label.replaceAll(";.*", "").replaceAll("[ 　]", "");
                // ラベルが空文字列は無視
                if (label.length() == 0) {
                    continue;
                }
                // 音声認識結果に追加
                if ((separator != null) && (sb.length() > 0)) {
                    sb.append(separator);
                }
                sb.append(label);
            }
        }
    }

    @Override
    public void onResult(ModelGroup modelGroup) {

    }

    @Override
    public void onRecord(short[] shorts) {
        if (shorts.length == 0) {
//            helper.stopRecognition();
            return;
        }
    }

    @Override
    public void onStopRecording() {
    }

    /**
     * 音声認識の終了通知の結果により以下のいずれかを行う。<br/>
     * ・エラーが発生していた場合、エラーダイアログを表示する。<br/>
     * ・音声認識結果が０件の場合、エラーダイアログを表示する。<br/>
     * ・Nベスト表示なし、または音声認識結果が１件の場合、処理を終了する。<br/>
     * ・Nベスト表示あり、かつ音声認識結果が２件以上の場合、最初のひとつを選んで処理を終了する。<br/>
     *
     * @see jp.co.nttit.EnterVoiceSP.service.helper.VoiceRecognitionEventListener;r#onFinish(java.lang.Exception)
     */
    @Override
    public void onFinish(SpeechRecException e) {
        Log.d(TAG, "helper.close()");
        helper.close();

        if (e != null) {
            // エラー発生した場合
            final String message = e.getType() + " " + e.getErrno() + "\n" + e.getMessage();
            sendMessage(message, Uti.API_NOTIFY_ERROR, 0);

        } else {
            // 音声認識結果を詰め替えて、無効なデータを削除
            LinkedList<String> resultList = new LinkedList<String>();
            if (this.resultList != null) {
                for (StringBuilder sb : this.resultList) {
                    if (sb.length() > 0) {
                        resultList.add(sb.toString());
                    }
                }
            }
            if (resultList.isEmpty()) {
                // 音声認識結果が０件の場合
                sendMessage("no result", Uti.API_NOTIFY_ERROR, 0);
                helper.close();

            } else if (!nbest || (resultList.size() == 1)) {
                // Nベスト表示なし、または音声認識結果が１件のみ
                final String replace = resultList.getFirst().toString();
                sendMessage(replace, Uti.API_NOTIFY_RESULT, 0);
                helper.close();
            } else {
                // Nベスト表示あり、かつ音声認識結果が２件以上
                final String[] items = new String[resultList.size()];
                for (int i = 0; i < items.length; i++) {
                    items[i] = resultList.get(i).toString();
                }
                sendMessage(items[0], Uti.API_NOTIFY_RESULT, 0);
                helper.close();
            }
        }
        sendMessage(null, Uti.API_NOTIFY_EVENT, Uti.STATE_END_OF_SPEECH);

    }

    @Override
    public void onRecordNR(short[] shorts) {

    }

    @Override
    public void onConnected() {

    }

}
