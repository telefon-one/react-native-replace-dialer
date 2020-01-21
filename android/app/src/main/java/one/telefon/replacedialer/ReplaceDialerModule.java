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


public class ReplaceDialerModule extends ReactContextBaseJavaModule {
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
        mContext=context;
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
            myCallback.invove(true);
            return;
        }

        TelecomManager telecomManager = (TelecomManager) this.mContext.getSystemService(Context.TELECOM_SERVICE);
    
        if (telecomManager.getDefaultDialerPackage() != this.mContext.getPackageName()) 
            myCallback.invove(false);
        else
            myCallback.invove(true);
    }
    
    @Override
    private void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==REQUEST_CODE_SET_DEFAULT_DIALER) 
        {
            setCallback.invove(resultCode);
        //checkSetDefaultDialerResult(resultCode)
        }
        if (requestCode==RC_DEFAULT_PHONE) 
        {
            setCallback.invove(resultCode);
        //checkSetDefaultDialerResult(resultCode)
        }
    }

    @ReactMethod
    public void setDefault(Callback myCallback) {
        Log.w(LOG, "setDefault "+this.mContext.getPackageName());
        setCallback=myCallback;

        Intent intent = new Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER);
        intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, this.mContext.getPackageName());
        this.mContext.startActivityForResult(intent, RC_DEFAULT_PHONE,new Bundle());
          
        // startActivityForResult(intent, REQUEST_CODE_SET_DEFAULT_DIALER); //Different
          // code
          // Huawei/ honor : ??? manual ??? startActivityForResult(new
          // Intent(android.provider.Settings.ACTION_SETTINGS), 0);
    }
}






