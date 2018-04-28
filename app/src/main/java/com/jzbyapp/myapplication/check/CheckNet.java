package com.jzbyapp.myapplication.check;

import android.app.Activity;
import android.util.Log;

import com.jzbyapp.myapplication.util.FactoryUtil;


/**
 * Created by Administrator on 2018/4/12.
 */

public class CheckNet extends DefaultCheck {

    private static final String TAG = "JzFactory";
    private static final String MODULE = "[CheckNet] ";
    private CheckThread mCheckThread;

    public CheckNet(Activity context, OnCheckProcessListener listener, boolean isTimeOut) {
        super(context, listener, false);
        setCurrentCheckName(MODULE);
    }

    @Override
    public void startCheck() {
        // TODO Auto-generated method stub
        super.startCheck();

        mCheckThread = new CheckThread();
        mCheckThread.start();
    }
    @Override
    public void stopCheck(boolean isFreeRes) {
        // TODO Auto-generated method stub
        try {
            super.stopCheck(isFreeRes);

            if (isFreeRes) {
                if (mCheckThread != null) {
                    mCheckThread.stopThread();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class CheckThread extends Thread {

        private boolean _isRun = false;

        public CheckThread() {
            _isRun = true;
        }

        public void stopThread() {
            _isRun = false;
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            super.run();

            while (_isRun) {
                String result = FactoryUtil.getInstance().PingServer();
                if (result.contains(", 0% packet loss")) {
//                    mHandler.post(new StatusRunnable(STATUS_SUCCESS, ""));
                    Log.i(TAG, MODULE + "PingServer success " );
                    stopCheck(true);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
}
