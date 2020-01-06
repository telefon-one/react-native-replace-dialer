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

    private static String LOG = "telefon.one.replacedialer.ReplaceDialerModule";

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
    public boolean isDefault() {
        Log.w(LOG, "isDefault");
        TelecomManager telecomManager = (TelecomManager) this.mContext.getSystemService(Context.TELECOM_SERVICE);
    
        if (telecomManager.getDefaultDialerPackage() != this.mContext.getPackageName()) 
            return false;
        else
            return true;
    }

    @ReactMethod
    public void setDefault() {
        Log.w(LOG, "setDefault");
          Intent intent = new Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER);
          intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, this.mContext.getPackageName());
          this.mContext.startActivityForResult(intent, RC_DEFAULT_PHONE,new Bundle());
          // startActivityForResult(intent, REQUEST_CODE_SET_DEFAULT_DIALER); //Different
          // code
          // Huawei/ honor : ??? manual ??? startActivityForResult(new
          // Intent(android.provider.Settings.ACTION_SETTINGS), 0);
    }
}






