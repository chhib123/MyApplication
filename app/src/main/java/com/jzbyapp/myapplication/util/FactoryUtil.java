package com.jzbyapp.myapplication.util;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import org.ngb.system.jzsysteminfo.JZSystemInfo;

/**
 * Created by Administrator on 2018/4/12.
 */

public class FactoryUtil {
    public static int mBuildMask = 0;

    public static final int FLAG_GATEWAY = 1; // 网关
    public static final int FLAG_WIFI = 1 << 1; // WIFI
    public static final int FLAG_BLUETOOTH = 1 << 2; // 蓝牙
    public static final int FLAG_TUNER = 1 << 3; // tuner
    public static final int FLAG_CACARD = 1 << 4; // 卡座
    public static final int FLAG_FRONT_PANEL_LED = 1 << 5; // 前面板led
    public static final int FLAG_FRONT_PANEL_KEY = 1 << 6;
    public static final int FLAG_CM = 1 << 7; // cable model
    public static final int FLAG_ONT = 1 << 8; // ONT
    private static FactoryUtil mInstance = null;
    public static final String SERVICE_ADDRESS = "192.168.30.65";
    public static final String STB_ADDRESS = "192.168.30.68";
    public static final int SERVICE_PORT = 10101;
    public static final int LENGTH_SERIAL_NUMBER = 9;
    public static final String BROADCAST_UPDATE_ETHMAC = "jz.broadcast.update.ethmac";
    public static final String BROADCAST_UPDATE_PRIDATA = "jz.broadcast.update.pridata";
    String StrCommand = "";

    public static int TunerFre = 395000;
    public static final String mUsbMountStr = "/mnt/sd";

    public static final int LENGTH_MAC = 12;
    public static final int MAX_LENGTH_PRIVATE_DATA = 50;
    public static final int IPANEL_READ_STBID = 0x101;
    public static final int IPANEL_READ_STBMAC = 0x102;
    public static final int IPANEL_WRITE_STBID = 0x201;
    public static final int IPANEL_WRITE_STBMAC = 0x202;
    public static boolean UseIpanel = true;

    static{
        System.loadLibrary("RootShell");
    }

    public native String StringFromRootShell(byte[] Command);
    public native String GetCMMacAddr();

    private FactoryUtil() {
    }

    public static FactoryUtil getInstance() {
        if (mInstance == null) {
            mInstance = new FactoryUtil();
        }
        return mInstance;
    }

    private JZSystemInfo mJZSystemInfo = new JZSystemInfo();

    public void SetEth1_DEFAULT_IP(){
        StrCommand = "busybox ifconfig eth1 "+ STB_ADDRESS;
        FactoryUtil.getInstance().StringFromRootShell(StrCommand.getBytes());
    }

    public void SetEth0_DHCP(){
        StrCommand = "busybox udcpc -i eth0";
        FactoryUtil.getInstance().StringFromRootShell(StrCommand.getBytes());
    }

    public String PingServer(){
        GetCMMacAddr();
        StrCommand = "ping -c 1 "+ SERVICE_ADDRESS;
        return FactoryUtil.getInstance().StringFromRootShell(StrCommand.getBytes());
    }

    public boolean saveMac(Context context, String mac) {
        if (!TextUtils.isEmpty(mac)) {
            mac = mac.toUpperCase();
            if (!mac.equals(getEthernetMac())) {
                setEthernetMac(mac);
                Intent intent = new Intent(FactoryUtil.BROADCAST_UPDATE_ETHMAC);
                intent.putExtra("mac", mac);
                context.sendBroadcast(intent);
                return true;
            }
        }
        return false;
    }

    public boolean savePrivateData(Context context, String privateData) {
        if (!TextUtils.isEmpty(privateData) && !privateData.equals(getPrivateData())) {
            setPrivateData(privateData);
            Intent intent = new Intent(FactoryUtil.BROADCAST_UPDATE_PRIDATA);
            intent.putExtra("privateData", privateData);
            context.sendBroadcast(intent);
            return true;
        }
        return false;
    }

    public String getEthernetMac() {
        try {
            byte[] bs;
            if(UseIpanel)
                bs = mJZSystemInfo.dvb_NVMReadForIpanel(IPANEL_READ_STBMAC,LENGTH_MAC);
            else
                bs = mJZSystemInfo.dvb_NVMReadMac();

            if (checkMacLegal(bs, LENGTH_MAC)) {
                return new String(bs, "utf-8").toUpperCase();
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
        return "";
    }
    public void setEthernetMac(String mac) {
        try {
            if(UseIpanel)
                mJZSystemInfo.dvb_NVMWriteForIpanel(IPANEL_WRITE_STBMAC,mac.getBytes());
            else
                mJZSystemInfo.dvb_NVMWriteMac(mac.getBytes());
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public String getPrivateData() {
        try {
            byte[] bs;
            if(UseIpanel)
                bs = mJZSystemInfo.dvb_NVMReadForIpanel(IPANEL_READ_STBID,MAX_LENGTH_PRIVATE_DATA);
            else
                bs = mJZSystemInfo.dvb_NVMReadAndroidBackDoorApkPrivateData(MAX_LENGTH_PRIVATE_DATA);

            byte[] rbs = getRealPrivateData(bs);
            if (checkPrivateDataLegal(rbs, MAX_LENGTH_PRIVATE_DATA)) {
                return new String(rbs, "utf-8");
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
        return "";
    }

    private byte[] getRealPrivateData(byte[] bs) {
        try {
            int count = 0;
            for (byte b : bs) {
                if (b == 0) {
                    break;
                }
                count++;
            }
            byte[] temp = new byte[count];
            for (int i = 0; i < count; i++) {
                temp[i] = bs[i];
            }
            return temp;
        } catch (Exception e) {
            // TODO: handle exception
        }
        return null;
    }

    public void setPrivateData(String privateData) {
        try {
            if(UseIpanel)
                mJZSystemInfo.dvb_NVMWriteForIpanel(IPANEL_WRITE_STBID,privateData.getBytes());
            else
                mJZSystemInfo.dvb_NVMWriteAndroidBackDoorApkPrivateData(privateData.getBytes());
        } catch (Exception e) {
            // TODO: handle exception
        }
    }
    private boolean checkMacLegal(byte[] bs, int l) {
        if (bs.length != l) {
            return false;
        }
        for (byte b : bs) {
            if ((b < '0' || b > '9') && (b < 'a' || b > 'f') && (b < 'A' || b > 'F')) {
                return false;
            }
        }
        return true;
    }

    private boolean checkPrivateDataLegal(byte[] bs, int maxLength) {
        if (bs.length > maxLength) {
            return false;
        }
        for (byte b : bs) {
            if (b < 32 || b >= 127) {
                return false;
            }
        }
        return true;
    }

    public String getFormatEthMac(String mac, String s) {
        try {
            return mac.substring(0, 2) + s + mac.substring(2, 4) + s + mac.substring(4, 6) + s
                    + mac.substring(6, 8) + s + mac.substring(8, 10) + s + mac.substring(10, 12);
        } catch (Exception e) {
            // TODO: handle exception
        }
        return "";
    }
}
