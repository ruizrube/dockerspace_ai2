// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package es.uca.vedils.vr;

import java.io.FileInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesActivities;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.annotations.UsesNativeLibraries;
import com.google.appinventor.components.annotations.UsesPermissions;
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
import com.google.appinventor.components.runtime.OnDestroyListener;
import com.google.appinventor.components.runtime.OnPauseListener;
import com.google.appinventor.components.runtime.OnResumeListener;
import com.google.appinventor.components.runtime.OnStopListener;
import com.google.appinventor.components.runtime.util.OnInitializeListener;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.SparseArray;
import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;
import es.uca.vedils.vr.helpers.VRPlayerActivity;


/**
 * Component for using the built in VoiceRecognizer to convert speech to text.
 * For more details, please see:
 * http://developer.android.com/reference/android/speech/RecognizerIntent.html
 *
 */

@UsesLibraries(libraries = "youtube.jar,libprotobuf-java-2.3-nano.jar")
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
	
	public String resultado="";
	public VRPlayerActivity hola;
	
	private String youtubeURL = "";
	
	public BroadcastReceiver onClickEventBroadCastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			onClick();
		}

	};
	public BroadcastReceiver onNewFrameEventBroadCastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			
			long currentPosition=intent.getExtras().getLong("currentPosition");

			 Log.e("APPINVENTOR", "position:  "+currentPosition);
			 
			 long minutes = TimeUnit.MILLISECONDS.toMinutes(currentPosition);
             long seconds = TimeUnit.MILLISECONDS.toSeconds(currentPosition);
             
             if(minutes>0) 
             {
            	 seconds=seconds-(minutes*60);
             }             
			onNewFrame(minutes,seconds);
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
	public void openVRPlayer() 
	{
		Intent intent = new Intent(container.$context(), VRPlayerActivity.class);
		intent.putExtra("mYoutubeLink", youtubeURL);
		registerReceivers();
		container.$context().startActivityForResult(intent,0);
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
	public String YoutubeURL() {
		    return youtubeURL;
		    
		  }
	 @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_TEXT, defaultValue = "")
	 @SimpleProperty
	  public void YoutubeURL(String url) {
	    this.youtubeURL=url;
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
