package be.kuleuven.mech.www.bluetooth_mobile_platform;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import android.view.View.OnClickListener;
import android.view.Window;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class MainActivity extends Activity implements OnClickListener
{
    // Debugging

    private static final String TAG = "MainActivity";
    private static final boolean D = true;

    // Message types sent from the BTService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    public static final String mscan = null;

    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int CLOSE_CONNECT  = 3;

    //private TextView mTitle;
    private TextView mScan;
    private TextView mblutooth;
    //private TextView mGravity;

    private Button forwardButton;
    private Button leftButton;
    //private Button stopButton;
    private Button rightButton;
    private Button backButton;
    private Button switchButton;


    //private Button gravityButton;
    private SensorManager sensorMgr = null;
    private Sensor sensor;
    private SensorEventListener lsn;
    TextView xViewA = null;
    TextView yViewA = null;
    TextView zViewA = null;
    private float X = 0;
    private float Y = 0;
    private float Z = 0;


	/*
    private byte[] FORWARD = {(byte) 0xFF, 0x5A, 0x5B, 0x00, 0x01, (byte) 0xFF};
    private byte[] BACK = {(byte) 0xFF, 0x5A, 0x5B, 0x00, 0x05, (byte) 0xFF};
    private byte[] STOP = {(byte) 0xFF, 0x5A, 0x5B, 0x00, 0x03, (byte) 0xFF};
    private byte[] LEFT = {(byte) 0xFF, 0x5A, 0x5B, 0x00, 0x02, (byte) 0xFF};
    private byte[] RIGHT = {(byte) 0xFF, 0x5A, 0x5B, 0x00, 0x04, (byte) 0xFF};
    */
    String FORWARD = "w";
    String BACK = "x";
    String STOP = "s";
    String LEFT = "a";
    String RIGHT = "d";
    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the main services
    private BTService mService = null;




    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(D) Log.e(TAG, "+++ ON CREATE +++");

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        //getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //Set up the custom title
        //mTitle = (TextView)findViewById(R.id.title_left_text);
        //mTitle.setText(R.string.app_name);
        //mTitle = (TextView)findViewById(R.id.title_right_text);

        mScan = (TextView)findViewById(R.id.scan_button);
        mblutooth = (TextView)findViewById(R.id.switch_button);
        //mGravity = (TextView)findViewById(R.id.gravity_button);


        // Set up click listeners for all the buttons
        View scanButton=this.findViewById(R.id.scan_button);
        scanButton.setOnClickListener(this);
        View aboutButton=this.findViewById(R.id.about_button);
        aboutButton.setOnClickListener(this);
        View exitButton=this.findViewById(R.id.exit_button);
        exitButton.setOnClickListener(this);
        View gravityopen=this.findViewById(R.id.gravity_open);
        gravityopen.setOnClickListener(this);
        View gravityclose=this.findViewById(R.id.gravity_close);
        gravityclose.setOnClickListener(this);


        /**�õ�SensorManager����**/
        sensorMgr  = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null)
        {
            // Device does not support Bluetooth �豸��֧������
            Toast.makeText(this, "Bluetooth Adapter does not exist",Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        switchButton =  (Button) findViewById(R.id.switch_button);
        switchButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if(mBluetoothAdapter.isEnabled()){
                    mBluetoothAdapter.disable();
                    mblutooth.setText("Off");
                }
                else{
                    mblutooth.setText("On");
                    //mBluetoothAdapter.enable();
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                }
            }
        });

     /*   gravityButton = (Button)findViewById(R.id.gravity_button);
        gravityButton.setOnClickListener(new OnClickListener()
        {
        	public void onClick(View v) {
        		mTitle.setText("����������Ӧ");
        		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        		Gravity();
            	sensorMgr.registerListener(lsn, sensor, SensorManager.SENSOR_DELAY_NORMAL);
             }
         });
         */
    }



    @Override
    public void onStart()
    {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");
        // If BT is not on, request that it be enabled.
        //
        if (!mBluetoothAdapter.isEnabled()) {
            // mBluetoothAdapter.enable();
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);

        }
        else {
            if (mService == null) directionControl();
        }
    }


    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {

            case R.id.about_button:
                Intent about=new Intent(this,About.class);
                startActivity(about);
                break;

            case R.id.scan_button:
                if (mService.getState() != BTService.STATE_CONNECTED){
                    Intent serverIntent = new Intent(this, Scan.class);
                    startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                }else{
                    mScan.setText("No connection");
                    mService.close();
                    //mTitle.setText(R.string.title_disconnected);
                }
                break;

            case R.id.exit_button:
                mService.close();
                //mBluetoothAdapter.disable();
                finish();
                break;

            case R.id.gravity_open:
                Toast.makeText(this, "Enable gravity control", Toast.LENGTH_SHORT).show();
                Gravity();
                sensorMgr.registerListener(lsn, sensor, SensorManager.SENSOR_DELAY_NORMAL);
                break;

            case R.id.gravity_close:
                Toast.makeText(this, "Disable gravity control", Toast.LENGTH_SHORT).show();
                sensorMgr.unregisterListener(lsn);
                break;

            // More buttons go here (if any) ...
        }
    }


    /**
     *
     */
    private void directionControl() {
        Log.d(TAG, "directionControl()");

        forwardButton = (Button)findViewById(R.id.forward_button);
        forwardButton.setOnTouchListener(new Button.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch(action)
                {
                    case MotionEvent.ACTION_DOWN:
                        sendMessage(FORWARD);
                        break;
                    case MotionEvent.ACTION_UP:
                        sendMessage(STOP);
                        break;
                }
                return false;
            }
        });

        leftButton = (Button) findViewById(R.id.left_button);
        leftButton.setOnTouchListener(new Button.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch(action)
                {
                    case MotionEvent.ACTION_DOWN:
                        sendMessage(LEFT);
                        break;
                    case MotionEvent.ACTION_UP:
                        sendMessage(STOP);
                        break;
                }
                return false;
            }
        });

        rightButton = (Button) findViewById(R.id.right_button);
        rightButton.setOnTouchListener(new Button.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch(action)
                {
                    case MotionEvent.ACTION_DOWN:
                        sendMessage(RIGHT);
                        break;
                    case MotionEvent.ACTION_UP:
                        sendMessage(STOP);
                        break;
                }
                return false;
            }
        });

        backButton = (Button) findViewById(R.id.back_button);
        backButton.setOnTouchListener(new Button.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch(action)
                {
                    case MotionEvent.ACTION_DOWN:
                        sendMessage(BACK);
                        break;
                    case MotionEvent.ACTION_UP:
                        sendMessage(STOP);
                        break;
                }
                return false;
            }
        });

        // Initialize the BTService to perform bluetooth connections
        mService = new BTService(this, mHandler);
    }

    /**
     *
     */
    private void Gravity(){
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        lsn = new SensorEventListener(){
            public void onSensorChanged(SensorEvent event) {
                X = event.values[SensorManager.DATA_X];
                Y = event.values[SensorManager.DATA_Y];
                Z = event.values[SensorManager.DATA_Z];
                gravityControl();
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // TODO Auto-generated method stub
            }
        };
    }
    /** Gravity control **/
    public void gravityControl(){

        if(Y <= -2.0 && Y >= -9){
            sendMessage(LEFT);
        }
        else if(Y >= 2.0 && Y <= 9){
            sendMessage(RIGHT);
        }
        else if(X <= -2.0 && X >= -9){
            sendMessage(FORWARD);
        }
        else if(X >= 2.0 && X <= 9){
            sendMessage(BACK);
        }
        else{
            sendMessage(STOP);
        }
    }

    /**
     *
     */
    private void sendMessage (String message) {
        // Check that we're actually connected before trying anything
        if (mService.getState() != BTService.STATE_CONNECTED) {
            //Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes
            byte[] send = message.getBytes();
            mService.write(send);
        }
     /*
        else {
        try {
        	mService.write(message);
        	}
        catch (Exception e) {
        	}
        }*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorMgr.registerListener(lsn, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        if(D) Log.e(TAG, "- ON PAUSE -");
        sensorMgr.unregisterListener(lsn);
    }

    @Override
    public void onStop() {
        super.onStop();
        if(D) Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(D) Log.e(TAG, "--- ON DESTROY ---");
    }

    private void ensureDiscoverable()
    {
        if(D) Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE)
        {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    // The Handler that gets information back from the BTService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage (Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BTService.STATE_CONNECTED:
                            Toast.makeText(getApplicationContext(), R.string.title_connecting,
                                    Toast.LENGTH_SHORT).show();
                            //mTitle.setText(R.string.title_connected_to);
                            //mTitle.append(mConnectedDeviceName);
                            mScan.setText("Connected");
                            break;
                        case BTService.STATE_CONNECTING:
                            //mTitle.setText(R.string.title_connecting);
                            break;
                        case BTService.STATE_LISTEN:
                            //mTitle.setText("listen...");
                        case BTService.STATE_NONE:
                            //mTitle.setText(R.string.title_not_connected);
                            mScan.setText("No connection");
                            break;
                        case BTService.STATE_CLOSE:
                            Toast.makeText(getApplicationContext(), R.string.title_disconnected,
                                    Toast.LENGTH_SHORT).show();
                            //mTitle.setText(R.string.title_disconnected);
                            mScan.setText("Bluetooth turned off");
                            break;

                    }
                    break;

                /*
            case MESSAGE_WRITE:
            	byte[] writeBuf = (byte[]) msg.obj;
            	// construct a string from the buffer
            	String writeMessage = new String(writeBuf);
            	mConversationArrayAdapter.add("Me:  " + writeMessage);
                break;
            case MESSAGE_READ:
            	byte[] readBuf = (byte[]) msg.obj;
            	// construct a string from the valid bytes in the buffer
            	String readMessage = new String(readBuf, 0, msg.arg1);
            	mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);
            	break;
            	   */

                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;

            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case CLOSE_CONNECT:
                connectDevice(data, false);

            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    directionControl();
                }
                else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(Scan.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object  BluetoothDevice
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

        // Attempt to connect to the device
        mService.connect(device, secure);

    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //  Intent serverIntent = null;
        switch (item.getItemId()) {
   /* 		case R.id.secure_connect_scan:
            // Launch the Scan to see devices and do scan
        	Intent serverIntent = new Intent(this, Scan.class);
       		startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
            return true;
        case R.id.insecure_connect_scan:
            // Launch the Scan to see devices and do scan
            serverIntent = new Intent(this, Scan.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
            return true;
            */
            case R.id.discoverable:
                // Ensure this device is discoverable by others
                ensureDiscoverable();
                return true;
        }
        return false;
    }
}
