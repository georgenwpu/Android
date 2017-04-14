package com.example.noise;

import com.example.noise.R;
import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class mainActivity extends Activity{

	TextView sound = (TextView)findViewById(R.id.sound);
	private static final String TAG = "AudioRecord";
    static final int SAMPLE_RATE_IN_HZ = 8000;
    static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,
            AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT);
    AudioRecord mAudioRecord;
    boolean isGetVoiceRun = false;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		sound.setText("hfklhdlhkgjfhlk");
		mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_DEFAULT,
                AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE);
        if (mAudioRecord == null) {
            Log.e("sound", "mAudioRecord��ʼ��ʧ��");
        }
		try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
		int n = 60;
		while(n>0)
		{
			getNoiseLevel();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            n -= 1;
		}
		mAudioRecord.release();
		mAudioRecord = null;
	}
	
	void getNoiseLevel() {
        if (isGetVoiceRun) {
            Log.e(TAG, "����¼����");
            return;
        }

        mAudioRecord.startRecording();
        short[] buffer = new short[BUFFER_SIZE];
        //r��ʵ�ʶ�ȡ�����ݳ��ȣ�һ�����r��С��buffersize
        int r = mAudioRecord.read(buffer, 0, BUFFER_SIZE);
        long v = 0;
        // �� buffer ����ȡ��������ƽ��������
        for (int i = 0; i < buffer.length; i++) {
            v += buffer[i] * buffer[i];
        }
        // ƽ���ͳ��������ܳ��ȣ��õ�������С��
        double mean = v / (double) r;
        double volume = 10 * Math.log10(mean);

        Log.d(TAG, "decibels value:" + volume);
        sound.setText("decibels value:" + volume);
        // ���һ��ʮ��
        mAudioRecord.stop();
    }
}
