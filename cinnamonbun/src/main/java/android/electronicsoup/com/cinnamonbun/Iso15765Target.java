package android.electronicsoup.com.cinnamonbun;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

//import java.util.Random;

public class Iso15765Target 
{
    private String TAG = "Iso15765Target";

    private byte mProtocol;
    private Iso15765MsgHandler mHandler = null;

    public Iso15765Target(byte protocol, Iso15765MsgHandler handler) {
		this.mProtocol = protocol;
		this.mHandler = handler;
    }

    public int getProtocol() {
		return(this.mProtocol);
    }

    public Iso15765MsgHandler getHandler() {
		return(this.mHandler);
    }

	public byte[] asByteArray() {
		byte[] array = new byte[1];

        array[0] = mProtocol;

		return(array);
	}

	public void register() {
		CinnamonBun.getInstance().registerTarget(this);
	}
}
