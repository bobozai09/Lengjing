package android.lengjing.com.lengjing.core;

/**
 * Created by Administrator on 2017/3/16 0016.
 */

public interface OpenVPNManagement {
    interface PausedStateCallback {
        boolean shouldBeRunning();
    }

    enum pauseReason {
        noNetwork,
        userPause,
        screenOff
    }

    int mBytecountInterval = 2;

    void reconnect();

    void pause(pauseReason reason);

    void resume();

    boolean stopVPN();

    /*
     * Rebind the interface
     */
    void networkChange(boolean sameNetwork);

    void setPauseCallback(PausedStateCallback callback);
}
