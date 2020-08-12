package es.uca.vedils.vr.helpers;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;



import java.io.IOException;
import java.util.List;

import com.google.vr.sdk.widgets.video.VrVideoEventListener;
import com.google.vr.sdk.widgets.video.VrVideoView;
import android.widget.LinearLayout;
import android.widget.Toast;

import android.net.Uri;
import es.uca.vedils.vr.model.YTMedia;
import es.uca.vedils.vr.model.YTSubtitles;
import es.uca.vedils.vr.model.YoutubeMeta;
import es.uca.vedils.vr.youtube.ExtractorException;
import es.uca.vedils.vr.youtube.YoutubeStreamExtractor;

public class VRPlayerActivity extends Activity {

    
    Bundle extras;

    private static final String STATE_PROGRESS = "state_progress";
    private static final String STATE_DURATION = "state_duration";

    private VrVideoView mVrVideoView;
    private boolean registersActive=false;
    private String mYoutubeVideoID = "";
    
    
    public BroadcastReceiver pauseVideoBroadCastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			mVrVideoView.pauseVideo();
			
			
		}

	};
	public BroadcastReceiver playVideoBroadCastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

		    mVrVideoView.playVideo();
			
			
		}

	};
	public BroadcastReceiver seektoVideoBroadCastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			long millis=intent.getExtras().getLong("millis");
			if(millis<mVrVideoView.getDuration()) {
		    mVrVideoView.seekTo(millis);
		    mVrVideoView.playVideo();
			}
			
		}

	};
    public BroadcastReceiver setVolumeBroadCastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            int volume=intent.getExtras().getInt("volume");

                mVrVideoView.setVolume(volume);



        }

    };
	public BroadcastReceiver closeVideoPlayerBroadCastReceiver = new BroadcastReceiver() {
        public void onReceive(final Context context, final Intent intent) {
            VRPlayerActivity.this.finish();
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        

        getExtraIntent();
        initViews();
        extractYoutubeUrl();
        
      

    }

    public void getExtraIntent() {
     
     this.extras = this.getIntent().getExtras();
     this.mYoutubeVideoID = this.extras.getString("mYoutubeVideoID");

    }
    private void registerReceivers() 
    {
        LocalBroadcastManager.getInstance((Context)this).registerReceiver(this.pauseVideoBroadCastReceiver, new IntentFilter("es.uca.vedils.vr.helpers.VRActivity.pauseVideoPlayer"));
        LocalBroadcastManager.getInstance((Context)this).registerReceiver(this.playVideoBroadCastReceiver, new IntentFilter("es.uca.vedils.vr.helpers.VRActivity.playVideoPlayer"));
        LocalBroadcastManager.getInstance((Context)this).registerReceiver(this.seektoVideoBroadCastReceiver, new IntentFilter("es.uca.vedils.vr.helpers.VRActivity.seektoVideoPlayer"));
        LocalBroadcastManager.getInstance((Context)this).registerReceiver(this.closeVideoPlayerBroadCastReceiver, new IntentFilter("es.uca.vedils.vr.helpers.VRActivity.closeVideoPlayer"));
        LocalBroadcastManager.getInstance((Context)this).registerReceiver(this.setVolumeBroadCastReceiver, new IntentFilter("es.uca.vedils.vr.helpers.VRActivity.setVolumeVideoPlayer"));

        registersActive=true;
    }
    private void unregisterReceivers() {
    	
    	 LocalBroadcastManager.getInstance((Context)this).unregisterReceiver(this.pauseVideoBroadCastReceiver);
         LocalBroadcastManager.getInstance((Context)this).unregisterReceiver(this.playVideoBroadCastReceiver);
         LocalBroadcastManager.getInstance((Context)this).unregisterReceiver(this.seektoVideoBroadCastReceiver);
         LocalBroadcastManager.getInstance((Context)this).unregisterReceiver(this.closeVideoPlayerBroadCastReceiver);
        LocalBroadcastManager.getInstance((Context)this).unregisterReceiver(this.setVolumeBroadCastReceiver);

         registersActive=false;
    	
    }
    
    private void initViews() {

        //Para crear un diseño de pantalla sin depender de un archivo xml, se debe hacer de la siguiente manera
        //primero creamos el layout donde iran todos los componentes de la pantalla
        LinearLayout layout=new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        //creamos los componentes que iran dentro del layout
        mVrVideoView= new VrVideoView(this);
        mVrVideoView.setDisplayMode(3);



        //añadimos los componentes dentro del layout
        layout.addView(mVrVideoView);
        

        //seleccionamos el layout como proveedor de elementos graficos de nuestro activity
        setContentView(layout);

        mVrVideoView.setEventListener(new ActivityEventListener());
        
        


    }
    @Override
    protected void onStart() {
        super.onStart();
        
        	registerReceivers();
        
      
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceivers();
        mVrVideoView.pauseRendering();
       
    }
    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceivers();
        mVrVideoView.shutdown();
       
       
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceivers();
        mVrVideoView.resumeRendering();

        	
        
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong(STATE_PROGRESS, mVrVideoView.getCurrentPosition());
        outState.putLong(STATE_DURATION, mVrVideoView.getDuration());

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        long progress = savedInstanceState.getLong(STATE_PROGRESS);

        mVrVideoView.seekTo(progress);

    }



    private class ActivityEventListener extends VrVideoEventListener {
        @Override
        public void onLoadSuccess() {
            super.onLoadSuccess();

        }

        @Override
        public void onLoadError(String errorMessage) {
            super.onLoadError(errorMessage);
        }

        @Override
        public void onClick() {
            super.onClick();
            final Intent onClickintent = new Intent("es.uca.vedils.vr.helpers.VRActivity.onClick");
            LocalBroadcastManager.getInstance(VRPlayerActivity.this).sendBroadcast(onClickintent);
        }

        @Override
        public void onDisplayModeChanged(int newDisplayMode) {
            super.onDisplayModeChanged(newDisplayMode);
        }

        @Override
        public void onCompletion() {
            super.onCompletion();
        }

        //synchornized este metodo para evitar que notifique dos veces el mismo tiempo transcurrido
        @Override
        public synchronized void onNewFrame() {
            super.onNewFrame();
//
//            Log.e("ONNEWFRAME", "position:  "+mVrVideoView.getCurrentPosition());
//            Log.e("ONNEWFRAME", "total:  "+mVrVideoView.getDuration());

            final Intent onNewFrameintent = new Intent("es.uca.vedils.vr.helpers.VRActivity.onNewFrame");
            onNewFrameintent.putExtra("currentPosition",mVrVideoView.getCurrentPosition());
            LocalBroadcastManager.getInstance(VRPlayerActivity.this).sendBroadcast(onNewFrameintent);

        }


    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    private void extractYoutubeUrl() {
        new YoutubeStreamExtractor(new YoutubeStreamExtractor.ExtractorListner(){

            @Override
            public void onExtractionDone(List<YTMedia> adativeStream, List<YTMedia> muxedStream, List<YTSubtitles> subList, YoutubeMeta meta) {


                if (adativeStream.isEmpty()) {

                    return;
                }
                if (muxedStream.isEmpty()) {

                    return;
                }
                String url="";
                try {
                    url= muxedStream.get(1).getUrl();
                }catch(Exception e)
                {
                    url= muxedStream.get(0).getUrl();
                }
                initVideo(url);


            }


            @Override
            public void onExtractionGoesWrong(final ExtractorException e) {

                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();


            }




        }).useDefaultLogin().Extract("https://youtu.be/"+mYoutubeVideoID);
    }

    private void initVideo(String url) {

        try {

            VrVideoView.Options options = new VrVideoView.Options();
            options.inputType = VrVideoView.Options.FORMAT_DEFAULT;
            mVrVideoView.loadVideo(Uri.parse(url), options);



            //mVrVideoView.loadVideoFromAsset("video.mp4", options);
        } catch( IOException e ) {
            //Handle exception
        }
    }
	}
