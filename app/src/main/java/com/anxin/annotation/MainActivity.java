package com.anxin.annotation;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.anxin.inject.Inject;
import com.anxin.inject.Unbinder;
import com.anxin.lib_annotation.BindView;
import com.anxin.lib_annotation.test.Test;

@Test
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    protected int count;
    @BindView(R.id.tv_show)
    protected TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new Test_MainActivity(this,"anxin");
        Log.d(TAG,"count = " + count);
        Unbinder tBind = Inject.bind(this);
        mTextView.setOnClickListener((v)->
                mTextView.setText("click hello world")
        );
    }


}
