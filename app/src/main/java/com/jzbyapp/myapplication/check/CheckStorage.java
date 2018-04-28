package com.jzbyapp.myapplication.check;

/**
 * Created by Administrator on 2018/4/19.
 */

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Environment;
import android.os.IBinder;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.android.internal.app.IMediaContainerService;
import com.jzbyapp.myapplication.R;
import com.jzbyapp.myapplication.util.FactoryUtil;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class CheckStorage extends DefaultCheck implements Runnable {

    private static final String TAG = "JzFactory";
    private static final String MODULE = "[CheckStorage] ";
    private static final String DEFAULT_CONTAINER_PACKAGE = "com.android.defcontainer";
    private static final ComponentName DEFAULT_CONTAINER_COMPONENT = new ComponentName(
            DEFAULT_CONTAINER_PACKAGE, "com.android.defcontainer.DefaultContainerService");
    private StorageManager mStorageManager;
    private List<StorageInfo> mStorageInfoList = new ArrayList<StorageInfo>();
    private StorageAdapter mStorageAdapter;
    private IMediaContainerService mDefaultContainer;

    private class StorageInfo {
        String path;
        String totle;
        String avail;
        boolean isPrimary;
        boolean isCheck;
    }

    private StorageEventListener mStorageListener = new StorageEventListener() {
        @SuppressLint("NewApi")
        @Override
        public void onStorageStateChanged(String path, String oldState, String newState) {
            try {
                Log.i(TAG, MODULE + "onStorageStateChanged path = " + path + ", oldState = "
                        + oldState + ", newState = " + newState);

                if ("mounted".equals(newState)) {
                    StorageInfo storageInfo = setStorageInfo(path);
                    if (storageInfo != null) {
                        mStorageInfoList.add(storageInfo);
                        mHandler.post(new StatusRunnable(STATUS_SUCCESS, ""));
                    }
                } else if ("removed".equals(newState) || "unmounted".equals(newState)) {
                    mStorageInfoList.remove(getStorageInfo(path));
                    mHandler.post(new StatusRunnable(STATUS_SUCCESS, ""));
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    };

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            final IMediaContainerService imcs = IMediaContainerService.Stub.asInterface(service);
            mDefaultContainer = imcs;
            mHandler.post(CheckStorage.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    public CheckStorage(Activity context, OnCheckProcessListener listener, boolean isTimeOut) {
        super(context, listener, isTimeOut);
        // TODO Auto-generated constructor stub
        mStorageManager = StorageManager.from(mContext);
        getListView().setFocusable(false);
        mStorageAdapter = new StorageAdapter();
        getListView().setAdapter(mStorageAdapter);
        setCurrentCheckName(MODULE);
    }

    @SuppressLint("NewApi")
    @Override
    public void startCheck() {
        // TODO Auto-generated method stub
        super.startCheck();

        Intent intent = new Intent().setComponent(DEFAULT_CONTAINER_COMPONENT);
        mContext.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void stopCheck(boolean isFreeRes) {
        // TODO Auto-generated method stub
        try {
            super.stopCheck(isFreeRes);

            if (isFreeRes) {
                if (mStorageManager != null && mStorageListener != null) {
                    mStorageManager.unregisterListener(mStorageListener);
                }
                mContext.unbindService(mServiceConnection);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public StorageAdapter getStorageAdapter() {
        return mStorageAdapter;
    }

    private StorageInfo setStorageInfo(String path) {
        try {
            StorageInfo storageInfo = new StorageInfo();
            long[] stats = mDefaultContainer.getFileSystemStats(path);

            storageInfo.path = path;
            Log.i(TAG, MODULE + "setStorageInfo path = " + path);
            storageInfo.totle = Formatter.formatFileSize(mContext, stats[0]);
            storageInfo.avail = Formatter.formatFileSize(mContext, stats[1]);
            return storageInfo;
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return null;
    }

    private StorageInfo getStorageInfo(String path) {
        try {
            for (int i = 0; i < mStorageInfoList.size(); i++) {
                StorageInfo storageInfo = mStorageInfoList.get(i);
                if (path.equals(storageInfo.path)) {
                    return storageInfo;
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
        return null;
    }

    public class StorageAdapter extends BaseAdapter {

        private LayoutInflater mInflater;

        private class ViewHolder {
            private TextView txtTitle;
            private TextView txtPath;
            private TextView txtSpaceAll;
            private TextView txtSpaceAvailable;
            private TextView txtRoW;
            private CheckBox ckbRoW;
        }

        public StorageAdapter() {
            mInflater = mContext.getLayoutInflater();
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mStorageInfoList.size();
        }

        @Override
        public Object getItem(int arg0) {
            // TODO Auto-generated method stub
            return mStorageInfoList.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            // TODO Auto-generated method stub
            return arg0;
        }

        @Override
        public View getView(int arg0, View arg1, ViewGroup arg2) {
            // TODO Auto-generated method stub
            ViewHolder viewHolder;
            if (arg1 == null) {
                arg1 = mInflater.inflate(R.layout.checkstorage_layout, null);
                viewHolder = new ViewHolder();
                viewHolder.txtTitle = (TextView) arg1.findViewById(R.id.txt_check_item_title);
                viewHolder.txtPath = (TextView) arg1.findViewById(R.id.txt_check_item_path);
                viewHolder.txtSpaceAll = (TextView) arg1
                        .findViewById(R.id.txt_check_item_space_all);
                viewHolder.txtSpaceAvailable = (TextView) arg1
                        .findViewById(R.id.txt_check_item_space_available);
                viewHolder.txtRoW = (TextView) arg1.findViewById(R.id.txt_check_item_read_or_write);
                viewHolder.ckbRoW = (CheckBox) arg1.findViewById(R.id.ckb_check_item_read_or_write);
                arg1.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) arg1.getTag();
            }

            String path = mStorageInfoList.get(arg0).path;
            if (TextUtils.isEmpty(path)) {
                Log.i(TAG, MODULE + "StorageAdapter path = " + path);
                return arg1;
            } else {
                if (mStorageInfoList.get(arg0).isPrimary) {
                    viewHolder.txtTitle.setText(
                            "存储设备-内置");
                    viewHolder.txtRoW.setVisibility(View.VISIBLE);
                    viewHolder.ckbRoW.setVisibility(View.VISIBLE);
                    viewHolder.ckbRoW.setChecked(mStorageInfoList.get(arg0).isCheck);
                } else {
                    if (path.startsWith(FactoryUtil.mUsbMountStr)) {
                        viewHolder.txtTitle.setText(
                                "存储设备");
                    } else {
                        viewHolder.txtTitle.setText(
                                "SD-存储设备");
                    }
                    viewHolder.txtRoW.setVisibility(View.GONE);
                    viewHolder.ckbRoW.setVisibility(View.GONE);
                }
                viewHolder.txtPath.setText(path);
                viewHolder.txtSpaceAll.setText(mStorageInfoList.get(arg0).totle);
                viewHolder.txtSpaceAvailable.setText(mStorageInfoList.get(arg0).avail);
                return arg1;
            }
        }
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        StorageVolume[] storageVolumes = mStorageManager.getVolumeList();
        Log.i(TAG, MODULE + "storageVolumes.length = " + storageVolumes.length);
        for (StorageVolume volume : storageVolumes) {
            Log.i(TAG, MODULE + "volume.getPath() = " + volume.getPath());
            // if (!volume.isEmulated()) {
            try {
                StorageInfo storageInfo = setStorageInfo(volume.getPath());
                storageInfo.isPrimary = volume.isPrimary();
                mStorageInfoList.add(storageInfo);
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
            // }
        }
        mHandler.post(new StatusRunnable(STATUS_SUCCESS, ""));
        mStorageManager.registerListener(mStorageListener);

        new CheckWriteSDThread().start();
    }

    private class CheckWriteSDThread extends Thread {

        private static final String TEST_STR = "this is a test string writing to file.";
        private static final String TEST_FILE_NAME = "jz_test_write_sd.txt";

        @Override
        public void run() {
            // TODO Auto-generated method stub
            super.run();

            try {
                String sdPath = Environment.getExternalStorageDirectory().getPath();
                File writeFile = new File(sdPath + File.separator + TEST_FILE_NAME);
                if (!writeFile.exists())
                    writeFile.createNewFile();
                FileOutputStream stream = new FileOutputStream(writeFile);
                byte[] buf = TEST_STR.getBytes();
                stream.write(buf);
                stream.close();

                File readFile = new File(sdPath + File.separator + TEST_FILE_NAME);
                if (readFile.exists()) {
                    InputStream in = new BufferedInputStream(new FileInputStream(readFile));
                    BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                    String re = br.readLine();
                    br.close();
                    in.close();
                    readFile.delete();
                    if (TEST_STR.equals(re)) {
                        for (int i = 0; i < mStorageInfoList.size(); i++) {
                            StorageInfo storageInfo = mStorageInfoList.get(i);
                            if (storageInfo.isPrimary) {
                                storageInfo.isCheck = true;
                                mHandler.post(new StatusRunnable(STATUS_SUCCESS, ""));
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }

    }

}

