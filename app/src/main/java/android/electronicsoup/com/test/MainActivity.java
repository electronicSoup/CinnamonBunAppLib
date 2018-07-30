package android.electronicsoup.com.test;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.electronicsoup.com.cinnamonbun.CanL2Frame;

public class MainActivity extends AppCompatActivity {

    private CanL2Frame mFrame = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
