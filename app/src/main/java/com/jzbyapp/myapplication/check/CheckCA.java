package com.jzbyapp.myapplication.check;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;

import org.ngb.toolkit.ca.CACard;
import org.ngb.toolkit.ca.CAManager;

/**
 * Created by Administrator on 2018/4/12.
 */

public class CheckCA extends DefaultCheck {

    private static final String TAG = "JzFactory";
    private static final String MODULE = "[CheckCaCard] ";
    private static final int CHECK_TIMEOUNT = 1000;
    private CheckThread mCheckThread;
//    private static CAManager mCaManager = null;

    public CheckCA(Activity context, OnCheckProcessListener listener, boolean isTimeOut) {
        super(context, listener, false);
        setCurrentCheckName(MODULE);
        getTitle().setText("CA");
    }

    @Override
    public void startCheck() {
        // TODO Auto-generated method stub
        super.startCheck();
//        mCaManager = CAManager.getCAManager();
//
//        if(mCaManager != null){
//            mCaManager.addCaEventListener(CaEvevtListener);
//        }

//        caListenerHandle = EventManager.getInstance().addUnGrabEventListener(NgbEvent.CAS_EVENT,
//                NgbEvent.ANY_SUBTYPE, 0, 255, EvevtListener);

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

//    CAEventListener CaEvevtListener = new CAEventListener(){
//
//        @Override
//        public void receiveCAEvent(CAEvent caEvent) {
//            if(caEvent instanceof CACardEvent){
//                CACardEvent evt = (CACardEvent)caEvent;
//
//                Log.i(TAG, MODULE+ "Receive CACardEvent! ");
//
//                if(evt.isOut() == true) {
//                    Log.i(TAG, MODULE+ "Receive CACardEvent! Card IN!");
//                }
//                else {
//                    Log.i(TAG, MODULE+ "Receive CACardEvent! Card OUT!");
//                }
//            }
//        }
//    };


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

            CACard caCard = CAManager.getCAManager().getCardInfo();
            while (_isRun) {
                String cardNum = caCard.getSerialNumber();
                Log.i(TAG, MODULE + "cardNum = " + cardNum);
                if (TextUtils.isEmpty(cardNum) || "0".equals(cardNum)) {
                    mHandler.post(new StatusRunnable(STATUS_CHECKING,
                            "正在检测..."));
                } else {
                    mHandler.post(new StatusRunnable(STATUS_SUCCESS,
                            "Ca: " + cardNum));
                    stopCheck(true);
                }
                try {
                    Thread.sleep(CHECK_TIMEOUNT);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
}
