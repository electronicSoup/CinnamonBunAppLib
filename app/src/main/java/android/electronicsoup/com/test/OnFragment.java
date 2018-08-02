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



public class OnFragment extends Fragment implements StateInterface
{
    private String    TAG = "LightSwitch";

    private Button    mOffButton = null;
	private View      mView = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.fragment_on, parent, false);

		this.mOffButton = (Button)mView.findViewById(R.id.off_button);

		this.mOffButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					CinnamonBun bun = CinnamonBun.getInstance();

					Log.d(TAG, "OnFragment - Off Button Pressed");
					AppMessage msg = new AppMessage(MainActivity.APP_MSG_OFF);

					if(!msg.send(CinnamonBun.getInstance().getOutputStream())) {
						Log.e(TAG, "Failed to send message to CinnamonBun");
					}

					bun.setFragment(new OffFragment());
				}
			});    

		return(mView);
    }

    public StateInterface processMessage(Message msg) {
		Log.d(TAG, "OnFragment - processMessage()");

		Fragment fragment = null;

		BunMessage bunMsg = (BunMessage)msg.obj;

		if(bunMsg.id() == MainActivity.BUN_MSG_OFF) {
			Log.d(TAG, "OnFragment - Received Off message: Change State");
			fragment = new OffFragment();
		} else if (bunMsg.id() == MainActivity.BUN_MSG_ON) {
			Log.d(TAG, "OnFragment - Received On message: Ignore");
		}

		return((StateInterface)fragment);
    }
}
