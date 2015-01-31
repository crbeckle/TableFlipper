package chris.ben.tableclicker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class OptionsActivity extends Activity {
	
	private Button vibrateButton;
	private Button musicButton;
	private boolean vibrateOnKill;
	private boolean playGameMusic;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
        
        Bundle b = getIntent().getExtras();
        vibrateOnKill = b.getBoolean(TableClickerActivity.VIBRATEOPTION);
        playGameMusic = b.getBoolean(TableClickerActivity.MUSICOPTION);
        ((TextView) findViewById(R.id.options_redditgold)).setText(b.getString(TableClickerActivity.RG));
        ((TextView) findViewById(R.id.options_maxlevel)).setText(b.getString(TableClickerActivity.MAXLEVEL));
        ((TextView) findViewById(R.id.options_fps)).setText(b.getString(TableClickerActivity.FPS));
        ((TextView) findViewById(R.id.options_flipstrength)).setText(b.getString(TableClickerActivity.FLIPSTRENGTH));
        ((TextView) findViewById(R.id.options_helpers)).setText(b.getString(TableClickerActivity.HELPERS));
        ((TextView) findViewById(R.id.options_upgradelevel)).setText(b.getString(TableClickerActivity.UPGRADELEVEL));
        
        vibrateButton = (Button) findViewById(R.id.options_vibratebutton);
        vibrateButton.setText((vibrateOnKill) ? "Vibrate On" : "Vibrate Off");
        vibrateButton.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		if (vibrateOnKill) {
        			vibrateOnKill = false;
        			vibrateButton.setText("Vibrate Off");
        		}
        		else {
        			vibrateOnKill = true;
        			vibrateButton.setText("Vibrate On");
        		}
        	}
        });
        
        musicButton = (Button) findViewById(R.id.options_musicbutton);
        musicButton.setText((playGameMusic) ? "Music On" : "Music Off");
        musicButton.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		if (playGameMusic) {
        			playGameMusic = false;
        			musicButton.setText("Music Off");
        		}
        		else {
        			playGameMusic = true;
        			musicButton.setText("Music On");
        		}
        	}
        });
	}
	
	@Override
    public void onBackPressed() {
		Bundle b = new Bundle();
		b.putBoolean(TableClickerActivity.VIBRATEOPTION, vibrateOnKill);
		b.putBoolean(TableClickerActivity.MUSICOPTION, playGameMusic);
		Intent intent = new Intent();
		intent.putExtras(b);
		setResult(Activity.RESULT_OK,intent);
		super.onBackPressed();
	}
}
