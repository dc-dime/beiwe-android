package org.beiwe.app.session;

import org.beiwe.app.DebugInterfaceActivity;
import org.beiwe.app.ui.LoginActivity;
import org.beiwe.app.ui.RegisterActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
 

/**A class used to manage login sessions. Uses SharedPreferences in order to save
 * username-password combinations.
 * @author Dor Samet */

public class LoginSessionManager {
	private static int PRIVATE_MODE = 0;
	
	// Private things that are encapsulated using functions in this class 
	private static SharedPreferences pref; 
    private static Editor editor;
    private static Context appContext;
    
    public static final String PREF_NAME = "BeiwePref";
    private static final String IS_LOGIN = "IsLoggedIn";
   
    // Public names for when inspecting the user's details. Used to call from outside the class.
    private static final String KEY_ID = "uid";
    private static final String KEY_PASSWORD = "password";
	private static final String IS_REGISTERED = "IsRegistered";
    
	
	/**Constructor method for the session manager class
     * @param context */
	private LoginSessionManager(Context context){
        appContext = context;
        pref = appContext.getSharedPreferences(PREF_NAME, PRIVATE_MODE); //sets Shared Preferences private mode
        editor = pref.edit();
        editor.commit();
    }
	
	public static void initialize( Context context) { new LoginSessionManager(context); } 
	
	
	/*###########################################################################################
	##################################### Boolean checks ###########################################
	###########################################################################################*/
	
	/** Quick check for login. **/
    public static boolean isLoggedIn(){ return pref.getBoolean(IS_LOGIN, false); }
    public static boolean isRegistered() { return pref.getBoolean(IS_REGISTERED, false); }

	
	/*###########################################################################################
	##################################### Credentials ###########################################
	###########################################################################################*/
	
	public static void setRegistered(boolean value) { editor.putBoolean(IS_LOGIN, value); }
	
	public static void setPassword (String password) {
		editor.putString(KEY_PASSWORD, password);
		editor.commit();
	}
		
	
   /** Creates a new login session. Interacts with the SharedPreferences.
    * @param userID
    * @param password */
	//TODO: Eli. Rename this function, probably split functionality between this and setLoginCredenctials
    public static void setLoginCredentialsAndLogIn(String userID, String password){
    	setLoginCredentials(userID, password); //remove?
    	editor.putBoolean(IS_REGISTERED, true);
    	editor.putBoolean(IS_LOGIN, true);
    	editor.commit();
    }
    
    
    public static void setLoginCredentials( String userID, String password ) {
    	editor.putString(KEY_ID, userID);
        editor.putString(KEY_PASSWORD, password);
        editor.commit();
    }
    
    public static String getPassword() { return pref.getString( KEY_PASSWORD, "NULLID" ); }
    
    public static String getPatientID() { return pref.getString(KEY_ID, null); }
    
        
    /*###########################################################################################
	##################################### Log Out ###############################################
	###########################################################################################*/
    
    /**Clears session details and SharedPreferences.  Sends user to {@link LoginActivity} */
    public static void logoutUser(){
    	editor.putBoolean(IS_LOGIN, false);
        editor.commit();
        Intent intent = new Intent(appContext, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        appContext.startActivity(intent);
    }
    
    
	/** logs user out without forcing an activity change. */
	public static void logoutUserPassive() {
		editor.putBoolean(IS_LOGIN, false);
		editor.commit();
	}
	
	
	/*###########################################################################################
	##################################### Log In ################################################
	###########################################################################################*/
	
	//TODO: Eli.  make this... less... dumb?  rewrite it...?
    /**Checks which page the user should scroll to. If there was an active session, the user will
     * be transferred to {@link DebugInterfaceActivity}, otherwise if there was information saved in
     * SharedPreferences, the user will be transferred to {@link LoginActivity}. Otherwise, it is
     * the user's first time, therefore will start with {@link RegisterActivity}. */
    public static Intent login(){
    	Class debug = RegisterActivity.class;
//    	Class debug = LoginActivity.class;
//    	Class debug = DebugInterfaceActivity.class;
//    	Class debug = MainMenuActivity.class;
    	
    	//  What the hell does this log statement mean.
    	Log.i("LoginSessionManager", "Already logged in: " + isRegistered() );
    	
    	if(isLoggedIn()) {
    		// If already logged in, take user to the main menu screen
    		// TODO: postproduction. before launch, uncomment this line:
    		//Intent intent = new Intent(appContext, MainMenuActivity.class);
    		return new Intent(appContext, debug); }
    	else {
        	if (isRegistered()) {
        		// If not logged in, but has registered, take user to the login screen
        		return new Intent(appContext, LoginActivity.class); }
        	else {
        		// If not logged in and hasn't registered, take user to registration screen
            	Log.i("LoginSessionManager", "First time logged in");
            	// TODO: DEBUG CODE. uncomment this line:
            	//Intent intent = new Intent(appContext, RegisterActivity.class);
            	return new Intent(appContext, debug); }
        }
    }
	

}