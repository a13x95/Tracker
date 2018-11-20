package helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
/*
** This class maintains session data across the app using SharedPreferences. We store a boolean flag isLoggedIn in shared preferences to check the login status.
 */
public class SessionManager
{
    //LogCat tag
    private static String TAG = SessionManager.class.getSimpleName();

    //Shared Preferences
    SharedPreferences preferences;

    Editor editor;
    Context _context;

    //Shared preferences mode
    int PRIVATE_MODE = 0;

    //Shared Preferences file name
    private static final String PREF_NAME = "TrackerLogin";

    private static final  String KEY_IS_LOGGEDIN= "isLoggedIn";

    public SessionManager(Context context){
        this._context = context;
        preferences = _context.getSharedPreferences(PREF_NAME,PRIVATE_MODE);
        editor = preferences.edit();
    }

    public void SetLoggin(boolean isLoggedIn){
        editor.putBoolean(KEY_IS_LOGGEDIN, isLoggedIn);
        //commit changes
        editor.commit();
    }

    public boolean isLoggedIn(){
        return preferences.getBoolean(KEY_IS_LOGGEDIN,false);
    }
}
