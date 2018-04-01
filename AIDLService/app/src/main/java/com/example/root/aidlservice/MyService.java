package com.example.root.aidlservice;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import aidl.IPostMsg;
import aidl.MsgBody;


/**
 * Created by root on 18-3-20.
 */

public class MyService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        log("onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        log("onCreate");
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        log("onDestroy");
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private IPostMsg.Stub binder = new IPostMsg.Stub() {
        private String msg;

        @Override
        public void setMsg(String msg) throws RemoteException {
            log("server get " + msg);
            this.msg = msg;
        }

        @Override
        public MsgBody getMsg1() throws RemoteException {
            MsgBody msgBody = new MsgBody();
            int id = 10;
            msgBody.msgId = id;
            msgBody.msgContext = msg;
            log("out getMsg " + msgBody.toString());
            return msgBody;
        }

        @Override
        public MsgBody getMsg2(MsgBody b) throws RemoteException {
            log("getMsg2 接收" + b.toString());
            b.msgId=222;
            b.msgContext="change";
            log("getMsg2 修改"+ b.toString());
            MsgBody msgBody = new MsgBody();
            int id = 12;
            msgBody.msgId = id;
            msgBody.msgContext = "out getMsg2";
            log("getMsg2 传出" + msgBody.toString());
            return msgBody;
        }

        @Override
        public MsgBody getMsg3(MsgBody b) throws RemoteException {
            log("getMsg3 接收" + b.toString());
            b.msgId=333;
            b.msgContext="change";
            log("getMsg3 修改"+ b.toString());
            MsgBody msgBody = new MsgBody();
            int id = 13;
            msgBody.msgId = id;
            msgBody.msgContext = "out getMsg3";
            log("getMsg3 传出" + msgBody.toString());
            return msgBody;
        }
        @Override
        public MsgBody getMsg4(MsgBody b) throws RemoteException {
            log("getMsg4 接收" + b.toString());
            b.msgId=444;
            b.msgContext="change";
            log("getMsg4 修改"+ b.toString());
            MsgBody msgBody = new MsgBody();
            int id = 14;
            msgBody.msgId = id;
            msgBody.msgContext = "out getMsg4";
            log("getMsg4 传出" + msgBody.toString());
            return msgBody;
        }
    };

    private void log(String msg) {
        Log.i("wangcan", "service " + msg);
    }

}
