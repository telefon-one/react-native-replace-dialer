# react-native-dialer-replacement

Dialer Replacement for androif based on react-native-tele(https://github.com/telefon-one/react-native-tele/) for React Native

**UPDATE**: Now is compatible with RN 0.60+ (AndroidX)


## Support

 - Android
 
## To do

 - [ ] Add ToDo

## Examples

- [Google.Play] TODO

## Installation
<activity>
...
  <!-- ReplaceDialer -->
  <intent-filter>
                  <!-- Handle links from other applications -->
                <action android:name="android.intent.action.VIEW" />
                <action android:name="android.intent.action.DIAL" />
                <!-- Populate the system chooser -->
                <category android:name="android.intent.category.DEFAULT" />
                <!-- Handle links in browsers -->
                <category android:name="android.intent.category.BROWSABLE" />

                <!--SCHEME TEL-->
                <data android:scheme="tel"/>
    </intent-filter>
    <intent-filter>
    <action android:name="android.intent.action.DIAL"/>
    <category android:name="android.intent.category.DEFAULT"/>
  </intent-filter>
  <!-- ReplaceDialer -->
...
      </activity>
...

<!-- ReplaceDialer Service -->
        <service
            android:name=".TeleService"
            android:permission="android.permission.BIND_INCALL_SERVICE"
            >
            <meta-data
                android:name="android.telecom.IN_CALL_SERVICE_UI"
                android:value="true"
                />
            <intent-filter>
                <action android:name="android.telecom.InCallService" />
            </intent-filter>
        </service>
<!-- ReplaceDialer Service -->
...
<application>

## Usage

## API

