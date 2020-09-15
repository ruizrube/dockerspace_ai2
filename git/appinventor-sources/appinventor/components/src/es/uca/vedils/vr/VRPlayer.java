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
@UsesNativeLibraries(v8aLibraries = "libpano_video_renderer.so" ,
v7aLibraries = "libpano_video_renderer.so",
x86_64Libraries="libpano_video_renderer.so")
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
@DesignerComponent(version = 20200911, description = "VR360 Player", category = ComponentCategory.EXTENSION, nonVisible = true, iconName = "images/sharing.png")
@SimpleObject(external = true)

@UsesPermissions(permissionNames = "android.permission.READ_EXTERNAL_STORAGE,android.permission.WRITE_EXTERNAL_STORAGE")
public class VRPlayer extends AndroidNonvisibleComponent implements Component, ActivityResultListener {

	private final ComponentContainer container;
	private AudioManager audioManager;
	private String videoURL = "";
	private String videoLocalAsset="";
	private long oldSecond=0;
	private long minutes=0;
	private long seconds=0;
	private boolean isLocalVideo=false;

	
	public BroadcastReceiver onClickEventBroadCastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			OnClick();
		}

	};
	public BroadcastReceiver onCompletionEventBroadCastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			OnCompletion();
		}

	};
	public BroadcastReceiver onNewFrameEventBroadCastReceiver = new BroadcastReceiver() {
		@RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
		@Override
		public void onReceive(Context context, Intent intent) {
			
			long currentPosition=intent.getExtras().getLong("currentPosition");

			//convierte en minutos y segundos el tiempo transcurrido de video

			  minutes = TimeUnit.MILLISECONDS.toMinutes(currentPosition);
              seconds = TimeUnit.MILLISECONDS.toSeconds(currentPosition);

              //si los minutos son mayores que 0, se asegura que el numero de segundos es correcto
             if(minutes>0) 
             {
            	 seconds=seconds-(minutes*60);
             }
             //para evitar que se notifiquen dos veces el mismo segundo, se hace una comparativa entre el anterior y el nuevo notificado
             if(oldSecond!=seconds) {
				 OnNewFrame(minutes, seconds);
				 oldSecond=seconds;
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
	public void OpenVRPlayer()
	{
		Intent intent = new Intent(container.$context(), VRPlayerActivity.class);
		intent.putExtra("isLocalVideo", isLocalVideo);
		if(isLocalVideo){
			intent.putExtra("mVideoLocalAsset", videoLocalAsset);
		}else{
			intent.putExtra("mVideoURL", videoURL);
		}

		registerReceivers();
		container.$context().startActivityForResult(intent,0);
	}
	@SimpleFunction(description = "Volume VRPlayer")
	public void VolumeVRPlayer(int volume)
	{
		audioManager = (AudioManager)container.$form().getSystemService(Context.AUDIO_SERVICE);
		int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		float percent = volume*0.01f;
		int percentVolume = (int) (maxVolume*percent);
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, percentVolume, 0);
	}
	public void registerReceivers() 
	{
		LocalBroadcastManager.getInstance(container.$form()).registerReceiver(onClickEventBroadCastReceiver,
				new IntentFilter("es.uca.vedils.vr.helpers.VRActivity.onClick"));
		LocalBroadcastManager.getInstance(container.$form()).registerReceiver(onNewFrameEventBroadCastReceiver,
				new IntentFilter("es.uca.vedils.vr.helpers.VRActivity.onNewFrame"));
		LocalBroadcastManager.getInstance(container.$form()).registerReceiver(onCompletionEventBroadCastReceiver,
				new IntentFilter("es.uca.vedils.vr.helpers.VRActivity.onCompletion"));
	}
	public void unregisterReceivers() 
	{
		LocalBroadcastManager.getInstance(container.$form()).unregisterReceiver(onClickEventBroadCastReceiver);
		LocalBroadcastManager.getInstance(container.$form()).unregisterReceiver(onNewFrameEventBroadCastReceiver);
		LocalBroadcastManager.getInstance(container.$form()).unregisterReceiver(onCompletionEventBroadCastReceiver);
	}

	@SimpleProperty
	public String VideoURL() {
		    return videoURL;
		    
		  }
	 @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_TEXT, defaultValue = "")
	 @SimpleProperty
	  public void VideoURL(String url) {

		if(url!=""){

			this.videoURL =url;
			isLocalVideo=false;

		}

	  }

	@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET,
			defaultValue = "")
	@SimpleProperty(
			description = "The \"path\" to the video.  Usually, this will be the "
					+ "name of the video file, which should be added in the Designer.",
			category = PropertyCategory.BEHAVIOR)
	public void VideoLocal(String path) {

		if(path!=""){
			isLocalVideo=true;
			videoLocalAsset=path;

		}

	}
	 @SimpleFunction
	 public void Pause()
	 {
		 Intent pauseIntent = new Intent("es.uca.vedils.vr.helpers.VRActivity.pauseVideoPlayer");
			LocalBroadcastManager.getInstance(container.$context()).sendBroadcast(pauseIntent);
	 }
	 @SimpleFunction
	 public void Play()
	 {
		 Intent playIntent = new Intent("es.uca.vedils.vr.helpers.VRActivity.playVideoPlayer");
			LocalBroadcastManager.getInstance(container.$context()).sendBroadcast(playIntent);
	 }
	 @SimpleFunction
	 public void SeekTo(long minutes, long seconds)
	 {
		 long millis=TimeUnit.SECONDS.toMillis(seconds+(minutes*60));
		 Intent seektoIntent = new Intent("es.uca.vedils.vr.helpers.VRActivity.seektoVideoPlayer");
		 seektoIntent.putExtra("millis", millis);
			LocalBroadcastManager.getInstance(container.$context()).sendBroadcast(seektoIntent);
	 }
	 @SimpleFunction
	 public void CloseVRPlayer()
	 {
		 Intent closeVRPlayerIntent = new Intent("es.uca.vedils.vr.helpers.VRActivity.closeVideoPlayer");
			LocalBroadcastManager.getInstance(container.$context()).sendBroadcast(closeVRPlayerIntent);
	 }
	
	 @SimpleEvent
		public void OnClick() {
			EventDispatcher.dispatchEvent(this, "OnClick");
			
		}
	 @SimpleEvent
		public void OnNewFrame(long minutes, long seconds) {
			EventDispatcher.dispatchEvent(this, "OnNewFrame",minutes,seconds);
			
		}
	@SimpleEvent
	public void OnCompletion() {
		EventDispatcher.dispatchEvent(this, "OnCompletion");

	}

	@Override
	public void resultReturned(int requestCode, int resultCode, Intent data) {
		
		unregisterReceivers();
		
	}

	
	
	
	 
	
	
}
