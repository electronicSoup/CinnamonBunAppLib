package android.electronicsoup.com.cinnamonbun;

import android.util.Log;

public class Iso15765Message
{
    private String TAG = "Iso15765Message";
    
    private byte mAddress;
    private byte mProtocol;
    private byte[] mData = null;

    public Iso15765Message(byte address, byte protocol, byte[] msg)
    {
        this.mProtocol = protocol;
        this.mAddress = address;
        this.mData = msg;
    }

    public Iso15765Message(byte[] buffer)
    {
		byte[] msg = new byte[buffer.length - 2];

        this.mAddress  = buffer[0];
        this.mProtocol = buffer[1];

		int index = 0;
		for(int loop = 2; loop < buffer.length; loop++) {
			msg[index++] = buffer[loop];
//			Log.d(TAG, "buffer[" + Integer.toString(loop) + "] = " + CinnamonBun.byteToString(buffer[loop]));
		}

        this.mData = msg;
    }

    public byte getAddress()
    {
        return mAddress;
    }

    public byte getProtocol()
    {
        return this.mProtocol;
    }

    public byte[] getData()
    {
        return mData;
    }

	public byte[] asByteArray() {
		int loop;
		byte[] array = new byte[mData.length + 2];

		Log.d(TAG, "asByteArray()");
        array[0] = mAddress;
        array[1] = mProtocol;

        for(loop = 0; loop < mData.length; loop++) {
            array[loop + 2] = mData[loop];
        }
		
		for(loop = 0; loop < array.length; loop++) {
			Log.d(TAG, "array[" + Integer.toString(loop) + "] = 0x" + CinnamonBun.byteToString(array[loop]));
		}
		return(array);
	}

	public void send() {
		AppMessage msg = new AppMessage(this);
		msg.send();
	}
}
