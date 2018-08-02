package android.electronicsoup.com.test;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.lang.String;
import java.util.Arrays;

import android.electronicsoup.com.cinnamonbun.AppMessage;
import android.electronicsoup.com.cinnamonbun.BunMessage;
import android.electronicsoup.com.cinnamonbun.CinnamonBun;
import android.electronicsoup.com.cinnamonbun.StateInterface;

public class OffFragment extends Fragment implements StateInterface
{
    private String    TAG = "LightSwitch";

    private Button    mOnButton = null;
    private View      mView = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.fragment_off, parent, false);

		this.mOnButton = (Button)mView.findViewById(R.id.on_button);

		this.mOnButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					CinnamonBun bun = CinnamonBun.getInstance();
		    
					Log.d(TAG, "OFF Fragment - On Button Pressed");
					AppMessage msg = new AppMessage(MainActivity.APP_MSG_ON);

					if(!msg.send(CinnamonBun.getInstance().getOutputStream())) {
						Log.e(TAG, "Failed to send message to CinnamonBun");
					}

					bun.setFragment(new OnFragment());
				}
			});

		return(mView);
    }

    public StateInterface processMessage(Message msg) {
		Log.d(TAG, "processMessage()");
	
		Fragment fragment = null;

		BunMessage bunMsg = (BunMessage)msg.obj;

		if (msg.what == CinnamonBun.WHAT_CINNAMON_BUN_MSG) {
			Log.d(TAG, "Got a CinnamonBun Message");
			if(bunMsg.id() == MainActivity.BUN_MSG_OFF) {
				Log.d(TAG, "OFF Fragment - Received Off message: Ignore");
			} else if (bunMsg.id() == MainActivity.BUN_MSG_ON) {
				Log.d(TAG, "OFF Fragment - Received On message: Change State");
				fragment = new OnFragment();
			}
		} else if (msg.what == CinnamonBun.WHAT_CINNAMON_BUN_STATUS) {
			Log.d(TAG, "Got a CinnamonBun Status");
		}

		return((StateInterface)fragment);
    }
}
