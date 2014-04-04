package co.tashawych.gyroll;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class InstructionsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_instructions);
	}
	
	public void btnPlayClickedInstructions(View v) {
		Intent playGame = new Intent(this, GyRollGame.class);
		startActivity(playGame);
	}

}
