// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package es.uca.vedils.vr;

import java.util.concurrent.TimeUnit;

import android.media.AudioManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.annotations.androidmanifest.ActionElement;
import com.google.appinventor.components.annotations.androidmanifest.ActivityElement;
import com.google.appinventor.components.annotations.androidmanifest.IntentFilterElement;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.ActivityResultListener;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import es.uca.vedils.vr.helpers.VRPlayerActivity;


@UsesLibraries(libraries = "rhino-1.7R4.jar,libprotobuf-java-2.3-nano.jar,gson-2.7.jar")
@UsesNativeLibraries(v8aLibraries = "libpano_video_renderer.so",
        v7aLibraries = "libpano_video_renderer.so",
        x86_64Libraries = "libpano_video_renderer.so")
@UsesActivities(activities = {
        @ActivityElement(name = "es.uca.vedils.vr.helpers.VRPlayerActivity",
                configChanges = "orientation|keyboardHidden",
                screenOrientation = "behind",
                intentFilters = {
                        @IntentFilterElement(actionElements = {
                                @ActionElement(name = "android.intent.action.MAIN")
                        })
                })
})
@DesignerComponent(version = 20200911, description = "VR360 Player", category = ComponentCategory.EXTENSION, nonVisible = true, iconName = "images/vrplayer.png")
@SimpleObject(external = true)

@UsesPermissions(permissionNames = "android.permission.READ_EXTERNAL_STORAGE,android.permission.WRITE_EXTERNAL_STORAGE")
public class VRPlayer extends AndroidNonvisibleComponent implements Component, ActivityResultListener {

    private final ComponentContainer container;
    private AudioManager audioManager;
    private String videoURL = "";
    private String videoLocalAsset = "";
    private long oldSecond = 0;
    private long minutes = 0;
    private long seconds = 0;
    private long end_interval_minutes = -1;
    private long end_interval_seconds = -1;
    private long start_interval_minutes = -1;
    private long start_interval_seconds = -1;
    private boolean isLocalVideo = false;
    private boolean isOpened = false;
    private boolean isLoaded = false;
    private long end_minutes = -1;
    private long end_seconds = -1;

    public BroadcastReceiver onLoadSuccessEventBroadCastReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
        @Override
        public void onReceive(Context context, Intent intent) {

            isLoaded = true;
            //cuando el video carga correctamente, recibo la duracion del mismo
            //asi soy capaz de determinar el instante en que acaba
            long duration = intent.getExtras().getLong("duration");

            //convierte en minutos y segundos el tiempo transcurrido de video

            end_minutes = TimeUnit.MILLISECONDS.toMinutes(duration);
            end_seconds = TimeUnit.MILLISECONDS.toSeconds(duration);

            //si los minutos son mayores que 0, se asegura que el numero de segundos es correcto
            if (end_minutes > 0) {
                end_seconds = end_seconds - (end_minutes * 60);
            }


        }

    };


    public BroadcastReceiver onClickEventBroadCastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            OnClick();
        }

    };


    public BroadcastReceiver onLoadErrorEventBroadCastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String errorMessage = intent.getExtras().getString("errorMessage");
            OnLoadError(errorMessage);

        }

    };
    public BroadcastReceiver onNewFrameEventBroadCastReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
        @Override
        public void onReceive(Context context, Intent intent) {

            long currentPosition = intent.getExtras().getLong("currentPosition");

            //convierte en minutos y segundos el tiempo transcurrido de video

            minutes = TimeUnit.MILLISECONDS.toMinutes(currentPosition);
            seconds = TimeUnit.MILLISECONDS.toSeconds(currentPosition);

            //si los minutos son mayores que 0, se asegura que el numero de segundos es correcto
            if (minutes > 0) {
                seconds = seconds - (minutes * 60);
            }
            //para evitar que se notifiquen dos veces el mismo segundo, se hace una comparativa entre el anterior y el nuevo notificado
            if (oldSecond != seconds) {

                if (end_minutes == minutes && end_seconds == seconds) {

                    OnCompletion();
                    //una vez lanzo el OnCompletion, devuelvo los valores de minutos y segundos al inicial
                    end_minutes = -1;
                    end_seconds = -1;
                }
                OnNewFrame(minutes, seconds);

                oldSecond = seconds;
            }
        }


    };


    /**
     * Creates a SpeechRecognizer component.
     *
     * @param container container, component will be placed in
     */
    public VRPlayer(ComponentContainer container) {
        super(container.$form());
        this.container = container;

    }

    @SimpleFunction(description = "Open VRPlayer")
    public void OpenVRPlayer() {
        Intent intent = new Intent(container.$context(), VRPlayerActivity.class);
        intent.putExtra("isLocalVideo", isLocalVideo);
        if (isLocalVideo) {
            intent.putExtra("mVideoLocalAsset", videoLocalAsset);
        } else {
            intent.putExtra("mVideoURL", videoURL);
        }

        registerReceivers();
        container.$context().startActivityForResult(intent, 0);
        isOpened = true;
    }

    /*@SimpleFunction(description = "Volume VRPlayer")
    public void VolumeVRPlayer(int volume) {
        audioManager = (AudioManager) container.$form().getSystemService(Context.AUDIO_SERVICE);
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float percent = volume * 0.01f;
        int percentVolume = (int) (maxVolume * percent);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, percentVolume, 0);
    }*/

    public void registerReceivers() {
        LocalBroadcastManager.getInstance(container.$form()).registerReceiver(onClickEventBroadCastReceiver,
                new IntentFilter("es.uca.vedils.vr.helpers.VRActivity.onClick"));
        LocalBroadcastManager.getInstance(container.$form()).registerReceiver(onNewFrameEventBroadCastReceiver,
                new IntentFilter("es.uca.vedils.vr.helpers.VRActivity.onNewFrame"));

        LocalBroadcastManager.getInstance(container.$form()).registerReceiver(onLoadErrorEventBroadCastReceiver,
                new IntentFilter("es.uca.vedils.vr.helpers.VRActivity.onLoadError"));

        LocalBroadcastManager.getInstance(container.$form()).registerReceiver(onLoadSuccessEventBroadCastReceiver,
                new IntentFilter("es.uca.vedils.vr.helpers.VRActivity.onLoadSuccess"));
    }

    public void unregisterReceivers() {
        LocalBroadcastManager.getInstance(container.$form()).unregisterReceiver(onClickEventBroadCastReceiver);
        LocalBroadcastManager.getInstance(container.$form()).unregisterReceiver(onNewFrameEventBroadCastReceiver);
        LocalBroadcastManager.getInstance(container.$form()).unregisterReceiver(onLoadErrorEventBroadCastReceiver);
        LocalBroadcastManager.getInstance(container.$form()).unregisterReceiver(onLoadSuccessEventBroadCastReceiver);


    }

    @SimpleProperty
    public String VideoURL() {
        return videoURL;

    }

    public void changeVideoSource(String source) {

        Log.e("VRPLAYER", "SOURCE:  " + source);
        Log.e("VRPLAYER", "ISOPENED:  " + isOpened);

        final Intent changeVideoSourceintent = new Intent("es.uca.vedils.vr.helpers.VRActivity.changeVideoSource");
        changeVideoSourceintent.putExtra("isLocalVideo", isLocalVideo);
        if (isLocalVideo) {
            changeVideoSourceintent.putExtra("mVideoLocalAsset", source);
        } else {
            changeVideoSourceintent.putExtra("mVideoURL", source);
        }
        LocalBroadcastManager.getInstance(container.$context()).sendBroadcast(changeVideoSourceintent);

    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_TEXT, defaultValue = "")
    @SimpleProperty
    public void VideoURL(String url) {

        //TODO AQUI ESTA LA MANTECA, FALLA EN LA SEGUNDA VUELTA

        Log.e("VRPLAYER", "ISOPENED:  " + isOpened);
        isLocalVideo = false;
        if (url != "") {

            if (isOpened) {

                changeVideoSource(url);

            }
            this.videoURL = url;


        }

    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET,
            defaultValue = "")
    @SimpleProperty(
            description = "The \"path\" to the video.  Usually, this will be the "
                    + "name of the video file, which should be added in the Designer.",
            category = PropertyCategory.BEHAVIOR)
    public void VideoLocal(String path) {

        if (path != "") {


            if (isOpened) {

                changeVideoSource(path);

            }

            isLocalVideo = true;
            videoLocalAsset = path;

        }

    }

    @SimpleProperty
    public String VideoLocal() {
        return videoLocalAsset;

    }

    @SimpleProperty
    public int Volume() {
        audioManager = (AudioManager) container.$form().getSystemService(Context.AUDIO_SERVICE);
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int currentVolumePercentage = 100 * currentVolume / maxVolume;
        return currentVolumePercentage;

    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_INTEGER, defaultValue = "50")
    @SimpleProperty
    public void Volume(int volume) {

        audioManager = (AudioManager) container.$form().getSystemService(Context.AUDIO_SERVICE);
        //int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float percent = volume * 0.01f;
        int percentVolume = (int) (maxVolume * percent);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, percentVolume, 0);

    }

    @SimpleProperty
    public boolean IsOpened() {

        return isOpened;

    }

    @SimpleFunction
    public void Pause() {
        Intent pauseIntent = new Intent("es.uca.vedils.vr.helpers.VRActivity.pauseVideoPlayer");
        LocalBroadcastManager.getInstance(container.$context()).sendBroadcast(pauseIntent);
    }

    @SimpleFunction
    public void Play() {
        Intent playIntent = new Intent("es.uca.vedils.vr.helpers.VRActivity.playVideoPlayer");
        LocalBroadcastManager.getInstance(container.$context()).sendBroadcast(playIntent);
    }

    @SimpleFunction
    public void SeekTo(long minutes, long seconds) {
        long millis = TimeUnit.SECONDS.toMillis(seconds + (minutes * 60));
        Intent seektoIntent = new Intent("es.uca.vedils.vr.helpers.VRActivity.seektoVideoPlayer");
        seektoIntent.putExtra("millis", millis);
        LocalBroadcastManager.getInstance(container.$context()).sendBroadcast(seektoIntent);
    }

    @SimpleFunction
    public void CloseVRPlayer() {
        isOpened = false;
        videoURL = "";
        videoLocalAsset = "";

        Intent closeVRPlayerIntent = new Intent("es.uca.vedils.vr.helpers.VRActivity.closeVideoPlayer");
        LocalBroadcastManager.getInstance(container.$context()).sendBroadcast(closeVRPlayerIntent);


    }

    @SimpleFunction
    public void PlayVideoInterval(long start_minutes, long start_seconds, long end_minutes, long end_seconds) {


        start_interval_minutes = start_minutes;
        start_interval_seconds = start_seconds;
        end_interval_minutes = end_minutes;
        end_interval_seconds = end_seconds;


        if (seconds > 0 && minutes >= 0) {
            SeekTo(start_interval_minutes, start_interval_seconds);
        }


    }

    @SimpleEvent
    public void OnClick() {
        EventDispatcher.dispatchEvent(this, "OnClick");

    }

    @SimpleEvent
    public void OnNewFrame(long minutes, long seconds) {


        //si quiero cargar un intervalo de video nada mas abrir el reproductor..
        //debo esperar un segundo
        if ((minutes == 0 && seconds == 1) && start_interval_seconds > 0) {
            SeekTo(start_interval_minutes, start_interval_seconds);
        }
        if (minutes == end_interval_minutes && seconds == end_interval_seconds) {

            OnCompletedVideoInterval();
        }


        EventDispatcher.dispatchEvent(this, "OnNewFrame", minutes, seconds);

    }

    @SimpleEvent
    public void OnCompletion() {
        EventDispatcher.dispatchEvent(this, "OnCompletion");

    }

    @SimpleEvent
    public void OnLoadError(String errorMessage) {
        EventDispatcher.dispatchEvent(this, "OnLoadError", errorMessage);

    }

    @SimpleEvent
    public void OnCompletedVideoInterval() {
        EventDispatcher.dispatchEvent(this, "OnCompletedVideoInterval");

    }

    @SimpleEvent
    public void OnLoadSuccess() {
        EventDispatcher.dispatchEvent(this, "OnLoadSuccess");

    }

    @Override
    public void resultReturned(int requestCode, int resultCode, Intent data) {

        unregisterReceivers();

    }


}
