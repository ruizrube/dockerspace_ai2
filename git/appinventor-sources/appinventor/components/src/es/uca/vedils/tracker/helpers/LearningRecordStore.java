package es.uca.vedils.tracker.helpers;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.UUID;

import com.google.appinventor.components.runtime.Clock;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.util.YailList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import android.os.AsyncTask;
import es.uca.vedils.tracker.ExperienceTracker;
import es.uca.vedils.tracker.User;
import gov.adlnet.xapi.client.StatementClient;
import gov.adlnet.xapi.model.Account;
import gov.adlnet.xapi.model.Activity;
import gov.adlnet.xapi.model.ActivityDefinition;
import gov.adlnet.xapi.model.Actor;
import gov.adlnet.xapi.model.Agent;
import gov.adlnet.xapi.model.Attachment;
import gov.adlnet.xapi.model.Context;
import gov.adlnet.xapi.model.IStatementObject;
import gov.adlnet.xapi.model.Result;
import gov.adlnet.xapi.model.Statement;
import gov.adlnet.xapi.model.StatementReference;
import gov.adlnet.xapi.model.SubStatement;
import gov.adlnet.xapi.model.Verb;
import gov.adlnet.xapi.model.adapters.ActorAdapter;
import gov.adlnet.xapi.model.adapters.StatementObjectAdapter;

//Used Java Library for xAPI: https://github.com/adlnet/jxapi
public class LearningRecordStore {

	private ComponentContainer componentContainer;

	private ExperienceTracker currentActivityTracker;

	// LRS information
	private StatementClient client;
	protected Gson gson;

	// Record data when not internet access
	private TinyDB tinyDB;
	private int tagDB;

	private DataBatchTimerTask timerSendData;
	private Timer timer = new Timer();
	private GPSTracker gpsTracker;
	private String NAMESPACE_URI = "http://vedils.uca.es/xapi/";

	public LearningRecordStore(ExperienceTracker currentActivityTracker, ComponentContainer componentContainer) {
		this.currentActivityTracker = currentActivityTracker;
		this.componentContainer = componentContainer;
		this.tinyDB = new TinyDB(componentContainer.$context(),currentActivityTracker.getClass().toString());
		this.tagDB = 0;
		this.timerSendData = new DataBatchTimerTask(this);
		this.gpsTracker = new GPSTracker(componentContainer.$context());
	}

	public synchronized void activateTimer() {
		if (timer == null) {
			timer = new Timer();
			timer.schedule(timerSendData, 0, currentActivityTracker.BatchTime() * 1000);
		}

	}

	public synchronized void deactivateTimer() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}

	public Statement buildXAPIStatement(User actor, String verb, User actorObject, Result result) {

		Statement statement = buildStatement();
		statement.setActor(buildActor(actor));
		statement.setVerb(buildVerb(verb));
		statement.setObject(buildActor(actorObject));
		// EN ESTE TIPO DE STATEMENTS (actor-verbo-actor) no se debe incluir el campo platform
		statement.setContext(buildContext(false));
		if (result != null) {
			statement.setResult(result);
		}
		return statement;

	}

	public Statement buildXAPIStatement(User actor, String verb, String activityObject, Result result) {

		Statement statement = buildStatement();
		statement.setActor(buildActor(actor));
		statement.setVerb(buildVerb(verb));
		statement.setObject(buildActivity(activityObject));
		statement.setContext(buildContext(true));
		if (result != null) {
			statement.setResult(result);
		}
		return statement;
	}

	public Statement buildXAPIStatement(User actor, String verb, Statement statementObject, Result result) {

		Statement statement = buildStatement();
		statement.setActor(buildActor(actor));
		statement.setVerb(buildVerb(verb));
		statement.setObject(new StatementReference(statementObject.getId()));
		statement.setContext(buildContext(true));
		if (result != null) {
			statement.setResult(result);
		}

		return statement;
	}


	private IStatementObject buildActivity(String activityName) {
		Activity xapiActivity = new Activity();

		if (!activityName.contains("http")) {
			xapiActivity.setId(NAMESPACE_URI + "activities/" + activityName);
		} else {
			xapiActivity.setId(activityName);
		}

		ActivityDefinition xapiDefinition = new ActivityDefinition();
		xapiDefinition.setType(NAMESPACE_URI + "activities");
		xapiActivity.setDefinition(xapiDefinition);

//		
//		if (activityDescription.Extensions() != null) {
//			definition.setExtensions(buildExtensions(activityDescription.Extensions()));
//		}

		return xapiActivity;

	}

	private Context buildContext(boolean includePlatform) {

		HashMap<String, JsonElement> extensions = new HashMap<String, JsonElement>();
		extensions.put(NAMESPACE_URI + "context/appContext", buildContextData());

		Context context = new Context();
		if(includePlatform) {
			context.setPlatform(componentContainer.$context().getApplicationInfo().packageName);				
		}
		context.setExtensions(extensions);

		return context;
	}

	private Actor buildActor(User actor) {
		Agent xapiActor = new Agent();
		xapiActor.setName(actor.UserName() + " " + actor.Surname());
		xapiActor.setMbox("mailto:" + actor.Email().replaceAll(" ", ""));

		if (actor.ExternalAccountHomePage() != null && actor.ExternalAccountHomePage().length() > 0) {
			if (actor.ExternalAccountName() != null && actor.ExternalAccountName().length() > 0) {
				Account account = new Account();
				account.setHomePage(actor.ExternalAccountHomePage());
				account.setName(actor.ExternalAccountName());
				xapiActor.setAccount(account);
			}
		}
		return xapiActor;
	}

	private Verb buildVerb(String verb) {
		Verb xapiVerb = new Verb();
		if (!verb.contains("http")) {
			xapiVerb.setId(NAMESPACE_URI + "verbs/" + verb);
		} else {
			xapiVerb.setId(verb);
		}
		return xapiVerb;
	}

	private Statement buildStatement() {
		Statement statement = new Statement();
		statement.setId(UUID.randomUUID().toString());
		statement.setTimestamp(Clock.FormatDate(Clock.Now(), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
		statement.setStored(Clock.FormatDate(Clock.Now(), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));

		return statement;

	}

	private JsonObject buildContextData() {
		JsonObject contextData = new JsonObject();
		contextData.addProperty(NAMESPACE_URI + "context/IP", DeviceInfoFunctions
				.getCurrentIP(currentActivityTracker.OnlyWiFi(), this.componentContainer.$context()));
		contextData.addProperty(NAMESPACE_URI + "context/MAC",
				DeviceInfoFunctions.getMAC(componentContainer.$context()));
		// contextData.addProperty(NAMESPACE_URI + "context/IMEI",
		// DeviceInfoFunctions.getIMEI(componentContainer.$context()));
		contextData.addProperty(NAMESPACE_URI + "context/APILevel", DeviceInfoFunctions.getAndroidAPIVersion());
		contextData.addProperty(NAMESPACE_URI + "context/Latitude", gpsTracker.getLatitude());
		contextData.addProperty(NAMESPACE_URI + "context/Longitude", gpsTracker.getLongitude());
		contextData.addProperty(NAMESPACE_URI + "context/Date", Clock.FormatDate(Clock.Now(), "MM/dd/yyyy HH:mm:ss"));
		contextData.addProperty(NAMESPACE_URI + "context/AppID",
				componentContainer.$context().getApplicationInfo().packageName);
		contextData.addProperty(NAMESPACE_URI + "context/ScreenID", componentContainer.$form().getLocalClassName());
		return contextData;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void recordData(final Statement xAPIStatement) {
		System.out.println("xAPI statement: " + xAPIStatement.serialize().getAsJsonObject().toString());

		if (DeviceInfoFunctions.checkInternetConnection(componentContainer.$context())
				&& currentActivityTracker.RealTime()) {
			validateConnection();

			new AsyncTask() {
				@Override
				protected Object doInBackground(Object... arg0) {
					try {
						client.postStatement(xAPIStatement);
					} catch (Exception e) {
						e.printStackTrace();
						// currentActivityTracker.XAPIError(e.getMessage());

					}
					return null;
				}
			}.execute();
		} else {

//			System.out.println(
//					"Store value in TinyDB: " + xAPIStatement.toString() + " with TAG " + String.valueOf(tagDB));
			tinyDB.StoreValue(String.valueOf(tagDB), xAPIStatement.serialize().getAsJsonObject().toString());
			tagDB++;
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void recordDataBatch() {

		List<String> listTags = (List<String>) tinyDB.GetTags();
		final ArrayList<Statement> listStatements = new ArrayList<Statement>();

		if (DeviceInfoFunctions.checkInternetConnection(componentContainer.$context())) {

			validateConnection();

			Statement aux;
			for (String tagAux : listTags) {
		//		System.out.println("Getting value from TinyDB with TAG " + String.valueOf(tagAux));
				aux = this.getDecoder().fromJson(tinyDB.GetValue(tagAux, "").toString(), Statement.class);

				// refrescar el contexto?? la IP podría cambiarse pero no la fecha
				// // String myIP =
				// DeviceInfoFunctions.getCurrentIP(currentActivityTracker.OnlyWiFi(),
//				this.componentContainer.$context());
				// List<String> listValues = new ArrayList<String>();
				// listValues.add(tinyDB.GetValue(tagAux,
				// "").toString().replaceFirst("'0.0.0.0'", "'" + myIP + "'"));
				//aux.setContext(buildContext());

				if (aux != null) {
					listStatements.add(aux);
				}

			}

			new AsyncTask() {
				@Override
				protected Object doInBackground(Object... arg0) {
					try {
						client.postStatements(listStatements);
					} catch (Exception e) {
						e.printStackTrace();
						// currentActivityTracker.XAPIError(e.getMessage());
					}
					return null;
				}
			}.execute();

			tinyDB.ClearAll();
			tagDB = 0;
		}

	}

	private void validateConnection() {
		if (this.client == null) {
			try {
				System.out.println("LRS connection URL: " + currentActivityTracker.RecordStoreURL());
				System.out.println("LRS connection Token: " + currentActivityTracker.RecordStoreToken());

				this.client = new StatementClient(currentActivityTracker.RecordStoreURL(),
						currentActivityTracker.RecordStoreToken());
			} catch (MalformedURLException e) {
				e.printStackTrace();
				currentActivityTracker.XAPIError(e.getMessage());
			}
		}
	}

	private JsonObject buildExtensionsForResult(List<Object> extensions) {
		JsonObject element = new JsonObject();
		List<Object> pair = new ArrayList<Object>();

		if (extensions != null) {
			for (Object value : extensions) {
				if (value instanceof YailList) { // main YailList
					YailList list = (YailList) value;
					element.addProperty(NAMESPACE_URI + list.getString(0), list.getString(1));
				} else {
					pair.add(value);
				}
			}

			if (pair.size() >= 3) { // only main list with elements
				element.addProperty(NAMESPACE_URI + pair.get(1).toString(), pair.get(2).toString());
			}
		}

		return element;
	}

	private HashMap<String, JsonElement> buildExtensions(List<Object> extensions) {
		HashMap<String, JsonElement> extensionsMap = new HashMap<String, JsonElement>();

		JsonObject element = buildExtensionsForResult(extensions);

		if (element != null) {
			extensionsMap.put(NAMESPACE_URI + "extensions", element);
		}

		return extensionsMap;
	}

	protected Gson getDecoder() {
		if (gson == null) {
			GsonBuilder builder = new GsonBuilder();
			builder.registerTypeAdapter(Actor.class, new ActorAdapter());
			builder.registerTypeAdapter(IStatementObject.class, new StatementObjectAdapter());
			gson = builder.create();
		}
		return gson;
	}
}