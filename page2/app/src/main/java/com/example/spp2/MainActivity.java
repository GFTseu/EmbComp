package com.example.spp2;

import static android.bluetooth.BluetoothDevice.BOND_BONDED;
import static android.bluetooth.BluetoothDevice.BOND_BONDING;
import static android.bluetooth.BluetoothDevice.BOND_NONE;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.RoundingMode;
import java.security.interfaces.RSAKey;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    BlueToothController blueToothController = new BlueToothController();
    public ArrayList<String> requestList = new ArrayList<>();
    public static MainActivity instance = null;
    public ArrayList<String> arrayList = new ArrayList<>();
    public ArrayList<String> deviceName = new ArrayList<>();
    private IntentFilter foundFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
    public ArrayAdapter adapter1;
    Set<BluetoothDevice> deviceList = null;
    BluetoothSocket bluetoothSocket = null;
    UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private Handler handler;
    private String currentDeviceName="";
    private String currentDeviceAddr="";

    private LineChart mLineChart;
    private LineChartUtil mLineChartUtil;

    private  TextView textRecvDataInfo;
    private  TextView textSendWaveType;
    private  TextView textSendWaveFreq;
    private  TextView textSendWaveDutyRatio;
    private  TextView textSendWaveHighVoltage;
    private  TextView textSendWaveLowVoltage;
    private  TextView textSendWaveHighVoltageTextView;
    private  TextView textSendWaveLowVoltageTextView;
    private  ListView BlueToothListView;

    public static ArrayList list = new ArrayList();
    public static float[] RecvDataVoltageFloat = new float[2000];
    public static Map map1 = new HashMap<String, String>();
    public static Map VolCntMap = new HashMap();
    public static boolean fakeMode = true;
    public static ArrayList limitLineList = new ArrayList<>();
    public static boolean cursorVisible = false;
    public static Double unitTime;
    public static ArrayList chartDataArray = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;
        //绑定按钮
        Button bt0 = (Button) findViewById(R.id.SearchBluetoothBtn);
        Button bt1 = (Button) findViewById(R.id.button1);
        Button bt2 = (Button) findViewById(R.id.button2);
        Button bt3 = (Button) findViewById(R.id.SendSignalDataBtn);
        Button bt4 = (Button) findViewById(R.id.button4);
        Button voltageExpressionChange = (Button) findViewById(R.id.WaveVoltageExpressionBtn);
        Button clearChart = (Button) findViewById(R.id.clearChart);
        Button cursor = (Button) findViewById(R.id.cursorSwitch);
        Button x1u = (Button) findViewById(R.id.X1U);
        Button x2u = (Button) findViewById(R.id.X2U);
        Button x1d = (Button) findViewById(R.id.X1D);
        Button x2d = (Button) findViewById(R.id.X2D);
        mLineChart = findViewById(R.id.chart);
        mLineChartUtil = new LineChartUtil(mLineChart);

        // 设置LineChart的属性
        mLineChartUtil.initLineChart(list);
        // 初始化折线图的数据
        mLineChartUtil.initLineChartData();
        //新建一个监视器类对象
        MyClickListener mcl = new MyClickListener();
        //button注册监视器
        bt0.setOnClickListener(mcl);
        bt1.setOnClickListener(mcl);
        bt2.setOnClickListener(mcl);
        bt3.setOnClickListener(mcl);
        bt4.setOnClickListener(mcl);
        voltageExpressionChange.setOnClickListener(mcl);
        clearChart.setOnClickListener(mcl);
        cursor.setOnClickListener(mcl);
        x1u.setOnClickListener(mcl);
        x2u.setOnClickListener(mcl);
        x1d.setOnClickListener(mcl);
        x2d.setOnClickListener(mcl);

        textRecvDataInfo = (TextView) findViewById(R.id.RecvDataInfo);
        textSendWaveType = (TextView) findViewById(R.id.WaveType);
        textSendWaveFreq = (TextView) findViewById(R.id.WaveFreq);
        textSendWaveDutyRatio = (TextView) findViewById(R.id.WaveDutyRatio);
        textSendWaveHighVoltage = (TextView) findViewById(R.id.WaveHighVoltage);
        textSendWaveLowVoltage = (TextView) findViewById(R.id.WaveLowVoltage);
        textSendWaveHighVoltageTextView = (TextView) findViewById(R.id.WaveHighVoltageTextView);
        textSendWaveLowVoltageTextView = (TextView) findViewById(R.id.WaveLowVoltageTextView);
        BlueToothListView = (ListView) findViewById(R.id.BluetoothListView);

        map1.put("Cnt","0");
        VolCntMap.put("VolCnt","0");
        limitLineList.clear();
        limitLineList.add(0f);
        limitLineList.add(0f);
        unitTime = (double) 0;


        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0) { // 接收到Socket数据消息

                    if (RecvDataVoltageFloat[1]!=0||RecvDataVoltageFloat[2]!=0||RecvDataVoltageFloat[3]!=0||RecvDataVoltageFloat[4]!=0||RecvDataVoltageFloat[5]!=0||RecvDataVoltageFloat[10]!=0||RecvDataVoltageFloat[20]!=0) {
                        runOnUiThread(() -> {
                            try {
                                mLineChartUtil = new LineChartUtil(mLineChart);
                                mLineChartUtil.refresh();
                                mLineChartUtil.initLineChart(new ArrayList());
                                mLineChartUtil.initLineChartData();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                        map1.clear();
                        map1.put("Cnt", "0");
                        VolCntMap.clear();
                        VolCntMap.put("VolCnt", "0");
                        list.clear();
                        limitLineList.clear();
                        textRecvDataInfo.setText("");
                        for (int i = 0; i < RecvDataVoltageFloat.length; i++) {
                            RecvDataVoltageFloat[i] = 0f;
                        }
                    }

                    String RecvDataString = (String) msg.obj;
//                    RecvDataString = RecvDataString.substring(0, RecvDataString.length()-8);
                    System.out.println(RecvDataString);

                    //RecvDataString 解析
                    String[] RecvDataStringArray = RecvDataString.split("y");
//                    String RecvDataPeriString = (RecvDataStringArray[0]).substring(2);
                    String RecvDataPeriClkCntString = RecvDataStringArray[0];
                    int RecvVolCnt = RecvDataStringArray.length - 1;
                    System.out.println(RecvVolCnt);

                    // 接收的数据中电平信息个数
                    int RecvDataVolNum = RecvDataStringArray.length;
                    if (VolCntMap.isEmpty()) VolCntMap.put("VolCnt","0");
                    VolCntMap.put("VolCnt", String.valueOf(Integer.parseInt((String) VolCntMap.get("VolCnt")) + RecvDataVolNum));

                    Integer RecvDataPeriClkCntInt = Integer.parseInt(RecvDataPeriClkCntString,16);
                    Double RecvDataPeriClkCntDouble = Double.valueOf(RecvDataPeriClkCntInt);
                    Double RecvDataPeriDouble;

                    // 展示频率、周期数据
                    NumberFormat nf1 = NumberFormat.getNumberInstance();
                    nf1.setMaximumFractionDigits(2);
                    nf1.setRoundingMode(RoundingMode.HALF_UP);
                    NumberFormat nf3 = NumberFormat.getNumberInstance();
                    nf3.setMaximumFractionDigits(3);
                    nf3.setRoundingMode(RoundingMode.HALF_UP);
                    Double freqDisplay;
                    String freqDisplayString;
                    String periDisplayString;
                    String tempPreiString;
                    boolean unitTimeDouble = false;

                    RecvDataPeriClkCntDouble *= 4;
                    if (RecvDataPeriClkCntDouble <= 100) unitTimeDouble = true;
                    System.out.println(unitTimeDouble+"ooooooooooooooooooooooooooooooooooooooooooooooooooooo");

                    if (RecvDataPeriClkCntDouble < 50) {
                        RecvDataPeriDouble = RecvDataPeriClkCntDouble * 20;
                        freqDisplay = 1000.0/RecvDataPeriDouble;
                        freqDisplayString = "Frequency: " + String.valueOf(nf1.format(freqDisplay)) + "MHz";
                        tempPreiString = String.valueOf(RecvDataPeriDouble);
                        if (tempPreiString.length() > 7) tempPreiString = tempPreiString.substring(0, 6);
                        periDisplayString = ";  Period: " + tempPreiString + "ns";
                        unitTime = RecvDataPeriDouble / RecvVolCnt;
                        if (unitTimeDouble) unitTime *= 2;
                        list.add(nf3.format(unitTime));
                        list.add("ns");
                    } else {
                        RecvDataPeriClkCntDouble /= 1000;
                        if (RecvDataPeriClkCntDouble < 50) {
                            RecvDataPeriDouble = RecvDataPeriClkCntDouble * 20;
                            freqDisplay = 1000.0 / RecvDataPeriDouble;
                            freqDisplayString = "Frequency: " + String.valueOf(nf1.format(freqDisplay)) + "kHz";
                            tempPreiString = String.valueOf(RecvDataPeriDouble);
                            if (tempPreiString.length() > 7) tempPreiString = tempPreiString.substring(0, 6);
                            periDisplayString = ";  Period: " + tempPreiString + "us";
                            unitTime = RecvDataPeriDouble / RecvVolCnt;
                            if (unitTimeDouble) unitTime *= 2;
                            list.add(nf3.format(unitTime));
                            list.add("us");
                        } else {
                            RecvDataPeriClkCntDouble /= 1000;
                            if (RecvDataPeriClkCntDouble < 50) {
                                RecvDataPeriDouble = RecvDataPeriClkCntDouble * 20;
                                freqDisplay = 1000.0 / RecvDataPeriDouble;
                                freqDisplayString = "Frequency: " + String.valueOf(nf1.format(freqDisplay)) + "Hz";
                                tempPreiString = String.valueOf(RecvDataPeriDouble);
                                if (tempPreiString.length() > 7) tempPreiString = tempPreiString.substring(0, 6);
                                periDisplayString = ";  Period: " + tempPreiString + "ms";
                                unitTime = RecvDataPeriDouble / RecvVolCnt;
                                list.add(nf3.format(unitTime));
                                list.add("ms");
                            } else {
                                RecvDataPeriClkCntDouble /= 1000;
                                RecvDataPeriDouble = RecvDataPeriClkCntDouble * 20;
                                freqDisplay = 1000.0 / RecvDataPeriDouble;
                                freqDisplayString = "Frequency: " + String.valueOf(nf1.format(freqDisplay)) + "mHz";
                                tempPreiString = String.valueOf(RecvDataPeriDouble);
                                if (tempPreiString.length() > 7) tempPreiString = tempPreiString.substring(0, 6);
                                periDisplayString = ";  Period: " + tempPreiString + "ss";
                                unitTime = RecvDataPeriDouble / RecvVolCnt;
                                list.add(nf3.format(unitTime));
                                list.add("s");

                            }
                        }
                    }

                    // 解析各点数据
                    for (int i = 0; i < RecvDataVolNum-1; i++) {
                        String RecvValueString = RecvDataStringArray[i+1];
                        Integer currentValueDataInt = Integer.parseInt(RecvValueString,16);
                        float currentValue = (float) ((5000 - currentValueDataInt/0.4096));
                        if (currentValue < 4900 && -4900 < currentValue) {
                            String currentValueString = String.valueOf(currentValue);
                            System.out.println(currentValue);
                            int dotIndex = currentValueString.indexOf(".");
                            if ((currentValueString.length() - dotIndex) >= 4) {
                                currentValueString = currentValueString.substring(0, dotIndex + 3);
                                currentValue = Float.parseFloat(currentValueString);
                            }
                            if (map1.isEmpty()) map1.put("Cnt", "0");
                            int currentIndex = Integer.parseInt((String) map1.get("Cnt"));
                            RecvDataVoltageFloat[currentIndex] = currentValue;
                            currentIndex++;
                            map1.put("Cnt", (String.valueOf(currentIndex)));

                            // 在UI线程更新数据到RefreshView组件，例如：
                            float finalCurrentValue = currentValue;
                            runOnUiThread(() -> {

                                try {
                                    mLineChartUtil.addEntry(finalCurrentValue, list, chartDataArray);
                                } catch (Exception e) {
                                    Log.w("MainActivity", "格式化数值失败 dataUI");
                                }

                                // 更新RefreshView组件的逻辑代码...
                                // 例如，刷新RefreshView的数据源或调用某种刷新动画等。
                            });
                        }
                    }

                    // 显示最大最小值
                    float maxV = 0, minV = 0;
                    for (int i = 0; i < (Integer.parseInt((String) map1.get("Cnt"))); i++) {
                        if (maxV < RecvDataVoltageFloat[i]) maxV = RecvDataVoltageFloat[i];
                        if (minV > RecvDataVoltageFloat[i]) minV = RecvDataVoltageFloat[i];
                    }
                    String volString = "Vmax: " + maxV + "mV;  Vmin: " + minV + "mV";
                    textRecvDataInfo.setText(freqDisplayString + periDisplayString + "\n" + volString);

                    // 再把波形重复4遍
                    for (int iter = 0; iter < 4; iter ++) {
                        for (int i = 0; i < RecvDataVolNum-2; i++) {
                            String RecvValueString = RecvDataStringArray[i + 1];
                            Integer currentValueDataInt = Integer.parseInt(RecvValueString, 16);
                            float currentValue = (float) (5000 - currentValueDataInt / 0.4096);
                            String currentValueString = String.valueOf(currentValue);
                            int dotIndex = currentValueString.indexOf(".");
                            if ((currentValueString.length() - dotIndex) >= 4) {
                                currentValueString = currentValueString.substring(0, dotIndex + 3);
                                currentValue = Float.parseFloat(currentValueString);
                            }
                            float finalCurrentValue = currentValue;
                            runOnUiThread(() -> {
                                try {
                                    mLineChartUtil.addEntry(finalCurrentValue, list, chartDataArray);
                                } catch (Exception e) {
                                    Log.w("MainActivity", "格式化数值失败 dataUI");
                                }
                            });
                        }
                    }


                } else { // 处理其他消息... }
                    super.handleMessage(msg);
                }
            };
        };

        ListView listView = (ListView) findViewById(R.id.BluetoothListView);
        //重写列表点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CharSequence content = ((TextView) view).getText();
                String con = content.toString();
                Log.e("setOnItemClickListener", "con:" + content.toString());
                String[] conArray = con.split("\n");
                Log.e("setOnItemClickListener", "conArray[0]:" + conArray[0] + "conArray[1]" + conArray[1] + "conArray[2]" + conArray[2]);
                String rightStr = conArray[1].substring(9, conArray[1].length());//获取蓝牙地址
                Log.e("setOnItemClickListener", "rightStr" + rightStr);

                BluetoothDevice device = blueToothController.mAdapter.getRemoteDevice(rightStr);//根据地址找到相应的设备

                try {
                    currentDeviceName=device.getName();
                    currentDeviceAddr=device.getAddress();
                    if (device.getBondState() == BOND_NONE)//未配对的进行配对，否则取消配对
                    {
                        deviceName.remove(con);
                        device.createBond();
                        con = "Name :" + device.getName()  + "\tState: Paired";
                        deviceName.add(con);
                        adapter1.notifyDataSetChanged();
                    }

                    Toast.makeText(getApplicationContext(), "Start Connecting Bluetooth", Toast.LENGTH_SHORT).show();
                    bluetoothSocket= device.createInsecureRfcommSocketToServiceRecord(SPP_UUID);
                    bluetoothSocket.connect();
                    if(bluetoothSocket.isConnected()) {
                        Toast.makeText(getApplicationContext(), "Bluetooth Connected", Toast.LENGTH_SHORT).show();
                        deviceName.remove(con);
                        con = "Name :" + device.getName() + "\nState: Connected";
                        deviceName.add(con);
                        adapter1.notifyDataSetChanged();
                        //   handler.post(new ReceivedRunnable(bluetoothSocket,textRecvDataInfo));
                        new SocketTask(bluetoothSocket, handler).execute();


                    }

                } catch (SecurityException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        //判断是否设备支持蓝牙
        if (!blueToothController.isSupportBlueTooth()) {
            Toast.makeText(getApplicationContext(), "设备不支持蓝牙，3s后退出app", Toast.LENGTH_SHORT).show();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    instance.finish();
                    System.exit(0);
                }
            }, 3000);
        }
        //获取并检查手机蓝牙权限
        GetPermission();
        if (!CheckPermision()) {
            Toast.makeText(getApplicationContext(), "Permission Unavailable", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getApplicationContext(), "Permission Available", Toast.LENGTH_SHORT).show();
        }
        if (!blueToothController.getBlueToothStatus()) {
            blueToothController.turnOnBlueTooth(1);
        }
        adapter1 = new ArrayAdapter(instance, R.layout.bluetooth_list_item, deviceName);//数组适配器
        //注册广播和过滤器
        foundFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        foundFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(bluetoothReceiver, foundFilter);
        listView.setAdapter(adapter1);

    }

//    private class MyAsyncTask extends AsyncTask<Void, Void, String> {
//        @SuppressLint("WrongThread")
//        @Override
//        protected String doInBackground(Void... params) {
//            // 在这里执行耗时的操作，例如网络请求
//            String msg =  CommonValue.getInstance().getData().poll();
//
//            onPostExecute(msg);
//            return msg;
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            if(result!=null) {
//                //待改造 解析 分段解析   02130000000032001900......
//                //此处直接转数值 做demo
//                float value = (float) (Float.parseFloat(result));
//                Log.w("MainActivity", "xxxxxxxx dataUI" + value);
//
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        mLineChartUtil.addEntry(value);
//                        textRecvDataInfo.setText(textRecvDataInfo.getText()+"\n"+value);
//                    }
//                });
//
//
//            }
//        }
//    }


    //重写button点击事件
    class MyClickListener implements View.OnClickListener {

        boolean isConnected = false;
        TextView textView = (TextView)findViewById(R.id.TextView0);
        TextView cursortextView = (TextView) findViewById(R.id.cursorInfo);

        public void showCursorInfo(Float currX1, Float currX2) {
            if (!MainActivity.cursorVisible) {
                cursortextView.setText("Cursor Off");
                return;
            }
            NumberFormat nf5 = NumberFormat.getNumberInstance();
            nf5.setMaximumFractionDigits(4);
            nf5.setRoundingMode(RoundingMode.HALF_UP);
            Float deltaX21 = currX2 - currX1;
            Float y1 = (Float) MainActivity.chartDataArray.get((int) ((float) currX1));
            Float y2 = (Float) MainActivity.chartDataArray.get((int) ((float) currX2));
            Float deltaY21 = y2 - y1;
            String timeScale;
            if (!list.isEmpty()) timeScale = (String) list.get(1);
            else timeScale = "ns";
            String dispTimeScale = timeScale;
            String currX1timeScale = timeScale;
            String currX2timeScale = timeScale;

            if (unitTime==0) unitTime = 15.2091408;
            deltaX21 *= Float.parseFloat(String.valueOf(unitTime));
            currX1 *= Float.parseFloat(String.valueOf(unitTime));
            currX2 *= Float.parseFloat(String.valueOf(unitTime));

            if (deltaX21 >= 1000 && !dispTimeScale.equals("s")) {
                deltaX21 /= 1000;
                if (dispTimeScale.equals("ns")) dispTimeScale = "us";
                else if (dispTimeScale.equals("us")) dispTimeScale = "ms";
                else if (dispTimeScale.equals("ms")) dispTimeScale = "s";

                if (deltaX21 >= 1000) {
                    deltaX21 /= 1000;
                    if (dispTimeScale.equals("us")) dispTimeScale = "ms";
                    else if (dispTimeScale.equals("ms")) dispTimeScale = "s";
                }
            }

            if (currX1 >= 1000 && !currX1timeScale.equals("s")) {
                currX1 /= 1000;
                if (currX1timeScale.equals("ns")) currX1timeScale = "us";
                else if (currX1timeScale.equals("us")) currX1timeScale = "ms";
                else if (currX1timeScale.equals("ms")) currX1timeScale = "s";

                if (currX1 >= 1000) {
                    currX1 /= 1000;
                    if (currX1timeScale.equals("us")) currX1timeScale = "ms";
                    else if (currX1timeScale.equals("ms")) currX1timeScale = "s";
                }
            }

            if (currX2 >= 1000 && !currX2timeScale.equals("s")) {
                currX2 /= 1000;
                if (currX2timeScale.equals("ns")) currX2timeScale = "us";
                else if (currX2timeScale.equals("us")) currX2timeScale = "ms";
                else if (currX2timeScale.equals("ms")) currX2timeScale = "s";

                if (currX2 >= 1000) {
                    currX2 /= 1000;
                    if (currX2timeScale.equals("us")) currX2timeScale = "ms";
                    else if (currX2timeScale.equals("ms")) currX2timeScale = "s";
                }
            }

            String line1 = "X1: " + nf5.format(currX1) + currX1timeScale + ";  Y1: " + y1 + "mV";
            String line2 = "X2: " + nf5.format(currX1) + currX2timeScale + ";  Y2: " + y2 + "mV";
            String line3 = "\u0394X = X2 - X1 = " + nf5.format(deltaX21) + dispTimeScale + ";  \u0394Y = Y2 - Y1 = " + deltaY21 + "mV";
            cursortextView.setText(line1 + ";  " + line2 + "\n" + line3);
        }

        @Override
        public void onClick(View v) {
            if (limitLineList.isEmpty()) {
                limitLineList.add(0f);
                limitLineList.add(0f);
            }
            Float currX1 = (Float) limitLineList.get(0);
            Float currX2 = (Float) limitLineList.get(1);
            Float highVisX = mLineChartUtil.getHighVisX();
            Float lowVisX = mLineChartUtil.getLowVisX();
            Float deltaX = highVisX - lowVisX;

            switch (v.getId()) {
                case R.id.SearchBluetoothBtn:
                    arrayList.clear();
                    deviceName.clear();
                    blueToothController.findDevice();
                    MainActivity.fakeMode = (!MainActivity.fakeMode);
                    break;
                case R.id.SendSignalDataBtn:
                    try {
                        String sendDataLegal = "1";   // 有效标志，1表示此次数据有效
                        String sendDataWaveType = textSendWaveType.getText().toString();
                        String sendDataWaveFreq = textSendWaveFreq.getText().toString();
                        String sendDataWaveDutyRatio = textSendWaveDutyRatio.getText().toString();
                        String sendDataWaveHighVoltage = textSendWaveHighVoltage.getText().toString();
                        String sendDataWaveLowVoltage = textSendWaveLowVoltage.getText().toString();


                        // WaveType
                        if (sendDataWaveType.equals("")) sendDataWaveType = "0";
                        if (sendDataWaveType.equals("sine") || sendDataWaveType.equals("Sine")) sendDataWaveType = "0";
                        else if (sendDataWaveType.equals("rec") || sendDataWaveType.equals("Rec")) sendDataWaveType = "1";
                        else if (sendDataWaveType.equals("tri") || sendDataWaveType.equals("Tri")) sendDataWaveType = "2";
                        else if (sendDataWaveType.equals("const") || sendDataWaveType.equals("Const")) sendDataWaveType = "3";
                        else {
                            sendDataLegal = "0";
                            sendDataWaveType = "";
                        }

                        // WaveFreq
                        if (sendDataWaveFreq.equals("")) {
                            sendDataWaveFreq = "000001000";  // 1kHz
                        } else {
                            String sendDataWaveFreqDataString = "";
                            // 频率的数据位
                            Double sendDataWaveFreqDataDouble;
                            // 频率的数量级
                            char sendDataWaveFreqMagnitudeLevel = sendDataWaveFreq.charAt(sendDataWaveFreq.length() - 3);
                            if (sendDataWaveFreqMagnitudeLevel == 'k' || sendDataWaveFreqMagnitudeLevel == 'K') {
                                sendDataWaveFreqDataString = sendDataWaveFreq.substring(0, sendDataWaveFreq.length()-3);
                                sendDataWaveFreqDataDouble = Double.parseDouble(sendDataWaveFreqDataString);
                                sendDataWaveFreqDataDouble *= 1000;
                            } else if (sendDataWaveFreqMagnitudeLevel == 'm' || sendDataWaveFreqMagnitudeLevel == 'M') {
                                sendDataWaveFreqDataString = sendDataWaveFreq.substring(0, sendDataWaveFreq.length()-3);
                                sendDataWaveFreqDataDouble = Double.parseDouble(sendDataWaveFreqDataString);
                                sendDataWaveFreqDataDouble *= 1000000;
                            } else {
                                sendDataWaveFreqDataString = sendDataWaveFreq.substring(0, sendDataWaveFreq.length()-2);
                                sendDataWaveFreqDataDouble = Double.parseDouble(sendDataWaveFreqDataString);
                            }
                            // 数据位乘1000后传输
                            int sendDataWaveFreqTempInt = (int) (sendDataWaveFreqDataDouble * 1.0);
                            sendDataWaveFreq = String.format("%09d", sendDataWaveFreqTempInt);
                        }


                        // WaveDutyRatio
                        // 占空比仅当波形为矩形波或三角波时才有效
                        if (sendDataWaveType.equals("1") || sendDataWaveType.equals("2")) {
                            if (sendDataWaveDutyRatio.equals("")) {
                                sendDataWaveDutyRatio = "050000";
                            } else {
                                Double sendDataWaveDutyRatioDataDouble = Double.valueOf(sendDataWaveDutyRatio);
                                int sendDataWaveDutyRatioDataInt = (int) (sendDataWaveDutyRatioDataDouble * 1000);
                                sendDataWaveDutyRatio = String.format("%06d", sendDataWaveDutyRatioDataInt);
                            }
                        } else {
                            if (!sendDataWaveDutyRatio.equals("")) {
                                Toast.makeText(getApplicationContext(), "ILLEGAL INPUT! Duty Ratio Unavailable", Toast.LENGTH_LONG).show();
                                sendDataWaveDutyRatio = "000000";
                                sendDataLegal = "0";  // 信号有效标志置为0
                            } else {
                                sendDataWaveDutyRatio = "";
                            }
                        }


                        // High Voltage and Low Voltage
                        // 分别用6位传输，第一位为符号位，后五位为数据*10000
                        String sendDataVoltageExpression = "0";  // 高低电平表示法
                        if (textSendWaveHighVoltageTextView.getText().equals("High Voltage")) {
                            sendDataVoltageExpression = "0";  // 高低电平表示法
                        } else if (textSendWaveHighVoltageTextView.getText().equals("VPP")) {
                            sendDataVoltageExpression = "1";  // VPP-Offset表示法
                        }

                        // 默认情况
                        if (sendDataWaveHighVoltage.equals("") && sendDataWaveLowVoltage.equals("")) {
                            sendDataWaveHighVoltage = "6000000";  // 默认高1V
                            sendDataWaveLowVoltage = "4000000";  // 默认低-1V
                        }

                        else if (!sendDataWaveHighVoltage.equals("") && !sendDataWaveLowVoltage.equals("")) {
                            String sendDataWaveHighVoltageDataString;
                            String sendDataWaveLowVoltageDataString;
                            Double sendDataWaveHighVoltageDataDouble;
                            Double sendDataWaveLowVoltageDataDouble;
                            int sendDataWaveHighVoltageDataInt;
                            int sendDataWaveLowVoltageDataInt;
                            char sendDataWaveHighVoltageMagnitudeLevel = sendDataWaveHighVoltage.charAt(sendDataWaveHighVoltage.length() - 2);
                            char sendDataWaveLowVoltageMagnitudeLevel = sendDataWaveLowVoltage.charAt(sendDataWaveLowVoltage.length() - 2);

                            // 高电平数据解析
                            if (sendDataWaveHighVoltageMagnitudeLevel == 'm' || sendDataWaveHighVoltageMagnitudeLevel == 'M') {
                                sendDataWaveHighVoltageDataString = sendDataWaveHighVoltage.substring(0, sendDataWaveHighVoltage.length()-2);
                                sendDataWaveHighVoltageDataDouble = Double.parseDouble(sendDataWaveHighVoltageDataString);
                                // 单位转化为 V
                                sendDataWaveHighVoltageDataDouble /= 1000;
                            } else {
                                sendDataWaveHighVoltageDataString = sendDataWaveHighVoltage.substring(0, sendDataWaveHighVoltage.length()-1);
                                sendDataWaveHighVoltageDataDouble = Double.parseDouble(sendDataWaveHighVoltageDataString);
                            }

                            // 低电平数据解析
                            if (sendDataWaveLowVoltageMagnitudeLevel == 'm' || sendDataWaveLowVoltageMagnitudeLevel == 'M') {
                                sendDataWaveLowVoltageDataString = sendDataWaveLowVoltage.substring(0, sendDataWaveLowVoltage.length()-2);
                                sendDataWaveLowVoltageDataDouble = Double.parseDouble(sendDataWaveLowVoltageDataString);
                                // 单位转化为 V
                                sendDataWaveLowVoltageDataDouble /= 1000;
                            } else {
                                sendDataWaveLowVoltageDataString = sendDataWaveLowVoltage.substring(0, sendDataWaveLowVoltage.length()-1);
                                sendDataWaveLowVoltageDataDouble = Double.parseDouble(sendDataWaveLowVoltageDataString);
                            }

                            System.out.println("sendDataWaveLowVoltageDouble:              " + sendDataWaveLowVoltageDataDouble);

                            // VPP模式数据有效检验
                            if (sendDataVoltageExpression.equals("1")) {
                                if (sendDataWaveHighVoltageDataDouble < 0) {
                                    // VPP小于0，数据无效
                                    sendDataLegal = "0";
                                    Toast.makeText(getApplicationContext(), "ILLEGAL INPUT! VPP should varies from 0 to 6V", Toast.LENGTH_LONG).show();
                                }
                                if (sendDataWaveLowVoltageDataDouble > 3 || sendDataWaveLowVoltageDataDouble < -3) {
                                    // abs(offset)大于3V，数据无效
                                    sendDataLegal = "0";
                                    Toast.makeText(getApplicationContext(), "ILLEGAL INPUT! Offset should varies from -3V to 3V", Toast.LENGTH_LONG).show();
                                }
                                Double tmpHighDataDouble = sendDataWaveLowVoltageDataDouble + sendDataWaveHighVoltageDataDouble/2;
                                Double tmpLowDataDouble = sendDataWaveLowVoltageDataDouble - sendDataWaveHighVoltageDataDouble/2;
                                sendDataWaveHighVoltageDataDouble = tmpHighDataDouble;
                                sendDataWaveLowVoltageDataDouble = tmpLowDataDouble;
                            }

                            // 高低电平模式数据有效检验，如果为VPP模式，同样需要检验
                            if (sendDataWaveHighVoltageDataDouble > 3 || sendDataWaveHighVoltageDataDouble < -3 || sendDataWaveLowVoltageDataDouble > 3 || sendDataWaveLowVoltageDataDouble < -3 ) {
                                sendDataLegal = "0";
                                Toast.makeText(getApplicationContext(), "ILLEGAL INPUT! Voltage should varies from -3 to 3V", Toast.LENGTH_LONG).show();
                            }
                            if (sendDataWaveHighVoltageDataDouble < sendDataWaveLowVoltageDataDouble) {
                                sendDataLegal = "0";
                                Toast.makeText(getApplicationContext(), "ILLEGAL INPUT! Low Voltage exceeds High Voltage", Toast.LENGTH_LONG).show();
                            }
                            System.out.println("sendDataWaveLowVoltageDouble:              " + sendDataWaveLowVoltageDataDouble);

                            sendDataWaveHighVoltageDataInt = (int) (1000000*(sendDataWaveHighVoltageDataDouble+5));
                            sendDataWaveLowVoltageDataInt = (int) (1000000*(sendDataWaveLowVoltageDataDouble+5));
                            System.out.println("sendDataWaveLowVoltageInt:              " + sendDataWaveLowVoltageDataInt);

                            sendDataWaveHighVoltage = String.format("%07d", sendDataWaveHighVoltageDataInt);
                            sendDataWaveLowVoltage = String.format("%07d", sendDataWaveLowVoltageDataInt);

                        } else {
                            if (sendDataVoltageExpression.equals("0")) {
                                // 高低电平有一个没设置，数据无效
                                sendDataLegal = "0";
                                sendDataWaveHighVoltage = "";
                                sendDataWaveLowVoltage = "";
                                Toast.makeText(getApplicationContext(), "Both High and Low Voltage should be clarified", Toast.LENGTH_LONG).show();
                            } else if (sendDataVoltageExpression.equals("1")) {
                                // VPP或Offset没设置，数据无效
                                sendDataLegal = "0";
                                sendDataWaveHighVoltage = "";
                                sendDataWaveLowVoltage = "";
                                Toast.makeText(getApplicationContext(), "Both VPP and Offset should be clarified", Toast.LENGTH_LONG).show();
                            }
                        }






                        // 蓝牙传DAC数据采用PQ协议
                        String sendDataString = "P" + sendDataLegal + "t" + sendDataWaveType + "f" + sendDataWaveFreq + "d" + sendDataWaveDutyRatio + "e" + sendDataVoltageExpression + "h" + sendDataWaveHighVoltage + "l" + sendDataWaveLowVoltage + "Q";
                        byte[] sendData = sendDataString.getBytes();
                        Log.w("MainActivity", "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx data send");
                        bluetoothSocket.getOutputStream().write(sendData);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case R.id.button1:
                    if (ActivityCompat.checkSelfPermission(instance, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        Toast.makeText(getApplicationContext(), "无权限操作", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    deviceList = blueToothController.mAdapter.getBondedDevices();
                    for (BluetoothDevice device : deviceList)
                    {
                        try {
                            Method isConnectedMethod = BluetoothDevice.class.getDeclaredMethod("isConnected", (Class[]) null);
                            isConnectedMethod.setAccessible(true);
                            isConnected = (boolean) isConnectedMethod.invoke(device, (Object[]) null);
                            if(isConnected)
                            {
                                bluetoothSocket = device.createRfcommSocketToServiceRecord(SPP_UUID);
                                bluetoothSocket.connect();
                                if(bluetoothSocket.isConnected())

                                {
                                    Toast.makeText(getApplicationContext(), "spp连接成功", Toast.LENGTH_SHORT).show();
                                    textView.setBackgroundColor(Color.parseColor("#4CAF50"));
                                }
                                else
                                {
                                    Toast.makeText(getApplicationContext(), "spp连接失败", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } catch (NoSuchMethodException e) {
                            throw new RuntimeException(e);
                        } catch (InvocationTargetException e) {
                            throw new RuntimeException(e);
                        } catch (IllegalAccessException | IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    break;
                case R.id.WaveVoltageExpressionBtn:
                    if (textSendWaveHighVoltageTextView.getText().equals("VPP")) {
                        textSendWaveHighVoltageTextView.setText("High Voltage");
                        textSendWaveLowVoltageTextView.setText("Low Voltage");
                    } else {
                        textSendWaveHighVoltageTextView.setText("VPP");
                        textSendWaveLowVoltageTextView.setText("Offset");
                    }


                    break;
                case R.id.clearChart:

                    //清空数据
                    runOnUiThread(() -> {
                        try {
                            mLineChartUtil = new LineChartUtil(mLineChart);
                            mLineChartUtil.refresh();
                            mLineChartUtil.initLineChart(new ArrayList());
                            mLineChartUtil.initLineChartData();
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    });

                    MainActivity.map1.clear();
                    MainActivity.map1.put("Cnt","0");
                    MainActivity.VolCntMap.clear();
                    MainActivity.VolCntMap.put("VolCnt","0");
                    MainActivity.list.clear();
                    MainActivity.limitLineList.clear();

                    textRecvDataInfo.setText("");

                    for (int i = 0; i < MainActivity.RecvDataVoltageFloat.length; i++) {
                        RecvDataVoltageFloat[i] = 0f;
                    }
                    MainActivity.cursorVisible = false;
                    mLineChartUtil.DeleteLimitLine();
                    break;
                case R.id.cursorSwitch:

                    runOnUiThread(() -> {
                        try {
                            mLineChartUtil.addEntry(1000, list, MainActivity.chartDataArray);
                        } catch (Exception e) {
                            Log.w("MainActivity", "格式化数值失败 dataUI");
                        }
                    });
                    runOnUiThread(() -> {
                        try {
                            mLineChartUtil.addEntry(190, list, MainActivity.chartDataArray);
                        } catch (Exception e) {
                            Log.w("MainActivity", "格式化数值失败 dataUI");
                        }
                    });
                    runOnUiThread(() -> {
                        try {
                            mLineChartUtil.addEntry(1200, list, MainActivity.chartDataArray);
                        } catch (Exception e) {
                            Log.w("MainActivity", "格式化数值失败 dataUI");
                        }
                    });
                    runOnUiThread(() -> {
                        try {
                            mLineChartUtil.addEntry(500, list, MainActivity.chartDataArray);
                        } catch (Exception e) {
                            Log.w("MainActivity", "格式化数值失败 dataUI");
                        }
                    });
                    runOnUiThread(() -> {
                        try {
                            mLineChartUtil.addEntry(100, list, MainActivity.chartDataArray);
                        } catch (Exception e) {
                            Log.w("MainActivity", "格式化数值失败 dataUI");
                        }
                    });
                    runOnUiThread(() -> {
                        try {
                            mLineChartUtil.addEntry(-900, list, MainActivity.chartDataArray);
                        } catch (Exception e) {
                            Log.w("MainActivity", "格式化数值失败 dataUI");
                        }
                    });
                    runOnUiThread(() -> {
                        try {
                            mLineChartUtil.addEntry(5000, list, MainActivity.chartDataArray);
                        } catch (Exception e) {
                            Log.w("MainActivity", "格式化数值失败 dataUI");
                        }
                    });
                    runOnUiThread(() -> {
                        try {
                            mLineChartUtil.addEntry(1900, list, MainActivity.chartDataArray);
                        } catch (Exception e) {
                            Log.w("MainActivity", "格式化数值失败 dataUI");
                        }
                    });

                    if (limitLineList.isEmpty()) {
                        limitLineList.add(0f);
                        limitLineList.add(0f);
                    }

                    MainActivity.cursorVisible = !MainActivity.cursorVisible;
                    System.out.println(mLineChartUtil.getHighVisX()+"  "+mLineChartUtil.getLowVisX());
                    if (MainActivity.cursorVisible) {
                        mLineChartUtil.ShowLimitLine(limitLineList);
                    } else {
                        limitLineList.clear();
                        currX1 = 0f;
                        currX2 = 0f;
                        mLineChartUtil.DeleteLimitLine();
                    }
                    showCursorInfo(currX1, currX2);
                    break;
                case R.id.X1U:
                    if (MainActivity.cursorVisible) {
                        if (limitLineList.isEmpty()) {
                            System.out.println("ff");
                            limitLineList.add(0f);
                            limitLineList.add(0f);
                        }
                        if (deltaX < 20) currX1 += 1;
                        else if (deltaX < 60) currX1 += 3;
                        else currX1 += 8;
                        if (currX1 < lowVisX) currX1 = (float) (Math.round(lowVisX) + 1);
                        if (currX1 > highVisX) currX1 = (float) (Math.round(highVisX) - 1);
                        limitLineList.clear();
                        limitLineList.add(currX1);
                        limitLineList.add(currX2);
                        mLineChartUtil.DeleteLimitLine();
                        mLineChartUtil.ShowLimitLine(limitLineList);
                        mLineChart.invalidate();
                        showCursorInfo(currX1, currX2);
                    }
                    break;
                case R.id.X1D:
                    if (MainActivity.cursorVisible) {
                        if (limitLineList.isEmpty()) {
                            System.out.println("ff");
                            limitLineList.add(0f);
                            limitLineList.add(0f);
                        }
                        if (deltaX < 20) currX1 -= 1;
                        else if (deltaX < 60) currX1 -= 3;
                        else currX1 -= 8;
                        if (currX1 < lowVisX) currX1 = (float) (Math.round(lowVisX) + 1);
                        if (currX1 > highVisX) currX1 = (float) (Math.round(highVisX) - 1);
                        limitLineList.clear();
                        limitLineList.add(currX1);
                        limitLineList.add(currX2);
                        mLineChartUtil.DeleteLimitLine();
                        mLineChartUtil.ShowLimitLine(limitLineList);
                        mLineChart.invalidate();
                        showCursorInfo(currX1, currX2);
                    }
                    break;
                case R.id.X2U:
                    if (MainActivity.cursorVisible) {
                        if (limitLineList.isEmpty()) {
                            System.out.println("ff");
                            limitLineList.add(0f);
                            limitLineList.add(0f);
                        }
                        if (deltaX < 20) currX2 += 1;
                        else if (deltaX < 60) currX2 += 3;
                        else currX2 += 8;
                        if (currX2 < lowVisX) currX2 = (float) (Math.round(lowVisX) + 1);
                        if (currX2 > highVisX) currX2 = (float) (Math.round(highVisX) - 1);
                        limitLineList.clear();
                        limitLineList.add(currX1);
                        limitLineList.add(currX2);
                        mLineChartUtil.DeleteLimitLine();
                        mLineChartUtil.ShowLimitLine(limitLineList);
                        mLineChart.invalidate();
                        showCursorInfo(currX1, currX2);
                    }
                    break;
                case R.id.X2D:
                    if (MainActivity.cursorVisible) {
                        if (limitLineList.isEmpty()) {
                            System.out.println("ff");
                            limitLineList.add(0f);
                            limitLineList.add(0f);
                        }
                        if (deltaX < 20) currX2 -= 1;
                        else if (deltaX < 60) currX2 -= 3;
                        else currX2 -= 8;
                        if (currX2 < lowVisX) currX2 = (float) (Math.round(lowVisX) + 1);
                        if (currX2 > highVisX) currX2 = (float) (Math.round(highVisX) - 1);
                        limitLineList.clear();
                        limitLineList.add(currX1);
                        limitLineList.add(currX2);
                        mLineChartUtil.DeleteLimitLine();
                        mLineChartUtil.ShowLimitLine(limitLineList);
                        mLineChart.invalidate();
                        showCursorInfo(currX1, currX2);
                    }
                    break;
            }
        }
    }




















    //定义一个蓝牙控制器类
    public class BlueToothController{
        private BluetoothAdapter mAdapter;//定义一个蓝牙适配器
        //添加构造函数
        public BlueToothController(){
            mAdapter = mAdapter.getDefaultAdapter();
        }
        //判断是否支持蓝牙
        public boolean isSupportBlueTooth(){
            if(mAdapter != null){//不为空则支持蓝牙
                return true;
            }
            else{
                return false;
            }
        }
        public boolean getBlueToothStatus(){//获取蓝牙状态
            // 断言,为了避免mAdapter为null导致return出错
            assert (mAdapter != null);
            // 蓝牙状态
            return mAdapter.isEnabled();
        }
        //打开蓝牙
        public void turnOnBlueTooth( int requestCode){
            try{
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                startActivityForResult(intent, requestCode);
            }catch (SecurityException e) {
                e.printStackTrace();
            }
        }
        public boolean findDevice(){
            assert(mAdapter!=null);
            try{
                return mAdapter.startDiscovery();//需要打开定位
            }catch (SecurityException e) {
                e.printStackTrace();
            }
            return false;
        }
    }
    public void GetPermission()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestList.add(Manifest.permission.BLUETOOTH_SCAN);
            requestList.add(Manifest.permission.BLUETOOTH_ADVERTISE);
            requestList.add(Manifest.permission.BLUETOOTH_CONNECT);
            requestList.add(Manifest.permission.ACCESS_FINE_LOCATION);
            requestList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            requestList.add(Manifest.permission.BLUETOOTH);
            requestList.add(Manifest.permission.BLUETOOTH_ADMIN);
        }
        if(requestList.size() != 0){
            //Toast.makeText(getApplicationContext(), "requestList ", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, requestList.toArray(new String[0]), 1);
        }
    }

    public boolean CheckPermision()
    {
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED)) {//待做：之后根据这个更改下检查权限的问题
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(getApplicationContext(), "BLUETOOTH_SCAN 无权限操作", Toast.LENGTH_SHORT).show();
            return false;
        }
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED))
        {
            Toast.makeText(getApplicationContext(), "BLUETOOTH_ADVERTISE 无权限操作", Toast.LENGTH_SHORT).show();
            return false;
        }
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED))
        {
            Toast.makeText(getApplicationContext(), "BLUETOOTH_CONNECT 无权限操作", Toast.LENGTH_SHORT).show();
            return false;
        }
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED))
        {
            Toast.makeText(getApplicationContext(), "ACCESS_FINE_LOCATION 无权限操作", Toast.LENGTH_SHORT).show();
            return false;
        }
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED))
        {
            Toast.makeText(getApplicationContext(), "ACCESS_COARSE_LOCATION 无权限操作", Toast.LENGTH_SHORT).show();
            return false;
        }
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED))
        {
            Toast.makeText(getApplicationContext(), "BLUETOOTH 无权限操作", Toast.LENGTH_SHORT).show();
            return false;
        }
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED))
        {
            Toast.makeText(getApplicationContext(), "BLUETOOTH_ADMIN 无权限操作", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();//Intent要完成的动作
            if (BluetoothDevice.ACTION_FOUND.equals(action))//如果要完成的动作是发现设备操作
            {
                String s;
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                /*
                 * 系统发现新的蓝牙设备了之后，会通过广播把这个设备的信息发送出来。所以我们要通过截获 Action 为BluetoothDevice.ACTION_FOUND的 Intent，并得到设备信息
                 * */
                try{

                    if(device.getBondState()==BOND_NONE)
                    {
                        s = "Name :" + device.getName() + "\n" + "Address: " + device.getAddress() + "\n" + "State: Unpaired";
                    }
                    else if(device.getBondState()==BOND_BONDING)
                    {
                        s = "Name :" + device.getName() + "\n" + "Address: " + device.getAddress() + "\n" + "State: Paring";
                    }
                    else
                    {
                        s = "Name :" + device.getName() + "\n" + "Address: " + device.getAddress() + "\n" + "State: Paired";
                    }
                    if(!deviceName.contains(s))
                    {
                        if(device.getName()!=null&&device.getName().contains("BicycleBT")){
                            deviceName.add(s);
                            arrayList.add(device.getAddress());
                            adapter1.notifyDataSetChanged();
                            blueToothController.mAdapter.cancelDiscovery();
                        }
                    }
                }catch (SecurityException e) {
                    e.printStackTrace();
                }

            }
            else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))//搜索完成
            {
                Toast.makeText(getApplicationContext(), "Searching Completed", Toast.LENGTH_SHORT).show();
                //unregisterReceiver(this);
            }else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
                Toast.makeText(getApplicationContext(), "Start Searching", Toast.LENGTH_SHORT).show();
            }
        }
    };
}