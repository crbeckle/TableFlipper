package chris.ben.tableclicker;

import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

public class FragmentMainGame extends Fragment{
	
	/* Setup Flag */
	private boolean setupComplete = false;
	
	/* Vibration Constant */
	private static final int VIBRATE_LENGTH = 100;
	
	/* Handler for invalidating game view */
	private static final int INVALIDATEMESSAGE = 0x101;
	private static final int NEWLEVELMESSAGE = 0x102;
	protected static final int DATAUPDATEMESSAGE = 0x103;
	@SuppressLint("HandlerLeak")
	final Handler gameDriverHandler = new Handler() {
		public void handleMessage(Message m) {
			switch (m.what) {
			case INVALIDATEMESSAGE:
				gameView.invalidate();
				break;
			case NEWLEVELMESSAGE:
				levelText.setText("LEVEL "+curLevel);
				break;
			}
		}
	};
	private Handler handler = new Handler();
	
	public void stopDriver() {
		Log.d("Game Driver", "Driver stopped!");
		handler.removeCallbacks(tfGameDriver);
		gameDriverHandler.removeMessages(INVALIDATEMESSAGE);
	}
	
	public void startDriver() {
		Log.d("Game Driver", "Driver started!");
		handler.postDelayed(tfGameDriver, DRIVER_SLEEPTIME);
	}
	
	/* Driver Constants */
	private static final int DRIVER_SLEEPTIME = 50;
	private static final double DRIVER_FPS = 1000.0 / DRIVER_SLEEPTIME;
	
	/* Character Stats */
	private double fps = 0.0;
	private double flipStrength = 1.0;
	private double currency = 0.0;
	
	/* Driver Variables */
	private boolean enemyKilled = false;
	private int numTouches = 0;
	private int curLevel = 1;
	private int maxLevel = 1;
	
	/* Game Objects */
	private TextView levelText = null;
	private TFGameView gameView = null;
	private TFLevel curLevelObj = null;
	private TFLevel nextLevelObj = null;
	private TableEnemy curEnemy =  null;
	private TableEnemy prevEnemy = null;
	private TFCharacter curChar = null;
	private ArrayList<Bitmap> enemies = null;
	
	public void updateGameData(double moneyUpdate, double fpsUpdate, double fsUpdate, TFCharacter charUpdate) {
		currency = moneyUpdate;
		fps = fpsUpdate;
		flipStrength = fsUpdate;
		curChar = charUpdate;
	}
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState){
		// Inflate the layout for this fragment
	    View temp_view = inflater.inflate(R.layout.maingame_layout, container, false);
	    setupComplete = create(temp_view);
	    return temp_view;
	}
	
	/* Helper methods to setup and teardown the structure of this class */
	private boolean create(View v) {
		
		/* Get the enclosing activity */
		TableClickerActivity act = (TableClickerActivity) getActivity();
		fps = act.getFPS();
		currency = act.getCurrency();
		flipStrength = act.getFlipStrength();
		curLevel = maxLevel = act.getMaxLevel();
		
		/* Initialize the game widgets */
        levelText = (TextView) v.findViewById(R.id.leveltext);
        levelText.setText("LEVEL "+curLevel);
        
        /* Initialize the Table Flipper Game View */
        gameView = (TFGameView) v.findViewById(R.id.gamescreen);
        gameView.setOnClickListener(new AdapterView.OnClickListener() {
        	public void onClick(View v) {
        		touch(1);
        	}
        });
        
        /* Initialize the character */
        if (curChar == null) {
	        curChar = act.getCurrentCharacter();
        }
        
        
        /* Initialize the Table Flipper level and first enemy */
        enemies = new ArrayList<Bitmap>();
        enemies.add(BitmapFactory.decodeResource(getResources(),R.drawable.table01));
        enemies.add(BitmapFactory.decodeResource(getResources(),R.drawable.table02));
        enemies.add(BitmapFactory.decodeResource(getResources(),R.drawable.table03));
        enemies.add(BitmapFactory.decodeResource(getResources(),R.drawable.table04));
        enemies.add(BitmapFactory.decodeResource(getResources(),R.drawable.table05));
        enemies.add(BitmapFactory.decodeResource(getResources(),R.drawable.table06));
        enemies.add(BitmapFactory.decodeResource(getResources(),R.drawable.table07));
        enemies.add(BitmapFactory.decodeResource(getResources(),R.drawable.table08));
        enemies.add(BitmapFactory.decodeResource(getResources(),R.drawable.table09));
        curLevelObj = new TFLevel(curLevel,enemies);
        nextLevelObj = new TFLevel(curLevel+1,enemies);
        curEnemy = curLevelObj.nextEnemy();
		
		return true;
	}
	
	private boolean destroy() {
		gameView.destroy();
		gameView = null;
		enemies = null;
		levelText = null;
		curLevelObj = null;
		nextLevelObj = null;
		curEnemy = null;
		prevEnemy = null;
		curChar = null;
		
		return false;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		// TODO THIS MIGHT CAUSE PROBLEMS!
		if (!setupComplete) {
			setupComplete = create(getView());
		}
		this.startDriver();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		this.stopDriver();
		
		TableClickerActivity myActivity = (TableClickerActivity) getActivity();
		myActivity.gameUpdate(currency,maxLevel);
		myActivity.save();
		
		if (setupComplete) {
			setupComplete = destroy();
		}
	}
	
	public synchronized void touch(int touchType) {
    	this.numTouches = (touchType == 0) ? 0 : this.numTouches + 1;
    }
    
    private Runnable tfGameDriver = new Runnable() {
    	
    	private int handRaiseCounter = 0;
    	
    	public void run() {
    			
    		/* Calculate the damage done */
    		int tempTouches = numTouches;
    		touch(0);
    		double damage = (flipStrength * tempTouches) + (fps / DRIVER_FPS);
    		
    		/* See if we flipped the current table */
    		double remainingHP = curEnemy.attack(damage);
    		if (remainingHP == 0.0) {
    			((TableClickerActivity)getActivity()).vibrate(VIBRATE_LENGTH);
    			prevEnemy = curEnemy;
    			currency += curEnemy.getReward();
    			enemyKilled = true;
    			handRaiseCounter = 0;
    			curEnemy = curLevelObj.nextEnemy();
    			if (curEnemy == null) {
    				curLevel++;
    				if (curLevel > maxLevel) {maxLevel = curLevel;}
    				curLevelObj = nextLevelObj;
    				curEnemy = curLevelObj.nextEnemy();
    				new LoadNextLevel().execute(new Integer[] {curLevel+1});
    				Message nextLev = new Message();
    				nextLev.what = NEWLEVELMESSAGE;
    				gameDriverHandler.sendMessage(nextLev);
    			}
    			
    			// Update game data in the central source
				TableClickerActivity myActivity = (TableClickerActivity) getActivity();
				myActivity.gameUpdate(currency,maxLevel);
    		}
    		
    		/* Keep track of how long to keep the character hand raised after flipping a table */
    		if (enemyKilled) {
    			handRaiseCounter++;
    		}
    		if (handRaiseCounter > 10) {
    			enemyKilled = false;
    		}
    		
    		/* Update the game view based on the damage done */
    		gameView.updateGame(curChar.getBody(), 
    				curChar.getHand(),
    				curChar.getHandPose((enemyKilled) ? 0.0 : remainingHP), 
    				curEnemy.getEnemy(),
    				curEnemy.getPose(), 
    				(enemyKilled) ? prevEnemy.getEnemy() : null,
    				(enemyKilled) ? prevEnemy.getPose() : 0, 
    				flipStrength, 
    				fps, 
    				currency, 
    				curLevelObj.getProgress(), 
    				remainingHP);
    		
    		/* Invalidate the game view to redraw it */
    		Message m = new Message();
    		m.what = INVALIDATEMESSAGE;
    		gameDriverHandler.sendMessage(m);
    		
    		/* Rerun the driver after the allotted sleep time */
    		handler.postDelayed(this, DRIVER_SLEEPTIME);
    	}
    };
    
    private class LoadNextLevel extends AsyncTask<Integer, Void, Void> {

		@Override
		protected Void doInBackground(Integer... ints) {
			int newLevelNum = ints[0];
	        nextLevelObj = new TFLevel(newLevelNum,enemies);
			
			return null;
		}
    }
}