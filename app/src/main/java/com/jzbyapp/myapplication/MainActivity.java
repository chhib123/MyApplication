package com.jzbyapp.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.ViewGroup;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WindowManager m = getWindowManager();
        Display d = m.getDefaultDisplay();
        WindowManager.LayoutParams attributes = getWindow().getAttributes();

        attributes.width = (int) (d.getWidth() * 0.6);;
        attributes.height = (int) (d.getHeight() * 0.8);;
        getWindow().setAttributes(attributes);

    }

}
