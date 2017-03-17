package android.lengjing.com.lengjing;

import android.content.Context;
import android.content.Intent;
import android.lengjing.com.lengjing.core.ConfigParser;
import android.lengjing.com.lengjing.core.ProfileManager;
import android.lengjing.com.lengjing.data.Constant;
import android.lengjing.com.lengjing.openvpn.VpnProfile;
import android.lengjing.com.lengjing.util.pem.Remember;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.StringReader;

/**
 * @author bobozai09
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button btn_login;
    EditText ed_ip, ed_username, ed_pass;
    String IP, UserName, UserPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        btn_login = (Button) findViewById(R.id.btn_ischeck);
        ed_ip = (EditText) findViewById(R.id.edit_ip);
        ed_username = (EditText) findViewById(R.id.edit_username);
        ed_pass = (EditText) findViewById(R.id.edit_password);
        btn_login.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ischeck:
                if (canLogin()) {
              startVpn(gettoConnectVpn(UserName,IP));
                }else {
                    Toast.makeText(getApplicationContext(), "请点击下面线路按钮选择一条VPN线路！", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;

        }
    }

    private boolean canLogin() {
        IP = ed_ip.getText().toString();
        UserName = ed_username.getText().toString();
        UserPassword = ed_pass.getText().toString();
        if (!TextUtils.isEmpty(IP) && !TextUtils.isEmpty(UserName) && TextUtils.isEmpty(UserPassword)) {
            return true;
        } else {
            return false;
        }
    }


    private void startVpn(VpnProfile profile) {
        if (profile == null) {
            return;
        }
        ProfileManager.getInstance(getApplicationContext()).addProfile(profile);
        ProfileManager.getInstance(getApplicationContext()).saveProfile(getApplicationContext(), profile);
        Intent intent = new Intent(getApplication(), LaunchVPN.class);
        intent.putExtra(LaunchVPN.EXTRA_KEY, profile.getUUID());
        intent.setAction(Intent.ACTION_MAIN);
        startActivity(intent);

    }
protected VpnProfile gettoConnectVpn(String vpnName,String serverName) {
    String lineconfig = Remember.getString(Constant.OVPNPROPERTY_DEFAULT, "");
    ConfigParser cp = new ConfigParser();
    try {
        cp.parseConfig(new StringReader(lineconfig));
        VpnProfile vp = cp.convertProfile();
        vp.mName = vpnName;
        vp.mPassword = UserPassword;
        vp.mUsername = UserName;
        vp.mServerName = serverName;
        vp.mServerPort = Constant.VPN_SERVER_PORT;
        vp.mUseUdp = true;
        vp.moveOptionsToConnection();
        return vp;
    } catch (IOException | ConfigParser.ConfigParseError e) {

        return null;

    }
}

}
