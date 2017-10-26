package com.firegroup.lanya;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.firegroup.lanya.FucUtil.FucUtil;
import com.firegroup.lanya.JsonParser.JsonParser;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.GrammarListener;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;

import static com.iflytek.cloud.SpeechConstant.ASR_NOMATCH_ERROR;
import static com.iflytek.cloud.SpeechConstant.VAD_BOS;
import static com.iflytek.cloud.SpeechConstant.VAD_EOS;

/**
 * Created by Froze on 2017/10/26.
 */

public class VoiceControl extends AppCompatActivity implements View.OnClickListener {
    private SpeechRecognizer mAsr;
    private Toast mToast;
    private String mCloudGrammar = null;
    private static final String KEY_GRAMMAR_ABNF_ID = "grammar_abnf_id";
    private static final String GRAMMAR_TYPE_ABNF = "abnf";
    private String GRAMMARID =null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToast = Toast.makeText(this,"",Toast.LENGTH_SHORT);
        Log.e("VoiceRec", "base creation");
        mAsr = SpeechRecognizer.createRecognizer(this,mInitListener);
        Log.e("VoiceRec", "set button listener");
        setContentView(R.layout.voice_control);
        if( null == mAsr ){
            // 创建单例失败，与 21001 错误为同样原因，参考 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=9688
            this.showTip( "创建对象失败，请确认成功调用 createUtility 进行初始化" );
            return;
        }
        else{
            Log.e("VoiceRec", "setParam");
            setParam();
            Log.e("VoiceRec", "finish setParam");
        }
        setButtonListener();
    }

    // initialize the layout; set the listener
    private void setButtonListener(){
        findViewById(R.id.voice_begin).setOnClickListener(this);
        findViewById(R.id.voice_end).setOnClickListener((this));
    }

    private InitListener mInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败,错误码："+code);
            }
        }
    };

    //temp var
    int ret = 0;
    String mContent;

    private boolean finish = true;
    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            // start rec
            case R.id.voice_begin:
                finish = false;
                ret = mAsr.startListening(mRecognizerListener);
                TextView textView = (TextView) findViewById(R.id.voice_content);
                textView.setText("Rec Result");
                if (ret != ErrorCode.SUCCESS) {
                    showTip("识别失败,错误码: " + ret);
                }
                break;

            // stop rec
            case R.id.voice_end:
                mAsr.cancel();
                finish = true;
                showTip("停止识别");
                break;
        }
    }

    private RecognizerListener mRecognizerListener = new RecognizerListener() {
        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            showTip("正在说话，音量大小：" + volume);
        }

        @Override
        public void onResult(final RecognizerResult result, boolean isLast) {
            if (null != result) {
                String text ;
                text = JsonParser.parseGrammarResult(result.getResultString());
                //display result
                TextView textView = (TextView) findViewById(R.id.voice_content);
                textView.append("\n");
                textView.append(text);
            } else {
                showTip("null result");
            }
            if(!finish){
                mAsr.cancel();
                ret = mAsr.startListening(mRecognizerListener);
                if (ret != ErrorCode.SUCCESS) {
                    showTip("新一轮识别失败,错误码: " + ret);
                }
//                else {
//                    showTip("new rec round");
//                }
            }
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            //showTip("结束说话");
//            if(!finish){
//                mAsr.stopListening();
//                mAsr.startListening(mRecognizerListener);
//            }
        }

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            //showTip("开始说话");
        }

        @Override
        public void onError(SpeechError error) {
            if(error.getErrorCode()==10703){
                mAsr.cancel();
                //showTip("loading grammar");
                ret = mAsr.startListening(mRecognizerListener);
                if (ret != ErrorCode.SUCCESS) {
                    showTip("识别失败,错误码: " + ret);
                }
            }
            else if(error.getErrorCode()!= 10119)
                showTip("onError Code："	+ error.getErrorCode());
//            if(!finish)
//                mAsr.startListening(mRecognizerListener);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
            ;
        }
    };

    public void setParam(){
        mCloudGrammar = FucUtil.readFile(this,"command.abnf","utf-8");
        mContent = new String(mCloudGrammar);
        mAsr.setParameter(VAD_EOS, "500");
        mAsr.setParameter(VAD_BOS, "3000");
        mAsr.setParameter(ASR_NOMATCH_ERROR,"false");
        mAsr.setParameter(SpeechConstant.TEXT_ENCODING,"utf-8");
        //设置识别引擎
        mAsr.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        //设置返回结果为json格式
        mAsr.setParameter(SpeechConstant.RESULT_TYPE, "json");
        mAsr.buildGrammar(GRAMMAR_TYPE_ABNF, mContent, mCloudGrammarListener);
        mAsr.setParameter(SpeechConstant.CLOUD_GRAMMAR, GRAMMARID);
        mAsr.setParameter( SpeechConstant.SUBJECT, "asr" );
        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mAsr.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");
        mAsr.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/asr.wav");
    }

    private GrammarListener mCloudGrammarListener = new GrammarListener() {
        @Override
        public void onBuildFinish(String grammarId, SpeechError error) {
            if(error == null){
                GRAMMARID= new String(grammarId);
                showTip("语法构建成功：" + grammarId);
            }else{
                showTip("语法构建失败,错误码：" + error.getErrorCode());
            }
        }
    };


    private void showTip(final String str){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mToast.setText(str);
                mToast.show();
            }
        });
    }
}
