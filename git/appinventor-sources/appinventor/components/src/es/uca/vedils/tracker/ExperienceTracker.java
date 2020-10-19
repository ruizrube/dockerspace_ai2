package es.uca.vedils.tracker;

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
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.Form;
import com.google.appinventor.components.runtime.Notifier;

import android.util.Log;
import es.uca.vedils.tracker.helpers.LearningRecordStore;
import gov.adlnet.xapi.model.Result;
import gov.adlnet.xapi.model.Score;
import gov.adlnet.xapi.model.Statement;

/**
 * ActivityTracker Component
 * 
 * @author SPI-FM at UCA
 *
 */

@SimpleObject(external = true)
@DesignerComponent(version = 20201010, description = "Component for sending XAPI statements to a learning record store", category = ComponentCategory.EXTENSION, nonVisible = true, iconName = "images/tracker.png")
@UsesLibraries(libraries = "jxapi-2.0.1-jar-with-dependencies.jar")

@UsesPermissions(permissionNames = "android.permission.INTERNET, " + "android.permission.ACCESS_NETWORK_STATE,"
		+ "android.permission.WRITE_EXTERNAL_STORAGE, " + "android.permission.READ_EXTERNAL_STORAGE,"
		+ "android.permission.ACCESS_FINE_LOCATION," + "android.permission.ACCESS_COARSE_LOCATION,"
		+ "android.permission.ACCESS_MOCK_LOCATION," + "android.permission.SYSTEM_ALERT_WINDOW,"
		+ "android.permission.BIND_ACCESSIBILITY_SERVICE," + "android.permission.ACCESS_LOCATION_EXTRA_COMMANDS,"
		+ "android.permission.READ_PHONE_STATE")
public class ExperienceTracker extends AndroidNonvisibleComponent implements Component {

	private static final String LOG_TAG = "ExperienceTracker";

	private String encodedUsernamePassword;
	private String lrsEndpoint;
	private int batchTime;
	private boolean realTime;
	private boolean onlyWithWifi;
	private LearningRecordStore lrsManager;
	private Form form;

	public ExperienceTracker(ComponentContainer componentContainer) {
		super(componentContainer.$form());
		form = componentContainer.$form();
		lrsManager = new LearningRecordStore(this, componentContainer);

		this.lrsEndpoint = "http://vedilsanalytics.uca.es/data/xAPI";
		this.onlyWithWifi = false;
		this.batchTime = 0;
		RealTime(true);

	}

	/*
	 * Sets whether data are only sent with WIFI connection.
	 * 
	 * @param onlyWithWifi
	 */
	@DesignerProperty(alwaysSend = true, editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
	@SimpleProperty
	public void OnlyWiFi(boolean onlyWithWifi) {
		this.onlyWithWifi = onlyWithWifi;
	}

	public boolean OnlyWiFi() {
		return this.onlyWithWifi;
	}

	/*
	 * Sets whether data must be sent on real time.
	 * 
	 * @param onlyWithWifi
	 */
	@DesignerProperty(alwaysSend = true, editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
	@SimpleProperty
	public void RealTime(boolean realTime) {
		this.realTime = realTime;

		if (!realTime && BatchTime() > 0) {
			lrsManager.activateTimer();
		} else {
			lrsManager.deactivateTimer();
		}

	}

	public boolean RealTime() {
		return this.realTime;
	}

	/**
	 * Specifies the time interval (in seconds) to send a batch of statements to the
	 * data store. It applies when RealTime is false
	 * 
	 * @param bachTime
	 */
	@DesignerProperty(alwaysSend = true, editorType = PropertyTypeConstants.PROPERTY_TYPE_INTEGER, defaultValue = "0")
	@SimpleProperty()
	public void BatchTime(int batchTime) {
		this.batchTime = batchTime;

		if (!realTime && BatchTime() > 0) {
			lrsManager.activateTimer();
		} else {
			lrsManager.deactivateTimer();
		}
	}

	/**
	 * Returns the id of the current Fusion Table.
	 * 
	 * @return batchTime
	 */
	@SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Returns the current time interval (in seconds) to send a batch of statements to the data store.", userVisible = true)
	public int BatchTime() {
		return this.batchTime;

	}

	/**
	 * Specifies the encoded UsernameAndPassword for connecting to a LRS.
	 * 
	 * @param encodedUsernamePassword
	 */
	@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
	@SimpleProperty
	public void RecordStoreToken(String encodedUsernamePassword) {
		this.encodedUsernamePassword = encodedUsernamePassword;
	}

	/**
	 * Returns the id of the current Fusion Table.
	 * 
	 * @return encodedUsernamePassword
	 */
	@SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Returns the encoded UsernameAndPassword of the LRS", userVisible = true)
	public String RecordStoreToken() {
		return encodedUsernamePassword;
	}

	/**
	 * Specifies the URL of the LRS endpoint.
	 * 
	 * @param tableId
	 */
	@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "http://vedilsanalytics.uca.es/data/xAPI")
	@SimpleProperty(userVisible = false)
	public void RecordStoreURL(String lrsEndpoint) {
		this.lrsEndpoint = lrsEndpoint;
	}

	/**
	 * Returns the the URL of the LRS endpoint.
	 * 
	 * @return tableId
	 */
	@SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Returns the URL of the LRS endpoint", userVisible = true)
	public String RecordStoreURL() {
		return lrsEndpoint;
	}

	/**
	 * Function to create a xAPI statement for a user with a verb and an object
	 * 
	 */
	@SimpleFunction(description = "Create an xAPI statement for a user with a verb and an object.")
	public Object CreateStatement(User actor, String verb, Object object) {

		return CreateStatementWithResult(actor, verb, object, null);

	}

	/**
	 * Function to create a xAPI statement for a user with a verb, an object and a
	 * result
	 *
	 */
	@SimpleFunction(description = "Create an xAPI statement for a user with a verb, an object and a result.")
	public Object CreateStatementWithResult(User actor, String verb, Object object, Object result) {
		// AI2 no permite polimorfismo

		if (object instanceof User) {
			return lrsManager.buildXAPIStatement(actor, verb, (User) object, (Result) result);
		} else if (object instanceof String) {
			return lrsManager.buildXAPIStatement(actor, verb, (String) object, (Result) result);
		} else if (object instanceof Statement) {
			return lrsManager.buildXAPIStatement(actor, verb, (Statement) object, (Result) result);
		} else {
			return null;
		}

	}

	/**
	 * Function to create a result for later providing it to a xAPI statement
	 */
	@SimpleFunction(description = "Create a result for later providing it to a xAPI statement.")
	public Object CreateExperienceResult(boolean completion, String duration, boolean success, float maxScore,
			float minScore, float rawScore, float scaledScore) {

		Result result = new Result();

		result.setCompletion(completion);
		result.setSuccess(success);
		result.setDuration(duration);

		Score score = new Score();
		score.setScaled(scaledScore);
		score.setMax(maxScore);
		score.setMin(minScore);
		score.setRaw(rawScore);

		result.setScore(score);

		// result.setExtensions(buildExtensionsForResult(experienceResult.ResultExtensions()));

		return result;

	}

	/**
	 * Function to dispatch a previously created xAPI statement. The statement will
	 * be immediately sent if Realtime property is on. Otherwise, it will be saved
	 * on the local device and sent when the time interval defined is reached or
	 * when function sendPendingStatements is launched.
	 */
	@SimpleFunction(description = "Dispatch a previously created xAPI statement. The statement will be immediately sent if Realtime property is on. Otherwise, it will be saved on the local device and sent when the time interval defined is reached or when function sendPendingStatements is launched.")
	public void SendStatement(Object statement) {

		lrsManager.recordData((Statement) statement);

	}

	/**
	 * Function to send locally-stored statements to the learning record store.
	 */
	@SimpleFunction(description = "Send locally-stored statements to the learning record store")
	public void SendPendingStatements() {
		lrsManager.recordDataBatch();
	}

	/**
	 * Indicates that the communication with the LRS signaled an error.
	 *
	 * @param message the error message
	 */
	@SimpleEvent
	public void XAPIError(String message) {
		// Log the error message for advanced developers
		Log.e(LOG_TAG, message);

		// Invoke the application's "FirebaseError" event handler
		boolean dispatched = EventDispatcher.dispatchEvent(this, "XAPIError", message);
		if (!dispatched) {
			// If the handler doesn't exist, then put up our own alert
			Notifier.oneButtonAlert(form, message, "XAPIError", "Continue");
		}
	}

}