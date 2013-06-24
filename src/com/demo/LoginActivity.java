package com.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

public class LoginActivity extends Activity {
	
	private EditText mInputName;
	private EditText mInputPassword;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.activity_login);
		
		mInputName = (EditText) findViewById(R.id.input_name);
		mInputPassword = (EditText) findViewById(R.id.input_password);
		
		findViewById(R.id.login).setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(LoginActivity.this ,MainActivity.class);
				startActivity(i);
			}
		});
		
		findViewById(R.id.register).setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

}
