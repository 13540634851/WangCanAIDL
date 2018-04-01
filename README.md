
最后一次更新：2018-3-25

- android测试版本：Android O
- 内容：验证Android AIDL中的tag（in out inout）


## 前言
android AIDL有3种不同的tag，在总结之前先参考了这篇博客 https://blog.csdn.net/luoyanglizi/article/details/51958091
但是发现还是不够，于是又自己研究了一下，总结出这篇文档。如果有错误的话希望指正。


## 验证

IPostMsg.aidl
```
package aidl;
import aidl.MsgBody;
interface IPostMsg {
    void setMsg(String msg);
    //(1)没有声明out能返回吗？ == 能
    MsgBody getMsg1();

    //(2)out 不能传入，服务端能修改客户端的b参数
    MsgBody getMsg2(out MsgBody b);

    //(3)in 能传入，服务端不能修改客户端的b参数
    MsgBody getMsg3(in MsgBody b);

    //(4)in 能传入，服务端能修改客户端的b参数
    MsgBody getMsg4(inout MsgBody b);
}
```

MsgBody.aidl
```
package aidl;
parcelable MsgBody;
```

```
package aidl;
import android.os.Parcel;
import android.os.Parcelable;
public class MsgBody implements Parcelable {
    public String msgContext;
    public int msgId;

    public MsgBody() {}

    protected MsgBody(Parcel in) {
        readFromParcel(in);
    }

    public static final Creator<MsgBody> CREATOR = new Creator<MsgBody>() {
        @Override
        public MsgBody createFromParcel(Parcel in) {
            return new MsgBody(in);
        }

        @Override
        public MsgBody[] newArray(int size) {
            return new MsgBody[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * @param dest
     * @param flags flags=PARCELABLE_WRITE_RETURN_VALUE:表示该对象从服务端返回客户端
     *              flags=PARCELABLE_ELIDE_DUPLICATES 程序内部复制变量的时候
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(msgContext);
        dest.writeInt(msgId);
    }

    public void readFromParcel(Parcel in) {
        msgContext = in.readString();
        msgId = in.readInt();
    }
    @Override
    public String toString() {
        return "id = " + msgId + " msgContext = " + msgContext;
    }
}

```

上面定义了MsgBody.aidl，IPostMsg.aidl，

值得注意的是：
1. MsgBody.aidl，IPostMsg.aidl即使在一个包下，IPostMsg.aidl中引用MsgBody时，也要写import aidl.MsgBody;
2. public MsgBody() {}必须有一个空的构造方法，否则使用out tag的时候会报错。

```
Error:(72, 9) 错误: 无法将类 MsgBody中的构造器 MsgBody应用到给定类型;
需要: Parcel
找到: 没有参数
原因: 实际参数列表和形式参数列表长度不同
```
3. ，使用out tag序列化的时候一定要有下面的函数

```
   public void readFromParcel(Parcel in) {
        msgContext = in.readString();
        msgId = in.readInt();
    }
```
否则会报

```
Error:(215, 2) 错误: 找不到符号
符号:   方法 readFromParcel(Parcel)
位置: 类型为MsgBody的变量 b
```

现在客服务都开启服务，客户端获取

##### 测试MsgBody getMsg1();
客户端先setMsg(String msg)
服务端用getMsg1()返回MsgBody

客户端

```
    log("setMsg = " + outStr);
    postMsg.setMsg(outStr);
    MsgBody b = postMsg.getMsg1();
    log("getMsg1 " + b.toString());
```
服务端

```
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
```

输出log

```
01-01 19:16:58.602 30532-30532/com.example.root.aidlclient I/wangcan: client setMsg = dffff
01-01 19:16:58.604 30263-30281/com.example.root.aidlservice:remote I/wangcan: service server get dffff
01-01 19:16:58.612 30263-30281/com.example.root.aidlservice:remote I/wangcan: service out getMsg id = 10 msgContext = dffff
01-01 19:16:58.616 30532-30532/com.example.root.aidlclient I/wangcan: client getMsg1 id = 10 msgContext = dffff
```
结论：直接retrun 返回不需要什么tag，跟一般java函数调用没有区别


##### MsgBody getMsg2(out MsgBody b);

客户端
```
//测试getMsg2
log("测试getMsg2");
 MsgBody test = new MsgBody();
        test.msgId = 999;
        test.msgContext = "test";
        
log("传入" + test.toString());
MsgBody r1 = postMsg.getMsg2(test);
log(test.toString());
```
服务端

```
       public MsgBody getMsg2(MsgBody b) throws RemoteException {
            log("getMsg2 接收" + b.toString());
            b.msgId=222;
            b.msgContext="change";
            log("getMsg2 修改"+ b.toString());
            return null;
        }
```

输出log

```
01-01 19:16:58.617 30532-30532/com.example.root.aidlclient I/wangcan: client 传入id = 999 msgContext = test
01-01 19:16:58.618 30263-30281/com.example.root.aidlservice:remote I/wangcan: service getMsg2 接收id = 0 msgContext = null
01-01 19:16:58.619 30263-30281/com.example.root.aidlservice:remote I/wangcan: service getMsg2 修改id = 222 msgContext = change
01-01 19:16:58.622 30532-30532/com.example.root.aidlclient I/wangcan: client id = 222 msgContext = change

```
结论：
如果使用out tag用于入参
1. service getMsg2 接收id = 0 msgContext = null 说明，服务端不能得到传入的数据
2. 在服务端修改值（service getMsg2 修改id = 222 msgContext = change），这个值被修改了


**服务端能修改客户端的入参数据，但不能获取这个参数数据**
  
##### MsgBody getMsg3(in MsgBody b);
客户端与服务端代码与getMsg2一样，直接看log

```
01-01 19:16:58.623 30532-30532/com.example.root.aidlclient I/wangcan: client 传入id = 999 msgContext = test
01-01 19:16:58.624 30263-30281/com.example.root.aidlservice:remote I/wangcan: service getMsg3 接收id = 999 msgContext = test
01-01 19:16:58.625 30263-30281/com.example.root.aidlservice:remote I/wangcan: service getMsg3 修改id = 333 msgContext = change
01-01 19:16:58.627 30532-30532/com.example.root.aidlclient I/wangcan: client id = 999 msgContext = test
```

结论：
如果使用in tag用于入参
1. service getMsg3 接收service getMsg3 接收id = 999 msgContext = test 说明，服务端能得到传入的数据
2. 在服务端修改值（service getMsg2 修改id = 333 msgContext = change），但是服务端的值没有改变，仍然是（client id = 999 msgContext = test）



**服务端不能修改客户端的入参数据，但能获取这个参数数据**
   
##### MsgBody getMsg4(inout MsgBody b);
客户端与服务端代码与上面两个一样，直接看log

```
01-01 19:16:58.628 30532-30532/com.example.root.aidlclient I/wangcan: client 传入id = 999 msgContext = test
01-01 19:16:58.629 30263-30281/com.example.root.aidlservice:remote I/wangcan: service getMsg4 接收id = 999 msgContext = test
01-01 19:16:58.630 30263-30281/com.example.root.aidlservice:remote I/wangcan: service getMsg4 修改id = 444 msgContext = change
01-01 19:16:58.632 30532-30532/com.example.root.aidlclient I/wangcan: client id = 444 msgContext = change
```
如果使用inout tag用于入参
1. “接收id = 999 msgContext = test” 说明，服务端能得到传入的数据
2. 在服务端修改值（service getMsg2 修改id = 333 msgContext = change），与后面客户端对应的参数的值（client id = 444 msgContext = change）
3. 最后retrun 返回跟getMsg1一样

**服务端能修改客户端的入参数据，也能获取这个参数数据**

## 最后的结论


1. 即使在一个包里面也要导入包名
2. MsgBody getMsg();可以直接返回，不需要什么tag
3. MsgBody getMsg2(out MsgBody b);  服务端能修改客户端的入参数据，但不能获取这个参数数据
4. MsgBody getMsg3(in MsgBody b);   服务端不能修改客户端的入参数据，但能获取这个参数数据
5. MsgBody getMsg4(inout MsgBody b);服务端能修改客户端的入参数据，也能获取这个参数数据
6. tag （in out inout）是相对与服务端说的，in就是数据能进入客户端，out是数据能出服务端，inout是能进能出
7. 使用out tag序列化的时候一定要有下面的函数和无参构造函数。aidl最后会编译出对应的java文件。如果使用的out tag，源码中使用这两个方法。

```
   public void readFromParcel(Parcel in) {
        msgContext = in.readString();
        msgId = in.readInt();
    }
    
    
```

我的测试代码：
https://gitee.com/namedcan/WangCanAIDL或直接git clone git@gitee.com:namedcan/WangCanAIDL.git











