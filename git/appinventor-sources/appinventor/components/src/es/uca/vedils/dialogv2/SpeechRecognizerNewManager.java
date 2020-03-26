package es.uca.vedils.dialogv2;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;

@RequiresApi(api = Build.VERSION_CODES.FROYO)
public class SpeechRecognizerNewManager implements RecognitionListener {

    private SpeechRecognizer speech;
    private Intent intent;
    private Context mContext;
    private boolean oneResult=true;

    public SpeechRecognizerNewManager(Context context, String language) {

        mContext=context;
        speech=SpeechRecognizer.createSpeechRecognizer(context);
        speech.setRecognitionListener(this);
        intent = new Intent(RecognizerIntent.ACTION_VOICE_SEARCH_HANDS_FREE);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,language);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,context.getPackageName());



    }

    public void startListening()
    {
        //todo poner una limitacion a que no se ejecute el startListening mientras haya otra en curso
        oneResult=true;
        speech.startListening(intent);
    }
    public void destroyObject()
    {
        speech.cancel();
        speech.destroy();
    }

    @Override
    public void onReadyForSpeech(Bundle params) {

    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onRmsChanged(float rmsdB) {

    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onError(int error) {

        final Intent onErrorIntent;


        switch (error)
        {

            case SpeechRecognizer.ERROR_NO_MATCH:

                onErrorIntent = new Intent("es.uca.vedils.dialogv2.SpeechRecognizerManager.onError");
                onErrorIntent.putExtra("errorType","ERROR_NO_MATCH");
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(onErrorIntent);
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:

                onErrorIntent = new Intent("es.uca.vedils.dialogv2.SpeechRecognizerManager.onError");
                onErrorIntent.putExtra("errorType","ERROR_RECOGNIZER_BUSY");
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(onErrorIntent);
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:

                onErrorIntent = new Intent("es.uca.vedils.dialogv2.SpeechRecognizerManager.onError");
                onErrorIntent.putExtra("errorType","ERROR_SPEECH_TIMEOUT");
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(onErrorIntent);
                break;


        }


    }

    @Override
    public void onResults(Bundle results) {



        //debido a que el onResults salta varias veces durante el reconocimiento de voz, solo capturo la primera de las veces que salta en cada escucha

        if(oneResult){
        ArrayList<String> matches = results.getStringArrayList(
                SpeechRecognizer.RESULTS_RECOGNITION);

        final Intent onResults = new Intent("es.uca.vedils.dialogv2.SpeechRecognizerManager.onResults");
        onResults.putExtra("speechResult",matches.get(0));
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(onResults);
            oneResult=false;

        }

    }

    @Override
    public void onPartialResults(Bundle partialResults) {

    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }
}
