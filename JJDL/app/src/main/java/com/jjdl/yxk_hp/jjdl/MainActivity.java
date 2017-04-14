package com.jjdl.yxk_hp.jjdl;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private TextView num_text;
    private Button btn_start, btn_stop;
    public int run_tag = 0;
    public int num = 0;
    private Thread mythread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        num_text = (TextView) findViewById(R.id.textView);
        btn_start = (Button) findViewById(R.id.btn_start);
        btn_stop = (Button) findViewById(R.id.btn_stop);

        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                run_tag = 1;
                btn_stop.setClickable(true);
                btn_start.setClickable(false);
                Log.d("TAG","1");
            }
        });

        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                run_tag = 0;
                btn_start.setClickable(true);
                btn_stop.setClickable(false);
                Log.d("TAG","0");
            }
        });

        mythread = new Thread(new Runnable() {
            @Override
            public void run() {
                num = 0;
                Runnable uirunnable;
                Log.d("TAG","Thread is established!!");
                Random rnd = new Random();
                while(true)
                {
                    if(run_tag == 1) {
                        num = rnd.nextInt(8) + 1;
                    }
                    uirunnable = new Runnable() {
                        @Override
                        public void run() {
                            num_text.setText(Integer.toString(num));
                        }
                    };
                    runOnUiThread(uirunnable);
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        mythread.start();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mythread.interrupt();
    }
}
