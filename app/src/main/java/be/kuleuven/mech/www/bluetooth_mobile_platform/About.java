package be.kuleuven.mech.www.bluetooth_mobile_platform;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.app.Activity;

/**
 * Created by Snowy on 15/1/4.
 */

public class About extends Activity {
    private Button Back;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        Back = (Button) findViewById(R.id.about_return);
        Back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
                return;
            }
        });
    }
}
