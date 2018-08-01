package android.electronicsoup.com.cinnamonbun;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;

public class Iso15765Dispatcher {

    private String TAG = "Iso15765Dispatcher";

    private ArrayList<Iso15765Target> targets = null;

    public Iso15765Dispatcher() {
		targets = new ArrayList<Iso15765Target>();
    }

    public void registerHandler(Iso15765Target target) {
		Log.d(TAG, "registerHandler()");
		targets.add(target);

		AppMessage appMsg = new AppMessage(target);
		appMsg.send();
    }


    public void dispatchMessage(Iso15765Message msg) {
		boolean found = false;
		Log.d(TAG, "DispatchMessage()"); 

		for(int i=0; i < targets.size(); i++) {
			if( msg.getProtocol() == targets.get(i).getProtocol()) {
				found = true;
				Log.d(TAG, "Handler found so dispatch");
				targets.get(i).getHandler().handleIso15765Msg(msg);
			}
        }

		if(!found) {
			Log.d(TAG, "No Handler found for Protcol " + CinnamonBun.byteToString(msg.getProtocol()));
		}
    }
}

