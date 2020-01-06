package one.telefon.tele;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.Process;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

import one.telefon.tele.utils.ArgumentUtils;
import one.telefon.tele.dto.ServiceConfigurationDTO;

import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.telecom.Call;
import android.telecom.InCallService;

public class TeleService extends InCallService {
    private static String TAG = "telefon.one.tele.TeleService";

    private boolean mInitialized;
    private HandlerThread mWorkerThread;
    private Handler mHandler;
    // private Endpoint mEndpoint;
    private ServiceConfigurationDTO mServiceConfiguration = new ServiceConfigurationDTO();
    private TeleBroadcastEmiter mEmitter;
    private List<TeleCall> mCalls = new ArrayList<>();
    private List<Object> mTrash = new LinkedList<>();
    private AudioManager mAudioManager;
    // private boolean mUseSpeaker = false;
    private PowerManager mPowerManager;
    private PowerManager.WakeLock mIncallWakeLock;
    // private TelephonyManager mTelephonyManager;
    private WifiManager mWifiManager;
    private WifiManager.WifiLock mWifiLock;
    // private boolean mGSMIdle;
    //private BroadcastReceiver mPhoneStateChangedReceiver = new PhoneStateChangedReceiver();

    // inCallService START
    @Override
    public void onCallAdded(Call call) {
        Log.d(TAG, "onCallAdded");

        //showApp();

        super.onCallAdded(call);
        TeleManager.updateCall(call, "onCallAdded");

        /* DisconnectCause getDisconnectCause() */

        call.registerCallback(new Call.Callback() {
            @Override
            public void onCallDestroyed(Call call) {
                Log.d(TAG, "onCallDestroyed");
                super.onCallDestroyed(call);
                TeleManager.updateCall(call, "onCallDestroyed");
                // sendHeadless("TeleService", "onCallDestroyed", "", "", 0, 0, 0, 0);
            }

            @Override
            public void onDetailsChanged(Call call, Call.Details details) {
                Log.d(TAG, "onDetailsChanged " + call.getRemainingPostDialSequence() + ":"
                        + details.getCallerDisplayName());
                super.onDetailsChanged(call, details);
                TeleManager.updateCall(call, "onDetailsChanged");

                // String num = details.getHandle().toString();
                // String name = details.getCallerDisplayName();
                // sendHeadless("TeleService", "onDetailsChanged", name, num, 0, 0, 0, 0);
            }

            @Override
            public void onStateChanged(Call call, int state) {
                Log.d(TAG, "onStateChanged state=" + state);
                super.onStateChanged(call, state);

                TeleManager.updateCall(call, "onStateChanged");
                // long connectTimeMillis = details.getConnectTimeMillis();
                // sendHeadless("TeleService", "onStateChanged", "", "", state, 0, 0,
                // connectTimeMillis);
            }

            /*
             * API
             * 
             * @Override public void onConnectionEvent(Call call, String event, Bundle
             * extras) { Log.d(TAG, "onConnectionEvent event=" + event); Log.d(TAG,
             * "getDisconnect code: " + call.getDetails().getDisconnectCause().getCode());
             * Log.d(TAG, "getDisconnect reason: " +
             * call.getDetails().getDisconnectCause().getReason()); Log.d(TAG,
             * "getDisconnect description: " +
             * call.getDetails().getDisconnectCause().getDescription()); Log.d(TAG,
             * "event : " + event); super.onConnectionEvent(call, event, extras);
             * sendHeadless("TeleService", "onConnectionEvent", event, "", 0, 0, 0, 0); }
             * 
             * 
             * 
             * @Override public void onRttRequest(Call call, int id) { Log.d(TAG,
             * "onRttRequest"); super.onRttRequest(call, id); sendHeadless("TeleService",
             * "onRttRequest", "", "", id, 0, 0, 0); }
             */

        });

    }

    @Override
    public void onCallRemoved(Call call) {
        Log.w(TAG, "onCallRemoved");
        super.onCallRemoved(call);

        TeleManager.updateCall(call, "onCallRemoved");

        // ADD: call.unregisterCallback(callCallback);
        // sendHeadless("TeleService", "onCallRemoved", "", "", 0, 0, 0, 0);
    }

    // inCallService END



    public TeleBroadcastEmiter getEmitter() {
        return mEmitter;
    }

    /*
     * @Override public IBinder onBind(Intent intent) { Log.d(TAG, "onBind()");
     * init(intent,0,0); //return null; return super(intent); }
     */

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");
        //return START_NOT_STICKY;
        return init(intent, flags, startId);
    }

    private int init(final Intent intent, int flags, int startId) {
        Log.d(TAG, "init");
        if (!mInitialized) {
            Log.d(TAG, "!mInitialized");
            if (intent != null && intent.hasExtra("service")) {
                mServiceConfiguration = ServiceConfigurationDTO.fromMap((Map) intent.getSerializableExtra("service"));
            }

            mWorkerThread = new HandlerThread(getClass().getSimpleName(), Process.THREAD_PRIORITY_FOREGROUND);
            mWorkerThread.setPriority(Thread.MAX_PRIORITY);
            mWorkerThread.start();
            mHandler = new Handler(mWorkerThread.getLooper());
            // mEmitter = new TeleBroadcastEmiter(this);

            mAudioManager = (AudioManager) getApplicationContext().getSystemService(AUDIO_SERVICE);
            mPowerManager = (PowerManager) getApplicationContext().getSystemService(POWER_SERVICE);
            mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            mWifiLock = mWifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF,
                    this.getPackageName() + "-wifi-call-lock");
            mWifiLock.setReferenceCounted(false);
            /*
             * mTelephonyManager = (TelephonyManager)
             * getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE); mGSMIdle
             * = mTelephonyManager.getCallState() == TelephonyManager.CALL_STATE_IDLE;
             * IntentFilter phoneStateFilter = new
             * IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
             * registerReceiver(mPhoneStateChangedReceiver, phoneStateFilter);
             */
            mInitialized = true;

            job(new Runnable() {
                @Override
                public void run() {
                    load();
                }
            });
        }

        if (intent != null) {
            job(new Runnable() {

                @Override
                public void run() {
                    handle(intent);
                }

            });
        }

        return START_NOT_STICKY;
    }




    private void load() {
        // Load native libraries
        Log.d(TAG, "load()");
    }

    @Override
    public void onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mWorkerThread.quitSafely();
        }

        /*
        try {
            if (mEndpoint != null) {
                mEndpoint.libDestroy();
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to destroy Tele library", e);
        }
        */

        //unregisterReceiver(mPhoneStateChangedReceiver);
        super.onDestroy();
    }

    private void job(Runnable job) {
        mHandler.post(job);
    }

    /*
    protected synchronized AudDevManager getAudDevManager() {
        return mEndpoint.audDevManager();
    }*/

    /*
    public void evict(final TeleAccount account) {
        if (mHandler.getLooper().getThread() != Thread.currentThread()) {
            job(new Runnable() {
                @Override
                public void run() {
                    evict(account);
                }
            });
            return;
        }

        // Remove link to account
        mAccounts.remove(account);

        // Remove transport
        try {
            mEndpoint.transportClose(account.getTransportId());
        } catch (Exception e) {
            Log.w(TAG, "Failed to close transport for account", e);
        }

        // Remove account in Tele
        account.delete();

    }*/

    public void evict(final TeleCall call) {
        if (mHandler.getLooper().getThread() != Thread.currentThread()) {
            job(new Runnable() {
                @Override
                public void run() {
                    evict(call);
                }
            });
            return;
        }

        mCalls.remove(call);
        call.delete();
    }

    private void handle(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }

        Log.d(TAG, "Handle \"" + intent.getAction() + "\" action (" + ArgumentUtils.dumpIntentExtraParameters(intent)
                + ")");

        switch (intent.getAction()) {
        // General actions
        case TeleActions.ACTION_START:
            handleStart(intent);
            break;

        // Account actions
        /*
        case TeleActions.ACTION_CREATE_ACCOUNT:
            handleAccountCreate(intent);
            break;
        case TeleActions.ACTION_REGISTER_ACCOUNT:
            handleAccountRegister(intent);
            break;
        case TeleActions.ACTION_DELETE_ACCOUNT:
            handleAccountDelete(intent);
            break;
        */
        // Call actions
        case TeleActions.ACTION_MAKE_CALL:
            handleCallMake(intent);
            break;
        case TeleActions.ACTION_HANGUP_CALL:
            handleCallHangup(intent);
            break;
        case TeleActions.ACTION_DECLINE_CALL:
            handleCallDecline(intent);
            break;
        case TeleActions.ACTION_ANSWER_CALL:
            handleCallAnswer(intent);
            break;

        /*
        case TeleActions.ACTION_HOLD_CALL:
            handleCallSetOnHold(intent);
            break;
        case TeleActions.ACTION_UNHOLD_CALL:
            handleCallReleaseFromHold(intent);
            break;
        case TeleActions.ACTION_MUTE_CALL:
            handleCallMute(intent);
            break;
        case TeleActions.ACTION_UNMUTE_CALL:
            handleCallUnMute(intent);
            break;
        case TeleActions.ACTION_USE_SPEAKER_CALL:
            handleCallUseSpeaker(intent);
            break;
        case TeleActions.ACTION_USE_EARPIECE_CALL:
            handleCallUseEarpiece(intent);
            break;
        case TeleActions.ACTION_XFER_CALL:
            handleCallXFer(intent);
            break;
        case TeleActions.ACTION_XFER_REPLACES_CALL:
            handleCallXFerReplaces(intent);
            break;
        case TeleActions.ACTION_REDIRECT_CALL:
            handleCallRedirect(intent);
            break;
        case TeleActions.ACTION_DTMF_CALL:
            handleCallDtmf(intent);
        case TeleActions.ACTION_CHANGE_CODEC_SETTINGS:
            handleChangeCodecSettings(intent);
            break;
        */

        // Configuration actions
        case TeleActions.ACTION_SET_SERVICE_CONFIGURATION:
            handleSetServiceConfiguration(intent);
            break;
        }
    }

    private void handleStart(Intent intent) {
        try {
            // Modify existing configuration if it changes during application reload.
            if (intent.hasExtra("service")) {
                ServiceConfigurationDTO newServiceConfiguration = ServiceConfigurationDTO
                        .fromMap((Map) intent.getSerializableExtra("service"));
                if (!newServiceConfiguration.equals(mServiceConfiguration)) {
                    updateServiceConfiguration(newServiceConfiguration);
                }
            }

            /*
            CodecInfoVector codVect = mEndpoint.codecEnum();
            JSONObject codecs = new JSONObject();

            for (int i = 0; i < codVect.size(); i++) {
                CodecInfo codInfo = codVect.get(i);
                String codId = codInfo.getCodecId();
                short priority = codInfo.getPriority();
                codecs.put(codId, priority);
                codInfo.delete();
            }

            JSONObject settings = mServiceConfiguration.toJson();
            settings.put("codecs", codecs);
            */

            mEmitter.fireStarted(intent/*, mAccounts*/, mCalls/*, settings*/);
        } catch (Exception error) {
            Log.e(TAG, "Error while building codecs list", error);
            throw new RuntimeException(error);
        }
    }

    private void handleSetServiceConfiguration(Intent intent) {
        try {
            updateServiceConfiguration(ServiceConfigurationDTO.fromIntent(intent));

            // Emmit response
            mEmitter.fireIntentHandled(intent, mServiceConfiguration.toJson());
        } catch (Exception e) {
            mEmitter.fireIntentHandled(intent, e);
        }
    }

    private void updateServiceConfiguration(ServiceConfigurationDTO configuration) {
        mServiceConfiguration = configuration;
    }

    /*
    private void handleAccountCreate(Intent intent) {
        try {
            AccountConfigurationDTO accountConfiguration = AccountConfigurationDTO.fromIntent(intent);
            TeleAccount account = doAccountCreate(accountConfiguration);

            // Emmit response
            mEmitter.fireAccountCreated(intent, account);
        } catch (Exception e) {
            mEmitter.fireIntentHandled(intent, e);
        }
    }

    private void handleAccountRegister(Intent intent) {
        try {
            int accountId = intent.getIntExtra("account_id", -1);
            boolean renew = intent.getBooleanExtra("renew", false);
            TeleAccount account = null;

            for (TeleAccount a : mAccounts) {
                if (a.getId() == accountId) {
                    account = a;
                    break;
                }
            }

            if (account == null) {
                throw new Exception("Account with \"" + accountId + "\" id not found");
            }

            account.register(renew);

            // -----
            mEmitter.fireIntentHandled(intent);
        } catch (Exception e) {
            mEmitter.fireIntentHandled(intent, e);
        }
    }

    private TeleAccount doAccountCreate(AccountConfigurationDTO configuration) throws Exception {
        AccountConfig cfg = new AccountConfig();

        // General settings
        AuthCredInfo cred = new AuthCredInfo("Digest", configuration.getNomalizedRegServer(),
                configuration.getUsername(), 0, configuration.getPassword());

        String idUri = configuration.getIdUri();
        String regUri = configuration.getRegUri();

        cfg.setIdUri(idUri);
        cfg.getRegConfig().setRegistrarUri(regUri);
        cfg.getRegConfig().setRegisterOnAdd(configuration.isRegOnAdd());
        cfg.getSipConfig().getAuthCreds().add(cred);

        cfg.getVideoConfig().getRateControlBandwidth();

        // Registration settings

        if (configuration.getContactParams() != null) {
            cfg.getSipConfig().setContactParams(configuration.getContactParams());
        }
        if (configuration.getContactUriParams() != null) {
            cfg.getSipConfig().setContactUriParams(configuration.getContactUriParams());
        }
        if (configuration.getRegContactParams() != null) {
            Log.w(TAG, "Property regContactParams are not supported on android, use contactParams instead");
        }

        if (configuration.getRegHeaders() != null && configuration.getRegHeaders().size() > 0) {
            SipHeaderVector headers = new SipHeaderVector();

            for (Map.Entry<String, String> entry : configuration.getRegHeaders().entrySet()) {
                SipHeader hdr = new SipHeader();
                hdr.setHName(entry.getKey());
                hdr.setHValue(entry.getValue());
                headers.add(hdr);
            }

            cfg.getRegConfig().setHeaders(headers);
        }

        // Transport settings
        int transportId = mTcpTransportId;

        if (configuration.isTransportNotEmpty()) {
            switch (configuration.getTransport()) {
            case "UDP":
                transportId = mUdpTransportId;
                break;
            case "TLS":
                transportId = mTlsTransportId;
                break;
            default:
                Log.w(TAG, "Illegal \"" + configuration.getTransport()
                        + "\" transport (possible values are UDP, TCP or TLS) use TCP instead");
                break;
            }
        }

        cfg.getSipConfig().setTransportId(transportId);

        if (configuration.isProxyNotEmpty()) {
            StringVector v = new StringVector();
            v.add(configuration.getProxy());
            cfg.getSipConfig().setProxies(v);
        }

        cfg.getMediaConfig().getTransportConfig().setQosType(pj_qos_type.PJ_QOS_TYPE_VOICE);

        cfg.getVideoConfig().setAutoShowIncoming(true);
        cfg.getVideoConfig().setAutoTransmitOutgoing(true);

        int cap_dev = cfg.getVideoConfig().getDefaultCaptureDevice();
        mEndpoint.vidDevManager().setCaptureOrient(cap_dev, pjmedia_orient.PJMEDIA_ORIENT_ROTATE_270DEG, true);

        // -----

        TeleAccount account = new TeleAccount(this, transportId, configuration);
        account.create(cfg);

        mTrash.add(cfg);
        mTrash.add(cred);

        mAccounts.add(account);

        return account;
    }

    private void handleAccountDelete(Intent intent) {
        try {
            int accountId = intent.getIntExtra("account_id", -1);
            TeleAccount account = null;

            for (TeleAccount a : mAccounts) {
                if (a.getId() == accountId) {
                    account = a;
                    break;
                }
            }

            if (account == null) {
                throw new Exception("Account with \"" + accountId + "\" id not found");
            }

            evict(account);

            // -----
            mEmitter.fireIntentHandled(intent);
        } catch (Exception e) {
            mEmitter.fireIntentHandled(intent, e);
        }
    } 
    */

    private void handleCallMake(Intent intent) {
        try {
            //int accountId = intent.getIntExtra("account_id", -1);
            //TeleAccount account = findAccount(accountId);
            String destination = intent.getStringExtra("destination");
            String settingsJson = intent.getStringExtra("settings");
            String messageJson = intent.getStringExtra("message");

            // -----
            //CallOpParam callOpParam = new CallOpParam(true);
            /*
            if (settingsJson != null) {
                CallSettingsDTO settingsDTO = CallSettingsDTO.fromJson(settingsJson);
                CallSetting callSettings = new CallSetting();

                if (settingsDTO.getAudioCount() != null) {
                    callSettings.setAudioCount(settingsDTO.getAudioCount());
                }
                if (settingsDTO.getVideoCount() != null) {
                    callSettings.setVideoCount(settingsDTO.getVideoCount());
                }
                if (settingsDTO.getFlag() != null) {
                    callSettings.setFlag(settingsDTO.getFlag());
                }
                if (settingsDTO.getRequestKeyframeMethod() != null) {
                    callSettings.setReqKeyframeMethod(settingsDTO.getRequestKeyframeMethod());
                }

                callOpParam.setOpt(callSettings);

                mTrash.add(callSettings);
            }
            */

            /*
            if (messageJson != null) {
                SipMessageDTO messageDTO = SipMessageDTO.fromJson(messageJson);
                SipTxOption callTxOption = new SipTxOption();

                if (messageDTO.getTargetUri() != null) {
                    callTxOption.setTargetUri(messageDTO.getTargetUri());
                }
                if (messageDTO.getContentType() != null) {
                    callTxOption.setContentType(messageDTO.getContentType());
                }
                if (messageDTO.getHeaders() != null) {
                    callTxOption.setHeaders(TeleUtils.mapToSipHeaderVector(messageDTO.getHeaders()));
                }
                if (messageDTO.getBody() != null) {
                    callTxOption.setMsgBody(messageDTO.getBody());
                }

                callOpParam.setTxOption(callTxOption);

                mTrash.add(callTxOption);
            }
            */

            TeleCall call = new TeleCall();
            call.makeCall(destination/*, callOpParam*/);

            //callOpParam.delete();

            // Automatically put other calls on hold.
            //doPauseParallelCalls(call);

            mCalls.add(call);
            mEmitter.fireIntentHandled(intent, call.toJson());
        } catch (Exception e) {
            mEmitter.fireIntentHandled(intent, e);
        }
    }

    private void handleCallHangup(Intent intent) {
        try {
            int callId = intent.getIntExtra("call_id", -1);
            TeleCall call = findCall(callId);
            //call.hangup(/*new CallOpParam(true)*/);
            call.disconnect();

            mEmitter.fireIntentHandled(intent);
        } catch (Exception e) {
            mEmitter.fireIntentHandled(intent, e);
        }
    }

    private void handleCallDecline(Intent intent) {
        try {
            int callId = intent.getIntExtra("call_id", -1);

            // -----
            TeleCall call = findCall(callId);
            //CallOpParam prm = new CallOpParam(true);
            //prm.setStatusCode("PJSIP_SC_DECLINE");
            //call.hangup(/*prm*/);
            call.reject();
            //prm.delete();

            mEmitter.fireIntentHandled(intent);
        } catch (Exception e) {
            mEmitter.fireIntentHandled(intent, e);
        }
    }

    private void handleCallAnswer(Intent intent) {
        try {
            int callId = intent.getIntExtra("call_id", -1);

            // -----
            TeleCall call = findCall(callId);
            //CallOpParam prm = new CallOpParam();
            //prm.setStatusCode("PJSIP_SC_OK");
            call.answer(); /*prm*/

            // Automatically put other calls on hold.
            //doPauseParallelCalls(call);

            mEmitter.fireIntentHandled(intent);
        } catch (Exception e) {
            mEmitter.fireIntentHandled(intent, e);
        }
    }

    /*
    private void handleCallSetOnHold(Intent intent) {
        try {
            int callId = intent.getIntExtra("call_id", -1);

            // -----
            TeleCall call = findCall(callId);
            call.hold();

            mEmitter.fireIntentHandled(intent);
        } catch (Exception e) {
            mEmitter.fireIntentHandled(intent, e);
        }
    }

    private void handleCallReleaseFromHold(Intent intent) {
        try {
            int callId = intent.getIntExtra("call_id", -1);

            // -----
            TeleCall call = findCall(callId);
            call.unhold();

            // Automatically put other calls on hold.
            doPauseParallelCalls(call);

            mEmitter.fireIntentHandled(intent);
        } catch (Exception e) {
            mEmitter.fireIntentHandled(intent, e);
        }
    }

    private void handleCallMute(Intent intent) {
        try {
            int callId = intent.getIntExtra("call_id", -1);

            // -----
            TeleCall call = findCall(callId);
            call.mute();

            mEmitter.fireIntentHandled(intent);
        } catch (Exception e) {
            mEmitter.fireIntentHandled(intent, e);
        }
    }

    private void handleCallUnMute(Intent intent) {
        try {
            int callId = intent.getIntExtra("call_id", -1);

            // -----
            TeleCall call = findCall(callId);
            call.unmute();

            mEmitter.fireIntentHandled(intent);
        } catch (Exception e) {
            mEmitter.fireIntentHandled(intent, e);
        }
    }

    private void handleCallUseSpeaker(Intent intent) {
        try {
            mAudioManager.setSpeakerphoneOn(true);
            mUseSpeaker = true;

            for (TeleCall call : mCalls) {
                emmitCallUpdated(call);
            }

            mEmitter.fireIntentHandled(intent);
        } catch (Exception e) {
            mEmitter.fireIntentHandled(intent, e);
        }
    }

    private void handleCallUseEarpiece(Intent intent) {
        try {
            mAudioManager.setSpeakerphoneOn(false);
            mUseSpeaker = false;

            for (TeleCall call : mCalls) {
                emmitCallUpdated(call);
            }

            mEmitter.fireIntentHandled(intent);
        } catch (Exception e) {
            mEmitter.fireIntentHandled(intent, e);
        }
    }

    private void handleCallXFer(Intent intent) {
        try {
            int callId = intent.getIntExtra("call_id", -1);
            String destination = intent.getStringExtra("destination");

            // -----
            TeleCall call = findCall(callId);
            call.xfer(destination, new CallOpParam(true));

            mEmitter.fireIntentHandled(intent);
        } catch (Exception e) {
            mEmitter.fireIntentHandled(intent, e);
        }
    }

    private void handleCallXFerReplaces(Intent intent) {
        try {
            int callId = intent.getIntExtra("call_id", -1);
            int destinationCallId = intent.getIntExtra("dest_call_id", -1);

            // -----
            TeleCall call = findCall(callId);
            TeleCall destinationCall = findCall(destinationCallId);
            call.xferReplaces(destinationCall, new CallOpParam(true));

            mEmitter.fireIntentHandled(intent);
        } catch (Exception e) {
            mEmitter.fireIntentHandled(intent, e);
        }
    }

    private void handleCallRedirect(Intent intent) {
        try {
            int callId = intent.getIntExtra("call_id", -1);
            String destination = intent.getStringExtra("destination");

            // -----
            TeleCall call = findCall(callId);
            call.redirect(destination);

            mEmitter.fireIntentHandled(intent);
        } catch (Exception e) {
            mEmitter.fireIntentHandled(intent, e);
        }
    }

    private void handleCallDtmf(Intent intent) {
        try {
            int callId = intent.getIntExtra("call_id", -1);
            String digits = intent.getStringExtra("digits");

            // -----
            TeleCall call = findCall(callId);
            call.dialDtmf(digits);

            mEmitter.fireIntentHandled(intent);
        } catch (Exception e) {
            mEmitter.fireIntentHandled(intent, e);
        }
    }

    private void handleChangeCodecSettings(Intent intent) {
        try {
            Bundle codecSettings = intent.getExtras();

            // -----
            if (codecSettings != null) {
                for (String key : codecSettings.keySet()) {

                    if (!key.equals("callback_id")) {

                        short priority = (short) codecSettings.getInt(key);

                        mEndpoint.codecSetPriority(key, priority);

                    }

                }
            }

            mEmitter.fireIntentHandled(intent);
        } catch (Exception e) {
            mEmitter.fireIntentHandled(intent, e);
        }
    }

    private TeleAccount findAccount(int id) throws Exception {
        for (TeleAccount account : mAccounts) {
            if (account.getId() == id) {
                return account;
            }
        }

        throw new Exception("Account with specified \"" + id + "\" id not found");
    }
    */

    private TeleCall findCall(int id) throws Exception {
        for (TeleCall call : mCalls) {
            /*if (call.getId() == id) {
                return call;
            }*/
        }

        throw new Exception("Call with specified \"" + id + "\" id not found");
    }

    /*
    void emmitRegistrationChanged(TeleAccount account, OnRegStateParam prm) {
        getEmitter().fireRegistrationChangeEvent(account);
    }
    

    void emmitMessageReceived(TeleAccount account, TeleMessage message) {
        getEmitter().fireMessageReceivedEvent(message);
    }
    */

    void emmitCallReceived(/*TeleAccount account,*/ TeleCall call) {
        // Automatically decline incoming call when user uses GSM
        //StartApp();

        mCalls.add(call);
        mEmitter.fireCallReceivedEvent(call);
    }

    void emmitCallStateChanged(TeleCall call/*, OnCallStateParam prm*/) {
        try {
            //if (call.getInfo().getState() == "PJSIP_INV_STATE_DISCONNECTED") {
            //    emmitCallTerminated(call, prm);
            //} else {
                emmitCallChanged(call/*, prm*/);
            //}
        } catch (Exception e) {
            Log.w(TAG, "Failed to handle call state event", e);
        }
    }

    void emmitCallChanged(TeleCall call/*, OnCallStateParam prm*/) {
        try {
            final int callId;// = call.getId();
            final String callState;// = call.getInfo().getState();

            job(new Runnable() {
                @Override
                public void run() {
                    // Acquire wake lock
                    if (mIncallWakeLock == null) {
                        mIncallWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "incall");
                    }
                    if (!mIncallWakeLock.isHeld()) {
                        mIncallWakeLock.acquire();
                    }

                    // Ensure that ringing sound is stopped
                    /*
                    if (callState != "PJSIP_INV_STATE_INCOMING" && !mUseSpeaker
                            && mAudioManager.isSpeakerphoneOn()) {
                        mAudioManager.setSpeakerphoneOn(false);
                    }
                    */

                    // Acquire wifi lock
                    mWifiLock.acquire();

                    /*
                    if (callState == "PJSIP_INV_STATE_EARLY"
                            || callState == "PJSIP_INV_STATE_CONFIRMED") {
                        mAudioManager.setMode(AudioManager.MODE_IN_CALL);
                    }*/
                }
            });
        } catch (Exception e) {
            Log.w(TAG, "Failed to retrieve call state", e);
        }

        mEmitter.fireCallChanged(call);
    }

    void emmitCallTerminated(TeleCall call/*, OnCallStateParam prm*/) {
        final int callId;// = call.getId();

        job(new Runnable() {
            @Override
            public void run() {
                // Release wake lock
                if (mCalls.size() == 1) {
                    if (mIncallWakeLock != null && mIncallWakeLock.isHeld()) {
                        mIncallWakeLock.release();
                    }
                }

                // Release wifi lock
                if (mCalls.size() == 1) {
                    mWifiLock.release();
                }

                // Reset audio settings
                if (mCalls.size() == 1) {
                    mAudioManager.setSpeakerphoneOn(false);
                    mAudioManager.setMode(AudioManager.MODE_NORMAL);
                }
            }
        });

        mEmitter.fireCallTerminated(call);
        evict(call);
    }

    void emmitCallUpdated(TeleCall call) {
        mEmitter.fireCallChanged(call);
    }


}
