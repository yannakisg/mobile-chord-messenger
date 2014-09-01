package edu.ucr.cs.mobilechord;

import org.apache.http.nio.reactor.IOReactorException;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class RegisterActivity extends Activity {
	
	private EditText inputUsername;
	private EditText inputPassword;
	private EditText inputIP;
	private EditText inputPort;
	
	private boolean showed = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Helper.setWifiIP(getWifiIP());
		try {
			Helper.createClientComponent();
		} catch (IOReactorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (savedInstanceState != null && savedInstanceState.containsKey("already_registered")) {
			Log.e("TEST", "SUCCESS");
			showed = false;
			showLoginActivity();
		}
		
		setContentView(R.layout.activity_register);		
		showed = true;
		
		inputUsername = (EditText) findViewById(R.id.username);
		inputPassword = (EditText) findViewById(R.id.password);
		inputIP = (EditText) findViewById(R.id.server_ip);
		inputPort = (EditText) findViewById(R.id.port);
	
		
		Button btnRegister = (Button) findViewById(R.id.btnRegister);
		
		btnRegister.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Helper.register(inputUsername.getText().toString(), inputPassword.getText().toString(), inputIP.getText().toString(), inputPort.getText().toString());
				showLoginActivity();
			}
		});
		
		
	}
	
	private String getWifiIP() {
		WifiManager wifiMgr = (WifiManager) getSystemService(WIFI_SERVICE);
		WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
		int ip = wifiInfo.getIpAddress();
		String ipString = String.format(
				   "%d.%d.%d.%d",
				   (ip & 0xff),
				   (ip >> 8 & 0xff),
				   (ip >> 16 & 0xff),
				   (ip >> 24 & 0xff));
		
		return ipString;
	}
	
	@Override
	protected void onSaveInstanceState(Bundle state) {
	    super.onSaveInstanceState(state);
	    if (showed) {
	    	state.putBoolean("already_registered", true);

	    	state.putString("username", inputUsername.getText().toString());
		
	    	state.putString("server_ip", inputIP.getText().toString());
	    	state.putString("server_port", inputPort.getText().toString());
	    }
		
	}
	
	
	private void showLoginActivity() {
		Intent nextScreen = new Intent(getApplicationContext(), LoginActivity.class);
		
		nextScreen.putExtra("username", inputUsername.getText().toString());
		nextScreen.putExtra("server_ip", inputIP.getText().toString());
		nextScreen.putExtra("server_port", inputPort.getText().toString());
		
		startActivity(nextScreen);
	}
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
