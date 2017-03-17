package android.lengjing.com.lengjing.util.pem;

import java.io.IOException;

/**
 * Created by Administrator on 2017/3/16 0016.
 */

public class PemGenerationException extends IOException {
    private Throwable cause;

    public PemGenerationException(String message, Throwable cause)
    {
        super(message);
        this.cause = cause;
    }

    public PemGenerationException(String message)
    {
        super(message);
    }

    public Throwable getCause()
    {
        return cause;
    }
}
