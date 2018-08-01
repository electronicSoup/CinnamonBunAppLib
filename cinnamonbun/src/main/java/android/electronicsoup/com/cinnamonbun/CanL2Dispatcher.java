package android.electronicsoup.com.cinnamonbun;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;

public class CanL2Dispatcher {

    private String TAG = "ES-CanL2Dispatcher";

    private ArrayList<CanL2Target> targets = null;

    public CanL2Dispatcher() 
    {
		targets = new ArrayList<CanL2Target>();
    }

    public void registerHandler(CanL2Target target)
    {
		Log.d(TAG, "CanL2Dispatcher registerHandler()");
		targets.add(target);

		AppMessage appMsg = new AppMessage(target);
		appMsg.send(CinnamonBun.getInstance().getOutputStream());
    }


    public void dispatchFrame(CanL2Frame frame)
    {
		Log.d(TAG, "DispatchFrame(" + Integer.toHexString(frame.getCanId()) + ")"); 

		for(int i=0; i < targets.size(); i++) {
			if( (frame.getCanId() & targets.get(i).getMask()) == (targets.get(i).getFilter() & targets.get(i).getMask())) {
				targets.get(i).getHandler().handleCanL2Frame(frame);
			}
        }
    }
}

