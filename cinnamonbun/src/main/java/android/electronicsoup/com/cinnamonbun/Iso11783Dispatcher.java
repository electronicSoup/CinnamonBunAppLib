package android.electronicsoup.com.cinnamonbun;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;

public class Iso11783Dispatcher {

    private String TAG = "Iso11783Dispatcher";

    private ArrayList<Iso11783Target> targets = null;

    public Iso11783Dispatcher() {
		targets = new ArrayList<Iso11783Target>();
    }

    public void registerHandler(Iso11783Target target) {
		Log.d(TAG, "registerHandler()");
		targets.add(target);

		AppMessage appMsg = new AppMessage(target);
		appMsg.send();
    }


    public void dispatchFrame(Iso11783Message msg) {
		Log.d(TAG, "DispatchFrame()"); 

		for(int i=0; i < targets.size(); i++) {
			if( msg.getPgn() == targets.get(i).getPgn()) {
				targets.get(i).getHandler().handleIso11783Msg(msg);
			}
        }
    }
}

