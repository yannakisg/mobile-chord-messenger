package edu.ucr.cs.mobilechord;

import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.BufferType;

public class TextActivity extends Activity {
	private String username;
	private String server_ip;
	private String server_port;
	
	private EditText recipient;
	private EditText text;
	private Set<String> sharedKeyAgreementSet;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_text);
		
		sharedKeyAgreementSet = new HashSet<String>();
		
		if (savedInstanceState != null && savedInstanceState.containsKey("already_registered")) {
			username = savedInstanceState.getString("username");
			server_ip = savedInstanceState.getString("server_ip");
			server_port = savedInstanceState.getString("server_port");
		} else {
			Intent i = getIntent();
			username = i.getStringExtra("username");
			server_ip = i.getStringExtra("server_ip");
			server_port = i.getStringExtra("server_port");
		}
		
		recipient = (EditText) findViewById(R.id.textRecipient);
		text = (EditText) findViewById(R.id.text);
		
		Button btnSend = (Button) findViewById(R.id.btnSend);
		btnSend.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String str_recipient = recipient.getText().toString();
				String str_text = text.getText().toString();
				
				if (str_recipient == null || str_recipient.equals("") || str_text == null || str_text.equals("")) {
					return;
				} else {
					Helper.sendTextMessageTo(TextActivity.this, str_recipient, str_text, sharedKeyAgreementSet.contains(str_recipient));
				}
			}
		});
	}
	
	public void displayMessage(String str_text) {
		text.setText(str_text, TextView.BufferType.NORMAL);
	}
}
