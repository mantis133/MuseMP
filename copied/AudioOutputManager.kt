package com.example.muse

import android.Manifest
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat


class HeadphoneReceiver() : BroadcastReceiver() {
    private val TAG = "HEADPHONE ACTION"
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_HEADSET_PLUG){
            when(intent.getIntExtra("state",-1)){
                0 -> {/*headphones unplugged*/
                    Log.d(TAG,"unplugged")
//                    MediaManager.togglePlayPause()
                }
                1 -> {/*headphones plugged in*/
                    Log.d(TAG,"plugged in")
                }
                else ->{/*who the hell knows*/}
            }

        }
        if (BluetoothDevice.ACTION_ACL_DISCONNECTED == intent?.action){
            val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                if (device != null) {
                    if (device.bluetoothClass.deviceClass == BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES ||
                        device.bluetoothClass.deviceClass == BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET) {
                        // Handle the pause/stop action
//                        MediaManager.togglePlayPause()
                    }
                }
            }
    }
}
