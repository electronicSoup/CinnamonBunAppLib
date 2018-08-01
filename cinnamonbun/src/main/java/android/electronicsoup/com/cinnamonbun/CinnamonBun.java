package android.electronicsoup.com.cinnamonbun;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbAccessory;

import android.os.IBinder;
import android.os.Messenger;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.Handler;
import android.os.RemoteException;
import android.view.View;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.lang.String;
import java.util.ArrayList;
import java.util.Random ;

/**
 * \class CinnamonBun
 *
 * \brief Class for utalising the electroinicSoup Cinnamon Bun.
 *
 * This class manages all interactions with the Android USB Accessory enabled
 * electronicSoup Cinnamon Bun. The class provides a number of methods for 
 * managing and sending data to the Bun and on instantiation the Application
 * passes in the necessary parameter to enable the class to inform the 
 * Application via messages of status changes and events received from 
 * the Bun.
 */
public class CinnamonBun extends Handler
{
    private static CinnamonBun sInstance = null;

    public static final int WHAT_CINNAMON_BUN_MSG       = 0;
    public static final int WHAT_CINNAMON_BUN_STATUS    = 1;
    public static final int WHAT_HEX_FILE               = 2;
    public static final int WHAT_ISO15765               = 3;

    public static final byte CINNAMON_BUN_INITIALISING  = 0;
    public static final byte CINNAMON_BUN_DISCONNECTED  = 1;
    public static final byte CINNAMON_BUN_CONNECTED     = 2;

    /*
     * Tag String used in Android Logging messages.
     */
    private String TAG = "CinnamonBun";

    private Context mContext = null;
    private FragmentManager mFragmentManager = null;

    private int mContainerId = 0;

    private byte                 mStatus = CINNAMON_BUN_INITIALISING;
    private String               mActionString = null;
    private PendingIntent        mPermissionIntent;
    private ParcelFileDescriptor mParcelFileDescriptor = null;
    private ReadThread           mReadThread = null;
    private FileOutputStream     mOutputStream = null;
    private Boolean              mPermissionRequested = false;
    private CanL2Dispatcher      mCanL2Dispatcher = null;
    private CanStatus            mCanStatus = null;
    private Iso15765Dispatcher   mIso15765Dispatcher = null;

    /*
     * Convienence method to enable printing a byte value
     */
    static public String byteToString(byte value)
    {
	if(value < 16) {
	    return ("0x0" + Integer.toHexString(((int)value) & 0xff));
	} else {
	    return ("0x" + Integer.toHexString(((int)value) & 0xff));
	}
    }

    public static CinnamonBun getInstance() {
        return sInstance;
    }

    public static CinnamonBun createInstance(Context context, FragmentManager fragmentManager, int containerId, Fragment initialFragment) {
        if(sInstance == null) {
            Log.d("CinnamonBun", "No current instance so call the Constructor");
            sInstance = new CinnamonBun(context, fragmentManager, containerId, initialFragment);
        }

        sInstance.mContext = context;
        sInstance.mContainerId = containerId;

        sInstance.mCanL2Dispatcher = new CanL2Dispatcher();
        sInstance.mIso15765Dispatcher = new Iso15765Dispatcher();

        sInstance.mFragmentManager = fragmentManager;

        Fragment fragment = fragmentManager.findFragmentById(containerId);

        if(fragment == null) {
            fragmentManager.beginTransaction().add(containerId, initialFragment).commit();
        }

        return sInstance;
    }

    /*
     * Constructor
     */
    private CinnamonBun(Context context, FragmentManager fragmentManager, int containerId, Fragment initialFragment) {
        Log.d(TAG, "CinnamonBun Constructor");

        this.mContext = context;
        this.mContainerId = containerId;
                
        this.mFragmentManager = fragmentManager;
        this.mStatus = CINNAMON_BUN_DISCONNECTED;
        this.mCanStatus = new CanStatus(CanStatus.L2_Uninitialised, CanStatus.no_baud);

        // Grab the packageName to use for an attach Intent
        mActionString = mContext.getPackageName() + ".action.USB_PERMISSION";
        Log.d(TAG, "mActionString is:" + mActionString);

        mPermissionIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(mActionString), 0);
        IntentFilter filter = new IntentFilter(mActionString);

        // Also add a few other actions to the intent...
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);

        // and register the intent with the specified context
        mContext.registerReceiver(receiver, filter);

        Log.d(TAG, "onCreate connectAccessory()");
        if(connectAccessory()) {
            setStatus(CINNAMON_BUN_CONNECTED);
        } else {
            setStatus(CINNAMON_BUN_DISCONNECTED);
        }
    }

    public void canConnect(byte baud_rate) {
        byte[] data = new byte[1];
        Log.d(TAG, "can_connect at baud rate : " + CanStatus.getBaudRateString(baud_rate));

        data[0] = baud_rate;

	data[0] = baud_rate;
	
        if(new AppMessage(AppMessage.APP_MSG_CAN_CONNECT, data).send(mOutputStream)) {
            Log.d(TAG, "CAN Connect message sent");
        } else {
            Log.d(TAG, "CAN Connect message Failed");
        }
    }

    private void setStatus(byte status) {
        if(mStatus != status) {
            mStatus = status;
            sendStatusMsgToApp(mStatus);

            /*
             * Request the CAN Status from the Connected CinnamonBun
             */
            if(new AppMessage(AppMessage.APP_MSG_CAN_STATUS_REQ).send(mOutputStream)) {
                Log.d(TAG, "CAN Status request sent");
            } else {
                Log.d(TAG, "CAN Status request Failed");
            }
        }
    }

    public byte getStatus() {
//      return(CINNAMON_BUN_CONNECTED);
        return(mStatus);
    }

    public CanStatus getCanStatus() {
        return(mCanStatus);
    }

    /*
     * stop() method halts receiving messages from CAN Bus Dongle
     */
    public void stop() {
        if(sInstance != null) {
            Log.d(TAG, "Stop");
            try {
                if(new AppMessage(AppMessage.APP_MSG_APP_DISCONNECT).send(mOutputStream)) {
                    Log.d(TAG, "Disconnect message sent");
                } else {
                    Log.d(TAG, "Disconnect message Failed");
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to send the Disconnect message to CinnamonBun");
            }
            if(mReadThread != null) {
                mReadThread.cancel();
            }

            try {
                mContext.unregisterReceiver(receiver);
            } catch (Exception e) {
                Log.e(TAG, "Failed to send UnRegister Receiver");
            }
            Log.d(TAG, "Clearing CinnamonBun Instance");
            sInstance = null;
        }
    }

    public void setFragment(Fragment fragment) {
        mFragmentManager.beginTransaction().replace(mContainerId, fragment).commit();
    }

    public void forwardMessageToFragment(Iso15765Message iso15765Msg) {
        Message msg = this.obtainMessage(WHAT_ISO15765, iso15765Msg);

        Fragment fragment = mFragmentManager.findFragmentById(mContainerId);

        if(fragment != null) {
            Fragment nextFragment = (Fragment)((StateInterface)fragment).processMessage(msg);
            if(nextFragment != null) {
                setFragment(nextFragment);
            }
        } else {
            Log.e(TAG, "No Fragment to process message");
        }
    }

    @Override
    public void handleMessage(Message msg) {
        Log.d(TAG, "Event Handler handleMessage");
        Fragment fragment = mFragmentManager.findFragmentById(mContainerId);

        if (msg.what == CinnamonBun.WHAT_CINNAMON_BUN_MSG) {
            Log.d(TAG, "Got a CinnamonBun Message");

            BunMessage bunMsg = (BunMessage)msg.obj;

            if(bunMsg.id() == BunMessage.BUN_MSG_CAN_L2_FRAME) {
                Log.d(TAG, "Trapping a CAN Frame message -> dispatcher");
                /*
                 * Don't forward Can L2 Frames to Application unless it has 
                 * registered a target for the message. Send it to the 
                 * dispatcher and return.
                 */
                CanL2Frame frame = new CanL2Frame(bunMsg.data());

                mCanL2Dispatcher.dispatchFrame(frame);

                return;
            } else if(bunMsg.id() == BunMessage.BUN_MSG_ISO15765_MSG) {
                Log.d(TAG, "BunMessage.BUN_MSG_ISO15765_MSG -> Dispatcher");

                Iso15765Message message = new Iso15765Message(bunMsg.data());

                mIso15765Dispatcher.dispatchMessage(message);

                return;
            } else if(bunMsg.id() == BunMessage.BUN_MSG_ISO11783_MSG) {
                Log.d(TAG, "BunMessage.BUN_MSG_ISO11783_MSG");
            }
        }

        if(fragment != null) {
            Fragment nextFragment = (Fragment)((StateInterface)fragment).processMessage(msg);
            if(nextFragment != null) {
                setFragment(nextFragment);
            }
        } else {
            Log.e(TAG, "No Fragment to process message");
        }
    } // handleMessage

//    @Override
//    public void handleMessage(Message msg) {
//        Log.d(TAG, "EscbAppApi:handleMessage() From source " + msg.what);
//
//              if(msg.what ==  this.whatSource) {
//            BunMessage bunMsg = (BunMessage)msg.obj;
//                      this.bunMsgHandler.handleMessage(msg);
//              }
//    } //handleMessage

    // Create a BroadcastReceiver for USB events
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            /* get the action for this event */
            String action = intent.getAction();

            Log.d(TAG, "BroadcastReceiver OnReceive" + action);
                        
            /*
             * if it corresponds to the packageName, then it was a permissions
             * grant request
             */
            if (mActionString.equals(action)) {
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    synchronized (this) {
                        try {
                            Toast.makeText(context, "Permission", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "BroadcastReceiver Permission connectAccessory()");
                            if(connectAccessory()) {
                                setStatus(CINNAMON_BUN_CONNECTED);
                            } else {
                                setStatus(CINNAMON_BUN_DISCONNECTED);
                            }

                        } catch (Exception e) {
                            Log.d(TAG, "Bun Error" + e.getMessage());
                            Log.d(TAG, e.getMessage());
                        }
                    }
                } else {
                    Log.d(TAG, "permission denied for accessory ");
                }
            } else if (UsbManager.ACTION_USB_ACCESSORY_ATTACHED.equals(action)) {
                Log.d(TAG, "ACTION_USB_ACCESSORY_ATTACHED");
                try {
                    Toast.makeText(context, "Attached", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "BroadcastReciver Device Attached connectAccessory()");
                    if(connectAccessory()) {
                        setStatus(CINNAMON_BUN_CONNECTED);
                    } else {
                        setStatus(CINNAMON_BUN_DISCONNECTED);
                    }

                } catch (Exception e) {
                    Log.d(TAG, "Bun Error" + e.getMessage());
                }
            } else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
                try{
                    mPermissionRequested = false;
                                      
                    Log.d(TAG, "Bun Detached");
                    if(mReadThread != null){
                        mReadThread.cancel();
                        mReadThread = null;
                    }

                    if(mOutputStream != null) {
                        mOutputStream.close();
                        mOutputStream = null;
                    } 

                    if(mParcelFileDescriptor != null){
                        mParcelFileDescriptor.close();
                        mParcelFileDescriptor = null;
                    }
                } catch (IOException e) {
                    Log.d(TAG, "IO Exception detaching Bun");
                }
                setStatus(CINNAMON_BUN_DISCONNECTED);
	    } else {
                Log.d(TAG, "UNPROCESSED");
            }
        }
    };

    public void sendFileMsgToFragment(File file) {
        Log.d(TAG, "sendFileMsgToFragment()");
        this.obtainMessage(WHAT_HEX_FILE, file).sendToTarget();
    }

    private void sendStatusMsgToApp(byte status) {
        BunMessage msg = new BunMessage(status);
        this.obtainMessage(WHAT_CINNAMON_BUN_STATUS, msg).sendToTarget();
    }

    private void processBunMessage(BunMessage msg) {
//              Log.d(TAG, "Received Unprocessed Message from BUN " + msg.id());
        /*
         * Before we send it on take not of any status messages
         */
        if(msg.id() == BunMessage.BUN_MSG_CAN_STATUS) {
            Log.d(TAG, "Trapping a CAN Status message");

            byte[] data = msg.data();

            for(int i = 0; i < data.length; i++) {
                Log.d(TAG, "Byte " + Integer.toString(i) + ":" + CinnamonBun.byteToString(data[i]));
            }
            mCanStatus.setCanStatus(data[0]);
            mCanStatus.setBaudRate(data[1]);
        } 
        this.obtainMessage(WHAT_CINNAMON_BUN_MSG, msg).sendToTarget();
    }

    /**
     * The thread for reading data from the Bun FileInputStream so the main 
     * thread doesn't get blocked.
     */
    private class ReadThread extends Thread {
        private String TAG = "CinnamonBun";
        private boolean continueRunning = true;

        private final int RX_BUFFER_SIZE = 1024;
        private byte[] rxBuffer = new byte[RX_BUFFER_SIZE]; 
        private int rxWriteIndex = 0;
        private int rxReadIndex = 0;
        private int rxBufferCount = 0;

        private FileInputStream inputStream;
        private ParcelFileDescriptor myParcelFileDescriptor;

        public ReadThread(ParcelFileDescriptor p) {
            myParcelFileDescriptor = p;
            inputStream = new FileInputStream(p.getFileDescriptor());
            Log.d(TAG, "ReadThread - Created");
        }

        @Override
        public void run() {
            byte[] buffer = new byte[RX_BUFFER_SIZE]; // buffer store for the stream
            int bytes; // bytes returned from read()

            Log.d(TAG, "ReadThread - run() ");
            
            while (continueRunning) {
                try {
                    Log.d(TAG, "ReadThread - Attempt read");
                    // Read from the InputStream
                    bytes = inputStream.read(buffer);

                    Log.d(TAG, "ReadThread Rx'd " + bytes +" Bytes. Current BufferCount " + rxBufferCount);

                    //
                    // Copy the read bytes into the rxBuffer
                    //
                    for(int loop = 0; loop < bytes; loop++){
                        rxBuffer[rxWriteIndex] = buffer[loop];

                        if( ((rxWriteIndex + 1) % RX_BUFFER_SIZE) == rxReadIndex) {
                            Log.e(TAG, "ReadThread - ERROR RX Circular buffer full rxReadIndex-" + rxReadIndex + " rxBufferCount-" +rxBufferCount);
                            break;
                        }

                        rxWriteIndex = (rxWriteIndex + 1) % RX_BUFFER_SIZE;
                        rxBufferCount++;
                    }

                    Log.d(TAG, "ReadThread - Bytes moved to circular buffer BufferCount " + rxBufferCount);

                    //
                    // Check the size of the next message
                    // and check is there enough data in the rxBuffer for 
                    // a complete message
                    //
                    int index = rxReadIndex;
                    int size = rxBuffer[index] & 0xff;

                    index = (index + 1) % RX_BUFFER_SIZE;

                    size = (size << 8) | (rxBuffer[index] & 0xff);
                    Log.d(TAG, "Received a message size " + size);

                    while( (rxBufferCount > 2) && (size <= rxBufferCount) ) {
                        BunMessage bunMsg;

                        // Move forward in circular buffer past 2 size bytes
                        rxReadIndex = (rxReadIndex + 1) % RX_BUFFER_SIZE;
                        rxBufferCount--;
                        rxReadIndex = (rxReadIndex + 1) % RX_BUFFER_SIZE;
                        rxBufferCount--;

                        byte id = (byte)(((int)rxBuffer[rxReadIndex]) & 0xff);

                        rxReadIndex = (rxReadIndex + 1) % RX_BUFFER_SIZE;
                        rxBufferCount--;

                        if(size == 1) {
                            bunMsg = new BunMessage(id);
                        } else {                                                                
                            byte[] data = new byte[size - 1];
                            for(int loop = 0; loop < size - 1; loop++) {
                                data[loop] = rxBuffer[rxReadIndex];
                                
                                rxReadIndex = (rxReadIndex + 1) % RX_BUFFER_SIZE;
                                rxBufferCount--;
                            }
                            Log.d(TAG, "ReadThread - bun data size:" + data.length);
                            bunMsg = new BunMessage(id, data);
                        }
                        //Log.d(TAG, "ReadThread - Message removed from ciruclar buffer. rxBufferCount:" + rxBufferCount);
                        processBunMessage(bunMsg);
                    }
                } catch (IOException e) {
                    // Exiting read thread
                    break;
                }
            }
        }

        public void cancel() {
            Log.d(TAG, "ReadThread - Cancel");
            continueRunning = false;
            try {
                inputStream.close();
            } catch (IOException e) {
            }
                        
            try {
                myParcelFileDescriptor.close();
            } catch (IOException e) {
            }
        }
    }

    private Boolean connectAccessory() {
        Log.d(TAG, "ConnectAccessory");
        mParcelFileDescriptor = getConnectedAccessoryFileDescriptor();
        if (mParcelFileDescriptor != null) {
            Log.d(TAG, "Got a mParcelFileDescriptor");
            // Create a new read thread to handle reading data from
            // the accessory
            if(mReadThread == null) {
                Log.d(TAG, "ReadThread null so set it up");
                mReadThread = new ReadThread(mParcelFileDescriptor);
                mReadThread.start();
                    
                // Open the output file stream for writing data out to
                // the accessory
                mOutputStream = new FileOutputStream(mParcelFileDescriptor.getFileDescriptor());

                // SendMessage to the Accessory
                if(new AppMessage(AppMessage.APP_MSG_APP_CONNECT).send(mOutputStream)) {
                    Log.d(TAG, "App Connect message sent to CinnamonBun");
                    return(true);
                } else {
                    Log.d(TAG, "Connect message Failed");
                }
            } else {
                Log.d(TAG, "ReadThread already setup");
                return true;
            }
        } else {
            Log.d(TAG, "ParcelFileDescriptor Null");

            /*
             * If we weren't able to open the ParcelFileDescriptor,
             * then we will not be able to talk to the device. Due
             * to a bug in the Android v2.3.4 OS this situation may
             * occur if a user presses the "home" or "back" buttons
             * while an accessory is still attached. In this case
             * the attempt to close the ReadThread will fail if a
             * read() is in progress on the FileInputStream. This
             * results in the ParcelFileDescriptor not being freed
             * up for later access. A future attempt to connect to
             * the accessory (via reopening the app) will end up
             * having the openAccessory() request return null,
             * ending up in this section of code.
             */
        }
        return false;
    }
    
    private ParcelFileDescriptor getConnectedAccessoryFileDescriptor(){
        UsbManager deviceManager = null;
        UsbAccessory[] accessories = null;
        UsbAccessory accessory = null;
        ParcelFileDescriptor fileDescriptor = null;

        Log.d(TAG, "getConnectedAccessory()");

        //deviceManager = UsbManager.getInstance(mContext);
        deviceManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);

        if (deviceManager != null) {
            // Get a list of all of the accessories from the UsbManager
            accessories = deviceManager.getAccessoryList();
            
            // If the list of accessories is empty, then throw exception
            if (accessories != null) {
                // Get the first accessory in the list (currently the Android OS
                // only supports one accessory, so this is it)
                accessory = accessories[0];
                
                // If the accessory isn't null, then let's try to attach to it.
                if (accessory != null) {
                    // If we have permission to access the accessory,
                    if (deviceManager.hasPermission(accessory)) {
                        Log.d(TAG, "Accessory found");
                        return(deviceManager.openAccessory(accessory));
                    } else {
                        Log.d(TAG, "Permission required");
                        if(!mPermissionRequested) {
                            /*
                             * Request permission
                             */
                            Log.d(TAG, "Request Permission to use Accessory");
                            deviceManager.requestPermission(accessory, mPermissionIntent);
                            mPermissionRequested = true;
                        } else {
                            Log.d(TAG, "ERROR - No Permission");
                        }
                    }
                } else {
                    Log.d(TAG, "ERROR - Accessory NULL");
                }
            } else {
                Log.d(TAG, "ERROR - Accessory List NULL");
            }
        } else {
            Log.d(TAG, "ERROR - Device Manager NULL");
        }
        return null;
    }

//    public Boolean sendMessage(AppMessage msg) {
//        if(mOutputStream != null) {
//            Log.d(TAG, "sendMessage()");
//
//            if(msg.write(mOutputStream)) {
//                return(true);
//            } else {
//                Log.d(TAG, "Failed to send message to the Dongle!");
//            }
//        } else {
//            Log.d(TAG, "USBAccessoryManager mOutputStream == null");
//        }
//        return(false);
//    }

    public FileOutputStream getOutputStream() {
        return(mOutputStream);
    }

    protected void registerTarget(CanL2Target target) {
        Log.d(TAG, "CinnamonBun registerTarget() Can Frame Target");
        mCanL2Dispatcher.registerHandler(target);
    }

    protected void registerTarget(Iso15765Target target) {
        Log.d(TAG, "registerTarget() ISO15765");
        mIso15765Dispatcher.registerHandler(target);
    }

    public Context getContext() {
        return(mContext);
    }
}
