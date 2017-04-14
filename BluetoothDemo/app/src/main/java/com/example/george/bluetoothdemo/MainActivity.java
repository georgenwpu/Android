package com.example.george.bluetoothdemo;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

/**
 * 蓝牙Demo
 * 功能：1、检测蓝牙设备打开状态，并打开蓝牙   √
 *      2、显示可以连接的蓝牙设备            √
 *      3、搜索设备并显示搜索到的设备         √
 *      4、和新的设备进行连接                √
 *      5、蓝牙发送数据
 *  Tips：1、应用不能在虚拟机上运行，可能是虚拟机的系统版本太高，在6.0.1以下均正常
 *
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    public  MyBluetooth mbt;
    public  ArrayAdapter AA_DevList;
    public  ListView LV_showDev;
    public  BroadcastReceiver mReceiver;
    public  Handler mhandler = new Handler();
    private Message msg = new Message();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn = (Button)findViewById(R.id.button);
        Button btn_show = (Button)findViewById(R.id.btn_show);
        Button btn_connect = (Button)findViewById(R.id.btn_connectDev);
        Button btn_send = (Button)findViewById(R.id.btn_send);
        EditText edit_content = (EditText)findViewById(R.id.edit_content);
        LV_showDev = (ListView) findViewById(R.id.ListView_ShowDev);

        mbt = new MyBluetooth(getBaseContext(), MainActivity.this);
        AA_DevList = new ArrayAdapter(getBaseContext(), android.R.layout.simple_expandable_list_item_1);
        LV_showDev.setAdapter(AA_DevList);

        // Create a BroadcastReceiver for ACTION_FOUND
        BroadcastReceiver mReceiver = new MyReceiver();
        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy

        btn.setOnClickListener(this);
        btn_show.setOnClickListener(this);
        btn_connect.setOnClickListener(this);
        btn_send.setOnClickListener(this);
        LV_showDev.setOnItemClickListener(this);

        mhandler.handleMessage(msg);

        Thread thread101 = new Thread(){
            @Override
            public void run() {
                if (msg.what == 101){
                    Toast.makeText(getBaseContext(), msg.getData().toString(), Toast.LENGTH_LONG);
                }
            }
        };

    }

    /**
     * 按键按下监听响应函数
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.button:
                Boolean aBoolean = mbt.TurnOnBluetooth();;
                break;
            case R.id.btn_show:                 //显示可以连接的蓝牙设备\
                Boolean DiscoveryState;
                mbt.ShowBluetoothList(AA_DevList);
                DiscoveryState = mbt.DiscoveryDevive();
                Log.d(Boolean.toString(DiscoveryState), "DisSta");
                break;
            case R.id.btn_connectDev:
                if (mbt.ConnDev[0] != null)
                    mbt.ConnectDevice(mbt.ConnDev[0]);
                else
                    Toast.makeText(this, "没有可连接设备！\n请等待设备扫描结束……", Toast.LENGTH_LONG);
                break;
            case R.id.btn_send:
                if (mbt.DevCon_tag == true){
                    ;
                }else {
                    Toast.makeText(getBaseContext(), "未连接设备，请先连接设备", Toast.LENGTH_LONG);
                }
                byte[] bytes = {1,0,1,0};
                mbt.transport(mbt.ConnDev[1], bytes);
                break;
            default:
                ;
        }
    }

    /**
     * 设备列表按下监听函数
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //BluetoothDevice device = mbt.ConnDev[0];
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mbt.ConnectDevice(mbt.ConnDev[position]);
        }
    }

    /**
     * 内部广播接受类，接受蓝牙设备扫描广播
     */
    public class MyReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device;
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                AA_DevList.add(device.getName() + "\n" + device.getAddress());
                mbt.ConnDev[mbt.num_dev++] = device;
                Log.d(device.getName(), "DevSca");
                mbt.myBthA.cancelDiscovery();
            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiver != null)
            unregisterReceiver(mReceiver);
    }
}




