package android.lengjing.com.lengjing.util.pem.encoders;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Administrator on 2017/3/16 0016.
 */

public interface Encoder {
    int encode(byte[] data, int off, int length, OutputStream out) throws IOException;

    int decode(byte[] data, int off, int length, OutputStream out) throws IOException;

    int decode(String data, OutputStream out) throws IOException;
}
