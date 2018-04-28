package com.jzbyapp.myapplication.thread;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import android.os.SystemProperties;

import com.jzbyapp.myapplication.R;
import com.jzbyapp.myapplication.util.FactoryUtil;

public class ReceiveDataThread extends Thread {

	private static final String TAG = "JzFactory";
	private static final String MODULE = "[ReceiveDataThread] ";
	private static final int MSG_REBOOT = 0;
	private static final int MSG_RESET = 1;
	private static final int ERROR_TOTAL = 1;
	private static final int ERROR_MAC = 2;
	private static final int ERROR_SERIAL = 4;
	private static final int ERROR_FRE = 8;
	private static final int ERROR_PRIVATE = 16;
	private static final int ERROR_UNKOWN = 32;
	private static final int BUFF_SIZE = 256;
	private byte[] mData = new byte[BUFF_SIZE];
	private Socket mSocket;
	private InputStream mInputStream;
	private OutputStream mOutputStream;
	private int mStbinfoTotalLen = 0, mStbinfoPrivateDataLen = 0, mStbinfoMacLen = 0,
			mStbinfoFreqLen = 0, mStbinfoSerialLen = 0;
	private Context mContext;
	private boolean mIsRun = true;
	private String mRebootCmd = "";

	public ReceiveDataThread(Context context) {
		mContext = context;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();

		Log.i(TAG, MODULE + "isNetworkAvailable = true");
		while (mIsRun) {
			createSocket();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		if ("REBOOT".equals(mRebootCmd)) {
			try {
				Thread.sleep(1000);
				Runtime.getRuntime().exec("reboot");
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if ("RESET".equals(mRebootCmd)) {
			try {
				Thread.sleep(1000);
//				new Difference(mContext, null).masterClear();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void createSocket() {
		try {
			Log.i(TAG, MODULE + "new ServerSocket");

			mSocket = new Socket(InetAddress.getByName(FactoryUtil.SERVICE_ADDRESS),
					FactoryUtil.SERVICE_PORT);
			mInputStream = mSocket.getInputStream();
			mOutputStream = mSocket.getOutputStream();

			while (mIsRun) {
				clearDataBuffer();
				int length = 0;
				while ((length = mInputStream.read(mData)) != -1) {
					Log.i(TAG, MODULE + "length = " + length);
					if (handleInputStream(mData, length) != 0) {
						releaseSocket(false);
						return;
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	private void clearDataBuffer() {
		for (int i = 0; i < BUFF_SIZE; i++) {
			mData[i] = 0;
		}
	}

	private String getReceiveDataStr(byte[] data, int length, boolean isPrint) {
		StringBuffer stringBuffer = new StringBuffer();
		for (int i = 0; i < length/* data.length && data[i] != 0 */; i++) {
			stringBuffer.append((char) data[i]);
		}
		String str = stringBuffer.toString();
		if (isPrint) {
			Log.i(TAG, MODULE + str);
		}
		return str;
	}

	public void releaseSocket(boolean isExit) {

		if (isExit) {
			mIsRun = false;
		}

		try {
			if (mSocket != null) {
				mSocket.close();
				Log.i(TAG, MODULE + "mSocket.close() finish");
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		try {
			if (mInputStream != null) {
				mInputStream.close();
				Log.i(TAG, MODULE + "mInputStream.close() finish");
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		try {
			if (mOutputStream != null) {
				mOutputStream.close();
				Log.i(TAG, MODULE + "mOutputStream.close() finish");
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	private int handleInputStream(byte[] data, int length) {
		try {
			String command = getReceiveDataStr(data, length, true);
			if (data.length > 12 && ('J' == data[0]) && ('Z' == data[1]) && ('2' == data[2])
					&& ('3' == data[3]) && ('3' == data[4]) && ('s' == data[5]) && ('t' == data[6])
					&& ('b' == data[7]) && ('i' == data[8]) && ('n' == data[9]) && ('f' == data[10])
					&& ('o' == data[11]) && (':' == data[12])) {
				return checkFirstData(data, mOutputStream);
			} else if (data.length > 4 && ('J' == data[0]) && ('Z' == data[1]) && ('2' == data[2])
					&& ('3' == data[3]) && ('3' == data[4])) {
				return saveUsedData(data, mOutputStream, length);
			} else {
				String[] commands = command.split("#");
				for (int i = 0; i < commands.length; i++) {
					Log.i(TAG, MODULE + "commands[" + i + "] = " + commands[i]);
				}
				if ("STOP".equals(command)) {
					mIsRun = false;
					return -1;
				}
				if ("00#0".equals(command)) {
					return -1;
				}
				String outPutStr = "";
				if (commands.length > 1) {
					mRebootCmd = "";
					if ("GET".equals(commands[0])) {
						if ("STBID".equals(commands[1])) {
							outPutStr = command + "#" + FactoryUtil.getInstance().getPrivateData(); //mark
						} else if ("SOFTVERSION".equals(commands[1])) {
							outPutStr = command + "#test";
						} else if ("MAC".equals(commands[1])) {
							outPutStr = command + "#" + FactoryUtil.getInstance().getEthernetMac();
						} else if ("DEVICEID".equals(commands[1])) {
							outPutStr = command + "#" + "not define";//FactoryUtil.getInstance().getDeviceId()
						}
					} else if ("OPRATE".equals(commands[0])) {
						if ("REBOOT".equals(commands[1])) {
							outPutStr = command + "#SUCCESS";
							mRebootCmd = "REBOOT";
						} else if ("RESET".equals(commands[1])) {
							outPutStr = command + "#SUCCESS";
							mRebootCmd = "RESET";
						}
					}
					Log.i(TAG, MODULE + "outPutStr = " + outPutStr);
					mOutputStream.write(outPutStr.getBytes());
					if (!TextUtils.isEmpty(mRebootCmd)) {
						mIsRun = false;
						return -1;
					} else {
						return 0;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	private int checkFirstData(byte[] data, OutputStream outputStream) throws IOException {

		int errorCode = 0;
		String totalStr = null, macStr = null, freqStr = null, stbidStr = null, privateStr = null;

		if (('J' == data[0]) && ('Z' == data[1]) && ('2' == data[2]) && ('3' == data[3])
				&& ('3' == data[4]) && ('s' == data[5]) && ('t' == data[6]) && ('b' == data[7])
				&& ('i' == data[8]) && ('n' == data[9]) && ('f' == data[10]) && ('o' == data[11])
				&& (':' == data[12])) {

			if ((data[13] >= '0' && data[13] <= '9') && (data[14] >= '0' && data[14] <= '9')
					&& (data[15] >= '0' && data[15] <= '9') && (data[17] >= '0' && data[17] <= '9')
					&& (data[18] >= '0' && data[18] <= '9') && (data[20] >= '0' && data[20] <= '9')
					&& (data[21] >= '0' && data[21] <= '9') && (data[23] >= '0' && data[23] <= '9')
					&& (data[24] >= '0' && data[24] <= '9') && (data[26] >= '0' && data[26] <= '9')
					&& (data[27] >= '0' && data[27] <= '9')
					&& (data[28] >= '0' && data[28] <= '9')) {

				char[] totalTemp = new char[3];
				char[] macTemp = new char[2];
				char[] freqTemp = new char[2];
				char[] stbidTemp = new char[2];
				char[] privateTemp = new char[3];

				totalTemp[0] = (char) data[13];
				totalTemp[1] = (char) data[14];
				totalTemp[2] = (char) data[15];
				totalStr = String.valueOf(totalTemp);
				mStbinfoTotalLen = Integer.valueOf(totalStr);

				macTemp[0] = (char) data[17];
				macTemp[1] = (char) data[18];
				macStr = String.valueOf(macTemp);
				mStbinfoMacLen = Integer.valueOf(macStr);

				freqTemp[0] = (char) data[20];
				freqTemp[1] = (char) data[21];
				freqStr = String.valueOf(freqTemp);
				mStbinfoFreqLen = Integer.valueOf(freqStr);

				stbidTemp[0] = (char) data[23];
				stbidTemp[1] = (char) data[24];
				stbidStr = String.valueOf(stbidTemp);
				mStbinfoSerialLen = Integer.valueOf(stbidStr);

				privateTemp[0] = (char) data[26];
				privateTemp[1] = (char) data[27];
				privateTemp[2] = (char) data[28];
				privateStr = String.valueOf(privateTemp);
				mStbinfoPrivateDataLen = Integer.valueOf(privateStr);

				if (mStbinfoTotalLen < (mStbinfoMacLen + mStbinfoFreqLen + mStbinfoSerialLen
						+ mStbinfoPrivateDataLen)) {
					errorCode |= ERROR_TOTAL;
				}
				if (17 != mStbinfoMacLen && 0 != mStbinfoMacLen) {
					errorCode |= ERROR_MAC;
				}
				if (30 != mStbinfoFreqLen && 0 != mStbinfoFreqLen) {
					errorCode |= ERROR_FRE;
				}
				if (FactoryUtil.LENGTH_SERIAL_NUMBER != mStbinfoSerialLen
						&& 0 != mStbinfoSerialLen) {
					errorCode |= ERROR_SERIAL;
				}
				if (FactoryUtil.MAX_LENGTH_PRIVATE_DATA < mStbinfoPrivateDataLen
						&& 0 != mStbinfoPrivateDataLen) {
					errorCode |= ERROR_PRIVATE;
				}
				if (errorCode != 0) {
					// sendMessage(DataInputActivity.MSG_SHOW_TOAST,
					// R.string.data_receive_error);
				}
			} else {
				errorCode |= ERROR_UNKOWN;
			}
		} else {
			errorCode |= ERROR_UNKOWN;
		}
		if (errorCode == 0) {
			String ReturnStr = "JZ2#3stbinfo:" + totalStr + "-" + macStr + "-" + freqStr + "-"
					+ stbidStr + "-" + privateStr;
			byte[] OutByte = ReturnStr.getBytes();
			outputStream.write(OutByte);
		}
		Log.i(TAG, MODULE + "checkFirstData errorCode = " + errorCode);
		return errorCode;
	}

	private int saveUsedData(byte[] data, OutputStream outputStream, int length)
			throws IOException {
		int errorCode = 0, curIndex = 0;
		int totalLength = mStbinfoTotalLen;

		String mac = "";
		String serialNum = "";
		String privateData = "";
		String mainFre = "";
		String mainSys = "";
		String mainQam = "";
		String upgradeFre = "";
		String upgradeSys = "";
		String upgradeQam = "";
		String umtPid = "";
		String umtTableId = "";

		if (('J' == data[0]) && ('Z' == data[1]) && ('2' == data[2]) && ('3' == data[3])
				&& ('3' == data[4])) {
			curIndex += 5;
			totalLength -= 5;
		} else {
			errorCode |= ERROR_UNKOWN;
			return errorCode;
		}

		while (totalLength > 0) {
			if (curIndex >= BUFF_SIZE) {
				errorCode |= ERROR_UNKOWN;
				return errorCode;
			}
			if (('J' == data[curIndex]) && ('Z' == data[curIndex + 1])
					&& (':' == data[curIndex + 5])) {
				if (('M' == data[curIndex + 2]) && ('A' == data[curIndex + 3])
						&& ('C' == data[curIndex + 4])) {
					curIndex += 6;
					totalLength -= 6;
					char[] macData = new char[FactoryUtil.LENGTH_MAC];
					if (totalLength >= 17) {
						if (transformDataAndCheckMacLegal(data, macData, curIndex)) {
							mac = String.valueOf(macData);
							Log.i(TAG, MODULE + "saveUsedData mac = " + mac);
						} else {
							errorCode |= ERROR_MAC;
						}
						curIndex += 17;
						totalLength -= 17;
					}
				} else if (('F' == data[curIndex + 2]) && ('R' == data[curIndex + 3])
						&& ('E' == data[curIndex + 4])) {
					curIndex += 6;
					totalLength -= 6;
					if (totalLength >= mStbinfoFreqLen) {/*
						char[] mainFreData = new char[FactoryUtil.LENGTH_FREQUENCY];
						if (transformDataAndCheckSerialLegal(data, mainFreData, curIndex,
								FactoryUtil.LENGTH_FREQUENCY)) {
							mainFre = String.valueOf(mainFreData);
							Log.i(TAG, MODULE + "saveUsedData mainFre = " + mainFre);
						}
						curIndex += 4;
						totalLength -= 4;

						char[] mainSysData = new char[FactoryUtil.LENGTH_SYMBOLRATE];
						if (transformDataAndCheckSerialLegal(data, mainSysData, curIndex,
								FactoryUtil.LENGTH_SYMBOLRATE)) {
							mainSys = String.valueOf(mainSysData);
							Log.i(TAG, MODULE + "saveUsedData mainSys = " + mainSys);
						}
						curIndex += 5;
						totalLength -= 5;

						char[] mainQamData = new char[FactoryUtil.LENGTH_MODULATION];
						if (transformDataAndCheckSerialLegal(data, mainQamData, curIndex,
								FactoryUtil.LENGTH_MODULATION)) {
							mainQam = String.valueOf(mainQamData);
							Log.i(TAG, MODULE + "saveUsedData mainQam = " + mainQam);
						}
						curIndex += 2;
						totalLength -= 2;

						char[] upgradeFreData = new char[FactoryUtil.LENGTH_FREQUENCY];
						if (transformDataAndCheckSerialLegal(data, upgradeFreData, curIndex,
								FactoryUtil.LENGTH_FREQUENCY)) {
							upgradeFre = String.valueOf(upgradeFreData);
							Log.i(TAG, MODULE + "saveUsedData upgradeFre = " + upgradeFre);
						}
						curIndex += 4;
						totalLength -= 4;

						char[] upgradeSysData = new char[FactoryUtil.LENGTH_SYMBOLRATE];
						if (transformDataAndCheckSerialLegal(data, upgradeSysData, curIndex,
								FactoryUtil.LENGTH_SYMBOLRATE)) {
							upgradeSys = String.valueOf(upgradeSysData);
							Log.i(TAG, MODULE + "saveUsedData upgradeSys = " + upgradeSys);
						}
						curIndex += 5;
						totalLength -= 5;

						char[] upgradeQamData = new char[FactoryUtil.LENGTH_MODULATION];
						if (transformDataAndCheckSerialLegal(data, upgradeQamData, curIndex,
								FactoryUtil.LENGTH_MODULATION)) {
							upgradeQam = String.valueOf(upgradeQamData);
							Log.i(TAG, MODULE + "saveUsedData upgradeQam = " + upgradeQam);
						}
						curIndex += 2;
						totalLength -= 2;

						char[] umtPidData = new char[FactoryUtil.LENGTH_UMT_PID];
						if (transformDataAndCheckSerialLegal(data, umtPidData, curIndex,
								FactoryUtil.LENGTH_UMT_PID)) {
							umtPid = String.valueOf(umtPidData);
						}
						curIndex += 5;
						totalLength -= 5;

						char[] umtTableIdData = new char[FactoryUtil.LENGTH_UMT_TABLEID];
						if (transformDataAndCheckSerialLegal(data, umtTableIdData, curIndex,
								FactoryUtil.LENGTH_UMT_TABLEID)) {
							umtTableId = String.valueOf(umtTableIdData);
							Log.i(TAG, MODULE + "saveUsedData umtTableId = " + umtTableId);
						}
						curIndex += 4;
						totalLength -= 4;
					*/}
				} else if (('S' == data[curIndex + 2]) && ('I' == data[curIndex + 3])
						&& ('D' == data[curIndex + 4])) {
					curIndex += 6;
					totalLength -= 6;
					char[] stbIdData = new char[mStbinfoSerialLen];
					if (totalLength >= mStbinfoSerialLen) {
						if (transformDataAndCheckSerialLegal(data, stbIdData, curIndex,
								mStbinfoSerialLen)) {
							serialNum = String.valueOf(stbIdData);
							Log.i(TAG, MODULE + "saveUsedData serialNum = " + serialNum);
						} else {
							errorCode |= ERROR_SERIAL;
						}
						curIndex += mStbinfoSerialLen;
						totalLength -= mStbinfoSerialLen;
					}
				} else if (('D' == data[curIndex + 2]) && ('A' == data[curIndex + 3])
						&& ('T' == data[curIndex + 4])) {
					int dataLen = 0;

					curIndex += 6;
					totalLength -= 6;
					if (totalLength >= 4) {
						dataLen = ((data[curIndex] - '0') * 100) + ((data[curIndex + 1] - '0') * 10)
								+ (data[curIndex + 2] - '0');
					}
					curIndex += 4;
					totalLength -= 4;
					if (totalLength >= dataLen) {
						char[] privateDataC = new char[dataLen];
						if (mStbinfoPrivateDataLen == dataLen
								&& transformDataAndCheckPrivateDataLegal(data, privateDataC,
										curIndex, dataLen)) {
							privateData = String.valueOf(privateDataC);
							Log.i(TAG, MODULE + "saveUsedData privateData = " + privateData);
						} else {
							errorCode |= ERROR_PRIVATE;
						}
						curIndex += dataLen;
						totalLength -= dataLen;
					}
				} else {
					curIndex++;
					totalLength--;
				}
			} else {
				curIndex++;
				totalLength--;
			}
		}
		if (errorCode == 0) {
			SystemProperties.set("sys.jzfactory.quick.write.flash", "1");
			boolean isSaveMac = FactoryUtil.getInstance().saveMac(mContext, mac);
			boolean isSaveSerialNum = true;//FactoryUtil.getInstance().saveSerialNum(mContext, serialNum);
			boolean isSavePrivateData = FactoryUtil.getInstance().savePrivateData(mContext,
					privateData);
//			boolean isSaveMainFre = FactoryUtil.getInstance().saveMainFre(mContext, mainFre,
//					mainSys, mainQam);
//			boolean isSaveUpgradeFre = FactoryUtil.getInstance().saveUpgradeFre(mContext,
//					upgradeFre, upgradeSys, upgradeQam, umtPid, umtTableId);
			if (isSaveMac || isSaveSerialNum || isSavePrivateData) {
//				FactoryUtil.getInstance().setMarkable();
//				FactoryUtil.getInstance().setSTBNumable();
				SystemProperties.set("sys.jzfactory.quick.write.flash", "0");
//				FactoryUtil.getInstance().setCommit();
				Handler handler = new Handler(Looper.getMainLooper());
				handler.post(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(mContext, "保存数据成功",
								Toast.LENGTH_LONG).show();
					}
				});
			}
//			mContext.sendBroadcast(new Intent(FactoryUtil.BROADCAST_EXIT_DATAINPUT));
			data[3] = '#';
			// outputStream.write(data);
			outputStream.write(getReceiveDataStr(data, length, false).getBytes());
			SystemProperties.set("sys.jzfactory.quick.write.flash", "0");
		}
		Log.i(TAG, MODULE + "saveUsedData errorCode = " + errorCode);
		return errorCode;
	}

	private boolean transformDataAndCheckMacLegal(byte[] dataIn, char[] dataOut, int offset) {
		int j = 0;
		for (int i = 0; i < 17; i++) {
			if (i == 2 || i == 5 || i == 8 || i == 11 || i == 14) {
				continue;
			}
			if ((dataIn[offset + i] >= '0' && dataIn[offset + i] <= '9')
					|| (dataIn[offset + i] >= 'a' && dataIn[offset + i] <= 'f')
					|| (dataIn[offset + i] >= 'A' && dataIn[offset + i] <= 'F')) {
				dataOut[j] = (char) dataIn[offset + i];
				j++;
			} else {
				return false;
			}
		}
		return true;
	}

	private boolean transformDataAndCheckSerialLegal(byte[] dataIn, char[] dataOut, int offset,
			int length) {
		for (int i = 0; i < length; i++) {
			if (dataIn[offset + i] >= '0' && dataIn[offset + i] <= '9') {
				dataOut[i] = (char) dataIn[offset + i];
			} else {
				return false;
			}
		}
		return true;
	}

	private boolean transformDataAndCheckPrivateDataLegal(byte[] dataIn, char[] dataOut, int offset,
			int length) {
		for (int i = 0; i < length; i++) {
			if (dataIn[offset + i] >= 33 && dataIn[offset + i] <= 127) {
				dataOut[i] = (char) dataIn[offset + i];
			} else {
				return false;
			}
		}
		return true;
	}

}
