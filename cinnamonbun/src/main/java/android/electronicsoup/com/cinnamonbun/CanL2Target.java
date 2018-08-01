package android.electronicsoup.com.cinnamonbun;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.Random;

public class CanL2Target 
{
    private String TAG = "ES-CanDispatcher";

    private int mMask = 0x00;
    private int mFilter = 0x00;
    private CanL2FrameHandler mHandler = null;

    public CanL2Target(int mask, int filter, CanL2FrameHandler handler) {
		this.mMask = mask;
		this.mFilter = filter;
		this.mHandler = handler;
    }

    public int getMask() {
		return(this.mMask);
    }

    public int getFilter() {
		return(this.mFilter);
    }

    public CanL2FrameHandler getHandler() {
		return(this.mHandler);
    }

	public byte[] asByteArray() {
		byte[] array = new byte[8];

        array[0] = (byte)((mMask >> 24) & 0xff);
        array[1] = (byte)((mMask >> 16) & 0xff);
        array[2] = (byte)((mMask >> 8) & 0xff);
        array[3] = (byte)(mMask & 0xff);

        array[4] = (byte)((mFilter >> 24) & 0xff);
        array[5] = (byte)((mFilter >> 16) & 0xff);
        array[6] = (byte)((mFilter >> 8) & 0xff);
        array[7] = (byte)(mFilter & 0xff);

		return(array);
	}

	public void register() {
		CinnamonBun.getInstance().registerTarget(this);
	}
}
