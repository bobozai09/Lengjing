package android.lengjing.com.lengjing;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.lengjing.com.lengjing.core.ProfileManager;
import android.lengjing.com.lengjing.core.VPNLaunchHelper;
import android.lengjing.com.lengjing.core.VpnStatus;
import android.lengjing.com.lengjing.openvpn.VpnProfile;
import android.net.VpnService;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import java.io.IOException;

/**
 * Created by Administrator on 2017/3/17 0017.
 */

public class LaunchVPN extends Activity {
    public static final String EXTRA_KEY = "de.blinkt.openvpn.shortcutProfileUUID";
    public static final String EXTRA_NAME = "de.blinkt.openvpn.shortcutProfileName";
    public static final String EXTRA_HIDELOG =  "de.blinkt.openvpn.showNoLogWindow";
    public static final String CLEARLOG = "clearlogconnect";


    private static final int START_VPN_PROFILE= 70;


    private ProfileManager mPM;
    private VpnProfile mSelectedProfile;
    private boolean mhideLog=false;

    private boolean mCmfixed=false;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        mPM =ProfileManager.getInstance(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Resolve the intent

        final Intent intent = getIntent();
        final String action = intent.getAction();

        // If the intent is a request to create a shortcut, we'll do that and exit


        if(Intent.ACTION_MAIN.equals(action)) {
            // Check if we need to clear the log
            if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(CLEARLOG, true))
                VpnStatus.clearLog();

            // we got called to be the starting point, most likely a shortcut
            String shortcutUUID = intent.getStringExtra( EXTRA_KEY);
            String shortcutName = intent.getStringExtra( EXTRA_NAME);
            mhideLog = intent.getBooleanExtra(EXTRA_HIDELOG, true);

            VpnProfile profileToConnect = ProfileManager.get(this,shortcutUUID);
            if(shortcutName != null && profileToConnect ==null)
                profileToConnect = ProfileManager.getInstance(this).getProfileByName(shortcutName);
            if(profileToConnect ==null) {
                VpnStatus.logError(R.string.shortcut_profile_notfound);
                // show Log window to display error
                showLogWindow();
                finish();
                return;
            }

            mSelectedProfile = profileToConnect;
            launchVPN();

        }
    }

    private void askForPW(final int type) {

        final EditText entry = new EditText(this);
        final View userpwlayout = getLayoutInflater().inflate(R.layout.userpass, null, false);

        entry.setSingleLine();
        entry.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        entry.setTransformationMethod(new PasswordTransformationMethod());

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Need " + getString(type));
        dialog.setMessage("Enter the password for profile " + mSelectedProfile.mName);

        if (type == R.string.password) {
            ((EditText)userpwlayout.findViewById(R.id.username)).setText(mSelectedProfile.mUsername);
            ((EditText)userpwlayout.findViewById(R.id.password)).setText(mSelectedProfile.mPassword);
            ((CheckBox)userpwlayout.findViewById(R.id.save_password)).setChecked(!TextUtils.isEmpty(mSelectedProfile.mPassword));
            ((CheckBox)userpwlayout.findViewById(R.id.show_password)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked)
                        ((EditText)userpwlayout.findViewById(R.id.password)).setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    else
                        ((EditText)userpwlayout.findViewById(R.id.password)).setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
            });

            dialog.setView(userpwlayout);
        } else {
            dialog.setView(entry);
        }

        AlertDialog.Builder builder = dialog.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (type == R.string.password) {
                            mSelectedProfile.mUsername = ((EditText) userpwlayout.findViewById(R.id.username)).getText().toString();

                            String pw = ((EditText) userpwlayout.findViewById(R.id.password)).getText().toString();
                            if (((CheckBox) userpwlayout.findViewById(R.id.save_password)).isChecked()) {
                                mSelectedProfile.mPassword=pw;
                            } else {
                                mSelectedProfile.mPassword=null;
                                mSelectedProfile.mTransientPW = pw;
                            }
                        } else {
                            mSelectedProfile.mTransientPCKS12PW = entry.getText().toString();
                        }
                        onActivityResult(START_VPN_PROFILE, Activity.RESULT_OK, null);

                    }

                });
        dialog.setNegativeButton(android.R.string.cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        VpnStatus.updateStateString("USER_VPN_PASSWORD_CANCELLED", "", R.string.state_user_vpn_password_cancelled,
                                VpnStatus.ConnectionStatus.LEVEL_NOTCONNECTED);
                        finish();
                    }
                });

        dialog.create().show();

    }
    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==START_VPN_PROFILE) {
            if(resultCode == Activity.RESULT_OK) {
                int needpw = mSelectedProfile.needUserPWInput(false);
                if(needpw !=0) {
                    VpnStatus.updateStateString("USER_VPN_PASSWORD", "", R.string.state_user_vpn_password,
                            VpnStatus.ConnectionStatus.LEVEL_WAITING_FOR_USER_INPUT);
                    askForPW(needpw);
                } else {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                    boolean showLogWindow = prefs.getBoolean("showlogwindow", true);

                    if(!mhideLog && showLogWindow)
                        showLogWindow();
                    new startOpenVpnThread().start();
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // User does not want us to start, so we just vanish
                VpnStatus.updateStateString("USER_VPN_PERMISSION_CANCELLED", "", R.string.state_user_vpn_permission_cancelled,
                        VpnStatus.ConnectionStatus.LEVEL_NOTCONNECTED);

                finish();
            }
        }
    }
    void showLogWindow() {

//		Intent startLW = new Intent(getBaseContext(),LogWindow.class);
//		startLW.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//		startActivity(startLW);

    }

    void showConfigErrorDialog(int vpnok) {
        AlertDialog.Builder d = new AlertDialog.Builder(this);
        d.setTitle(R.string.config_error_found);
        d.setMessage(vpnok);
        d.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();

            }
        });
        d.show();
    }

    void launchVPN () {
        int vpnok = mSelectedProfile.checkProfile(this);
        if(vpnok!= R.string.no_error_found) {
            showConfigErrorDialog(vpnok);
            return;
        }

        Intent intent = VpnService.prepare(this);
        // Check if we want to fix /dev/tun
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean usecm9fix = prefs.getBoolean("useCM9Fix", false);
        boolean loadTunModule = prefs.getBoolean("loadTunModule", false);

        if(loadTunModule)
            execeuteSUcmd("insmod /system/lib/modules/tun.ko");

        if(usecm9fix && !mCmfixed ) {
            execeuteSUcmd("chown system /dev/tun");
        }


        if (intent != null) {
            VpnStatus.updateStateString("USER_VPN_PERMISSION", "", R.string.state_user_vpn_permission,
                    VpnStatus.ConnectionStatus.LEVEL_WAITING_FOR_USER_INPUT);
            // Start the query
            try {
                startActivityForResult(intent, START_VPN_PROFILE);
            } catch (ActivityNotFoundException ane) {
                // Shame on you Sony! At least one user reported that
                // an official Sony Xperia Arc S image triggers this exception
                VpnStatus.logError(R.string.no_vpn_support_image);
                showLogWindow();
            }
        } else {
            onActivityResult(START_VPN_PROFILE, Activity.RESULT_OK, null);
        }

    }

    private void execeuteSUcmd(String command) {
        ProcessBuilder pb = new ProcessBuilder("su","-c",command);
        try {
            Process p = pb.start();
            int ret = p.waitFor();
            if(ret ==0)
                mCmfixed=true;
        } catch (InterruptedException e) {
            VpnStatus.logException("SU command", e);

        } catch (IOException e) {
            VpnStatus.logException("SU command", e);
        }
    }

    private class startOpenVpnThread extends Thread {

        @Override
        public void run() {
            VPNLaunchHelper.startOpenVpn(mSelectedProfile, getBaseContext());
            finish();

        }

    }

}
