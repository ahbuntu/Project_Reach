package com.projectreach.gripnavigation;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.app.Fragment;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;


public class RFduinoActivity extends Activity implements BluetoothAdapter.LeScanCallback {

    private static final String TAG = "RFduinoActivity";

    private static final int REQUEST_ENABLE_BT = 101;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice bluetoothDevice;

    private boolean scanStarted;
    private boolean scanning;

    // State machine
    final private static int STATE_BLUETOOTH_OFF = 1;
    final private static int STATE_DISCONNECTED = 2;
    final private static int STATE_CONNECTING = 3;
    final private static int STATE_CONNECTED = 4;

    private int state;

    private Button enableBluetoothButton;
    private TextView scanStatusText;
    private Button scanButton;
    private TextView deviceInfoText;
    private TextView connectionStatusText;
    private Button connectButton;

    private RFduinoService rfduinoService;

    private final ServiceConnection rfduinoServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            rfduinoService = ((RFduinoService.LocalBinder) service).getService();
            if (rfduinoService.initialize()) {
                if (rfduinoService.connect(bluetoothDevice.getAddress())) {
                    upgradeState(STATE_CONNECTING);
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            rfduinoService = null;
            downgradeState(STATE_DISCONNECTED);
        }
    };


    //region BroadcastReceiver implementations

    private final BroadcastReceiver bluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
            if (state == BluetoothAdapter.STATE_ON) {
                upgradeState(STATE_DISCONNECTED);
            } else if (state == BluetoothAdapter.STATE_OFF) {
                downgradeState(STATE_BLUETOOTH_OFF);
            }
        }
    };


    private final BroadcastReceiver scanModeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            scanning = (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_NONE);
            scanStarted &= scanning;
            updateUi();
        }
    };


    private final BroadcastReceiver rfduinoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (RFduinoService.ACTION_CONNECTED.equals(action)) {
                upgradeState(STATE_CONNECTED);
            } else if (RFduinoService.ACTION_DISCONNECTED.equals(action)) {
                downgradeState(STATE_DISCONNECTED);
            } else if (RFduinoService.ACTION_DATA_AVAILABLE.equals(action)) {
//                addData(intent.getByteArrayExtra(RFduinoService.EXTRA_DATA));
                ByteBuffer buffer = ByteBuffer.wrap(intent.getByteArrayExtra(RFduinoService.EXTRA_DATA)).order(ByteOrder.LITTLE_ENDIAN);
                float val = buffer.getFloat();
                Log.d(TAG, String.valueOf(val));
            }
        }
    };

    //endregion

    public RFduinoActivity() {
        // Required empty public constructor
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rfduino);

        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Bluetooth
        enableBluetoothButton = (Button) findViewById(R.id.enableBluetooth);
        enableBluetoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                enableBluetoothButton.setEnabled(false);
//                enableBluetoothButton.setText(
//                        bluetoothAdapter.enable() ? "Enabling bluetooth..." : "Enable failed!");

                // Ensures Bluetooth is available on the device and it is enabled. If not,
// displays a dialog requesting user permission to enable Bluetooth.
                if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
            }
        });

        // Find Device
        scanStatusText = (TextView) findViewById(R.id.scanStatus);

        scanButton = (Button) findViewById(R.id.scan);


        // Device Info
        deviceInfoText = (TextView) findViewById(R.id.deviceInfo);

        // Connect Device
        connectionStatusText = (TextView) findViewById(R.id.connectionStatus);

        connectButton = (Button) findViewById(R.id.connect);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanStarted = true;
//                BluetoothLeScanner mBTscanner = mBluetoothAdapter.getBluetoothLeScanner();
//                List<ScanFilter> mBTdevices = new ArrayList<>();
//                mBTdevices.add(new ScanFilter.Builder().setServiceUuid(RFduinoService.PARCEL_UUID_SERVICE)
//                        .build());
////                ScanFilter uuidFilter = new ScanFilter.Builder().setServiceUuid(new UUID[]{RFduinoService.UUID_SERVICE});
//                ScanSettings btScanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
//                        .build();
//
//                ScanCallback btScanCallback = new ScanCallback() {
//                    @Override
//                    public void onScanResult(int callbackType, ScanResult result) {
//                        super.onScanResult(callbackType, result);
//                    }
//                };
//
//
//                mBTscanner.startScan(mBTdevices, btScanSettings, btScanCallback);

                mBluetoothAdapter.startLeScan(
                        new UUID[]{RFduinoService.UUID_SERVICE}, RFduinoActivity.this);
            }
        });

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);
                connectionStatusText.setText("Connecting...");
                Intent rfduinoIntent = new Intent(RFduinoActivity.this, RFduinoService.class);
                startService(rfduinoIntent);
                bindService(rfduinoIntent, rfduinoServiceConnection, BIND_AUTO_CREATE);

            }
        });
        // Send
//        valueEdit = (EditData) fragView.findViewById(R.id.value);
//        valueEdit.setImeOptions(EditorInfo.IME_ACTION_SEND);
//        valueEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if (actionId == EditorInfo.IME_ACTION_SEND) {
//                    sendValueButton.callOnClick();
//                    return true;
//                }
//                return false;
//            }
//        });
//
//        sendZeroButton = (Button) fragView.findViewById(R.id.sendZero);
//        sendZeroButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                rfduinoService.send(new byte[]{0});
//            }
//        });
//
//        sendValueButton = (Button) fragView.findViewById(R.id.sendValue);
//        sendValueButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                rfduinoService.send(valueEdit.getData());
//            }
//        });
//
//        // Receive
//        clearButton = (Button) fragView.findViewById(R.id.clearData);
//        clearButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dataLayout.removeAllViews();
//            }
//        });
//
//        dataLayout = (LinearLayout) fragView.findViewById(R.id.dataLayout);
    }


    @Override
    protected void onStart() {
        super.onStart();

        registerReceiver(scanModeReceiver, new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED));
        registerReceiver(bluetoothStateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        registerReceiver(rfduinoReceiver, RFduinoService.getIntentFilter());

        updateState(mBluetoothAdapter.isEnabled() ? STATE_DISCONNECTED : STATE_BLUETOOTH_OFF);
    }

    @Override
    protected void onStop() {
        super.onStop();

        unbindService(rfduinoServiceConnection);

        mBluetoothAdapter.stopLeScan(this);

        unregisterReceiver(scanModeReceiver);
        unregisterReceiver(bluetoothStateReceiver);
        unregisterReceiver(rfduinoReceiver);
    }


    private void upgradeState(int newState) {
        if (newState > state) {
            updateState(newState);
        }
    }

    private void downgradeState(int newState) {
        if (newState < state) {
            updateState(newState);
        }
    }

    private void updateState(int newState) {
        state = newState;
        updateUi();
    }

    private void updateUi() {
        // Enable Bluetooth
        boolean on = state > STATE_BLUETOOTH_OFF;
        enableBluetoothButton.setEnabled(!on);
        enableBluetoothButton.setText(on ? "Bluetooth enabled" : "Enable Bluetooth");
        scanButton.setEnabled(on);

        // Scan
        if (scanStarted && scanning) {
            scanStatusText.setText("Scanning...");
            scanButton.setText("Stop Scan");
            scanButton.setEnabled(true);
        } else if (scanStarted) {
            scanStatusText.setText("Scan started...");
            scanButton.setEnabled(false);
        } else {
            scanStatusText.setText("");
            scanButton.setText("Scan");
            scanButton.setEnabled(true);
        }

        // Connect
        boolean connected = false;
        String connectionText = "Disconnected";
        if (state == STATE_CONNECTING) {
            connectionText = "Connecting...";
        } else if (state == STATE_CONNECTED) {
            connected = true;
            connectionText = "Connected";
        }
        connectionStatusText.setText(connectionText);
        connectButton.setEnabled(bluetoothDevice != null && state == STATE_DISCONNECTED);
    }

    @Override
    public void onLeScan(BluetoothDevice device, final int rssi, final byte[] scanRecord) {
        mBluetoothAdapter.stopLeScan(this);
        bluetoothDevice = device;

        RFduinoActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                deviceInfoText.setText(
                        BluetoothHelper.getDeviceInfoText(bluetoothDevice, rssi, scanRecord));
                updateUi();
            }
        });
    }

}
