package com.jzbyapp.myapplication.check;

import android.app.Activity;
import android.util.Log;

import com.jzbyapp.myapplication.util.FactoryUtil;

import org.davic.net.Locator;
import org.ngb.appmanager.application.AppID;
import org.ngb.broadcast.dvb.si.SIService;
import org.ngb.broadcast.dvb.tuning.DvbcTuningParameters;
import org.ngb.event.CableEvent;
import org.ngb.event.EventManager;
import org.ngb.event.NgbEvent;
import org.ngb.event.NgbEventListener;
import org.ngb.event.NgbEventListenerHandle;
import org.ngb.media.MediaManager;
import org.ngb.media.Player;
import org.ngb.toolkit.channelscan.ChannelScanEngine;
import org.ngb.toolkit.channelscan.ChannelScanEvent;
import org.ngb.toolkit.channelscan.ChannelScanFailureEvent;
import org.ngb.toolkit.channelscan.ChannelScanFinishEvent;
import org.ngb.toolkit.channelscan.ChannelScanListener;
import org.ngb.toolkit.channelscan.ChannelScanSuccessEvent;


/**
 * Created by Administrator on 2018/4/13.
 */

public class CheckTuner extends DefaultCheck implements ChannelScanListener {
    private static final String TAG = "JzFactory";
    private static final String MODULE = "[CheckTuner] ";
    private Player mPlayer;
    private ChannelScanEngine mChannelScanEngine;
    private DvbcTuningParameters[] mDvbcTuningParameters = new DvbcTuningParameters[1];
    private Locator mLocator;
    private int ProNum = 0;
    private NgbEventListenerHandle cableListenerHandle= null;


    public CheckTuner(Activity context, OnCheckProcessListener listener, boolean isTimeOut) {
        super(context, listener, false);
        setCurrentCheckName(MODULE);
        getTitle().setText("Tuner");
        SetContent("等待锁频...");
        cableListenerHandle = EventManager.getInstance().addUnGrabEventListener(NgbEvent.CABLE_EVENT,
                NgbEvent.ANY_SUBTYPE, 0, 255, EvevtListener);
    }

    @Override
    public void startCheck() {
        super.startCheck();
        Log.i(TAG, MODULE + "AutoCheckActivity.mTunerFre = " + FactoryUtil.TunerFre);
        startSearch();//temp
    }

    public void startSearch(){
        Log.i(TAG, MODULE + "startSearch");
        try {
            mChannelScanEngine = ChannelScanEngine.createInstance();
            mChannelScanEngine.addChannelScanListener(this);
            mDvbcTuningParameters[0] = new DvbcTuningParameters(FactoryUtil.TunerFre, 3,
                    6875);
            mChannelScanEngine.startScan(ChannelScanEngine.CHANNELSCAN_TYPE_MANUAL,
                    mDvbcTuningParameters);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            releaseChannelScanEngine();
        }
    }

    @Override
    public void stopCheck(boolean isFreeRes) {
        // TODO Auto-generated method stub
        try {
            super.stopCheck(isFreeRes);
            EventManager.getInstance().removeEventListener(cableListenerHandle);

            if (isFreeRes) {
                releasePlayer();
                releaseChannelScanEngine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void releasePlayer() {
        if (mPlayer != null) {
            mPlayer.getVideoControl().resetBounds();
            mPlayer.stop();
            mPlayer.deallocate();
            mPlayer = null;
        }
    }

    private void releaseChannelScanEngine() {
        if (mChannelScanEngine != null) {
            mChannelScanEngine.removeChannelScanListener(this);
            mChannelScanEngine.cancel();
            mChannelScanEngine.release();
            mChannelScanEngine = null;
        }
    }

    @Override
    public void processEvent(ChannelScanEvent arg0){
        // TODO Auto-generated method stub

        Log.i(TAG, MODULE + "processEvent");
        if (arg0 instanceof ChannelScanFailureEvent) {

            Log.i(TAG, MODULE + "ChannelScanFailureEvent");
            // releaseChannelScanEngine();
        } else if (arg0 instanceof ChannelScanSuccessEvent) {
            Log.i(TAG, MODULE + "ChannelScanSuccessEvent");
            SIService[] siServices = ((ChannelScanSuccessEvent) arg0).getResult();
            for (int i = 0; i < siServices.length; i++) {
                if (i == 0) {
                    mLocator = siServices[i].getDvbLocator();
                }
                Log.i(TAG, MODULE + "siService.getServiceName = " + siServices[i].getServiceName());
                ProNum = i+1;
            }
        } else if (arg0 instanceof ChannelScanFinishEvent) {

            Log.i(TAG, MODULE + "ChannelScanFinishEvent");
            if (ProNum>0) {
                mPlayer = MediaManager.getInstance().createPlayer((Locator) null);
                Log.i(TAG, MODULE + "mLocator = " + mLocator);
                mPlayer.setDataSource(mLocator);
                mPlayer.start();

                mHandler.post(new StatusRunnable(STATUS_SUCCESS,
                        "搜索到" + ProNum + "个节目"));
                stopCheck(false);

            } else {
                mHandler.post(new StatusRunnable(STATUS_FAIL,
                        "测试失败"));
                stopCheck(true);
            }
        }
    }

    NgbEventListener EvevtListener = new NgbEventListener(){
        @Override
        public AppID getAppID() {
            return null;
        }

        @Override
        public void notifyEvent(NgbEvent ngbEvent) {
            if(CableEvent.CABLE_CONNECT == ngbEvent.getSubtype()){
                startSearch();
                mHandler.post(new StatusRunnable(STATUS_CHECKING,
                        "开始搜索..."));
                EventManager.getInstance().removeEventListener(cableListenerHandle);
            }
            Log.i(TAG, MODULE + " ngbEvent.getSubtype()=" + ngbEvent.getSubtype());
        }
    };

}
