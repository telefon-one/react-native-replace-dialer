import React, {DeviceEventEmitter, NativeModules} from 'react-native';
import {EventEmitter} from 'events'

import Call from './Call'


export default class Endpoint extends EventEmitter {

    constructor() {
        super();


        // Subscribe to Calls events
        DeviceEventEmitter.addListener('teleCallReceived', this._onCallReceived.bind(this));
        DeviceEventEmitter.addListener('teleCallChanged', this._onCallChanged.bind(this));
        DeviceEventEmitter.addListener('teleCallTerminated', this._onCallTerminated.bind(this));
        DeviceEventEmitter.addListener('teleCallScreenLocked', this._onCallScreenLocked.bind(this));
        DeviceEventEmitter.addListener('teleMessageReceived', this._onMessageReceived.bind(this));
        DeviceEventEmitter.addListener('teleConnectivityChanged', this._onConnectivityChanged.bind(this));
    }

    /**
     * Returns a Promise that will be resolved once Tele module is initialized.
     * Do not call any function while library is not initialized.
     *
     * @returns {Promise}
     */
    start(configuration) {
        return new Promise(function(resolve, reject) {
            NativeModules.TeleModule.start(configuration, (successful, data) => {
                if (successful) {
                    let calls = [];


                    if (data.hasOwnProperty('calls')) {
                        for (let d of data['calls']) {
                            calls.push(new Call(d));
                        }
                    }

                    let extra = {};

                    for (let key in data) {
                        if (data.hasOwnProperty(key) && key != "calls") {
                            extra[key] = data[key];
                        }
                    }

                    resolve({
                        calls,
                        ...extra
                    });
                } else {
                    reject(data);
                }
            });
        });
    }


    /**
     * Make an outgoing call to the specified URI.
     * Available call settings:
     * 
     *
     * @param sim {Sim} TODO
     * @param destination {String} Destination SIP URI.
     * @param callSettings {PjSipCallSetttings} Outgoing call settings.
     * @param msgSettings {PjSipMsgData} Outgoing call additional information to be sent with outgoing SIP message.
     */
    makeCall(sim, destination, callSettings, msgData) {
        //destination = this._normalize(account, destination);

        return new Promise(function(resolve, reject) {
            NativeModules.TeleModule.makeCall(sim, destination, callSettings, msgData, (successful, data) => {
                if (successful) {
                    resolve(new Call(data));
                } else {
                    reject(data);
                }
            });
        });
    }

    /**
     * Send response to incoming INVITE request.
     *
     * @param call {Call} Call instance
     * @returns {Promise}
     */
    answerCall(call) {
        return new Promise((resolve, reject) => {
            NativeModules.TeleModule.answerCall(call.getId(), (successful, data) => {
                if (successful) {
                    resolve(data);
                } else {
                    reject(data);
                }
            });
        });
    }

    /**
     * Hangup call by using method that is appropriate according to the call state.
     *
     * @param call {Call} Call instance
     * @returns {Promise}
     */
    hangupCall(call) {
        // TODO: Add possibility to pass code and reason for hangup.
        return new Promise((resolve, reject) => {
            NativeModules.TeleModule.hangupCall(call.getId(), (successful, data) => {
                if (successful) {
                    resolve(data);
                } else {
                    reject(data);
                }
            });
        });
    }

    /**
     * Hangup call by using Decline (603) method.
     *
     * @param call {Call} Call instance
     * @returns {Promise}
     */
    declineCall(call) {
        return new Promise((resolve, reject) => {
            NativeModules.TeleModule.declineCall(call.getId(), (successful, data) => {
                if (successful) {
                    resolve(data);
                } else {
                    reject(data);
                }
            });
        });
    }

    /**
     * Put the specified call on hold. This will send re-INVITE with the appropriate SDP to inform remote that the call is being put on hold.
     *
     * @param call {Call} Call instance
     * @returns {Promise}
     */
    holdCall(call) {
        return new Promise((resolve, reject) => {
            NativeModules.TeleModule.holdCall(call.getId(), (successful, data) => {
                if (successful) {
                    resolve(data);
                } else {
                    reject(data);
                }
            });
        });
    }

    /**
     * Release the specified call from hold. This will send re-INVITE with the appropriate SDP to inform remote that the call is resumed.
     *
     * @param call {Call} Call instance
     * @returns {Promise}
     */
    unholdCall(call) {
        return new Promise((resolve, reject) => {
            NativeModules.TeleModule.unholdCall(call.getId(), (successful, data) => {
                if (successful) {
                    resolve(data);
                } else {
                    reject(data);
                }
            });
        });
    }

    /**
     * @param call {Call} Call instance
     * @returns {Promise}
     */
    muteCall(call) {
        return new Promise((resolve, reject) => {
            NativeModules.TeleModule.muteCall(call.getId(), (successful, data) => {
                if (successful) {
                    resolve(data);
                } else {
                    reject(data);
                }
            });
        });
    }

    /**
     * @param call {Call} Call instance
     * @returns {Promise}
     */
    unMuteCall(call) {
        return new Promise((resolve, reject) => {
            NativeModules.TeleModule.unMuteCall(call.getId(), (successful, data) => {
                if (successful) {
                    resolve(data);
                } else {
                    reject(data);
                }
            });
        });
    }

    /**
     * @param call {Call} Call instance
     * @returns {Promise}
     */
    useSpeaker(call) {
        return new Promise((resolve, reject) => {
            NativeModules.TeleModule.useSpeaker(call.getId(), (successful, data) => {
                if (successful) {
                    resolve(data);
                } else {
                    reject(data);
                }
            });
        });
    }

    /**
     * @param call {Call} Call instance
     * @returns {Promise}
     */
    useEarpiece(call) {
        return new Promise((resolve, reject) => {
            NativeModules.TeleModule.useEarpiece(call.getId(), (successful, data) => {
                if (successful) {
                    resolve(data);
                } else {
                    reject(data);
                }
            });
        });
    }

    /**
     * Initiate call transfer to the specified address.
     * This function will send REFER request to instruct remote call party to initiate a new INVITE session to the specified destination/target.
     *
     * @param account {Account} Account associated with call.
     * @param call {Call} The call to be transferred.
     * @param destination URI of new target to be contacted. The URI may be in name address or addr-spec format.
     * @returns {Promise}
     */
    /*
    xferCall(account, call, destination) {
        destination = this._normalize(account, destination);

        return new Promise((resolve, reject) => {
            NativeModules.PjSipModule.xferCall(call.getId(), destination, (successful, data) => {
                if (successful) {
                    resolve(data);
                } else {
                    reject(data);
                }
            });
        });
    }
    */
    /**
     * Initiate attended call transfer.
     * This function will send REFER request to instruct remote call party to initiate new INVITE session to the URL of destCall.
     * The party at destCall then should "replace" the call with us with the new call from the REFER recipient.
     *
     * @param call {Call} The call to be transferred.
     * @param destCall {Call} The call to be transferred.
     * @returns {Promise}
     */
    /*
    xferReplacesCall(call, destCall) {
        return new Promise((resolve, reject) => {
            NativeModules.PjSipModule.xferReplacesCall(call.getId(), destCall.getId(), (successful, data) => {
                if (successful) {
                    resolve(data);
                } else {
                    reject(data);
                }
            });
        });
    }
    */
    /**
     * Redirect (forward) specified call to destination.
     * This function will send response to INVITE to instruct remote call party to redirect incoming call to the specified destination/target.
     *
     * @param account {Account} Account associated with call.
     * @param call {Call} The call to be transferred.
     * @param destination URI of new target to be contacted. The URI may be in name address or addr-spec format.
     * @returns {Promise}
     */
    /*
    redirectCall(account, call, destination) {
        destination = this._normalize(account, destination);

        return new Promise((resolve, reject) => {
            NativeModules.PjSipModule.redirectCall(call.getId(), destination, (successful, data) => {
                if (successful) {
                    resolve(data);
                } else {
                    reject(data);
                }
            });
        });
    }
    */
    /**
     * Send DTMF digits to remote using RFC 2833 payload formats.
     *
     * @param call {Call} Call instance
     * @param digits {String} DTMF string digits to be sent as described on RFC 2833 section 3.10.
     * @returns {Promise}
     */
    /*
    dtmfCall(call, digits) {
        return new Promise((resolve, reject) => {
            NativeModules.PjSipModule.dtmfCall(call.getId(), digits, (successful, data) => {
                if (successful) {
                    resolve(data);
                } else {
                    reject(data);
                }
            });
        });
    }
    */

    /**
     * @fires Endpoint#connectivity_changed
     * @private
     * @param data {Object}
     */
    _onConnectivityChanged(data) {
        /**
         * Fires when registration status has changed.
         *
         * @event Endpoint#connectivity_changed
         * @property {Account} account
         */
        this.emit("connectivity_changed", new Account(data));
    }

    /**
     * @fires Endpoint#registration_changed
     * @private
     * @param data {Object}
     */
    _onRegistrationChanged(data) {
        /**
         * Fires when registration status has changed.
         *
         * @event Endpoint#registration_changed
         * @property {Account} account
         */
        this.emit("registration_changed", new Account(data));
    }

    /**
     * @fires Endpoint#call_received
     * @private
     * @param data {Object}
     */
    _onCallReceived(data) {
        /**
         * TODO
         *
         * @event Endpoint#call_received
         * @property {Call} call
         */
        this.emit("call_received", new Call(data));
    }

    /**
     * @fires Endpoint#call_changed
     * @private
     * @param data {Object}
     */
    _onCallChanged(data) {
        /**
         * TODO
         *
         * @event Endpoint#call_changed
         * @property {Call} call
         */
        this.emit("call_changed", new Call(data));
    }

    /**
     * @fires Endpoint#call_terminated
     * @private
     * @param data {Object}
     */
    _onCallTerminated(data) {
        /**
         * TODO
         *
         * @event Endpoint#call_terminated
         * @property {Call} call
         */
        this.emit("call_terminated", new Call(data));
    }

    /**
     * @fires Endpoint#call_screen_locked
     * @private
     * @param lock bool
     */
    _onCallScreenLocked(lock) {
        /**
         * TODO
         *
         * @event Endpoint#call_screen_locked
         * @property bool lock
         */
        this.emit("call_screen_locked", lock);
    }

    /**
     * @fires Endpoint#message_received
     * @private
     * @param data {Object}
     */
    _onMessageReceived(data) {
        /**
         * TODO
         *
         * @event Endpoint#message_received
         * @property {Message} message
         */
        this.emit("message_received", new Message(data));
    }

    /**
     * @fires Endpoint#connectivity_changed
     * @private
     * @param available bool
     */
    _onConnectivityChanged(available) {
        /**
         * @event Endpoint#connectivity_changed
         * @property bool available True if connectivity matches current Network settings, otherwise false.
         */
        this.emit("connectivity_changed", available);
    }

    /**
     * Normalize Destination URI
     *
     * @param account
     * @param destination {string}
     * @returns {string}
     * @private
     */
    /*
    _normalize(account, destination) {
        if (!destination.startsWith("sip:")) {
            let realm = account.getRegServer();

            if (!realm) {
                realm = account.getDomain();
                let s = realm.indexOf(":");

                if (s > 0) {
                    realm = realm.substr(0, s + 1);
                }
            }

            destination = "sip:" + destination + "@" + realm;
        }

        return destination;
    }
    */    
    // setUaConfig(UaConfig value)
    // setMaxCalls
    // setUserAgent
    // setNatTypeInSdp

    // setLogConfig(LogConfig value)
    // setLevel
}
