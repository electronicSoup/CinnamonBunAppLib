package android.electronicsoup.com.cinnamonbun;

import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * App message structure.
 *
 * A message from the Android App to the Connected Cinnamon Bun consists of 
 * two Bytea containing the byte count of the bytes to follow. Then a single 
 * byte identifier and an array of data bytes.
 */
public class AppMessage 
{
    static final String TAG = "AppMessage";

    public static final byte APP_MSG_ERROR            = (byte)0x00;
    public static final byte APP_MSG_APP_DISCONNECT   = (byte)0x01;
    public static final byte APP_MSG_APP_CONNECT      = (byte)0x02;
	public static final byte APP_MSG_FLASH_REPROGRAM  = (byte)0x03;
	public static final byte APP_MSG_FLASH_ERASE_PAGE = (byte)0x04;
	public static final byte APP_MSG_FLASH_WRITE_ROW  = (byte)0x05;
	public static final byte APP_MSG_FLASH_REFLASHED  = (byte)0x06;
    public static final byte APP_MSG_CAN_CONNECT      = (byte)0x07;
    public static final byte APP_MSG_CAN_STATUS_REQ   = (byte)0x08;
    public static final byte APP_MSG_CAN_L2_FRAME     = (byte)0x09;
    public static final byte APP_MSG_CAN_L2_TARGET    = (byte)0x0a;
    public static final byte APP_MSG_ISO15765_MSG     = (byte)0x0b;
    public static final byte APP_MSG_ISO15765_TARGET  = (byte)0x0c;
    public static final byte APP_MSG_ISO11783_MSG     = (byte)0x0d;
    public static final byte APP_MSG_ISO11783_TARGET  = (byte)0x0e;

    public static final byte APP_MSG_USER_OFFSET      = (byte)0x20;

	private byte             mId       = 0;
	private byte[]           mData     = null;
    private byte[]           mTxBuffer = null;

    public AppMessage (byte id) {
        mTxBuffer = new byte[3];
		mId = id;
	
        mTxBuffer[0] = 0x00; //  Number of bytes following this first count
        mTxBuffer[1] = 0x01; //  Number of bytes following this first count
        mTxBuffer[2] = id;
	}   

    public AppMessage (byte id, byte[] data) {
		mId = id;
        mData = data;

		mTxBuffer = new byte[data.length + 3];
		mTxBuffer[0] = (byte)(((data.length + 1) >> 8) & 0xff);
		mTxBuffer[1] = (byte)((data.length +1) & 0xff);

		/*
         * Add the message ID to the transmit buffer.
		 */
		mTxBuffer[2] = id;

		for(int loop = 0; loop < data.length; loop++) {
			mTxBuffer[loop + 3] = data[loop];
		}
    }

    public AppMessage (CanL2Frame frame) {
        byte loop;
		byte[] frameArray = frame.asByteArray();

		mId = APP_MSG_CAN_L2_FRAME;

		/*
		 * Get a transmit buffer. We need 3 for USB APP Message id and size 
		 * plus 5 for CAN Id and data size byte, plus the bytes for CAN Data.
		 */
		mTxBuffer = new byte[frameArray.length + 3];

        mTxBuffer[0] = 00; //Size
        mTxBuffer[1] = (byte)((byte)mTxBuffer.length - (byte)2); //Size
        mTxBuffer[2] = APP_MSG_CAN_L2_FRAME;

		for(loop = 0; loop < frameArray.length; loop++) {
			mTxBuffer[3 + loop] = frameArray[loop];
		}
    }

    public AppMessage (CanL2Target target) {
        byte loop;
		byte[] targetArray = target.asByteArray();

		mId = APP_MSG_CAN_L2_TARGET;

		/*
		 * Get a transmit buffer. We need 3 for USB APP Message id and size 
		 * plus 5 for CAN Id and data size byte, plus the bytes for CAN Data.
		 */
		mTxBuffer = new byte[targetArray.length + 3];

        mTxBuffer[0] = 00; //Size
        mTxBuffer[1] = (byte)((byte)mTxBuffer.length - (byte)2); //Size
        mTxBuffer[2] = mId;

		for(loop = 0; loop < targetArray.length; loop++) {
			mTxBuffer[3 + loop] = targetArray[loop];
		}
    }

    public AppMessage (Iso15765Message msg) {
        int loop;
		byte[] frameArray = msg.asByteArray();

		mId = APP_MSG_ISO15765_MSG;

		/*
		 * Get a transmit buffer. We need 3 for USB APP Message id and size 
		 * plus 5 for CAN Id and data size byte, plus the bytes for CAN Data.
		 */
		mTxBuffer = new byte[frameArray.length + 3];

		byte[] bytes = ByteBuffer.allocate(4).putInt(mTxBuffer.length - 2).array();

        mTxBuffer[0] = bytes[2];
        mTxBuffer[1] = bytes[3];
        mTxBuffer[2] = APP_MSG_ISO15765_MSG;

		for(loop = 0; loop < frameArray.length; loop++) {
			mTxBuffer[3 + loop] = frameArray[loop];
		}
    }

    public AppMessage (Iso15765Target target) {
        byte loop;
		byte[] targetArray = target.asByteArray();

		mId = APP_MSG_ISO15765_TARGET;

		/*
		 * Get a transmit buffer. 
		 */
		mTxBuffer = new byte[targetArray.length + 3];

        mTxBuffer[0] = 00; //Size
        mTxBuffer[1] = (byte)((byte)mTxBuffer.length - (byte)2); //Size
        mTxBuffer[2] = mId;

		for(loop = 0; loop < targetArray.length; loop++) {
			mTxBuffer[3 + loop] = targetArray[loop];
		}
    }

    public AppMessage (Iso11783Target target) {
        byte loop;
		byte[] targetArray = target.asByteArray();

		mId = APP_MSG_ISO11783_TARGET;

		/*
		 * Get a transmit buffer. 
		 */
		mTxBuffer = new byte[targetArray.length + 3];

        mTxBuffer[0] = 00; //Size
        mTxBuffer[1] = (byte)((byte)mTxBuffer.length - (byte)2); //Size
        mTxBuffer[2] = mId;

		for(loop = 0; loop < targetArray.length; loop++) {
			mTxBuffer[3 + loop] = targetArray[loop];
		}
    }

//    public boolean write(FileOutputStream stream){
//		if(this.txBuffer != null) {
//			Log.d(TAG, "Write buffer ID:" + CinnamonBun.byteToString(this.id) + " - length " + this.txBuffer.length + " to the CinnamonBun");
//
//			try	{
//				stream.write(txBuffer);
//				return(true);
//			}
//			catch(IOException ex) {
//				Log.d(TAG, "Failed to send message to Bun");
//				return(false);
//			}
//		}
//		return(false);
//    }

    public Boolean send(FileOutputStream stream) {
		Log.d(TAG, "AppMessage send(FileOutputStream stream)");

		if(stream != null) {
			try	{
				stream.write(mTxBuffer);
				return(true);
			} catch(IOException ex) {
				Log.d(TAG, "Failed to send message to Bun");
			}
		} else {
			Log.e(TAG, "No Output Stream");
		}
		return(false);
    }

    public Boolean send() {
		Log.d(TAG, "AppMessage send()");

		FileOutputStream stream = CinnamonBun.getInstance().getOutputStream();

		if(stream != null) {
			try	{
				stream.write(mTxBuffer);
				return(true);
			} catch(IOException ex) {
				Log.e(TAG, "Failed to send message to Bun", ex);
			}
		} else {
			Log.e(TAG, "No Output Stream");

			if(CinnamonBun.getInstance() == null) {
				Log.e(TAG, "CinnamonBun is Null");
			}
		}
		return(false);
    }
}
