package com.example.spp2;

import android.bluetooth.BluetoothSocket;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.IOException;


public class ReceivedRunnable implements Runnable {
    private static final String TAG = ReceivedRunnable.class.getName();

    private BluetoothSocket socket;
    private TextView textView;

    public ReceivedRunnable(BluetoothSocket socket,TextView textView) {

        this.socket = socket;
        this.textView = textView;
    }

    @Override
    public void run() {
        BufferedInputStream in = null;
        try {
            in = new BufferedInputStream(socket.getInputStream());
            StringBuffer sb = new StringBuffer();
            while (true) {
                if (in.available() > 0) {
                    byte[] buf = new byte[in.available()];
                    in.read(buf);
                    String data = HexStrToStr(ByteArrayToHexStr(buf));
                    if (data.startsWith("B")) {
                        sb = new StringBuffer();
                        if (data.length() > 1) {
                            sb.append(data.substring(1, data.length()));
                        }
                    } else if (data.endsWith("E")) {
                        sb.append(data.substring(0,data.length()-1));
                        CommonValue.getInstance().addData(sb.toString());
                        Log.w(TAG, "xxxxxxxx data" + sb.toString());

                    } else {
                        sb.append(data);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static  String ByteArrayToHexStr(byte[] byteArray) {
        if (byteArray == null) {
            return null;
        }
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[byteArray.length * 2];
        for (int i = 0; i < byteArray.length; i++) {
            int temp = byteArray[i] & 0xFF;
            hexChars[i * 2] = hexArray[temp >>> 4];
            hexChars[i * 2 + 1] = hexArray[temp & 0x0F];
        }
        return new String(hexChars);
    }

    public static String HexStrToStr(String hexStr) {
        //能被16整除,肯定可以被2整除
        byte[] array = new byte[hexStr.length() / 2];
        try {
            for (int i = 0; i < array.length; i++) {
                array[i] = (byte) (0xff & Integer.parseInt(hexStr.substring(i * 2, i * 2 + 2), 16));
            }
            hexStr = new String(array, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        return hexStr;
    }

}