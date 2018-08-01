package android.electronicsoup.com.cinnamonbun;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import java.io.File;

public interface StateInterface
{
    StateInterface processMessage(Message msg);
}
