/*
 * Application Name: Table Clicker
 * Application Description: A clicker game where your clicks flip tables and gain you (fake) Reddit Gold!
 * Application Authors: Chris Beckley and Ben Gelman
 * 
 * IMPORTANT INFORMATION ABOUT THIS APP:
 * 		All of the songs that are played in this app were taken from various Internet sources.
 * 		The title screen song, Lenny's song (booty song), and the shop music in game all came from Youtube videos 
 * 		while the in-game battle music (all 9 songs) came from http://www.tannerhelland.com/music-directory/. We
 * 		had no hand whatsoever in making any of these artistic works, but are solely using them for the purpose
 * 		of demonstrating our application as a final project. Furthermore, approximately 75% of the code for the
 * 		MusicHandler.java class came from: 
 * http://stackoverflow.com/questions/6884590/android-how-to-create-fade-in-fade-out-sound-effects-for-any-music-file-that-my
 * 
 * 		We uniquely created the remaining (approximately) 25% of the MusicHandler.java code, but the original version comes
 * 		from the above link from users sngreco and Pelanes.
 * 
 * KNOWN BUGS:
 * 	 1.	Using your Android device's "home" key will correctly exit the application; however, if you are in the title screen /
 * 		options menu / instructions page when you hit home, the title screen music will continue playing outside the app.
 * 		Furthermore, if you exit the app while in the game, the app will correctly exit, but will crash upon resuming.
 * 	 2.	We were able to fix most of the memory leaks within the game, but every time you go into the game, it loads all of
 * 		the Bitmaps needed into memory, which sucks up a chunk of heap memory. Thus, if you go back to the title screen,
 * 		then back to the game, then back to the title screen then back to the game (continuously), eventually the app will
 * 		crash with an "out of memory" error.
 * */

package chris.ben.tableclicker;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


@SuppressLint("HandlerLeak") 
public class TitleScreen extends Activity {
	
	/* Option Variables */
	private static final int OPTIONS_RESULT = 1;
	private boolean vibrateOnKill = false;
	private boolean playGameMusic = true;
	
	/* Music Handling */
	private static final int FADE_AMT = 1000;
	private static final int NO_FADE = 0;
	private static final int BOOTYMUSICDONE = 0x101;
	private MusicHandler booty = null;
	private MusicHandler titleScreenPlayer = null;
	final Handler bootyDoneHandler = new Handler() {
		public void handleMessage(Message m) {
			switch (m.what) {
			case BOOTYMUSICDONE:
				titleScreenPlayer.resume(FADE_AMT);
				break;
			}
		}
	};
	
	/* Lenny Handling */
	private int lennyTouches = 0;
	protected static final int ROTATEIDENTIFIER = 0x102;
	private final Handler refreshHandler = new Handler() {
		public void handleMessage(Message m) {
			switch (m.what) {
			case ROTATEIDENTIFIER:
				lennyHandRight.setImageBitmap(lrhAfter);
				lennyHandLeft.setImageBitmap(llhAfter);
				break;
			}
			super.handleMessage(m);
		}
	};
	private Handler handler = new Handler();
	private ImageView lennyHandRight = null;
	private ImageView lennyHandLeft = null;
	Matrix matLeft = null;
	Matrix matRight = null;
	Bitmap llhBefore = null;
	Bitmap llhAfter = null;
	Bitmap lrhBefore = null;
	Bitmap lrhAfter = null;
	
	private double rg = 0.0;
	private int maxLevel = 1;
	private double fps = 0.0;
	private double flipStrength = 1.0;
	private ArrayList<Integer> helpers = null;
	private int upgradeLevel = 0;
	private final String saveFile = "tf_save";
	
	private ProgressBar loader;
	private BufferedReader saveReader = null;
	private TextView lennyText = null;
	private ArrayList<String> lennyTextChoices = null;
	private ArrayList<String> bootyTextChoices = null;
	private Random rand = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title_screen);
        
        /* Set up refresh handler for Lenny hands */
        lennyHandRight = (ImageView) findViewById(R.id.ts_handrightimage);
        lennyHandLeft = (ImageView) findViewById(R.id.ts_handleftimage);
        matLeft = new Matrix();
        matRight = new Matrix();
        
        /* Set up the music */
        booty  = new MusicHandler(getBaseContext());
        booty.load(R.raw.bootysong, false, bootyDoneHandler, BOOTYMUSICDONE);
        titleScreenPlayer = new MusicHandler(getBaseContext());
        titleScreenPlayer.load(R.raw.reunion, true);
        titleScreenPlayer.play(FADE_AMT);
        
        /* Set up the secret Lenny text */
        lennyText = (TextView) findViewById(R.id.ts_lennysaystext);
        lennyText.setVisibility(View.INVISIBLE);
        bootyTextChoices = new ArrayList<String>();
        bootyTextChoices.add("Lovin the booty!");
        bootyTextChoices.add("Shakin the booty!");
        bootyTextChoices.add("Swiggity swooty, I\'m coming for that booty!");
        bootyTextChoices.add("Chasin the booty!");
        bootyTextChoices.add("Smokin booty!");
        bootyTextChoices.add("Back up the booty!");
        bootyTextChoices.add("I want the booty!");
        bootyTextChoices.add("Round booty!");
        bootyTextChoices.add("Fine booty!");
        lennyTextChoices = new ArrayList<String>();
        lennyTextChoices.add("I\'ll flip *your* table!");
        lennyTextChoices.add("Wanna flip my table?");
        lennyTextChoices.add("Heh heh heh");
        // Need to add some more...
        rand = new Random();
        
        /* Initially set the progress bar to invisible (only used for loading) */
        loader = (ProgressBar) findViewById(R.id.ts_loadgameprogress);
        loader.setVisibility(View.INVISIBLE);
        
        /* IMAGE VIEW HANDLER */
        ImageView lenny = (ImageView) findViewById(R.id.ts_mainimage);
        AdapterView.OnClickListener ocl_lenny;
        ocl_lenny = new AdapterView.OnClickListener() {
        	public void onClick(View v) {
        		
        		lennyText.setVisibility(View.VISIBLE);
        		if (!booty.playing()) {
        			lennyTouches++;
        			lennyText.setText(lennyTextChoices.get(rand.nextInt(lennyTextChoices.size())));
        			
        			if (lennyTouches > 20) {
            			lennyText.setText("Gimme that booty!");
            			lennyTouches = 0;
            			if (titleScreenPlayer.playing()) { 
            				titleScreenPlayer.pause(FADE_AMT); 
            			}
            			booty.play(FADE_AMT);
            		}
        		}
        		else {
        			lennyText.setText(bootyTextChoices.get(rand.nextInt(bootyTextChoices.size())));
        		}
        	}
        };
        lenny.setOnClickListener(ocl_lenny);
        
        /* BUTTON HANDLERS */
        Button b_continue = (Button) findViewById(R.id.ts_continuebutton);
        AdapterView.OnClickListener ocl_continue;
        ocl_continue = new AdapterView.OnClickListener() {
        	public void onClick(View v) {
        		// Show that we're loading the data from the save file
        		loader.setVisibility(View.VISIBLE);
        		try {
        			saveReader = new BufferedReader(new InputStreamReader(openFileInput(saveFile)));
        			helpers = null;
        			String line;
        			int i = 0;
        			while ((line = saveReader.readLine()) != null) {
        				saveLineParse(line,i);
        				i++;
        			}
        			saveReader.close();
        		}
        		catch (Exception e) {
        			// Hopefully no exceptions in IO
        		}
        		
        		// Stop the music without killing the player
        		musicTeardown(false);
        		
        		// Populate the intent and start up the game activity
        		Bundle b = new Bundle();
        		b.putDouble(TableClickerActivity.RG, rg);
        		b.putInt(TableClickerActivity.MAXLEVEL, maxLevel);
        		b.putDouble(TableClickerActivity.FPS, fps);
        		b.putDouble(TableClickerActivity.FLIPSTRENGTH, flipStrength);
        		b.putIntegerArrayList(TableClickerActivity.HELPERS, helpers);
        		b.putInt(TableClickerActivity.UPGRADELEVEL, upgradeLevel);
        		b.putBoolean(TableClickerActivity.VIBRATEOPTION, vibrateOnKill);
        		b.putBoolean(TableClickerActivity.MUSICOPTION, playGameMusic);
        		Intent continue_intent = new Intent(TitleScreen.this,TableClickerActivity.class);
        		continue_intent.putExtras(b);
        		loader.setVisibility(View.INVISIBLE);
        		startActivity(continue_intent);
        	}
        };
        b_continue.setOnClickListener(ocl_continue);
        
        Button b_newgame = (Button) findViewById(R.id.ts_newgamebutton);
        AdapterView.OnClickListener ocl_newgame;
        ocl_newgame = new AdapterView.OnClickListener() {
        	public void onClick(View v) {
        		// Stop the music without killing the player
        		musicTeardown(false);
        		
        		// Populate the intent and start up the game activity
        		Bundle b = new Bundle();
        		b.putDouble(TableClickerActivity.RG, 1000000000.0); 
        		b.putInt(TableClickerActivity.MAXLEVEL, 1); 
        		b.putDouble(TableClickerActivity.FPS, 0.0);
        		b.putDouble(TableClickerActivity.FLIPSTRENGTH, 1.0);
        		b.putIntegerArrayList(TableClickerActivity.HELPERS, new ArrayList<Integer>());
        		b.putInt(TableClickerActivity.UPGRADELEVEL, 0);
        		b.putBoolean(TableClickerActivity.VIBRATEOPTION, vibrateOnKill);
        		b.putBoolean(TableClickerActivity.MUSICOPTION, playGameMusic);
        		Intent newgame_intent = new Intent(TitleScreen.this,TableClickerActivity.class);
        		newgame_intent.putExtras(b);
        		startActivity(newgame_intent);
        	}
        };
        b_newgame.setOnClickListener(ocl_newgame);
        
        Button b_instructions = (Button) findViewById(R.id.ts_instructionsbutton);
        AdapterView.OnClickListener ocl_instructions;
        ocl_instructions = new AdapterView.OnClickListener() {
        	public void onClick(View v) {
        		Intent instructions_intent = new Intent(TitleScreen.this,InstructionsActivity.class);
        		startActivity(instructions_intent);
        	}
        };
        b_instructions.setOnClickListener(ocl_instructions);
        
        Button b_options = (Button) findViewById(R.id.ts_optionsbutton);
        AdapterView.OnClickListener ocl_options;
        ocl_options = new AdapterView.OnClickListener() {
        	public void onClick(View v) {
        		Bundle b = new Bundle();
        		String[] lines = {"Reddit Gold:0.0","Max Level:0","FPS:0.0","Flip Strength:1.0","Helpers:0,0,0,0,0","Upgrade Level:0"};
        		try {
        			saveReader = new BufferedReader(new InputStreamReader(openFileInput(saveFile)));
        			String line;
        			int i = 0;
        			while ((line = saveReader.readLine()) != null) {
        				lines[i] = line;
        				i++;
        			}
        			saveReader.close();
        		}
        		catch (Exception e) {
        			// Hopefully no exceptions in IO
        		}
        		
				b.putString(TableClickerActivity.RG, lines[0]);
				b.putString(TableClickerActivity.MAXLEVEL, lines[1]);
				b.putString(TableClickerActivity.FPS, lines[2]);
				b.putString(TableClickerActivity.FLIPSTRENGTH, lines[3]);
				b.putString(TableClickerActivity.HELPERS, lines[4]);
				b.putString(TableClickerActivity.UPGRADELEVEL, lines[5]);
        		b.putBoolean(TableClickerActivity.VIBRATEOPTION, vibrateOnKill);
        		b.putBoolean(TableClickerActivity.MUSICOPTION, playGameMusic);
        		Intent options_intent = new Intent(TitleScreen.this,OptionsActivity.class);
        		options_intent.putExtras(b);
        		startActivityForResult(options_intent, OPTIONS_RESULT);
        	}
        };
        b_options.setOnClickListener(ocl_options);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode == OPTIONS_RESULT) {
    		vibrateOnKill = data.getExtras().getBoolean(TableClickerActivity.VIBRATEOPTION);
    		playGameMusic = data.getExtras().getBoolean(TableClickerActivity.MUSICOPTION);
    		Log.d("Options Return", "Vibrate = "+vibrateOnKill+" , Music = "+playGameMusic);
    	}
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	
    	/* Restart refreshing of Lenny's hands and the music (if it was stopped) */
    	if (!booty.playing() && !titleScreenPlayer.playing()) {
    		titleScreenPlayer.resume(FADE_AMT);
    	}
    	handler.postDelayed(refreshRunner, 1000);
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	/* Stop the refresher running Lenny's hands */
    	handler.removeCallbacks(refreshRunner);
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	
    	/* Fully destroy the music player */
    	musicTeardown(true);
    	titleScreenPlayer = null;
    }
    
    private void musicTeardown(boolean fullTeardown) {
	    /* Gracefully end the music players */
	    if (booty.playing()) { booty.pause(FADE_AMT); }
	    if (titleScreenPlayer.playing()) { titleScreenPlayer.pause(FADE_AMT); }
	    
	    if (fullTeardown) {
	    	booty.stopAndRelease(NO_FADE);
	    	titleScreenPlayer.stopAndRelease(NO_FADE);
	    }
    }
    
    private void saveLineParse(String line, int linenum) {
    	// Extract the value of interest in the line and handle it
    	String value = (linenum == 4) ? line.substring(line.indexOf(':')+1,line.length()-1) : line.substring(line.indexOf(':')+1,line.length());
    	Log.d("File Parse", ""+linenum+": "+line);
    	Log.d("File Parse", "Extracted value: "+value);
		try {
			switch (linenum) {
			case 0:
				rg = Double.parseDouble(value);
				break;
			case 1:
				maxLevel = Integer.parseInt(value);
				break;
			case 2:
				fps = Double.parseDouble(value);
				break;
			case 3:
				flipStrength = Double.parseDouble(value);
				break;
			case 4:
				if (helpers == null) {helpers = new ArrayList<Integer>();}
				List<String> temp = Arrays.asList(value.split(","));
				String s = "";
				for (int i = 0; i < temp.size(); i++) {
					s = temp.get(i);
					if (!s.equals(""))
						helpers.add(Integer.parseInt(s));
				}
				break;
			case 5:
				upgradeLevel = Integer.parseInt(value);
				break;
			default:
				break;
			}
			Log.d("File Parse", "Line parsed successfully!");
		}
		catch (Exception e) {
			// Let's hope that all worked!
		}
    }
    
    /* The driver for Lenny's hands */
    private Runnable refreshRunner = new Runnable() {
    	int messageIndex = 0;
    	public void run() {
    		
    		llhBefore = BitmapFactory.decodeResource(getResources(), R.drawable.lennyhandleft);
    		lrhBefore = BitmapFactory.decodeResource(getResources(), R.drawable.lennyhandright);
    		Message m = new Message();
    		if (this.messageIndex == 0) {
    			this.messageIndex = 1;
    			matLeft.postRotate((float) 45);
    			matRight.postRotate((float) -45);
    		}
    		else {
    			this.messageIndex = 0;
    			matLeft.postRotate((float) -45);
    			matRight.postRotate((float) 45);
    		}
    		llhAfter = Bitmap.createBitmap(llhBefore,0,0,llhBefore.getWidth(),llhBefore.getHeight(),matLeft,true);
    		lrhAfter = Bitmap.createBitmap(lrhBefore,0,0,lrhBefore.getWidth(),lrhBefore.getHeight(),matRight,true);
    		m.what = TitleScreen.ROTATEIDENTIFIER;
    		TitleScreen.this.refreshHandler.sendMessage(m);
    		
    		handler.postDelayed(this, 1000);
    	}
    };
}
