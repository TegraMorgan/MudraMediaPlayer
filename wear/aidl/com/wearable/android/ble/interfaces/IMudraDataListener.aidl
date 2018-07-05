// IMudraDataListener.aidl
package com.wearable.android.ble.interfaces;

/**
 * Callback for receiving Mudra device data and events
 * <p>
 * For receiving notifications with device data and events such as: gesture recognized, proportional
 * control force applied with a certain finger, and IMU orientation data.
 *
 * @param  dataType  integer value representing the data type.
 *         possible values: 0=gesture data, 1=proportial force data, 2=IMU data
 * @param  data the data as float array:
 * if dataType=0 (gestures): data is a float array with gestures classes probabilities (confidence level)
 * if dataType=1 (proportional): data is a float array in this sceme: { [0]=index finger is pressing probability,
 *                               [1]=middle finger is pressing probability, [2]=applied force between 0..1}
 * if dataType=2 (IMU-data): IMU data represented in array of float[7] where data[0..2] is the acceleration axis
 *                           and data[3..7] is the gyroscope rotation quaternion.
 *
 * @see Mudra app on Play Store for examples of data.
 *
 * <p>Note: Please release callbacks as fast as you can to avoid performance issues in experience.</p>
 */
interface IMudraDataListener {
  void onMudraDataReady(int dataType, in float[] data);
}
