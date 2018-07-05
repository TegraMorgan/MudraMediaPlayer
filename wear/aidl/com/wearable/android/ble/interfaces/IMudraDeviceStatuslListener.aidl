// IMudraDeviceStatuslListener.aidl
package com.wearable.android.ble.interfaces;

// Callback for receiving device status notifications
interface IMudraDeviceStatuslListener {
/**IAsyncDeviceStatuslListener
 * Callback called when device status changed
 * For receiving notifications on device scan results, device connected/disconnected, etc.
 *
 * @param  statusType  integer value representing the notification type.
 *         possible values: 0=Device Scan Started, 1=Device Scan Stopped, 2=device found in scan,
  *        3=device connected, 4=device disconnected
 * @param  deviceAddress the relevant device bluetooth address as String
 *
 * <p>Note: Please release callbacks as fast as you can to avoid performance issues in experience.</p>
 */
  void onMudraStatusChanged(int statusType, in String deviceAddress);
}
