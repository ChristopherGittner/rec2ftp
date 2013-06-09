package org.gittner.rec2ftp.statics;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SettingsManager {
    static SharedPreferences prefs = null;

    public static void init(Context context){
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static String getUsername(){
        try{
            return prefs.getString("pref_username", "");
        }
        catch (ClassCastException e){
            return "";
        }
    }

    public static String getPassword(){
        try{
            return prefs.getString("pref_password", "");
        }
        catch (ClassCastException e){
            return "";
        }
    }

    public static String getHost(){
        try{
            return prefs.getString("pref_host", "");
        }
        catch (ClassCastException e){
            return "";
        }
    }

    public static int getPort(){
        try{
            return Integer.parseInt(prefs.getString("pref_port", "21"));
        }
        catch (ClassCastException e){
            return 21;
        }
    }

    public static String getDirectory(){
        try{
            return prefs.getString("pref_directory", "");
        }
        catch (ClassCastException e){
            return "";
        }
    }

    public static boolean IsFirstLaunch(){
        try{
            return prefs.getBoolean("pref_first_launch", true);
        }
        catch (ClassCastException e){
            return true;
        }
    }

    public static void setFirstLaunch(boolean firstLaunch){
        SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean("pref_first_launch", firstLaunch);

        editor.commit();
    }

    public static String getRights(){
        try{
            return prefs.getString("pref_rights", "");
        }
        catch (ClassCastException e){
            return "";
        }
    }

    public static String getFilePrefix(){
        try{
            return prefs.getString("pref_file_prefix", "");
        }
        catch (ClassCastException e){
            return "";
        }
    }

    public static boolean getAppendIndex(){
        try{
            return prefs.getBoolean("pref_append_index", true);
        }
        catch (ClassCastException e){
            return true;
        }
    }

    public static boolean getVideoCaptureTweak() {
        try{
            return prefs.getBoolean("pref_record_video_tweak", false);
        }
        catch (ClassCastException e){
            return true;
        }
    }

    public static boolean getRemoveSuccessfullUploads() {
        try{
            return prefs.getBoolean("pref_remove_successfull_uploads", false);
        }
        catch (ClassCastException e){
            return false;
        }
    }

    public static String getAutorunOption() {
        try{
            return prefs.getString("pref_autorun", "off");
        }
        catch (ClassCastException e){
            return "off";
        }
    }
}
