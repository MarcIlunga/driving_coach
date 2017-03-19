package starthack.drivingcoach;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * Created by marius on 18.03.2017.
 */
public class BleManager {

    private static final String XDK_Name = "XDK_DEMO_MAR";
    private static final int REQUEST_ENABLE_BT = 13;
    private static final long SCAN_PERIOD = 10000;
    private static final String TAG = "Gatt";

    private Activity parent;
    private BluetoothAdapter blAdapter;
    private boolean scanning;
    private Handler mHandler = new Handler();
    private boolean XDKFound;

    private static String XKD_SERVICE_S = "00005301-0000-0041-4c50-574953450000";
    private static String XKD_CHARAC_W_S = "00005302-0000-0041-4c50-574953450000";
    private static String XKD_CHARAC_R_S = "00005303-0000-0041-4c50-574953450000";
    private static BluetoothGattService XKD_SERVICE;
    private static BluetoothGattCharacteristic XKD_CHARAC_W;
    private static BluetoothGattCharacteristic XKD_CHARAC_R;


    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    /*public final static UUID UUID_HEART_RATE_MEASUREMENT =
            UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);*/

    public BleManager(Activity parent){
        this.parent = parent;

        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager = (BluetoothManager) parent.getSystemService(Context.BLUETOOTH_SERVICE);
        blAdapter = bluetoothManager.getAdapter();
        Log.d("<<<<<", "Got adapter");

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (blAdapter == null || !blAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            parent.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        Log.d("<<<<<", "Bluetooth enabled");

        scanLeDevice(true);

    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanning = false;
                    blAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);
            XDKFound = false;
            scanning = true;
            blAdapter.startLeScan(mLeScanCallback);
        } else {
            scanning = false;
            blAdapter.stopLeScan(mLeScanCallback);
        }
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    if(!XDKFound){
                        String deviceName = device == null ? "Unknown" : device.getName() == null ? "Unknown" : device.getName();
                        Log.d("Device Available: ", deviceName);
                        if(XDK_Name.equals(deviceName)){
                            Log.d("<<<<<", "FOUND");
                            XDKFound = true;
                            mBluetoothGatt = device.connectGatt(parent, false, mGattCallback);
                        }
                    }
                }
            };


    private final BluetoothGattCallback mGattCallback =
            new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                                    int newState) {
                    String intentAction;
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        intentAction = ACTION_GATT_CONNECTED;
                        mConnectionState = STATE_CONNECTED;
                        broadcastUpdate(intentAction);
                        Log.i(TAG, "Connected to GATT server.");
                        Log.i(TAG, "Attempting to start service discovery:" +
                                mBluetoothGatt.discoverServices());

                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        intentAction = ACTION_GATT_DISCONNECTED;
                        mConnectionState = STATE_DISCONNECTED;
                        Log.i(TAG, "Disconnected from GATT server.");
                        broadcastUpdate(intentAction);
                    }
                }

                @Override
                // New services discovered
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    Log.w(TAG, "onServicesDiscovered received: " + status);
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                        List<BluetoothGattService> services = gatt.getServices();
                        for (BluetoothGattService service : services) {
                            Log.d("<<<<<", "Service: " + service.getUuid());
                            if(service.getUuid().toString().equals(XKD_SERVICE_S)){
                                XKD_SERVICE = service;
                            }
                            List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                            for(BluetoothGattCharacteristic charac : characteristics){
                                Log.d("<<<<<", "Charac: " + charac.getUuid());
                                if(charac.getUuid().toString().equals(XKD_CHARAC_W_S)){
                                    XKD_CHARAC_W = charac;
                                } else if(charac.getUuid().toString().equals(XKD_CHARAC_R_S)){
                                    XKD_CHARAC_R = charac;
                                }
                            }
                        }
                        boolean sanityCheck = XKD_SERVICE !=null && XKD_CHARAC_W !=null && XKD_CHARAC_R != null;
                        Log.d("<<<<<", "Sanity Check: " + sanityCheck);
                        if(sanityCheck){

                            boolean error = !gatt.setCharacteristicNotification(XKD_CHARAC_R, true);
                            if(error){
                                Log.d("", "error setCharacteristicNotification");
                                return;
                            }

                            BluetoothGattDescriptor descriptor =
                                    XKD_CHARAC_R.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));

                            if (descriptor == null) {
                                Log.d("", "error getDescriptor");
                                return;
                            }

                            boolean result = descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            if (result) {
                                gatt.writeDescriptor(descriptor);
                            }

                            Log.d("<<<<<", "Descriptor Write Asked");
                        }
                    } else {
                       //Log.w(TAG, "onServicesDiscovered received: " + status);
                    }
                }

                @Override
                public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                    byte[] value = descriptor.getValue();
                    boolean notification_write = value == BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
                    Log.d("<<<<<", "onDescriptorWrite: " + notification_write);
                    if(notification_write){
                        String s = "start";
                        //byte[] b = s.getBytes(StandardCharsets.US_ASCII);
                        XKD_CHARAC_W.setValue(s.getBytes());
                        gatt.writeCharacteristic(XKD_CHARAC_W);
                        XKD_CHARAC_W.setWriteType(2);
                        Log.d("<<<<<", "Start Sent");
                    }
                }

                @Override
                // Result of a characteristic read operation
                public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,
                                                 int status) {
                    Log.d("<<<<<", "onCharacteristicRead: " + (characteristic == XKD_CHARAC_R));
                }

                @Override

                public void onCharacteristicWrite(BluetoothGatt gatt,
                                                  BluetoothGattCharacteristic characteristic, int status) {
                    Log.d("", "Write Happened");
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                    }
                }

                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt,
                                                    BluetoothGattCharacteristic characteristic) {

                    Log.d("<<<<<","onCharacteristicChanged");
                    byte[] value = characteristic.getValue();
                    String message = new String(value).trim();
                    //Log.d("<<<<<", message);
                    String[] temp = message.split(" ");
                    int x = Integer.parseInt(temp[0]);
                    int y = Integer.parseInt(temp[1]);
                    int z = Integer.parseInt(temp[2]);
                    DataEcoAnalysis.analyse(x, y, z);
                }

            };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        parent.sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        // This is special handling for the Heart Rate Measurement profile. Data
        // parsing is carried out as per profile specifications.
        /*if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            int flag = characteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
                Log.d(TAG, "Heart rate format UINT16.");
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
                Log.d(TAG, "Heart rate format UINT8.");
            }
            final int heartRate = characteristic.getIntValue(format, 1);
            Log.d(TAG, String.format("Received heart rate: %d", heartRate));
            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
        } else {*/
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for(byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                intent.putExtra(EXTRA_DATA, new String(data) + "\n" +
                        stringBuilder.toString());
            }
        //}
        parent.sendBroadcast(intent);
    }

    public void close(){
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

}
