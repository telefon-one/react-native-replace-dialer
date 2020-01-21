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
import com.facebook.react.bridge.Callback;


import android.telecom.TelecomManager;

//TODO : https://stackoverflow.com/questions/53411220/pass-activity-result-into-a-react-native-module
public class ReplaceDialerModule extends ReactContextBaseJavaModule /*implements ActivityEventListener*/ {
    ReactApplicationContext mContext;
    
    private static Callback setCallback;

    private static String LOG = "one.telefon.replacedialer.ReplaceDialerModule";

    // for default dialer
    private TelecomManager telecomManager;
    private static final int RC_DEFAULT_PHONE = 3289;
    private static final int RC_PERMISSION = 3810;

    private static final int REQUEST_CODE_SET_DEFAULT_DIALER = 123;

    public ReplaceDialerModule(ReactApplicationContext context) {
        super(context);
        this.mContext=context;
        //this.mContext.addActivityEventListener(this);
    }
    
    @Override
    public String getName() {
        return "ReplaceDialerModule";
    }

    @ReactMethod
    public void isDefaultDialer(Callback myCallback) {
        Log.w(LOG, "isDefaultDialer()");

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M)
        {
            myCallback.invoke(true);
            return;
        }

        TelecomManager telecomManager = (TelecomManager) this.mContext.getSystemService(Context.TELECOM_SERVICE);

        if (telecomManager.getDefaultDialerPackage().equals(this.mContext.getPackageName())) 
        {
            Log.w(LOG, "invoke(true)");
            myCallback.invoke(true);
        }
        else
        {
            Log.w(LOG, "invoke(false)");
            myCallback.invoke(false);
        }
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
    public void setDefaultDialer(Callback myCallback) {
        Log.w(LOG, "setDefaultDialer() "+this.mContext.getPackageName());
        setCallback=myCallback;

        Intent intent = new Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER);
        intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, this.mContext.getPackageName());
        this.mContext.startActivityForResult(intent, RC_DEFAULT_PHONE,new Bundle());
          
        myCallback.invoke(true);
        // startActivityForResult(intent, REQUEST_CODE_SET_DEFAULT_DIALER); //Different
          // code
          // Huawei/ honor : ??? manual ??? startActivityForResult(new
          // Intent(android.provider.Settings.ACTION_SETTINGS), 0);
    }
}






