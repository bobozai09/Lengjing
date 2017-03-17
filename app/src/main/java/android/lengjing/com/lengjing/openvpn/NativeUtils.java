package android.lengjing.com.lengjing.openvpn;

import android.os.Build;

import java.security.InvalidKeyException;

/**
 * Created by Administrator on 2017/3/16 0016.
 */

public class NativeUtils {
    public static native byte[] rsasign(byte[] input, int pkey) throws InvalidKeyException;

    public static native String[] getIfconfig() throws IllegalArgumentException;

 public    static native void jniclose(int fdint);

    public static native String getNativeAPI();

    static {
        System.loadLibrary("opvpnutil");
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN)
            System.loadLibrary("jbcrypto");
    }
}
