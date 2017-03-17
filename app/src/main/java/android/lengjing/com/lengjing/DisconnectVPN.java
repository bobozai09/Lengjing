package android.lengjing.com.lengjing;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.lengjing.com.lengjing.core.OpenVPNService;
import android.lengjing.com.lengjing.core.ProfileManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

/**
 * Created by Administrator on 2017/3/17 0017.
 */

public class DisconnectVPN extends Activity implements DialogInterface.OnClickListener, DialogInterface.OnCancelListener {
    public static final String ISSHOWDIALOG ="isShowDilog" ;
    protected OpenVPNService mService;
    private  boolean   isShowDialog=true;
    private ServiceConnection mConnection = new ServiceConnection() {


        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            OpenVPNService.LocalBinder binder = (OpenVPNService.LocalBinder) service;
            mService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService =null;
        }

    };
    private AlertDialog mDilog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent=getIntent();
        if(intent!=null){
            isShowDialog=intent.getBooleanExtra(ISSHOWDIALOG,true);
        }
        Intent intent0 = new Intent(this, OpenVPNService.class);
        intent0.setAction(OpenVPNService.START_SERVICE);
        bindService(intent0, mConnection, Context.BIND_AUTO_CREATE);
        if(isShowDialog)
            showDisconnectDialog();
        else {
            ProfileManager.setConntectedVpnProfileDisconnected(this);
            if (mService != null && mService.getManagement() != null)
                mService.getManagement().stopVPN();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mDilog!=null)
                        mDilog.dismiss();
                    finish();

                }
            },500);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(mConnection);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        if(mDilog!=null)
            mDilog.dismiss();
        finish();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            if(mDilog!=null)
                mDilog.dismiss();
            if (mService != null && mService.getManagement() != null) {
                boolean isClose = mService.getManagement().stopVPN();
                ProfileManager.setConntectedVpnProfileDisconnected(this);

            }
        }
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (getIntent() !=null && OpenVPNService.DISCONNECT_VPN.equals(getIntent().getAction()))
            setIntent(null);
    }
    private void showDisconnectDialog() {
        if(mDilog==null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.title_cancel);
            builder.setMessage(R.string.cancel_connection_query);
            builder.setNegativeButton(android.R.string.no, this);
            builder.setPositiveButton(android.R.string.yes, this);
            builder.setOnCancelListener(this);
            mDilog = builder.show();
        }
        mDilog.show();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(mDilog!=null)
            mDilog.dismiss();
    }
}
