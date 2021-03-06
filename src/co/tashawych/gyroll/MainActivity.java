package co.tashawych.gyroll;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}
	
	public void btnPlayClicked(View v) {
		Intent playGame = new Intent(this, GyRollGame.class);
		startActivity(playGame);
	}
	
	public void btnInstructionsClicked(View v) {
		Intent instructions = new Intent(this, InstructionsActivity.class);
		startActivity(instructions);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
