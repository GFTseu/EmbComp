package com.example.spp2;

import static android.bluetooth.BluetoothDevice.BOND_BONDING;
import static android.bluetooth.BluetoothDevice.BOND_NONE;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;


import com.github.mikephil.charting.charts.LineChart;

import org.w3c.dom.Text;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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


    public static ArrayList list = new ArrayList();
    public static Map map1 = new HashMap<String, String>();
    public static Map VolCntMap = new HashMap();
    public static boolean fakeMode = true;
    public static ArrayList limitLineList = new ArrayList<>();
    public static Double unitTime;

    public EditText forwardGeValue;
    public EditText backGeValue;
    public Spinner forwardGeSpinner;
    public Spinner backGeSpinner;
    public String currentForwardGeSpinnerString;
    public String currentBackGeSpinnerString;
    public TextView currentStepFrequency;
    public TextView currentVelocity;


    Integer[] forwardGeValueStored = {0, 0, 0};
    Integer[] backGeValueStored = {0, 0, 0, 0, 0, 0, 0, 0};


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

        Button forwardGeParamDec = (Button) findViewById(R.id.forwardGeParamDec);
        Button forwardGeParamInc = (Button) findViewById(R.id.forwardGeParamInc);
        Button backGeParamDec = (Button) findViewById(R.id.backGeParamDec);
        Button backGeParamInc = (Button) findViewById(R.id.backGeParamInc);
        Button forwardGeParams = (Button) findViewById(R.id.forwardGeParams);
        Button backGeParams = (Button) findViewById(R.id.backGeParams);

        mLineChart = findViewById(R.id.chart);
        mLineChartUtil = new LineChartUtil(mLineChart);

        // 设置LineChart的属性
        mLineChartUtil.initLineChart(list);
        // 初始化折线图的数据
        mLineChartUtil.initLineChartData();
        //新建一个监视器类对象
        MyClickListener mcl = new MyClickListener();
        //button注册监视器
        forwardGeParams.setOnClickListener(mcl);
        backGeParams.setOnClickListener(mcl);
        forwardGeParamDec.setOnClickListener(mcl);
        forwardGeParamInc.setOnClickListener(mcl);
        backGeParamDec.setOnClickListener(mcl);
        backGeParamInc.setOnClickListener(mcl);

        bt0.setOnClickListener(mcl);
        bt1.setOnClickListener(mcl);
        bt2.setOnClickListener(mcl);
        bt3.setOnClickListener(mcl);
        bt4.setOnClickListener(mcl);

        forwardGeValue = (EditText) findViewById(R.id.forwardGeValue);
        backGeValue = (EditText) findViewById(R.id.backGeValue);
        forwardGeSpinner = (Spinner) findViewById(R.id.forwardGeSpinner);
        backGeSpinner = (Spinner) findViewById(R.id.backGeSpinner);
        forwardGeSpinner.setOnItemSelectedListener(new forwardSpinnerOnItemSelectedListener());
        backGeSpinner.setOnItemSelectedListener(new backSpinnerOnItemSelectedListener());
        currentStepFrequency = (TextView) findViewById(R.id.currentStepFreqency);
        currentVelocity = (TextView) findViewById(R.id.currentVelocity);

        map1.put("Cnt","0");
        VolCntMap.put("VolCnt","0");
        limitLineList.clear();
        limitLineList.add(0f);
        limitLineList.add(0f);
        unitTime = (double) 0;


        handler = new Handler(Looper.getMainLooper()) {
            @SuppressLint("SetTextI18n")
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0) { // 接收到Socket数据消息
                    String RecvDataString = (String) msg.obj;
                    if (RecvDataString.charAt(1) == '0') {
                        String[] RecvDataStringArray = RecvDataString.split("y");
                        Integer currentStepFrequencyValue = Integer.parseInt(RecvDataStringArray[1]);
                        Integer currentVelocityValue = Integer.parseInt(RecvDataStringArray[2]);
                        currentStepFrequency.setText("当前踏频："+ currentStepFrequencyValue + "次/分钟");
                        currentVelocity.setText("当前车速：" + currentVelocityValue + "km/h");
                    }



                } else {
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

    private class forwardSpinnerOnItemSelectedListener implements OnItemSelectedListener{
        @Override
        public void onItemSelected(AdapterView<?> adapter,View view,int position,long id) {
            currentForwardGeSpinnerString = adapter.getItemAtPosition(position).toString();
            forwardGeValue.setText(forwardGeValueStored[Integer.parseInt(currentForwardGeSpinnerString.substring(2))-1].toString());
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {}
    }

    private class backSpinnerOnItemSelectedListener implements OnItemSelectedListener{
        @Override
        public void onItemSelected(AdapterView<?> adapter,View view,int position,long id) {
            currentBackGeSpinnerString = adapter.getItemAtPosition(position).toString();
            backGeValue.setText(backGeValueStored[Integer.parseInt(currentBackGeSpinnerString.substring(2))-1].toString());
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {}
    }

    public void btGeValueSend() {
        String s = "B0";
        for (int i: forwardGeValueStored) s += (String.format("%02d", i+50));
        s += ("b");
        for (int i: backGeValueStored) s += (String.format("%02d", i+50));
        s += ("E\n");
        try {
            bluetoothSocket.getOutputStream().write(s.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //重写button点击事件
    class MyClickListener implements View.OnClickListener {

        boolean isConnected = false;
        TextView textView = (TextView)findViewById(R.id.TextView0);

        @Override
        public void onClick(View v) {
            if (limitLineList.isEmpty()) {
                limitLineList.add(0f);
                limitLineList.add(0f);
            }

            String commandBack1 = "B1E\n";
            int currentForwardGeValue;
            int currentBackGeValue;

            switch (v.getId()) {
                case R.id.SearchBluetoothBtn:
                    arrayList.clear();
                    deviceName.clear();
                    blueToothController.findDevice();
                    MainActivity.fakeMode = (!MainActivity.fakeMode);
                    break;
                case R.id.forwardGeParams: case R.id.backGeParams:
                    try {
                        bluetoothSocket.getOutputStream().write(commandBack1.getBytes());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case R.id.forwardGeParamDec:
                    // 修改档位数值
                    currentForwardGeValue = Integer.parseInt(forwardGeValue.getText().toString());
                    currentForwardGeValue -= 1;
                    // 将结果存到数组
                    forwardGeValueStored[Integer.parseInt(currentForwardGeSpinnerString.substring(2))-1] = currentForwardGeValue;
                    // 显示结果
                    forwardGeValue.setText(String.valueOf(currentForwardGeValue));
                    btGeValueSend();
                    break;
                case R.id.forwardGeParamInc:
                    // 修改档位数值
                    currentForwardGeValue = Integer.parseInt(forwardGeValue.getText().toString());
                    currentForwardGeValue += 1;
                    // 将结果存到数组
                    forwardGeValueStored[Integer.parseInt(currentForwardGeSpinnerString.substring(2))-1] = currentForwardGeValue;
                    // 显示结果
                    forwardGeValue.setText(String.valueOf(currentForwardGeValue));
                    btGeValueSend();
                    break;
                case R.id.backGeParamDec:
                    // 修改档位数值
                    currentBackGeValue = Integer.parseInt(backGeValue.getText().toString());
                    currentBackGeValue -= 1;
                    // 将结果存到数组
                    backGeValueStored[Integer.parseInt(currentBackGeSpinnerString.substring(2))-1] = currentBackGeValue;
                    // 显示结果
                    backGeValue.setText(String.valueOf(currentBackGeValue));
                    btGeValueSend();
                    break;
                case R.id.backGeParamInc:
                    // 修改档位数值
                    currentBackGeValue = Integer.parseInt(backGeValue.getText().toString());
                    currentBackGeValue += 1;
                    // 将结果存到数组
                    backGeValueStored[Integer.parseInt(currentBackGeSpinnerString.substring(2))-1] = currentBackGeValue;
                    // 显示结果
                    backGeValue.setText(String.valueOf(currentBackGeValue));
                    btGeValueSend();
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
    public void GetPermission() {
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

    public boolean CheckPermision() {
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