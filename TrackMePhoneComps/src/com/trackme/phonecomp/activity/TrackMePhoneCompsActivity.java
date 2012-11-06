package com.trackme.phonecomp.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;

import com.trackme.phonecomp.service.TrackMePhoneCompsService;
import com.trackme.phonecomp.activity.R;

public class TrackMePhoneCompsActivity extends Activity {

	private Context thisContext;
	private Button exitButton;
	private Button startButton;
	private Button stopButton;
	private Vibrator vb;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		drawUI();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		drawUI();
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		drawUI();
	}
	
	private boolean isMyServiceRunning() {
	    ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if ("com.trackme.phonecomp.service.TrackMePhoneCompsService".equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
	
	private void drawUI(){
		setContentView(R.layout.main);
		thisContext = this;

		exitButton = (Button)findViewById(R.id.exit_button);
		startButton = (Button)findViewById(R.id.start_button);
		stopButton = (Button)findViewById(R.id.stop_button);

		if(!isMyServiceRunning()){
			startButton.setEnabled(true);
			stopButton.setEnabled(false);
		}else{
			startButton.setEnabled(false);
			stopButton.setEnabled(true);
		}
		
		myOnClickListener exitListener = new myOnClickListener();
		exitButton.setOnClickListener(exitListener);

		myOnClickListener startListener = new myOnClickListener();
		startButton.setOnClickListener(startListener);

		myOnClickListener stopListener = new myOnClickListener();
		stopButton.setOnClickListener(stopListener);
	}
	
	public class myOnClickListener implements View.OnClickListener{

		@Override
		public void onClick(View v) {

			switch(v.getId()){

			case R.id.start_button:
				startService(new Intent(thisContext, TrackMePhoneCompsService.class));
				startButton.setEnabled(false);
				stopButton.setEnabled(true);
				vb.vibrate(50);
				break;

			case R.id.stop_button:
				stopService(new Intent(thisContext, TrackMePhoneCompsService.class));
				startButton.setEnabled(true);
				stopButton.setEnabled(false);
				vb.vibrate(50);
				break;

			case R.id.exit_button:
				finish();
				vb.vibrate(50);
				break;
			}
		}
	}
}