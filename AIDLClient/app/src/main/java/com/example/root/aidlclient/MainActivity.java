package com.example.root.aidlclient;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import aidl.IPostMsg;
import aidl.MsgBody;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private IPostMsg postMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.seed).setOnClickListener(this);
        initServer();
    }

    private void initServer() {
        log("initServer");
        Intent intent = new Intent();
        intent.setAction("com.example.root.aidlservice.remote");
        intent.setPackage("com.example.root.aidlservice");
        bindService(intent, connection, BIND_AUTO_CREATE);
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            log("onServiceConnected");
            postMsg = IPostMsg.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            log("onServiceDisconnected");
            postMsg = null;
        }
    };

    @Override
    protected void onDestroy() {
        unbindService(connection);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        EditText editText = findViewById(R.id.input);
        TextView textView = findViewById(R.id.output);
        String outStr = editText.getText().toString();
        String inStr;
        if (postMsg != null) {
            try {
                log("setMsg = " + outStr);
                postMsg.setMsg(outStr);
                MsgBody b = postMsg.getMsg1();
                log("getMsg1 " + b.toString());

                MsgBody test = null;

                //测试getMsg2
                log(3);
                log("测试getMsg2");
                test = getTest();
                log("传入" + test.toString());
                MsgBody r1 = postMsg.getMsg2(test);
                log("getMsg2 比较：");
                log(r1.toString());
                log(test.toString());

                //测试getMsg3
                log(3);
                log("测试getMsg3");
                test = getTest();
                log("传入" + test.toString());
                MsgBody r2 = postMsg.getMsg3(test);
                log("getMsg3 比较：");
                log(r2.toString());
                log(test.toString());

                //测试getMsg4
                log(3);
                log("测试getMsg4");
                test = getTest();
                log("传入" + test.toString());
                MsgBody r3 = postMsg.getMsg4(test);
                log("getMsg3 比较：");
                log(r3.toString());
                log(test.toString());

            } catch (RemoteException e) {
                log("RemoteException");
                e.printStackTrace();
            }
        } else {
            log("postMsg == null");
        }
    }

    private MsgBody getTest() {
        MsgBody test = new MsgBody();
        test.msgId = 999;
        test.msgContext = "test";
        return test;
    }

    private void log(String msg) {
        Log.i("wangcan", "client " + msg);
    }

    private void log(int line) {
        for (int i = 0; i < Math.max(line, 1); i++){
            Log.i("wangcan", "            ");
        }
    }
}
