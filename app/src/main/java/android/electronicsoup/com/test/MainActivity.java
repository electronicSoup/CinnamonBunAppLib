package android.electronicsoup.com.test;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.electronicsoup.com.cinnamonbun.CanL2Frame;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import android.graphics.Color;

import android.widget.ImageButton;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import android.electronicsoup.com.cinnamonbun.CinnamonBun;
import android.electronicsoup.com.cinnamonbun.BunMessage;
import android.electronicsoup.com.cinnamonbun.AppMessage;
import android.electronicsoup.com.cinnamonbun.StateInterface;

public class MainActivity extends AppCompatActivity {
    private String TAG = "LightSwitch";

    public final static byte BUN_MSG_OFF = BunMessage.BUN_MSG_USER_OFFSET;
    public final static byte BUN_MSG_ON  = BunMessage.BUN_MSG_USER_OFFSET + 1;

    public final static byte APP_MSG_OFF = AppMessage.APP_MSG_USER_OFFSET;
    public final static byte APP_MSG_ON  = AppMessage.APP_MSG_USER_OFFSET + 1;
    
    private CinnamonBun mBun = null;
    private Context mContext = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container);

        this.mContext = getApplicationContext();

		this.mBun = CinnamonBun.createInstance(mContext, getFragmentManager(), R.id.fragmentContainer, new OffFragment());
    }
}
