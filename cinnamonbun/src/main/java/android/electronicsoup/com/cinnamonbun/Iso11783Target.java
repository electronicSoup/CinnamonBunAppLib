package android.electronicsoup.com.cinnamonbun;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

//import java.util.Random;

public class Iso11783Target 
{
    private String TAG = "ES-Iso11783Target";

    private int mPGN;
    private Iso11783MsgHandler mHandler = null;

    public Iso11783Target(int pgn, Iso11783MsgHandler handler) {
		this.mPGN = pgn;
		this.mHandler = handler;
    }

    public int getPgn() {
		return(this.mPGN);
    }

    public Iso11783MsgHandler getHandler() {
		return(this.mHandler);
    }

	public byte[] asByteArray() {
		byte[] array = new byte[4];

        array[0] = (byte)((mPGN >> 24) & 0xff);
        array[1] = (byte)((mPGN >> 16) & 0xff);
        array[2] = (byte)((mPGN >> 8) & 0xff);
        array[3] = (byte)(mPGN & 0xff);

		return(array);
	}
}
