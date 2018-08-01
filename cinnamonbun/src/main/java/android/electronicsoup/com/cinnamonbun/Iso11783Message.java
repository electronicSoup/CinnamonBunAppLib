package android.electronicsoup.com.cinnamonbun;

import android.util.Log;

public class Iso11783Message
{
    private String TAG = "Iso11783Message";
    
    private int mPGN;
	private byte mAddress;
    private byte[] mData = null;

    public Iso11783Message(int pgn, byte[] msg) {
        this.mPGN = pgn;
        this.mData = msg;
    }

    public int getPgn() {
        return mPGN;
    }

	public byte getAddress() {
		return mAddress;
	}

    public byte[] getData()
    {
        return mData;
    }
}
