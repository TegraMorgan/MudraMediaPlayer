// IMudraAPI.aidl
//////////////////////////////////////////////////////////////////////////////
//
// (C) Copyright 2017 by Wearable Devices ltd.
//
// The information contained herein is confidential, proprietary to  Wearable
// Devices ltd., and considered a trade secret as defined in section 499C of the
// penal code of the State of California. Use of this information by anyone
// other than authorized employees of Wearable Devices ltd. is granted only under a
// written non-disclosure agreement, expressly prescribing the scope and
// manner of such use.
//
//////////////////////////////////////////////////////////////////////////////

package com.wearable.android.ble.interfaces;

import com.wearable.android.ble.interfaces.IMudraDataListener;
import com.wearable.android.ble.interfaces.IMudraDeviceStatuslListener;


/**
 * Mudra Android API
 */
interface IMudraAPI {

/**
 * Initializes the SDK, and sets callbacks for asynchronous dataflow

 *
 * @param  deviceCallback  callback for receiving device status notifications
 * @param  dataCallback callback for receiving device data notifications
 */
    void initMudra(IMudraDeviceStatuslListener deviceCallback, IMudraDataListener dataCallback);

/**
 * Start scanning for Mudra devices
 * <p>
 *     The scan duration is maximum 10 seconds and can be stopped manually.
 *     Scan results will be received asynchronously via deviceCallback
 *     @pre init() should be called with deviceCallback before start scanning
 */
    void mudraStartScan();

/**
 * Stops the scanning for Mudra devices manually
 */
    void mudraStopScan();

/**
 * Start receiving SNC raw data from the SNC sensors on device
 *
 *     @pre init() should be called with deviceCallback before start scanning
 */
    void startRawSNCDataTransmission();

/**
 * Stop receiving SNC raw data from the SNC sensors on device
 *     @pre startRawSNCDataTransmittion() should be called with sncDataCallback before calling
 *         this function.
 */
    void stopRawSNCDataTransmission();

/**
 * Connect to specific device address
 * @param  address  Address of the Mudra device to connect to.
 *     @pre init() should be called with deviceCallback before start scanning
 */
    void connectMudraDevice(in String address);

/**
 * Disconnects from currently connected Mudra device
 */
    void disconnectMudraDevice();

/**
 * Releases the SDK and the async callbacks
 *     @pre init() should be called with deviceCallback before start scanning
 */
    void releaseMudra();



}
