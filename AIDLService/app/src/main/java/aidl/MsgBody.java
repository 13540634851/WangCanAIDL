package aidl;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by can on 2018/3/22.
 */

public class MsgBody implements Parcelable {
    public String msgContext;
    public int msgId;

    public MsgBody() {
    }

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
