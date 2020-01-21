package one.telefon.replacedialer;

import android.app.Activity;

import android.view.Window;
import android.view.WindowManager;
import android.os.Bundle;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.facebook.react.bridge.*;
import com.facebook.react.ReactActivity;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;


import android.telecom.TelecomManager;

//TODO : https://stackoverflow.com/questions/53411220/pass-activity-result-into-a-react-native-module
public class ReplaceDialerModule extends ReactContextBaseJavaModule /*implements ActivityEventListener*/ {
    ReactApplicationContext reactContext;
    
    private static Callback setCallback;

    private static String LOG = "one.telefon.replacedialer.ReplaceDialerModule";

    // for default dialer
    private TelecomManager telecomManager;
    private static final int RC_DEFAULT_PHONE = 3289;
    private static final int RC_PERMISSION = 3810;

    private static final int REQUEST_CODE_SET_DEFAULT_DIALER = 123;

    public ReplaceDialerModule(ReactApplicationContext context) {
        super(context);
        this.reactContext=context;
        this.reactContext.addActivityEventListener(this);
    }
    
    @Override
    public String getName() {
        return "ReplaceDialerModule";
    }

    @ReactMethod
    public void isDefault(Callback myCallback) {
        Log.w(LOG, "isDefault");
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M)
        {
            myCallback.invoke(true);
            return;
        }

        TelecomManager telecomManager = (TelecomManager) this.reactContext.getSystemService(Context.TELECOM_SERVICE);
    
        if (telecomManager.getDefaultDialerPackage() != this.reactContext.getPackageName()) 
            myCallback.invoke(false);
        else
            myCallback.invoke(true);
    }
    
    /*
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==REQUEST_CODE_SET_DEFAULT_DIALER) 
        {
            setCallback.invoke(resultCode);
        //checkSetDefaultDialerResult(resultCode)
        }
        if (requestCode==RC_DEFAULT_PHONE) 
        {
            setCallback.invoke(resultCode);
        //checkSetDefaultDialerResult(resultCode)
        }
    }
        
    @Override
    public void onNewIntent(Intent intent) {
  
    }
    */

    @ReactMethod
    public void setDefault(Callback myCallback) {
        Log.w(LOG, "setDefault "+this.reactContext.getPackageName());
        setCallback=myCallback;

        Intent intent = new Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER);
        intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, this.reactContext.getPackageName());
        this.reactContext.startActivityForResult(intent, RC_DEFAULT_PHONE,new Bundle());
          
        myCallback.invoke(true);
        // startActivityForResult(intent, REQUEST_CODE_SET_DEFAULT_DIALER); //Different
          // code
          // Huawei/ honor : ??? manual ??? startActivityForResult(new
          // Intent(android.provider.Settings.ACTION_SETTINGS), 0);
    }
}






