package es.uca.vedils.tracker;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.ComponentContainer;

@SimpleObject(external = true)
@DesignerComponent(version = 20201010, description = "Component for managing user data", category = ComponentCategory.EXTENSION, nonVisible = true, iconName = "images/user.png")

public class User extends AndroidNonvisibleComponent implements Component {
	
	private String id;
	private String userName;
	private String surname;
	private String email;
		
	private String externalAccountName;
	private String externalAccountHomePage;
	
	
	public User(ComponentContainer componentContainer) {
		super(componentContainer.$form());
		this.id = "";
		this.userName = "";
		this.surname = "";
		this.email = "";
		this.externalAccountName = "";
		this.externalAccountHomePage = "";
	}
	
	/**
	 * Specifies the id of the user of the application.
	 * 
	 * @param id
	 */
	@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
		      defaultValue = "")
	@SimpleProperty
	public void Id(String id) {
		this.id = id;
	}
	
	
	/**
	 * Return the id of the user of the application.
	 * 
	 * Return id 
	 */
	@SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Return the id of the user of the application.", userVisible = true)
	public String Id() {
		return this.id;
	}
	
	/**
	 * Specifies the name of the user of the application.
	 * 
	 * @param name
	 */
	@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
		      defaultValue = "")
	@SimpleProperty
	public void UserName(String name) {
		this.userName = name;
	}
	
	
	/**
	 * Return the name of the user of the application.
	 * 
	 * Return name 
	 */
	@SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Return the name of the user of the application.", userVisible = true)
	public String UserName() {
		return this.userName;
	}
	
	
	/**
	 * Specifies the surname of the user of the application.
	 * 
	 * @param surname
	 */
	@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
		      defaultValue = "")
	@SimpleProperty
	public void Surname(String surname) {
		this.surname = surname;
	}
	
	/**
	 * Return the surname of the user of the application.
	 * 
	 * Return surname 
	 */
	@SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Return the surname of the user of the application.", userVisible = true)
	public String Surname() {
		return this.surname;
	}
	
	
	/**
	 * Specifies the e-mail of the user of the application.
	 * 
	 * @param email 
	 */
	@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
		      defaultValue = "")
	@SimpleProperty
	public void Email(String email) {
		this.email = email;
	}
	
	
	/**
	 * Return the e-mail of the user of the application.
	 * 
	 * Return email
	 */
	@SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Return the e-mail of the user of the application.", userVisible = true)
	public String Email() {
		return this.email;
	}
	
	
	
	
	/**
	 * Specifies the account name of the user on a external system.
	 * 
	 * @param externalAccountName
	 */
	@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
		      defaultValue = "")
	@SimpleProperty
	public void ExternalAccountName(String externalAccountName) {
		this.externalAccountName = externalAccountName;
	}
	
	/**
	 * Return the account name of the user on a external system.
	 * 
	 * Return externalAccountName
	 */
	@SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Return the account name of the user on a external system.", userVisible = true)
	public String ExternalAccountName() {
		return this.externalAccountName;
	}
	
	
	/**
	 * Specifies the home page of the external system where the user is registered.
	 * 
	 * @param externalAccountHomePage
	 */
	@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
		      defaultValue = "")
	@SimpleProperty
	public void ExternalAccountHomePage(String externalAccountHomePage) {
		this.externalAccountName = externalAccountHomePage;
	}
	
	/**
	 * Return Sets the home page of the external system where the user is registered.
	 * 
	 * Return externalAccountHomePage
	 */
	@SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Return the home page of the external system where the user is registered.", userVisible = true)
	public String ExternalAccountHomePage() {
		return this.externalAccountHomePage;
	}
	

	
	
	
}