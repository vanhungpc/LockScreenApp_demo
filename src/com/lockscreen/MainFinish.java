package com.lockscreen;

import android.app.Activity;
import android.os.Bundle;

public class MainFinish extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_finish);
		finish();
	}
}
