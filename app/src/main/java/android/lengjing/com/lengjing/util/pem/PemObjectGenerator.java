package android.lengjing.com.lengjing.util.pem;

/**
 * Created by Administrator on 2017/3/16 0016.
 */

public interface PemObjectGenerator {
    PemObject generate()
            throws PemGenerationException;
}
