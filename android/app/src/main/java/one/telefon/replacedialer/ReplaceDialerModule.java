package one.telefon.replacedialer;

import android.app.Activity;
import android.content.Intent;

import com.facebook.react.bridge.*;




public class ReplaceDialerModule extends ReactContextBaseJavaModule {

    private static String LOG = "telefon.one.replacedialer.ReplaceDialerModule";


    public ReplaceDialerModule(ReactApplicationContext context) {
        super(context);
    }
    
    @Override
    public String getName() {
        return "ReplaceDialerModule";
    }

    @ReactMethod
    public boolean isDefault() {
        Log.w(LOG, "isDefault");
        TelecomManager telecomManager = (TelecomManager) getSystemService(Context.TELECOM_SERVICE);
    
        if (telecomManager.getDefaultDialerPackage() != getPackageName()) 
            return false;
        else
            return true;
    }

    @ReactMethod
    public void setDefault() {
        Log.w(LOG, "setDefault");
          Intent intent = new Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER);
          intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, getPackageName());
          startActivityForResult(intent, RC_DEFAULT_PHONE);
          // startActivityForResult(intent, REQUEST_CODE_SET_DEFAULT_DIALER); //Different
          // code
          // Huawei/ honor : ??? manual ??? startActivityForResult(new
          // Intent(android.provider.Settings.ACTION_SETTINGS), 0);
    }
}






