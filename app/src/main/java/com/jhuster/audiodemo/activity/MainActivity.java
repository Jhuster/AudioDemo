package com.jhuster.audiodemo.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.jhuster.audiodemo.R;
import com.jhuster.audiodemo.tester.CaptureTester;
import com.jhuster.audiodemo.tester.NativeAudioTester;
import com.jhuster.audiodemo.tester.PlayerTester;
import com.jhuster.audiodemo.tester.Tester;

public class MainActivity extends AppCompatActivity {

    private Spinner mTestSpinner;
    private Tester mTester;

    public static final String[] TEST_PROGRAM_ARRAY = {
            "录制wav文件", "播放wav文件", "Native录制pcm", "Native播放pcm"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTestSpinner = (Spinner) findViewById(R.id.TestSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, TEST_PROGRAM_ARRAY);
        mTestSpinner.setAdapter(adapter);
    }

    public void onClickStartTest(View v) {
        switch (mTestSpinner.getSelectedItemPosition()) {
            case 0:
                mTester = new CaptureTester();
                break;
            case 1:
                mTester = new PlayerTester();
                break;
            case 2:
                mTester = new NativeAudioTester(true);
                break;
            case 3:
                mTester = new NativeAudioTester(false);
                break;
            default:
                break;
        }
        if (mTester != null) {
            mTester.startTesting();
            Toast.makeText(this, "Start Testing !", Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickStopTest(View v) {
        if (mTester != null) {
            mTester.stopTesting();
            Toast.makeText(this, "Stop Testing !", Toast.LENGTH_SHORT).show();
        }
    }
}
