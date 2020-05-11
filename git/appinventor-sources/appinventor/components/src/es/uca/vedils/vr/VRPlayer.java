// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package es.uca.vedils.vr;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import android.Manifest;
import android.media.AudioManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.annotations.androidmanifest.ActionElement;
import com.google.appinventor.components.annotations.androidmanifest.ActivityElement;
import com.google.appinventor.components.annotations.androidmanifest.IntentFilterElement;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.*;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.google.appinventor.components.runtime.util.MediaUtil;
import es.uca.vedils.vr.helpers.VRPlayerActivity;
import es.uca.vedils.workflow.Workflow;
import es.uca.vedils.workflow.helpers.WorkflowLoader;


/**
 * Component for using the built in VoiceRecognizer to convert speech to text.
 * For more details, please see:
 * http://developer.android.com/reference/android/speech/RecognizerIntent.html
 *
 */

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
@DesignerComponent(version = 201910905, description = "VR360 Player", category = ComponentCategory.EXTENSION, nonVisible = true, iconName = "images/sharing.png")
@SimpleObject(external = true)

@UsesPermissions(permissionNames = "android.permission.READ_EXTERNAL_STORAGE,android.permission.WRITE_EXTERNAL_STORAGE")
public class VRPlayer extends AndroidNonvisibleComponent implements Component, ActivityResultListener {

	private final ComponentContainer container;
	private AudioManager audioManager;
	public String resultado="";
	private String youtubeVideoID = "";
	private String localVideoPath="";
	private long oldSecond=0;
	private long oldMinute=0;
	private long minutes=0;
	private long seconds=0;

	
	public BroadcastReceiver onClickEventBroadCastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			onClick();
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
				 onNewFrame(minutes, seconds);
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
	public void openVRPlayer(String videoSource)
	{
		Intent intent = new Intent(container.$context(), VRPlayerActivity.class);
		if(videoSource.contains("."))
		{

			intent.putExtra("mLocalVideoPath", localVideoPath);

		}else {

			intent.putExtra("mYoutubeVideoID", youtubeVideoID);

		}
		registerReceivers();
		container.$context().startActivityForResult(intent, 0);

	}
	@SimpleFunction(description = "Volume VRPlayer")
	public void setVolumeVRPlayer(int volume)
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
	}
	public void unregisterReceivers() 
	{
		LocalBroadcastManager.getInstance(container.$form()).unregisterReceiver(onClickEventBroadCastReceiver);
		LocalBroadcastManager.getInstance(container.$form()).unregisterReceiver(onNewFrameEventBroadCastReceiver);
	}
	@SimpleProperty
	public String YoutubeVideoID() {
		    return youtubeVideoID;
		    
		  }
	 @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_TEXT, defaultValue = "")
	 @SimpleProperty
	  public void YoutubeVideoID(String url) {
	    this.youtubeVideoID =url;
	  }



	@SimpleProperty(category = PropertyCategory.APPEARANCE)
	public String LocalVideo() {
		return localVideoPath;
	}

	@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET, defaultValue = "")
	@SimpleProperty
	public void LocalVideo(final String path) {

		if (MediaUtil.isExternalFile(path)
				&& container.$form().isDeniedPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
			container.$form().askPermission(Manifest.permission.READ_EXTERNAL_STORAGE, new PermissionResultHandler() {
				@Override
				public void HandlePermissionResponse(String permission, boolean granted) {
					if (granted) {
						LocalVideo(path);
					} else {
						container.$form().dispatchPermissionDeniedEvent(VRPlayer.this, "Definition", permission);
					}
				}
			});
			return;
		}

		localVideoPath = (path == null) ? "" : path;

	}




	 @SimpleFunction
	 public void pause() 
	 {
		 Intent pauseIntent = new Intent("es.uca.vedils.vr.helpers.VRActivity.pauseVideoPlayer");
			LocalBroadcastManager.getInstance(container.$context()).sendBroadcast(pauseIntent);
	 }
	 @SimpleFunction
	 public void play() 
	 {
		 Intent playIntent = new Intent("es.uca.vedils.vr.helpers.VRActivity.playVideoPlayer");
			LocalBroadcastManager.getInstance(container.$context()).sendBroadcast(playIntent);
	 }
	 @SimpleFunction
	 public void seekTo(long minutes, long seconds) 
	 {
		 long millis=TimeUnit.SECONDS.toMillis(seconds+(minutes*60));
		 Intent seektoIntent = new Intent("es.uca.vedils.vr.helpers.VRActivity.seektoVideoPlayer");
		 seektoIntent.putExtra("millis", millis);
			LocalBroadcastManager.getInstance(container.$context()).sendBroadcast(seektoIntent);
	 }
	 @SimpleFunction
	 public void closeVRPlayer() 
	 {
		 Intent closeVRPlayerIntent = new Intent("es.uca.vedils.vr.helpers.VRActivity.closeVideoPlayer");
			LocalBroadcastManager.getInstance(container.$context()).sendBroadcast(closeVRPlayerIntent);
	 }
	
	 @SimpleEvent
		public void onClick() {
			EventDispatcher.dispatchEvent(this, "onClick");
			
		}
	 @SimpleEvent
		public void onNewFrame(long minutes, long seconds) {
			EventDispatcher.dispatchEvent(this, "onNewFrame",minutes,seconds);
			
		}

	@Override
	public void resultReturned(int requestCode, int resultCode, Intent data) {
		
		unregisterReceivers();
		
	}

	
	
	
	 
	
	
}
