package com.jzbyapp.myapplication.check;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.jzbyapp.myapplication.R;

/**
 * Created by Administrator on 2018/4/12.
 */

public class DefaultCheck {
    private static final String TAG = "JzFactory";
    private static final String MODULE = "[DefaulCheck] ";
    protected OnCheckProcessListener mListener;
    protected Activity mContext;
    protected Handler mHandler;
    private boolean mIsTimeOut = false;
    private TimeOutRunnable mTimeOutRunnable;
    private int mCheckStatus = STATUS_PREPARE;
    private int mCheckTimeOut = 8000;
    private String mCurrentCheckName;
    private TextView mTxtTitle;
    private TextView mTxtContent;
    private View checkItem;
    private ListView mListView;

    public static final int STATUS_PREPARE = 0;
    public static final int STATUS_CHECKING = 1;
    public static final int STATUS_SUCCESS = 2;
    public static final int STATUS_FAIL = 3;


    public interface OnCheckProcessListener {
        public void onCheckProcess(DefaultCheck checkObj, int status, String str);
    }

    public DefaultCheck(Activity context, OnCheckProcessListener listener, boolean isTimeOut) {
        mContext = context;
        mListener = listener;
        mIsTimeOut = isTimeOut;
        mTimeOutRunnable = new TimeOutRunnable();
        mHandler = new Handler(Looper.getMainLooper());
        initView();
    }

    private void initView() {

        if (this instanceof CheckStorage) {
            mListView = (ListView) mContext.findViewById(R.id.check_storage_list);
            checkItem = (View) mContext.findViewById(R.id.StorageCheckItem);
        }
        else if(this instanceof CheckNet){
            Log.i(TAG, MODULE + "CheckNet just for check net ");
        }
        else{
            ViewGroup viewGroup = (ViewGroup) mContext.findViewById(R.id.CheckItemGrid);
            View view = LayoutInflater.from(mContext).inflate(R.layout.checkitem_layout, viewGroup,
                    false);
            checkItem = (View) view.findViewById(R.id.CheckItem);
            mTxtTitle = (TextView) view.findViewById(R.id.Item_title);
            mTxtContent = (TextView) view.findViewById(R.id.Item_content);
            viewGroup.addView(view);
        }


    }
    public ListView getListView() {
        return mListView;
    }
    public TextView getTitle() {
        return mTxtTitle;
    }

    public void SetDraw(int status){
        Log.i(TAG, MODULE + "SetDraw = " + mCurrentCheckName+status);
        if(this instanceof CheckStorage){

        }
        else if(this instanceof CheckNet){

        }
        else{
            switch (status){
                case STATUS_PREPARE:
                    checkItem.setBackgroundColor(mContext.getResources().getColor(R.color.colorPrepare));
                    break;
                case STATUS_CHECKING:
                    checkItem.setBackgroundColor(mContext.getResources().getColor(R.color.colorChecking));
                    break;
                case STATUS_SUCCESS:
                    checkItem.setBackgroundColor(mContext.getResources().getColor(R.color.colorSuccess));
                    break;
                case STATUS_FAIL:
                    checkItem.setBackgroundColor(mContext.getResources().getColor(R.color.colorFail));
                    break;
            }
        }
    }

    public void SetContent(String str){
        mTxtContent.setText(str);
    }

    private int getCheckStatus() {
        return mCheckStatus;
    }
    private void setCheckStatus(int status) {
        mCheckStatus = status;
    }

    public void startCheck() {
        if (getCheckStatus() == STATUS_PREPARE) {
            setCheckStatus(STATUS_CHECKING);
            SetDraw(STATUS_CHECKING);
            if (mIsTimeOut) {
                mHandler.postDelayed(mTimeOutRunnable, mCheckTimeOut);
            }
        }
    }

    public void stopCheck(boolean isFreeRes) throws Exception {

        Log.i(TAG, MODULE + mCurrentCheckName + " isFreeRes = " + isFreeRes + ", checkStatus "
                + getCheckStatus());
        SetDraw(getCheckStatus());
        if (getCheckStatus() > STATUS_CHECKING) {
            throw new Exception(mCurrentCheckName + " has already stopped");
        } else if (getCheckStatus() == STATUS_CHECKING) {
            if (mIsTimeOut && mTimeOutRunnable != null) {
                mHandler.removeCallbacks(mTimeOutRunnable);
                mTimeOutRunnable = null;
            }
            if (isFreeRes) {
               // setCheckStatus(STATUS_FINISH);
            }
        }
    }

    protected void setCurrentCheckName(String currentCheckName) {
        mCurrentCheckName = currentCheckName;
    }
    protected class StatusRunnable implements Runnable {

        private int _status;
        private String _str;

        public StatusRunnable(int status, String str) {
            _status = status;
            _str = str;
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            mListener.onCheckProcess(DefaultCheck.this, _status, _str);
        }

    }
    private class TimeOutRunnable implements Runnable {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            try {
                stopCheck(true);

                if (mListener != null) {
                    mListener.onCheckProcess(DefaultCheck.this, STATUS_FAIL,
                            "Time Out Failed !");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
