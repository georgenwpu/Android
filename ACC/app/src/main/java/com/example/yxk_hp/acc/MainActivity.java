package com.example.yxk_hp.acc;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;
import java.io.FileOutputStream;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements SensorEventListener{

    private SensorManager sensorManager;
    TextView xCoor; // declare X axis object
    TextView yCoor; // declare Y axis object
    TextView zCoor; // declare Z axis object
    TextView State;
    private Timer timer = new Timer();
    private TimerTask task;
    public int switchOn = 0;//初始时刻采样开关关闭
    private FileOutputStream file_res;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        xCoor=(TextView)findViewById(R.id.xcoor); // create X axis object
        yCoor=(TextView)findViewById(R.id.ycoor); // create Y axis object
        zCoor=(TextView)findViewById(R.id.zcoor); // create Z axis object
        State = (TextView)findViewById(R.id.state);

        sensorManager=(SensorManager)getSystemService(SENSOR_SERVICE);
        // add listener. The listener will be MyActivity (this) class
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);

        //文件读写
        try{
            file_res = openFileOutput("res.txt", Context.MODE_PRIVATE);
        }
        catch (Exception e){
            e.printStackTrace();
            State.setText("文件打开异常");
        }

        task = new TimerTask() {
            @Override
            public void run() {
                switchOn = 1;
            }
        };
        timer.schedule(task, 0, 10); //设置采样时间10ms

    }

    // 当精度发生变化时调用
    public void onAccuracyChanged(Sensor sensor,int accuracy){
		
    }

    // 当sensor事件发生时候调用
    public void onSensorChanged(SensorEvent event){

        String content = " ";
        // check sensor type
        if(switchOn == 1 && event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){

            // assign directions
            float x=event.values[0];
            float y=event.values[1];
            float z=event.values[2];

            xCoor.setText("X: "+x);
            yCoor.setText("Y: "+y);
            zCoor.setText("Z: "+z);

            content = x+" "+y+""+z+"\n";
            try{
                file_res.write(content.getBytes());
                file_res.flush();
            }
            catch(Exception e) {
                e.printStackTrace();
                State.setText("文件写入异常");
            }

            //Turn off the Switch utill the TimerTask turn it on
            switchOn = 0;
        }
    }

    @Override
    protected void onDestroy() {
        sensorManager.unregisterListener(this);
        super.onDestroy();
        timer.cancel();
        try {
            file_res.close();
        }
        catch(Exception e){
            e.printStackTrace();
            State.setText("文件关闭异常");
        }
    }
}