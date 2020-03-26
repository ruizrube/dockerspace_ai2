package es.uca.vedils.dialogv2;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;

import es.uca.vedils.dialogv2.Dialog;
import es.uca.vedils.vr.helpers.VRPlayerActivity;

/**
 * Created by yoda on 16/11/15.
 * Modified by rubarez on 24/03/20
 */
public class SpeechRecognizerManager {

    protected AudioManager mAudioManager;
    protected SpeechRecognizer mSpeechRecognizer;
    protected Intent mSpeechRecognizerIntent;
    protected Context mContext;
    protected boolean mIsListening;
    private boolean mIsStreamSolo;


    private boolean mMute=false;



    private final static String TAG="SpeechRecognizerManager";

    private onResultsReady mListener;



    @RequiresApi(api = Build.VERSION_CODES.FROYO)
    public SpeechRecognizerManager(Context context, onResultsReady listener)
    {
        mContext=context;
        try{
            mListener=listener;
        }
        catch(ClassCastException e)
        {
            Log.e(TAG,e.toString());
        }
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        mSpeechRecognizer.setRecognitionListener(new SpeechRecognitionListener());
        mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                context.getPackageName());
        startListening();

    }

    @RequiresApi(api = Build.VERSION_CODES.FROYO)
    private void listenAgain()
    {
        if(mIsListening) {
            mIsListening = false;
            mSpeechRecognizer.cancel();
            startListening();
        }
    }


    private void startListening()
    {
        if(!mIsListening)
        {
            mIsListening = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                // turn off beep sound
                if (!mIsStreamSolo && mMute) {
                    mAudioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
                    mAudioManager.setStreamMute(AudioManager.STREAM_ALARM, true);
                    mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
                    mAudioManager.setStreamMute(AudioManager.STREAM_RING, true);
                    mAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
                    mIsStreamSolo = true;
                }
            }
            mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
        }
    }

    public void destroy()
    {
        mIsListening=false;
        if (!mIsStreamSolo) {
            mAudioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
            mAudioManager.setStreamMute(AudioManager.STREAM_ALARM, false);
            mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
            mAudioManager.setStreamMute(AudioManager.STREAM_RING, false);
            mAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, false);
            mIsStreamSolo = true;
        }
        Log.d(TAG, "onDestroy");
        if (mSpeechRecognizer != null)
        {
            mSpeechRecognizer.stopListening();
            mSpeechRecognizer.cancel();
            mSpeechRecognizer.destroy();
            mSpeechRecognizer=null;
        }

    }

    protected class SpeechRecognitionListener implements RecognitionListener
    {

        @Override
        public void onBeginningOfSpeech() {}

        @Override
        public void onBufferReceived(byte[] buffer)
        {

        }

        @Override
        public void onEndOfSpeech()
        {}

        @Override
        public synchronized void onError(int error)
        {

            /*if(error==SpeechRecognizer.ERROR_RECOGNIZER_BUSY)
            {

                final Intent onErrorIntent = new Intent("es.uca.vedils.dialogv2.SpeechRecognizerManager.onError");
                onErrorIntent.putExtra("errorType","ERROR_RECOGNIZER_BUSY");
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(onErrorIntent);

                /*if(mListener!=null) {
                    ArrayList<String> errorList=new ArrayList<String>(1);
                    errorList.add("ERROR RECOGNIZER BUSY");
                    if(mListener!=null)
                        mListener.onResults(errorList);
                }*/

                //return;
            //}

            if(error==SpeechRecognizer.ERROR_NO_MATCH)
            {
                final Intent onErrorIntent = new Intent("es.uca.vedils.dialogv2.SpeechRecognizerManager.onError");
                onErrorIntent.putExtra("errorType","ERROR NO MATCH");
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(onErrorIntent);

                /*if(mListener!=null) {
                	
                	 ArrayList<String> errorList=new ArrayList<String>(1);
                     errorList.add("ERROR NO MATCH");
                     mListener.onResults(errorList);


                }*/
            }

            if(error==SpeechRecognizer.ERROR_NETWORK)
            {
                final Intent onErrorIntent = new Intent("es.uca.vedils.dialogv2.SpeechRecognizerManager.onError");
                onErrorIntent.putExtra("errorType","ERROR_NETWORK");
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(onErrorIntent);

               /* ArrayList<String> errorList=new ArrayList<String>(1);
                errorList.add("STOPPED LISTENING");
                if(mListener!=null)
                    mListener.onResults(errorList);*/
            }
            Log.d(TAG, "error = " + error);
            /*new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    listenAgain();
                }
            },100);*/


        }

        @Override
        public void onEvent(int eventType, Bundle params)
        {

        }

        @Override
        public void onPartialResults(Bundle partialResults)
        {

        }

        @Override
        public void onReadyForSpeech(Bundle params) {}

        @Override
        public void onResults(Bundle results)
        {
            if(results!=null && mListener!=null) {
                mListener.onResults(results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION));
            }else
                {
                    //listenAgain();
                }


        }

        @Override
        public void onRmsChanged(float rmsdB) {}

    }

    public boolean ismIsListening() {
        return mIsListening;
    }


    public interface onResultsReady
    {
        public void onResults(ArrayList<String> results);
    }

    public void mute(boolean mute)
    {
        mMute=mute;
    }

    public boolean isInMuteMode()
    {
        return mMute;
    }

}