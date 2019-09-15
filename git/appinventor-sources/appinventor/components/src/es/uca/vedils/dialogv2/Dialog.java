package es.uca.vedils.dialogv2;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.ActivityResultListener;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.Form;
import com.google.appinventor.components.runtime.PermissionResultHandler;
import com.google.appinventor.components.runtime.TextToSpeech;
import com.google.appinventor.components.runtime.collect.Maps;
import com.google.appinventor.components.runtime.util.OnInitializeListener;
import com.google.protobuf.Value;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.Manifest;

import com.google.cloud.dialogflow.v2beta1.DetectIntentRequest;
import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse;
import com.google.cloud.dialogflow.v2beta1.QueryInput;
import com.google.cloud.dialogflow.v2beta1.SessionName;
import com.google.cloud.dialogflow.v2beta1.SessionsClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.Locale;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse;
import com.google.cloud.dialogflow.v2beta1.QueryInput;
import com.google.cloud.dialogflow.v2beta1.SessionName;
import com.google.cloud.dialogflow.v2beta1.SessionsClient;
import com.google.cloud.dialogflow.v2beta1.SessionsSettings;
import com.google.cloud.dialogflow.v2beta1.TextInput;

import android.util.Log;

@UsesLibraries(libraries = "guava-28.0-android.jar,annotations-4.1.1.4.jar,commons-logging-1.2.jar,jackson-core-2.9.9.jar,opencensus-contrib-http-util-0.21.0.jar,grpc-grpclb-1.23.0.jar,google-http-client-jackson2-1.31.0.jar,javax.annotation-api-1.3.2.jar,httpcore-4.4.11.jar,grpc-okhttp-1.23.0.jar,google-cloud-core-grpc-1.90.0.jar,google-auth-library-credentials-0.17.1.jar,google-auth-library-oauth2-http-0.17.1.jar,grpc-context-1.23.0.jar,gax-grpc-1.48.1.jar,api-common-1.8.1.jar,protobuf-java-util-3.9.1.jar,error_prone_annotations-2.3.2.jar,auto-value-annotations-1.6.6.jar,grpc-api-1.23.0.jar,failureaccess-1.0.1.jar,grpc-stub-1.23.0.jar,proto-google-cloud-dialogflow-v2beta1-0.73.0.jar,proto-google-iam-v1-0.12.0.jar,grpc-netty-shaded-1.23.0.jar,proto-google-common-protos-1.16.0.jar,listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar,commons-codec-1.11.jar,protobuf-java-3.9.1.jar,okhttp-2.5.0.jar,grpc-auth-1.23.0.jar,grpc-alts-1.23.0.jar,google-cloud-dialogflow-0.108.0-alpha.jar,httpclient-4.5.9.jar,j2objc-annotations-1.3.jar,google-cloud-core-1.90.0.jar,gson-2.7.jar,checker-compat-qual-2.5.5.jar,threetenbp-1.3.3.jar,grpc-core-1.23.0.jar,opencensus-api-0.21.0.jar,google-http-client-beta.jar,animal-sniffer-annotations-1.17.jar,grpc-protobuf-1.23.0.jar,grpc-protobuf-lite-1.23.0.jar,jsr305-3.0.2.jar,opencensus-contrib-grpc-metrics-0.21.0.jar,okio-1.13.0.jar,proto-google-cloud-dialogflow-v2-0.73.0.jar,perfmark-api-0.17.0.jar,commons-lang3-3.5.jar,gax-1.48.1.jar")
@DesignerComponent(version = 201910910, description = "Component for using a conversational agent", category = ComponentCategory.EXTENSION, nonVisible = true, iconName = "images/speechRecognizer.png")
@UsesPermissions(permissionNames = "android.permission.INTERNET, android.permission.RECORD_AUDIO")
@SimpleObject(external = true)
public class Dialog extends AndroidNonvisibleComponent implements Component {

	private SessionsClient sessionsClient;
	private SessionName session;
	private String path;
	private String uuid = UUID.randomUUID().toString();
	private final ComponentContainer container;
	private SpeechRecognizerManager mSpeechManager;
	private Map<String, Value> fieldsMap;
	private String language;
	private static final Map<String, Locale> iso3LanguageToLocaleMap = Maps.newHashMap();

	public Dialog(ComponentContainer container) {
		super(container.$form());
		this.container = container;

	}

	/**
	 * Sets the language for this SpeechRecognizer component.
	 *
	 * @param language is the ISO2 (i.e. 2 letter) or ISO3 (i.e. 3 letter) language
	 *                 code to set this SpeechRecognizer component to.
	 */
	@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_TEXT_TO_SPEECH_LANGUAGES, defaultValue = Component.DEFAULT_VALUE_TEXT_TO_SPEECH_LANGUAGE)
	@SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Sets the language for Dialogflow.")
	public void Language(final String language) {
		
		Locale locale;
		switch (language.length()) {
		case 3:
			locale = iso3LanguageToLocale(language);
			this.language = locale.getISO3Language();
			break;
		case 2:
			locale = new Locale(language);
			this.language = locale.getLanguage();
			break;
		default:
			locale = Locale.getDefault();
			this.language = locale.getLanguage();
			break;
		}
	}

	/**
	 * Gets the language for this SpeechRecognizer component. This will be either an
	 * ISO2 (i.e. 2 letter) or ISO3 (i.e. 3 letter) code depending on which kind of
	 * code the property was set with.
	 *
	 * @return the language code for this SpeechRecognizer component.
	 */
	@SimpleProperty
	public String Language() {
		return language;
	}

	private static Locale iso3LanguageToLocale(String iso3Language) {
		Locale mappedLocale = iso3LanguageToLocaleMap.get(iso3Language);
		if (mappedLocale == null) {
			// Language codes should be lower case, but maybe the user doesn't know that.
			mappedLocale = iso3LanguageToLocaleMap.get(iso3Language.toLowerCase(Locale.ENGLISH));
		}
		return mappedLocale == null ? Locale.getDefault() : mappedLocale;
	}
	@SimpleProperty
	public String Credentials() {
		
		return path;

	}

	@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET, defaultValue = "")
	@SimpleProperty
	public void Credentials(String path) {
		
		this.path = path;
	}

	@SimpleFunction
	public void StartListening()

	{


		mSpeechManager = new SpeechRecognizerManager(container.$form(), new SpeechRecognizerManager.onResultsReady() {
			@Override
			public void onResults(ArrayList<String> results) {

				OnFinishListening(results.get(0).toString());
				mSpeechManager.destroy();

			}
		});

		
	}

	@SimpleFunction
	public void SendQuery(String query) {
		if(!query.equals("")) {
			
		String language=this.language+"-"+this.language.toUpperCase();
			Log.e("DIALOG", "info:  " + language);
		QueryInput queryInput = QueryInput.newBuilder()
				.setText(TextInput.newBuilder().setText(query).setLanguageCode(language)).build();
		new RequestTask(container.$form(), session, sessionsClient, queryInput).execute();
		}else 
		{
			Log.e("DIALOG", "error:  " + "NO TEXT QUERY");
		}
	}

	@SimpleFunction
	public void InitSession(String path) {


		try {

			InputStream stream = container.$form().getAssets().open(path);
			GoogleCredentials credentials = GoogleCredentials.fromStream(stream);
			String projectId = ((ServiceAccountCredentials) credentials).getProjectId();
			SessionsSettings.Builder settingsBuilder = SessionsSettings.newBuilder();
			SessionsSettings sessionsSettings = settingsBuilder
					.setCredentialsProvider(FixedCredentialsProvider.create(credentials)).build();
			sessionsClient = SessionsClient.create(sessionsSettings);
			session = SessionName.of(projectId, uuid);

		} catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));

			Log.e("DIALOG", "error:  " + "FAIL TO INIT DIALOGFLOW SESSION"+": "+errors.toString());
		}

	}

	public void getResponseData(DetectIntentResponse response) {
		String action = response.getQueryResult().getAction();
		String fulfillment = response.getQueryResult().getFulfillmentText();
		String query = response.getQueryResult().getQueryText();
		boolean hasParameters=response.getQueryResult().hasParameters();
		
		if(response.getQueryResult().hasParameters()) {
		fieldsMap = response.getQueryResult().getParameters().getFields();
		}

		OnResponse(action, fulfillment, query,hasParameters);

	}

	@SimpleFunction
	public String GetParameterFromResponse(String key) {
		
		String parameterValue="";
		try 
		{
			parameterValue=fieldsMap.get(key).getStringValue();
		}catch(Exception e) 
		{
			Log.e("DIALOG", "error:  " + "FAIL TO GET PARAMETER VALUE");
		}
		return parameterValue;
	}
	

	@SimpleEvent
	public void OnResponse(String action, String fulfillment, String query, boolean hasParameters) {
		EventDispatcher.dispatchEvent(this, "OnResponse", action, fulfillment, query, hasParameters);

	}
	@SimpleEvent
	public void OnFinishListening(String response) {
		EventDispatcher.dispatchEvent(this, "OnFinishListening",response);

	}

	public class RequestTask extends AsyncTask<Void, Void, DetectIntentResponse> {

		Activity activity;
		private SessionName session;
		private SessionsClient sessionsClient;
		private QueryInput queryInput;

		RequestTask(Activity activity, SessionName session, SessionsClient sessionsClient,
				QueryInput queryInput) {
			this.activity = activity;
			this.session = session;
			this.sessionsClient = sessionsClient;
			this.queryInput = queryInput;
		}

		@Override
		protected DetectIntentResponse doInBackground(Void... voids) {
			try {
				DetectIntentRequest detectIntentRequest = DetectIntentRequest.newBuilder()
						.setSession(session.toString()).setQueryInput(queryInput).build();
				return sessionsClient.detectIntent(detectIntentRequest);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(DetectIntentResponse response) {

			Log.e("RESPUESTA:",response.getQueryResult().getAction());
			getResponseData(response);
		}
	}

	

	

}