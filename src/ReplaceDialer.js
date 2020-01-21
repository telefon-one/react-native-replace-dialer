import React, { NativeModules } from 'react-native';

export default class ReplaceDialer {
  constructor() {
    //super();
  }

  checkNativeModule() {
    // Produce an error if we don't have the native module
    if (NativeModules.ReplaceDialerModule == null) {
      throw new Error(`react-native-replace-dialer: NativeModule.ReplaceDialerModule is null. To fix this issue try these steps:
              • Rebuild and re-run the app.
              • If you are using CocoaPods on iOS, run \`pod install\` in the \`ios\` directory and then rebuild and re-run the app. You may also need to re-open Xcode to get the new pods.
              • Check that the library was linked correctly when you used the link command by running through the manual installation instructions in the README.
              * If you are getting this error while unit testing you need to mock the native module. Follow the guide in the README.
              If none of these fix the issue, please open an issue on the Github repository: https://github.com/telefon-one/react-native-replace-dialer`);
    }
  }

  isDefaultDialer(cb) {
    this.checkNativeModule();
    return NativeModules.ReplaceDialerModule.isDefaultDialer((data) => {
      console.log("isDefaultDialer()",data);
      cb(data);
      //if (successful) {
      // }
    });
  }

  setDefaultDialer(cb) {
    this.checkNativeModule();
    //return NativeModules.ReplaceDialerModule.setDefault();
    return NativeModules.ReplaceDialerModule.setDefaultDialer((data) => {
      console.log("setDefaultDialer",data);
      cb(data);
      //if (successful) {
      //}
    });
  }
}
