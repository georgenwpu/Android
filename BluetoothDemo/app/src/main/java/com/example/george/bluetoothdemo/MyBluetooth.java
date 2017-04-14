package com.example.george.bluetoothdemo;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Handler;


/**
 * 蓝牙设备控制类，实现MainActivity中的5个功能
 * Created by George on 2017/4/8.
 */

public class MyBluetooth {

    public  BluetoothAdapter myBthA = BluetoothAdapter.getDefaultAdapter();
    private Context btContext;
    private MainActivity btActivity;
    public  BluetoothDevice[] ConnDev = new BluetoothDevice[20];
    public  int num_dev = 0;
    public  boolean DevCon_tag = false;
    public  UUID MYUUID = UUID.randomUUID();
    private String  NAME = "myBT";
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;


    public MyBluetooth(Context context, MainActivity activity){
        btContext = context;
        btActivity = activity;
    }

    //打开蓝牙
    public Boolean TurnOnBluetooth() {
        if (!myBthA.isEnabled()) {
            int REQUEST_ENABLE_BT = 1;
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            btActivity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

            return true;
        }
        return false;
    }

    //显示可连接的设备
    public void ShowBluetoothList(ArrayAdapter mAAd) {
        Set<BluetoothDevice> pairedDevice = myBthA.getBondedDevices();
        //ConnDev = pairedDevice.toArray(new BluetoothDevice[5]);
        int DevNum = pairedDevice.size();
        Toast.makeText(btContext, Integer.toString(DevNum), Toast.LENGTH_LONG).show();
        if (DevNum > 0) {
            for (BluetoothDevice device : pairedDevice) {
                // Add the name and address to an array adapter to show in a ListView
                mAAd.add(device.getName() + "\n" + device.getAddress());
                ConnDev[num_dev++] = device;
            }
        }
    }

    // 蓝牙设备搜索
    public Boolean DiscoveryDevive(){
        Boolean rst = myBthA.startDiscovery();
        return rst;
    }

    public  void ConnectDevice(BluetoothDevice device){
        connectThread = new ConnectThread(device);
        connectThread.run();
    }

    public  void transport(BluetoothDevice device, byte[] bytes){
        if (DevCon_tag == true){
            ;
        }else {
            Toast.makeText(btContext, "设备未连接无法发送！", Toast.LENGTH_LONG);
        }
        connectedThread = new ConnectedThread(connectThread.mmSocket);
        connectedThread.write(bytes);
    }



    /**
     * 内部类：连接线程
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;
            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
            // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MYUUID);
                } catch (IOException e) { }
            mmSocket = tmp;
        }
        public void run() {
        // Cancel discovery because it will slow down the connection
            myBthA.cancelDiscovery();
            try {
                // Connect the device through the socket. This will block
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                return;
            }
            // Do work to manage the connection (in a separate thread)
            //manageConnectedSocket(mmSocket);
        }
        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }


    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = myBthA.listenUsingRfcommWithServiceRecord(NAME, MYUUID);
            } catch (IOException e) { }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    //manageConnectedSocket(socket);
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) { }
        }
    }


    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    int MESSAGE_READ = 101;
                   btActivity.mhandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                ;
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

}
