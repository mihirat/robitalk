/*
 * 2015 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package com.example.robitalk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.FaceDetector;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import jp.ne.docomo.smt.dev.common.http.AuthApiKey;
import jp.ne.docomo.smt.dev.dialogue.data.DialogueResultData;
import jp.ne.docomo.smt.dev.dialogue.param.DialogueRequestParam;
import jp.ne.docomo.smt.dev.knowledge.data.KnowledgeAnswerData;
import jp.ne.docomo.smt.dev.knowledge.data.KnowledgeMessageData;
import jp.ne.docomo.smt.dev.knowledge.data.KnowledgeResultData;
import jp.ne.docomo.smt.dev.sentenceunderstanding.data.SentenceCommandData;
import jp.ne.docomo.smt.dev.sentenceunderstanding.data.SentenceDialogStatusData;
import jp.ne.docomo.smt.dev.sentenceunderstanding.data.SentenceResultData;
import jp.ne.docomo.smt.dev.sentenceunderstanding.data.SentenceSlotStatusData;
import jp.ne.docomo.smt.dev.sentenceunderstanding.data.SentenceUserUtteranceData;


/**
 * メイン画面表示クラス
 */
public class MainActivity extends Activity {
    // APIキー(開発者ポータルから取得したAPIキーをここに記述する)
    private static final String APIKEY = "";

    private static final int SETTING_REQUEST = 100; // 設定画面のリクエストコード

    // Viewオブジェクト
    private static ListItemManager sListItemManager;   // 会話表示View管理クラスオブジェクト
    private static ScrollView sScrollView;             // 会話表示のスクロールビューオブジェクト
    private static ImageButton sSpeechRecgBtn;         // 音声認識開始/停止ボタンオブジェクト
    private LinearLayout mBaseLayout;           // 会話表示のベースレイアウトオブジェクト

    // ドコモAPIオブジェクト
//    private SpeechRecognitionFU mSpeechRecognitionFU;   // 音声認識FUETREK
    private SpeechRecognitionNTT mSpeechRecognitionNTT;   // 音声認識FUETREK

    private static boolean sSpeechRecognitionFlag = false;  // 音声認識起動フラグ(true:起動中 ,false:待機中)
    private static Context sContext = null;                 // Context
    private static int sVoiceEngine = 0;                    // 音声合成エンジン(0:AI, 1:IT)
    private static String sVoiceName = "maki";             // AIの話者
    private static int sSpeekerID = 0;                      // ITの話者
    private static boolean sTextToSpeechFlag = false;       // 音声合成起動フラグ(true:起動中 ,false:待機中)
    private static TextView sCommandTv;                     // コマンド情報
    private int mSbmMode = 0;                       // SBMモード

    // dialogue parameters
    private static String dContextID;
    private static int dAge;
    private static String dNickname;
    private static String dSex;
    private static String dBloodtype;
    private static String dPlace;
    private static String dConstellaration;
    private SharedPreferences sp;

    // calendar
    private static long mCalendarID = -100;
    private static int mCalendarIDPos = 0;
    private static ArrayList<CalendarEvent> resList;

    private Camera mCamera;
    private SurfaceView preview;
    private SurfaceView overlay;
//    private CameraListener cameraListener;
//    private OverlayListener overlayListener;

    private long currentTime;
    private long stopTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        // ドコモ提供のSDKを利用する場合、認証情報初期化を行う
        AuthApiKey.initializeAuth(APIKEY);

        // Viewの設定
        initView();

        // 会話表示View管理クラス
        sListItemManager = new ListItemManager(this, mBaseLayout);

        // Contextの取得
        sContext = this.getApplicationContext();

        // load shared preferences
        sp = getPreferences(Context.MODE_PRIVATE);
        dAge = sp.getInt("age", 25);
        dNickname = sp.getString("nickname", "ユーザー");
        dSex = sp.getString("sex", "女");
        dBloodtype = sp.getString("bloodtype", "B");
        dPlace = sp.getString("place", "東京");
        dConstellaration = sp.getString("constellaration","魚座");

        // camera
//        preview = new SurfaceView(this);
//        ((FrameLayout) findViewById(R.id.preview)).addView(preview);
//        cameraListener = new CameraListener(preview);
//        preview.setOnTouchListener(ontouchListener);

//        overlay = (SurfaceView) findViewById(R.id.overlay);
//        overlayListener = new OverlayListener(overlay);


    }

//    @Override
//    protected void onPostCreate(Bundle savedInstanceState) {
//        super.onPostCreate(savedInstanceState);
//        preview.getHolder().addCallback(cameraListener);
//        overlay.getHolder().addCallback(overlayListener);
//    }
//
//    View.OnTouchListener ontouchListener = new View.OnTouchListener() {
//        @Override
//        public boolean onTouch(View v, MotionEvent event) {
//            if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                if (mCamera != null) {
//                    // 撮影実行(AF開始)
//                    mCamera.autoFocus(null);
//                }
//            }
//
//            return false;
//        }
//    };
//
//
//
//    private class CameraListener implements
//            SurfaceHolder.Callback,
//            Camera.PreviewCallback
//    {
//        private SurfaceView surfaceView;
//        private SurfaceHolder surfaceHolder;
//        private Rect faceRect = new Rect();
//
//        public CameraListener(SurfaceView surfaceView) {
//            this.surfaceView = surfaceView;
//        }
//        @Override
//        public void surfaceCreated(SurfaceHolder holder) {
//            surfaceHolder = holder;
//            try {
////                int cameraId = -1;
////                Camera.CameraInfo info = new Camera.CameraInfo();
////                for (int id = 0; id < Camera.getNumberOfCameras(); id++) {
////                    Camera.getCameraInfo(id, info);
////                    if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
////                        cameraId = id;
////                        break;
////                    }
////                }
////                camera = Camera.open(cameraId);
////                camera = Camera.open();
//                mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
//                mCamera.setDisplayOrientation(90);
//
//                mCamera.setPreviewDisplay(holder);
//                List<Camera.Size> sizes = mCamera.getParameters().getSupportedPreviewSizes();
//                mCamera.getParameters().setPreviewSize(sizes.get(0).width, sizes.get(0).height);
//                mCamera.getParameters().setPreviewFpsRange(1000, 2000);
//            } catch (Exception e) {
////                Log.e(TAG, e.toString(), e);
//            }
//        }
//
//        @Override
//        public void surfaceChanged(SurfaceHolder holder, int format, int width,
//                                   int height) {
//            // 変更されたとき
//            surfaceHolder = holder;
//            mCamera.setPreviewCallback(this);
//            mCamera.startPreview();
//        }
//
//        @Override
//        public void surfaceDestroyed(SurfaceHolder holder) {
//            // 破棄されたとき
//            mCamera.setPreviewCallback(null);
//            mCamera.release();
//            mCamera = null;
//        }
//
//        @Override
//        public void onPreviewFrame(byte[] data, Camera camera) {
//            Bitmap image = decodePreview(data);
//
//            FaceDetector faceDetector = new FaceDetector(image.getWidth(), image.getHeight(), 1);
//            FaceDetector.Face[] faces = new FaceDetector.Face[1];
//            int n = faceDetector.findFaces(image, faces);
//
//            if (n > 0) {
//                PointF midPoint = new PointF(0, 0);
//                faces[0].getMidPoint(midPoint); // 顔認識結果を取得
//                float eyesDistance = faces[0].eyesDistance(); // 顔認識結果を取得
//                faceRect.left = (int) (midPoint.x - eyesDistance * 0.8);
//                faceRect.right = (int) (midPoint.x + eyesDistance * 0.8);
//                faceRect.top = (int) (midPoint.y - eyesDistance * 0.2);
//                faceRect.bottom = (int) (midPoint.y + eyesDistance * 0.2);
//
//                currentTime = System.currentTimeMillis();
//                long interval = currentTime - stopTime;
//                if (interval > 10000) {
//                    if (midPoint.x > image.getWidth() / 4 && midPoint.x < image.getWidth() * 3 / 4 &&
//                            midPoint.y > image.getHeight() / 4 && midPoint.y < image.getHeight() * 3 / 4) {
//                        if (!sSpeechRecognitionFlag && !sTextToSpeechFlag) {
//                            startSpeechRecognition();
//                            stopTime = System.currentTimeMillis();
//                        }
//                    }
//                }
////                overlayListener.drawFace(faceRect, Color.YELLOW, image);
//            }
//        }
//
//        private int[] rgb;
//        private Bitmap tmpImage;
//        private Bitmap decodePreview(byte[] data) {
//            int width = mCamera.getParameters().getPreviewSize().width;
//            int height = mCamera.getParameters().getPreviewSize().height;
//            if (rgb == null) {
//                rgb = new int[width*height];
//                tmpImage = Bitmap.createBitmap(height ,width , Bitmap.Config.RGB_565);
//            }

//            decodeYUV420SP(rgb, data, width, height);
//            tmpImage.setPixels(rgb, 0, height, 0, 0, height, width);
//            return tmpImage;
//        }
//    };


    @Override
    protected void onResume() {
        super.onResume();

        if (mBaseLayout.getChildCount() == 0) {
            // 初回表示のみ
            // 初回表示が遅れる対応
            new DelayShowStartMessage().execute(100); // 0.1秒ウエイト
            return;
        }

        // 画面下までスクロール
        scrollToBottom();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // 音声認識の停止
        stopSpeechRecognition();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 音声認識の停止
        stopSpeechRecognition();

        // 音声認識サービスの終了
        sListItemManager = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_conversation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i = null;
        switch (item.getItemId()) {
            case R.id.menu_appinfo:
                // アプリ情報画面へ遷移
                i = new Intent(this, AppInformationActivity.class);
                startActivity(i);
                return true;
            case R.id.menu_appsettings:
                // 設定画面へ遷移
                i = new Intent(this, AppSettingsActivity.class);
                // 子への引継ぎ情報
                i.putExtra("voiceEngine", sVoiceEngine);
                i.putExtra("voiceName", sVoiceName);
                i.putExtra("speekerID", sSpeekerID);
                i.putExtra("sbmMode", mSbmMode);
                i.putExtra("calendarID", mCalendarID);
                i.putExtra("calendarIDPos", mCalendarIDPos);
                i.putExtra("age",dAge);
                i.putExtra("bloodtype",dBloodtype);
                i.putExtra("constellaration",dConstellaration);
                i.putExtra("nickname",dNickname);
                i.putExtra("place",dPlace);
                i.putExtra("sex",dSex);

                startActivityForResult(i, SETTING_REQUEST);
                return true;
            default:
                break;
        }
        return false;
    }

    /**
     * 設定画面の後処理
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SETTING_REQUEST:   // 設定画面
                if (resultCode == RESULT_OK) {
                    // 設定画面からの受け取ったデータを設定する
                    sVoiceEngine = data.getIntExtra("voiceEngine", 0);
                    sVoiceName = data.getStringExtra("voiceName");
                    sSpeekerID = data.getIntExtra("speekerID", 0);
                    mCalendarID = data.getLongExtra("calendarID", 0);
                    mCalendarIDPos = data.getIntExtra("calendarIDPos", 0);
                    mSbmMode = data.getIntExtra("sbmMode", 0);
                    dAge = data.getIntExtra("age",0);
                    dBloodtype = data.getStringExtra("bloodtype");
                    dConstellaration = data.getStringExtra("constellaration");
                    dNickname = data.getStringExtra("nickname");
                    dPlace = data.getStringExtra("place");
                    dSex = data.getStringExtra("sex");

                    // save user inputs
                    SharedPreferences sp = getPreferences(Context.MODE_PRIVATE);
                    SharedPreferences.Editor e = sp.edit();
                    e.putInt("age", dAge);
                    e.putString("bloodtype", dBloodtype);
                    e.putString("constellaration", dConstellaration);
                    e.putString("nickname", dNickname);
                    e.putString("place", dPlace);
                    e.putString("sex", dSex);
                    e.commit();

                    resList = getList(mCalendarID);

                }
                break;
            default:
                break;
        }
    }

    /**
     * 初回表示が遅れる対応。非同期でウエイト後にUI表示処理追加
     */
    private class DelayShowStartMessage extends AsyncTask<Integer, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {
                Thread.sleep(params[0].intValue());
            } catch (InterruptedException e) {
                // エラー表示
                Toast.makeText(sContext, e.getMessage(), Toast.LENGTH_LONG).show();
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result.booleanValue()) {
                return;
            }

            //初回テキスト表示（既に表示されている場合は表示されない）
            if (sListItemManager != null) {
                // テキスト出力
                sListItemManager.setFirstMessage();
                // 音声合成出力
                startMotionAPI(getString(R.string.txt_first_message));
                startTextToSpeech(getString(R.string.txt_first_message));
            }
        }
    }

    ;

    /**
     * 音声認識開始/終了ボタンのクリックリスナー
     */
    private View.OnClickListener mClickSpeechRecg = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (sSpeechRecognitionFlag) {
                // 音声認識起動中なら停止
                stopSpeechRecognition();
            } else {
                // 音声合成起動中なら無効
                if (!sTextToSpeechFlag) {
                    //音声認識スタート
                    startSpeechRecognition();
                }
            }
        }
    };

    /**
     * 「はじめから」ボタンのクリックリスナー
     */
    private View.OnClickListener mClickRestart = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // 音声合成起動中なら無効
            if (!sTextToSpeechFlag) {
                if (sSpeechRecognitionFlag) {
                    // 音声認識起動中なら停止
                    stopSpeechRecognition();
                }
                // テキストクリア
                TextView textView = (TextView) findViewById(R.id.textView1);
                textView.setText("");
                // 表示テキストを全て削除
                sListItemManager.clearAll();
                // テキスト出力
                sListItemManager.setFirstMessage();
                // 音声合成出力
                startTextToSpeech(getString(R.string.txt_first_message));
            }
        }
    };

    /**
     * Viewの初期化
     */
    private void initView() {
        // 会話表示エリア
        mBaseLayout = (LinearLayout) findViewById(R.id.conersation_base);
        sScrollView = (ScrollView) findViewById(R.id.scrollView);
//        sCommandTv = (TextView) findViewById(R.id.textView1);

        // マイクボタン
        sSpeechRecgBtn = (ImageButton) findViewById(R.id.btn_recg);
        sSpeechRecgBtn.setOnClickListener(mClickSpeechRecg);

        // はじめからボタン
        ((ImageButton) findViewById(R.id.btn_restart)).setOnClickListener(mClickRestart);
    }

    /**
     * スクロールビューを一番下まで自動スクロールさせる。
     */
    private static void scrollToBottom() {
        AsyncTask<Void, Void, Boolean> waitScroll = new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    Thread.sleep(100); // 0.1秒ディレイ
                } catch (InterruptedException e) {
                    // エラー表示
                    Toast.makeText(sContext, e.getMessage(), Toast.LENGTH_LONG).show();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                sScrollView.fullScroll(View.FOCUS_DOWN);
            }

            ;
        };
        waitScroll.execute();
    }

    /**
     * 音声認識の開始
     */
    private void startSpeechRecognition() {
        sSpeechRecognitionFlag = true;
        if (mSpeechRecognitionNTT == null) {
            mSpeechRecognitionNTT = new SpeechRecognitionNTT(this, APIKEY, sHandler);
        }
        mSpeechRecognitionNTT.start();
    }

    /**
     * 音声認識の停止
     */
    private void stopSpeechRecognition() {
        if (mSpeechRecognitionNTT != null) {
            mSpeechRecognitionNTT.cancel();
            mSpeechRecognitionNTT = null;
        }
        sSpeechRecognitionFlag = false;
    }

    /**
     * 音声合成の開始
     */
    private static void startTextToSpeech(String msg) {
        switch (sVoiceEngine) {
            case 0:    // AI
                TextToSpeechAI textToSpeechAI = new TextToSpeechAI(sHandler);
                textToSpeechAI.setVoiceName(sVoiceName);
                textToSpeechAI.start(msg);
                break;
            case 1:    // IT
                TextToSpeechIT textToSpeechIT = new TextToSpeechIT(APIKEY, sHandler);
                textToSpeechIT.setSpeakerID(sSpeekerID);
                textToSpeechIT.start(msg);
                break;
            default:
                break;
        }
    }

    /**
     * 雑談対話の開始
     */
    private static void startDialogue(String msg) {
        sListItemManager.setInterpretationProgress();
        //画面下までスクロール
        scrollToBottom();
        DialogueRequestParam param = new DialogueRequestParam();
        // 設定値をリクエストパラメータに設定
        param.setSex(dSex);
        param.setNickname(dNickname);
        param.setAge(dAge);
        param.setBloodtype(dBloodtype);
        param.setPlace(dPlace);
        param.setConstellations(dConstellaration);
        param.setContext(dContextID);
        param.setUtt(msg);

        new DialogueAPI(sHandler).start(param);
    }

    /**
     * 知識Q&A対話の開始
     */
    private static void startKnowledgeAPI(String msg) {
        sListItemManager.setInterpretationProgress();
        //画面下までスクロール
        scrollToBottom();

        new KnowledgeAPI(sHandler).start(msg);
    }

    /**
     * 発話理解の開始
     */
    private static void startSpeechUnderstanding(String msg) {
        new SpeechUnderstanding(sHandler).start(msg);
    }

    // calendar start
    private static void startCalendarManager(List<SentenceSlotStatusData> slotStatusDataList) {
        sListItemManager.setInterpretationProgress();
        //画面下までスクロール
        scrollToBottom();
        new CalendarManager(sHandler).start(slotStatusDataList);
    }

    // motion start
    private static void startMotionAPI(String str) {
//        sListItemManager.setInterpretationProgress();
//        //画面下までスクロール
//        scrollToBottom();
        new MotionAPI(str).start(0);
    }

    /**
     * UIスレッドでの処理
     */
    private static Handler sHandler = new Handler() {

        @Override
        public void handleMessage(final Message msg) {
            super.handleMessage(msg);

            // メッセージの振り分け
            switch (msg.arg1) {
                case Uti.API_DIALOGUE:              // 雑談対話
                    dialogueProc(msg);
                    break;
                case Uti.API_KNOWLEDGE:             // 知識Q&A
                    knowledgeProc(msg);
                    break;
                case Uti.API_SPEECHRECOGNITION:     // 音声認識
                    speechRecognitionProc(msg);
                    break;
                case Uti.API_SPEECHUNDERSTANDING:   // 発話理解
                    speechUnderstandingsProc(msg);
                    break;
                case Uti.API_TEXTTOSPEECH:          // 音声合成
                    textToSpeechProc(msg);
                    break;
                case Uti.CALENDAR:          // schedule
                    calendarProc(msg);
                    break;

                default:
                    break;
            }
        }

        /**
         * 音声認識の後処理
         */
        private void speechRecognitionProc(Message msg) {
            switch (msg.arg2) {
                case Uti.API_NOTIFY_RESULT:
                    String result = (String) msg.obj;
                    if (result != null) {
                        // 音声認識結果の表示
                        sListItemManager.setSpeechResult(result);
                        // 発話理解
                        startSpeechUnderstanding(result);
                    } else {
                        // 認識失敗
                        sListItemManager.setSpeechTimeOutError();
                        //画面下までスクロール
                        scrollToBottom();
                        // 音声合成出力
                        startTextToSpeech(sContext.getString(R.string.txt_error_speech_timout));
                    }
                    break;
                case Uti.API_NOTIFY_EVENT:
                    speechRecognitionEvent(msg.what);
                    break;
                case Uti.API_NOTIFY_ERROR:
                    sListItemManager.removeSpeechProgress();
                    // エラー表示
                    Toast.makeText(sContext, (String) msg.obj, Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }

        /**
         * 音声認識のイベント処理
         */
        private void speechRecognitionEvent(int what) {
            switch (what) {
                case Uti.STATE_NON://スタンバイ状態
                    //黒マイク画像
                    sSpeechRecgBtn.setImageResource(R.drawable.btn_mic);
                    break;
                case Uti.STATE_READY_FOR_SPEECH://音声認識開始(発話待ち)
                    //赤マイク画像
                    sSpeechRecgBtn.setImageResource(R.drawable.btn_rec);
                    sSpeechRecgBtn.setEnabled(true);
                    break;
                case Uti.STATE_BEGINNING_OF_SPEECH://発話開始
                    // 削除すべきアイテムを削除する。（はい/いいえボタン、地名一覧、電話帳）
                    sListItemManager.removeViews();
                    //音声認識中テキスト表示
                    sListItemManager.setSpeechProgress();
                    //画面下までスクロール
                    scrollToBottom();
                    break;
                case Uti.STATE_END_OF_SPEECH://発話終了
                    //黒マイク画像
                    sSpeechRecgBtn.setImageResource(R.drawable.btn_mic);
                    sSpeechRecognitionFlag = false;
                    break;
                case Uti.NOTIFY_RESULT://音声認識結果
                    break;
                case Uti.NOTIFY_CANCEL:
                    //黒マイク画像
                    sSpeechRecgBtn.setImageResource(R.drawable.btn_mic);
                    sSpeechRecognitionFlag = false;
                    sListItemManager.removeSpeechProgress();
                    break;
                default:
                    break;
            }
        }

        /**
         * 音声合成の後処理
         */
        private void textToSpeechProc(Message msg) {
            switch (msg.arg2) {
                case Uti.API_NOTIFY_RESULT:
                    break;
                case Uti.API_NOTIFY_EVENT:
                    switch(msg.what) {
                        case 0: // 停止
                            sTextToSpeechFlag = false;
                            break;
                        case 1: // 開始
                            sTextToSpeechFlag = true;
                            break;
                        default:
                            break;
                    }
                    break;
                case Uti.API_NOTIFY_ERROR:
                    sTextToSpeechFlag = false;
                    // エラー表示
                    Toast.makeText(sContext, (String) msg.obj, Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }

        /**
         * 雑談対話の後処理
         */
        private void dialogueProc(Message msg) {
            switch (msg.arg2) {
                case Uti.API_NOTIFY_RESULT:
                    DialogueResultData resultData = (DialogueResultData) msg.obj;
                    String utt = resultData.getUtt();
                    dContextID = resultData.getContext();
                    String yomi = resultData.getYomi();
                    if (utt != null && yomi != null) {
                        // 雑談対話結果の表示
                        sListItemManager.setInterpretationResultParam(utt, false);
                        // 画面下までスクロール
                        scrollToBottom();

                        startMotionAPI(yomi);
                        // 音声合成出力
                        startTextToSpeech(yomi);

                    }
                    break;
                case Uti.API_NOTIFY_EVENT:
                    break;
                case Uti.API_NOTIFY_ERROR:
                    // エラー表示
                    Toast.makeText(sContext, (String) msg.obj, Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }

        /**
         * scheduleの後処理
         */
        private void calendarProc(Message msg) {
            String textForSpeech = null;
            switch (msg.arg2) {
                case Uti.API_NOTIFY_RESULT:
                    Time required = (Time) msg.obj;

                    if (required != null && mCalendarID != -100) {
                        int numOfList = resList.size();
                        boolean picked = false;

                        if (numOfList != 0) {
                            for(int i=0;i<numOfList;i++) {
                                int pseudoI = numOfList - 1 - i;

                                CalendarEvent event = resList.get(pseudoI);
                                Time dtstart = new Time();
                                dtstart.set(event.dtstart);

                                if(dtstart.monthDay == required.monthDay && dtstart.month == required.month && dtstart.hour > required.hour) {
                                    textForSpeech = event.title;

                                    Time dtend = new Time();
                                    dtend.set(event.dtend);
                                    String fmt = "%H:%M";
                                    String msgHead = String.format("%1$sから%2$sまで", dtstart.format(fmt), dtend.format(fmt));
                                    textForSpeech = msgHead + textForSpeech + "の予定です";
                                    //
                                    sListItemManager.setInterpretationResultParam(textForSpeech, false);
                                    // 画面下までスクロール
                                    scrollToBottom();

                                    startMotionAPI(textForSpeech);
                                    // 音声合成出力
                                    startTextToSpeech(textForSpeech);
                                    picked = true;
                                }
                            }
                        }

                        if(!picked){
                            textForSpeech = "その日の予定は登録されていません";
                            //
                            sListItemManager.setInterpretationResultParam(textForSpeech, false);
                            // 画面下までスクロール
                            scrollToBottom();

                            startMotionAPI(textForSpeech);
                            // 音声合成出力
                            startTextToSpeech(textForSpeech);

                        }

                    }

                    if(mCalendarID == -100){
                        textForSpeech = "カレンダーを使うアカウントを選択してください";
                        //
                        sListItemManager.setInterpretationResultParam(textForSpeech, false);
                        // 画面下までスクロール
                        scrollToBottom();

                        startMotionAPI(textForSpeech);
                        // 音声合成出力
                        startTextToSpeech(textForSpeech);


                    }
                    break;
                case Uti.API_NOTIFY_EVENT:
                    break;
                case Uti.API_NOTIFY_ERROR:
                    // エラー表示
                    Toast.makeText(sContext, (String) msg.obj, Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }


        /**
         * 知識Q&Aの後処理
         */
        private void knowledgeProc(Message msg) {
            String textForSpeech = null;
            String textForDisplay = null;
            switch (msg.arg2) {
                case Uti.API_NOTIFY_RESULT:
                    KnowledgeResultData resultData = (KnowledgeResultData) msg.obj;
                    if (resultData != null) {
                        String code = resultData.getCode();
                        KnowledgeMessageData messageData = resultData.getMessage();
                        if (messageData != null) {
                            textForSpeech = messageData.getTextForSpeech();
                            textForDisplay = messageData.getTextForDisplay();
                        }
                        if (code.startsWith("S")) {
                            ArrayList<KnowledgeAnswerData> answerDatas = resultData.getAnswers();
                            StringBuffer strBuffer = new StringBuffer();

                            if (0 < answerDatas.size()) {
                                // 回答文テキスト(answerText)と引用元URL(linkUrl)を表示し、引用元URLを利用してユーザが引用元のページへ遷移できるようにする。
                                // レイアウトの TextView に android:autoLink="web" を追加する。
                                strBuffer.append(textForDisplay + "\n" + answerDatas.get(0).getAnswerText()
                                        + " " + answerDatas.get(0).getLinkUrl());
                                // 雑談対話結果の表示
                                sListItemManager.setInterpretationResultParam(textForDisplay, false);
                                // 画面下までスクロール
                                scrollToBottom();

                                startMotionAPI(textForSpeech);
                                // 音声合成出力
                                startTextToSpeech(textForSpeech);

                            }
                        } else {
                            // 雑談対話結果の表示
                            sListItemManager.setInterpretationResultParam(textForSpeech, false);
                            // 画面下までスクロール
                            scrollToBottom();

                            startMotionAPI(textForSpeech);
                            // 音声合成出力
                            startTextToSpeech(textForSpeech);

                        }
                    }
                    break;
                case Uti.API_NOTIFY_EVENT:
                    break;
                case Uti.API_NOTIFY_ERROR:
                    // エラー表示
                    Toast.makeText(sContext, (String) msg.obj, Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }

        /**
         * 発話理解の後処理
         */
        private void speechUnderstandingsProc(Message msg) {
            switch (msg.arg2) {
                case Uti.API_NOTIFY_RESULT:
                    SentenceResultData resultData = (SentenceResultData) msg.obj;
                    // 対話ステータス
                    SentenceDialogStatusData dialogStatus = resultData.getDialogStatus();
                    if (dialogStatus != null) {
                        // コマンド情報
                        SentenceCommandData command = dialogStatus.getCommand();
                        // コマンド情報の表示
//                        sCommandTv.setText(command.getCommandId() + ":" + command.getCommandName());
                        // ユーザ発話内容
                        SentenceUserUtteranceData utterance = resultData.getUserUtterance();
                        List<SentenceSlotStatusData> slotStatusList = dialogStatus.getSlotStatusDataList();


                        switch(command.getCommandId()) {
                            case "BK00101":
                                // 知識Q&A
                                startKnowledgeAPI(utterance.getUtteranceText());
                                break;
                            case "BT01302":
                                // schedule check
                                startCalendarManager(slotStatusList);
                                break;

                            default:
                                // 雑談対話
                                startDialogue(utterance.getUtteranceText());
                                break;
                        }
                    }
                    break;
                case Uti.API_NOTIFY_EVENT:
                    break;
                case Uti.API_NOTIFY_ERROR:
                    // エラー表示
                    Toast.makeText(sContext, (String) msg.obj, Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * カレンダー情報の取得
     */
    private ArrayList<CalendarEvent> getList(long calendarId) {
        ArrayList<CalendarEvent> list = new ArrayList<CalendarEvent>();
        String selection = CalendarContract.Events.CALENDAR_ID + " = ?";
        String[] selectionArgs = new String[] { Long.toString(calendarId) };
        Cursor c = getContentResolver().query(
                CalendarContract.Events.CONTENT_URI,
                new String[]{CalendarContract.Events._ID,
                        CalendarContract.Events.TITLE,
                        CalendarContract.Events.DESCRIPTION,
                        CalendarContract.Events.DTSTART,
                        CalendarContract.Events.EVENT_TIMEZONE,
                        CalendarContract.Events.DTEND,
                        CalendarContract.Events.EVENT_END_TIMEZONE,
                        CalendarContract.Events.ALL_DAY,}, selection,
                selectionArgs, CalendarContract.Events.DTSTART + " DESC");
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    do {
                        // @formatter:off
                        CalendarEvent bean = new CalendarEvent();
                        bean.id = c.getLong(c
                                .getColumnIndex(CalendarContract.Events._ID));
                        bean.title = c.getString(c
                                .getColumnIndex(CalendarContract.Events.TITLE));
                        bean.description = c
                                .getString(c
                                        .getColumnIndex(CalendarContract.Events.DESCRIPTION));
                        bean.dtstart = c
                                .getLong(c
                                        .getColumnIndex(CalendarContract.Events.DTSTART));
                        bean.eventTimezone = c
                                .getString(c
                                        .getColumnIndex(CalendarContract.Events.EVENT_TIMEZONE));
                        bean.dtend = c.getLong(c
                                .getColumnIndex(CalendarContract.Events.DTEND));
                        bean.eventEndTimezone = c
                                .getString(c
                                        .getColumnIndex(CalendarContract.Events.EVENT_END_TIMEZONE));
                        list.add(bean);
                        // @formatter:on
                    } while (c.moveToNext());
                }
            } finally {
                c.close();
            }
        }
        return list;
    }

    /**
     * カレンダー情報を入れ込む
     */
    private static class CalendarEvent {
        private long id;
        private String title;
        private String description;
        private long dtstart;
        private String eventTimezone;
        private long dtend;
        private String eventEndTimezone;
    }

    // camera 系
    private class OverlayListener implements SurfaceHolder.Callback
    {
        private SurfaceView surfaceView;
        private SurfaceHolder surfaceHolder;

        private Paint paint = new Paint();

        public OverlayListener(SurfaceView surfaceView) {
            this.surfaceView = surfaceView;
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            surfaceHolder = holder;
            surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            surfaceHolder = holder;
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // nop.
        }

        public void drawFace(Rect rect1, int color, Bitmap previewImage) {
            try {
                Canvas canvas = surfaceHolder.lockCanvas();
                if (canvas != null) {
                    try {
                        //canvas.drawBitmap(previewImage,0,0, paint);
                        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                        canvas.scale(
                                (float)surfaceView.getWidth()/previewImage.getWidth(),
                                (float)surfaceView.getHeight()/previewImage.getHeight());
                        paint.setColor(color);
                        canvas.drawRect(rect1, paint);
                    } finally {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }
            } catch (IllegalArgumentException e) {
//                Log.w(TAG, e.toString());
            }
        }

    }

    // from https://code.google.com/p/android/issues/detail?id=823
    private void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) {
        final int frameSize = width * height;

        for (int j = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++) {
                int srcp = j*width + i;
                int y = (0xff & ((int) yuv420sp[srcp])) - 16;
                if (y < 0) y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }

                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                if (r < 0) r = 0; else if (r > 262143) r = 262143;
                if (g < 0) g = 0; else if (g > 262143) g = 262143;
                if (b < 0) b = 0; else if (b > 262143) b = 262143;

                // 90度回転
                int xx = height-j-1;
                int yy = width-i-1;
                int dstp = yy * height + xx;
                rgb[dstp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
            }
        }
    }

}
