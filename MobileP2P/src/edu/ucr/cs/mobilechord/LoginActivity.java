package edu.ucr.cs.mobilechord;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends Activity  {
	
	private EditText inputUsername;
	private EditText inputPassword;
	private EditText inputIP;
	private EditText inputPort;
	private EditText inputLocation;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);		
		
		inputUsername = (EditText) findViewById(R.id.username);
		inputPassword = (EditText) findViewById(R.id.password);
		inputIP = (EditText) findViewById(R.id.server_ip);
		inputPort = (EditText) findViewById(R.id.port);
		inputLocation = (EditText) findViewById(R.id.location);
		
		if (savedInstanceState != null && savedInstanceState.containsKey("already_registered")) {
			inputUsername.setText(savedInstanceState.getString("username"));
			inputIP.setText(savedInstanceState.getString("server_ip"));
			inputPort.setText(savedInstanceState.getString("server_port"));
		} else {
			Intent i = getIntent();
			inputUsername.setText(i.getStringExtra("username"));
			inputIP.setText(i.getStringExtra("server_ip"));
			inputPort.setText(i.getStringExtra("server_port"));
		}
		
		Button btnLogin = (Button) findViewById(R.id.btnLogin);
		btnLogin.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Helper.login(inputLocation.getText().toString(), inputUsername.getText().toString(), inputPassword.getText().toString(), inputIP.getText().toString(), inputPort.getText().toString());
				showTextActivity();
			}
		});
	}
	
	@Override
	protected void onSaveInstanceState(Bundle state) {
		super.onSaveInstanceState(state);
		state.putBoolean("already_registered", true);

    	state.putString("username", inputUsername.getText().toString());
	
    	state.putString("server_ip", inputIP.getText().toString());
    	state.putString("server_port", inputPort.getText().toString());
	}
	
	private void showTextActivity() {
		Intent nextScreen = new Intent(getApplicationContext(), TextActivity.class);
		startActivity(nextScreen);
	}
}
