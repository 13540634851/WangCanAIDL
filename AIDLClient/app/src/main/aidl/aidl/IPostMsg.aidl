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
