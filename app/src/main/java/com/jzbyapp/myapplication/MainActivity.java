package com.jzbyapp.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.EthernetManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.jzbyapp.myapplication.check.CheckCA;
import com.jzbyapp.myapplication.check.CheckNet;
import com.jzbyapp.myapplication.check.CheckStorage;
import com.jzbyapp.myapplication.check.CheckTuner;
import com.jzbyapp.myapplication.check.DefaultCheck;
import com.jzbyapp.myapplication.check.DefaultCheck.OnCheckProcessListener;
import com.jzbyapp.myapplication.thread.ReceiveDataThread;
import com.jzbyapp.myapplication.util.FactoryUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnCheckProcessListener{

    private TextView mSNnumber;
    private List<DefaultCheck> mDefaultChecks = new ArrayList<DefaultCheck>();
    private static final String TAG = "JzFactory";
    private static final String MODULE = "[MainActivity] ";
    private ReceiveDataThread mReceiveDataThread;

    private TextView mTxtEthMac;
    private TextView mTxtPrivateData;
    private TextView mTxtSoftWare;
    private TextView mTxtHardWare;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String action = intent.getAction();
           if (FactoryUtil.BROADCAST_UPDATE_ETHMAC.equals(action)) {
                mTxtEthMac.setText(FactoryUtil.getInstance()
                        .getFormatEthMac(intent.getStringExtra("mac"), "-"));
            }else if (FactoryUtil.BROADCAST_UPDATE_PRIDATA.equals(action)) {
                mTxtPrivateData.setText(intent.getStringExtra("privateData"));
            }

        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WindowManager m = getWindowManager();
        Display d = m.getDefaultDisplay();
        WindowManager.LayoutParams attributes = getWindow().getAttributes();

        attributes.width = (int) (d.getWidth() * 0.6);;
        attributes.height = (int) (d.getHeight() * 0.7);;
        getWindow().setAttributes(attributes);

        initBuildMask();

        /*update info show*/
        registerReceiver();

        /*Check item init*/
        initDefaultChecks();
        startCheckModule();

        /*Info Show*/
        initView();
        initData();

//        FactoryUtil.getInstance().SetEth1_DEFAULT_IP();

    }
    protected void onDestroy() {

        super.onDestroy();
        Log.i(TAG, MODULE + "onDestroy");
        stopCheckModule();
        unregisterReceiver(mBroadcastReceiver);
        if (mReceiveDataThread != null) {
            mReceiveDataThread.releaseSocket(true);
        }
    }

    private void initBuildMask() {
       FactoryUtil.mBuildMask = 0xff;
    }

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(FactoryUtil.BROADCAST_UPDATE_ETHMAC);
        intentFilter.addAction(FactoryUtil.BROADCAST_UPDATE_PRIDATA);
        registerReceiver(mBroadcastReceiver, intentFilter);
    }

    private void initDefaultChecks(){

        mDefaultChecks.add(new CheckStorage(this, this, false));
        mDefaultChecks.add(new CheckNet(this, this, false));
        if ((FactoryUtil.mBuildMask & FactoryUtil.FLAG_CACARD) != 0) {
            mDefaultChecks.add(new CheckCA(this, this, false));
        }
        if ((FactoryUtil.mBuildMask & FactoryUtil.FLAG_TUNER) != 0) {
            mDefaultChecks.add(new CheckTuner(this, this, false));
        }
    }
    private void startCheckModule() {
        for (int i = 0; i < mDefaultChecks.size(); i++) {
            DefaultCheck defaulCheck = mDefaultChecks.get(i);
            defaulCheck.startCheck();
        }
    }

    private void stopCheckModule() {
        for (int i = 0; i < mDefaultChecks.size(); i++) {
            DefaultCheck defaulCheck = mDefaultChecks.get(i);
            try {
                defaulCheck.stopCheck(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private void initView() {
        mSNnumber = (TextView) findViewById(R.id.SN);
        mTxtEthMac = (TextView) findViewById(R.id.MAC);
        mTxtPrivateData = (TextView) findViewById(R.id.privateNo);
        mTxtSoftWare = (TextView) findViewById(R.id.SoftWare);
        mTxtHardWare = (TextView) findViewById(R.id.HardWare);
    }
    private void initData() {
        mSNnumber.setText("123456");
//        String StrCommand = "busybox udhcpc -i eth1";
//        mSNnumber.setText(FactoryUtil.getInstance().StringFromRootShell(StrCommand.getBytes()));
//        mTxtEthMac.setText(FactoryUtil.getInstance()
//                .getFormatEthMac(FactoryUtil.getInstance().getEthernetMac(), "-"));
//        mTxtPrivateData.setText(FactoryUtil.getInstance().getPrivateData());
    }

    /*change color*/
    @Override
    public void onCheckProcess(DefaultCheck checkObj, int status, String str) {
        // TODO Auto-generated method stub
        if (checkObj instanceof CheckStorage) {
            ((CheckStorage) checkObj).getStorageAdapter().notifyDataSetChanged();
        }
        else if(checkObj instanceof CheckNet){
            StartReceiveData();
        }
        else{
            if(str!=null)
                checkObj.SetContent(str);

            checkObj.SetDraw(status);
        }
    }

    public void StartReceiveData() {
        if (mReceiveDataThread == null || !mReceiveDataThread.isAlive()) {
            Log.i(TAG, MODULE + "start ReceiveDataThread");
            mReceiveDataThread = new ReceiveDataThread(this);
            mReceiveDataThread.start();
        }
    }
}
