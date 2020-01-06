import React, {NativeModules} from 'react-native';

export default class ReplaceDialer {
  constructor() {
    super();
  }

  isDefault() {
    return NativeModules.ReplaceDialerModule.isDefault();
  }

  setDefault() {
    return NativeModules.ReplaceDialerModule.setDefault();
    //return new Promise(function(resolve, reject) {
    //    NativeModules.ReplaceDialerModule.setDefault(configuration, (successful, data) => {
    //        if (successful) {
    //        }
  }
}
