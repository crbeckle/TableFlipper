package chris.ben.tableclicker;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

@SuppressLint("HandlerLeak") 
public class TableClickerActivity extends FragmentActivity {

	/* Setup Boolean */
	private boolean setupComplete = false;
	
	/* Data Passing Constants */
	public static final String RG = "chris.ben.tableclickeractivity.RG";
	public static final String MAXLEVEL = "chris.ben.tableclickeractivity.MAXLEVEL";
	public static final String FPS = "chris.ben.tableclickeractivity.FPS";
	public static final String FLIPSTRENGTH = "chris.ben.tableclickeractivity.FLIPSTRENGTH";
	public static final String HELPERS = "chris.ben.tableclickeractivity.HELPERS";
	public static final String UPGRADELEVEL = "chris.ben.tableclickeractivity.UPGRADELEVEL";
	public static final String VIBRATEOPTION = "chris.ben.tableclickeractivity.VIBRATEOPTION";
	public static final String MUSICOPTION = "chris.ben.tableclickeractivity.MUSICOPTION";
	
	/* Vibration Handling */
	private boolean vibrateOnKill;
	private Vibrator v;
	public void vibrate(int millis) {
    	if (vibrateOnKill) {
    		v.vibrate(millis);
    	}
    }
	
	/* Music Handling */
	private boolean playGameMusic;
	private MusicHandler battleMusicPlayer = null;
	private MusicHandler shopMusicPlayer = null;
	private static final String resUriHead = "android.resource://chris.ben.tableclicker/";
	private static boolean onGame = true;
	private static final int FADE_AMT = 1000;
	private static final int NO_FADE = 0;
	private static final int MUSICDONEMESSAGE = 0x101;
	private static final int ONGAMEMESSAGE = 0x102;
	private static final int ONSHOPMESSAGE = 0x103;
	private Random mRand = null;
	private ArrayList<Uri> battleMusic = null;
	private final Handler musicHandler = new Handler() {
		public void handleMessage(Message m) {
			switch (m.what) {
			case MUSICDONEMESSAGE:
				battleMusicPlayer.setSong(battleMusic.get(mRand.nextInt(battleMusic.size())),
						false,
						musicHandler,
						MUSICDONEMESSAGE);
				battleMusicPlayer.play(FADE_AMT);
				break;
			case ONGAMEMESSAGE:
				onGame = true;
				shopMusicPlayer.pause(FADE_AMT);
				battleMusicPlayer.resume(FADE_AMT);
				break;
			case ONSHOPMESSAGE:
				if (onGame) {
					battleMusicPlayer.pause(FADE_AMT);
					shopMusicPlayer.resume(FADE_AMT);
					onGame = false;
				}
				break;
			}
		}
	};
	
	/* Fragments */
	private static final int GAME_TAB = 0;
	private static final int CHARACTER_TAB = 1;
	private static final int HELPER_TAB = 2;
	private FragmentCharacters characterfrag;
	private FragmentMainGame gamefrag;
	private FragmentHelpers helperfrag;
	private Fragment[] fragarr;
	private ViewPager myViewPager;
	private MyAdapter myFragmentPagerAdapter;
	
	/* Game State Variables */
	private double rg = 0.0;
	private int maxLevel = 0;
	private double fps = 0.0;
	private double flipStrength = 0.0;
	private ArrayList<Integer> helpers = null;
	private int upgradeLevel = 0;
	private final String saveFile = "tf_save";
	private TFCharacter defaultChar = null;
	private ArrayList<TFCharacter> characters = null;
	private ArrayList<TFHelper> helperObjects = null;
	
	/* State Variable Getter Methods */
	public double getCurrency() { return rg; }
	public int getMaxLevel() { return maxLevel; }
	public double getFPS() { return fps; }
	public double getFlipStrength() { return flipStrength; }
	public ArrayList<Integer> getHelpers() { return helpers; }
	public int getUpgradeLevel() { return upgradeLevel; }
	public ArrayList<TFHelper> getHelperObjects() { return helperObjects; }
	public ArrayList<TFCharacter>getCharacters() { return characters; }
	public TFCharacter getCurrentCharacter() {
		if (upgradeLevel == 0)
			return defaultChar;
		else
			return characters.get(upgradeLevel - 1);
	}
	
	/* State Variable Setting Methods */
	public void gameUpdate(double c, int level) {
		this.rg = c;
		this.maxLevel = level;
	}
	public void helperUpdate(ArrayList<Integer> newHelpers, double fpSec, double c) {
		this.helpers = newHelpers;
		this.fps = fpSec;
		this.rg = c;
	}
	public void characterUpdate(int ul, double fs, double c) {
		this.upgradeLevel = ul;
		this.flipStrength = fs;
		this.rg = c;
	}
	
	/* Saving */ 
	public void save() {
		try {
			FileOutputStream fos = openFileOutput(saveFile, Context.MODE_PRIVATE);
			fos.write(("Currency:"+rg+"\n").getBytes());
			fos.write(("Max Level:"+maxLevel+"\n").getBytes());
			fos.write(("FPS:"+fps+"\n").getBytes());
			fos.write(("Flip Strength:"+flipStrength+"\n").getBytes());
			fos.write(("Helpers:").getBytes());
			for (int i = 0; i < helpers.size(); i++) {
				fos.write((helpers.get(i).toString()+",").getBytes());
			}
			fos.write(("\n").getBytes());
			fos.write(("Upgrade Level:"+upgradeLevel+"\n").getBytes());
			Log.d("File Save","Wrote Upgrade Level ("+upgradeLevel+")");
			fos.close();
			fos = null;
			Log.d("File Save", "File finished saving");
		}
		catch (Exception e) {}
	}
	
	@Override
	public void onBackPressed() {
		/* Make sure to save before exiting */
		this.save();
		super.onBackPressed();
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tableclicker);
        
        setupComplete = create();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	if (!setupComplete) 
    		setupComplete = create();
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	// Make sure no lingering messages cause crashes
    	musicHandler.removeMessages(MUSICDONEMESSAGE);
    	// Fully destroy the music players
    	if (playGameMusic) { musicTeardown(true); }
    	if (setupComplete)
    		setupComplete = destroy();
    }
    
    private void musicTeardown(boolean fullTeardown) {
	    /* Gracefully end the music player */
	    if (battleMusicPlayer.playing()) { battleMusicPlayer.pause(FADE_AMT); }
	    if (shopMusicPlayer.playing()) { shopMusicPlayer.pause(FADE_AMT); }
	    
	    if (fullTeardown) {
	    	battleMusicPlayer.stopAndRelease(NO_FADE);
	    	shopMusicPlayer.stopAndRelease(NO_FADE);
	    }
    }
    
    /* Methods to initialize and teardown the class structure */
    private boolean create() {
    	
    	/* Setup the characters */
        TFCharacter[] chars = {
    			new TFCharacter("Donger",
    		    		"Your table flipping is slowly improving. You may not be the best at lifting tables yet, but at least you can raise your dongers.",
    		    		BitmapFactory.decodeResource(getResources(),getResources().getIdentifier("donger_body","drawable",getPackageName())),
    					BitmapFactory.decodeResource(getResources(),getResources().getIdentifier("donger_hand_finished","drawable",getPackageName())),
    					BitmapFactory.decodeResource(getResources(),getResources().getIdentifier("donger","drawable",getPackageName())),
    					2.0,
    					500.0),
    			new TFCharacter("Buffout",
    				    "Raising dongers isn\'t enough for you anymore. Your experience in table flipping has earned you bigger biceps and some serious punching skills. Those fighting tips from Ricky are really coming in handy.",
    				  	BitmapFactory.decodeResource(getResources(),getResources().getIdentifier("buffout_body","drawable",getPackageName())),
    					BitmapFactory.decodeResource(getResources(),getResources().getIdentifier("buffout_hand_finished","drawable",getPackageName())),
    					BitmapFactory.decodeResource(getResources(),getResources().getIdentifier("buffout","drawable",getPackageName())),
    					5.0,
    					10000.0),
    			new TFCharacter("Musician",
    				    "We didn\'t start the fire. It was always burning since the world\'s been turning. We didn\'t start the fire. No we didn\'t light it but we tried to fight it... Just kidding. We started the fire. And now we\'re going to burn all the flippin tables.",
    				    BitmapFactory.decodeResource(getResources(),getResources().getIdentifier("musician_body","drawable",getPackageName())),
    					BitmapFactory.decodeResource(getResources(),getResources().getIdentifier("musician_hand_finished","drawable",getPackageName())),
    					BitmapFactory.decodeResource(getResources(),getResources().getIdentifier("musician","drawable",getPackageName())),
    					10.0,
    					25000.0),
    			new TFCharacter("Veteran",
    				    "Behind you is a massacre. Tables donged, punched, and burned. A life of fighting has made you an expert flipper and tables flee before your mightiness. But this is war, and you know you have a job to do.",
    				    BitmapFactory.decodeResource(getResources(),getResources().getIdentifier("veteran_body","drawable",getPackageName())),
    					BitmapFactory.decodeResource(getResources(),getResources().getIdentifier("veteran_hand_finished","drawable",getPackageName())),
    					BitmapFactory.decodeResource(getResources(),getResources().getIdentifier("veteran","drawable",getPackageName())),
    					25.0,
    					100000.0),
    			new TFCharacter("Lenny",
    				    "If tables had booty, Lenny would be after it.",
    				    BitmapFactory.decodeResource(getResources(),getResources().getIdentifier("lennychar_body","drawable",getPackageName())),
    					BitmapFactory.decodeResource(getResources(),getResources().getIdentifier("lennychar_hand_finished","drawable",getPackageName())),
    					BitmapFactory.decodeResource(getResources(),getResources().getIdentifier("lennychar","drawable",getPackageName())),
    					100.0,
    					1000000.0)
    	};
        
        characters = new ArrayList<TFCharacter>(Arrays.asList(chars));
        defaultChar = new TFCharacter("default",
        		"The default character",
        		BitmapFactory.decodeResource(getResources(),getResources().getIdentifier("default_body","drawable",getPackageName())),
    			BitmapFactory.decodeResource(getResources(),getResources().getIdentifier("default_hand","drawable",getPackageName())),
    			BitmapFactory.decodeResource(getResources(),getResources().getIdentifier("default_flipper_original","drawable",getPackageName())),
    			1.0,
    			0.0);
        
        /* Setup the helpers */
        TFHelper[] help = {
    			new TFHelper("Lily",
    		    		"A nice girl you met at Ikea. she isn\'t too great at flipping tables, but she will give you some moral support.",
    		    		BitmapFactory.decodeResource(getResources(),getResources().getIdentifier("lily","drawable",getPackageName())),
    					1.0,
    					50.0),
    			new TFHelper("Part Timer",
    				    "That kid working part time at the Ikea loading docks. He thinks he is all that: always showing off. He isn\'t the best around, but he\'s willing to join your side for a little Reddit Gold.",
    				  	BitmapFactory.decodeResource(getResources(),getResources().getIdentifier("part_timer","drawable",getPackageName())),
    					3.0,
    					150.0),
    			new TFHelper("Ricky Balbonga",
    				    "Never had much, that Ricky. Always had a bone to pick with the tables, ever since growing up on the street. Seeing you walk out of Ikea, a mess of tables flipped behind your back, a spark had ignited in his soul.",
    				    BitmapFactory.decodeResource(getResources(),getResources().getIdentifier("ricky_balbonga","drawable",getPackageName())),
    					10.0,
    					1000.0),
    			new TFHelper("Bob Joel",
    				    "Bob led the world to believe that he didn\'t start the fire. This was a cover up to hide his identity from the tables. Bob DID start the fire, and now he\'s ready to get some more revenge.",
    				    BitmapFactory.decodeResource(getResources(),getResources().getIdentifier("bob_joel","drawable",getPackageName())),
    					20.0,
    					3000.0),
    			new TFHelper("Fairy",
    				    "A magical being has recognized the table menace and is willing to contribute her spells to your cause. Why does she need so much Reddit Gold, and how will she use it? No mere mortal will ever know.",
    				    BitmapFactory.decodeResource(getResources(),getResources().getIdentifier("fairy","drawable",getPackageName())),
    					100.0,
    					50000.0)
    	};
        
        helperObjects = new ArrayList<TFHelper>(Arrays.asList(help));
        
        /* Populate game data with passed in values */
        Bundle b = this.getIntent().getExtras();
        rg = b.getDouble(RG);
        maxLevel = b.getInt(MAXLEVEL);
        fps = b.getDouble(FPS);
        flipStrength = b.getDouble(FLIPSTRENGTH);
        helpers = b.getIntegerArrayList(HELPERS);
        upgradeLevel = b.getInt(UPGRADELEVEL);
        vibrateOnKill = b.getBoolean(VIBRATEOPTION);
        playGameMusic = b.getBoolean(MUSICOPTION);
        b = null;
        
        /* VIBRATION SETUP */
        if (vibrateOnKill) {
        	v = (Vibrator) getBaseContext().getSystemService(Context.VIBRATOR_SERVICE);
        }
        
        /* MUSIC SETUP */
        if (playGameMusic) {
	        mRand = new Random();
	        battleMusic = new ArrayList<Uri>();
	        battleMusic.add(Uri.parse(resUriHead+R.raw.battle1));
	        battleMusic.add(Uri.parse(resUriHead+R.raw.battle2));
	        battleMusic.add(Uri.parse(resUriHead+R.raw.battle3));
	        battleMusic.add(Uri.parse(resUriHead+R.raw.battle4));
	        battleMusic.add(Uri.parse(resUriHead+R.raw.battle5));
	        battleMusic.add(Uri.parse(resUriHead+R.raw.battle6));
	        battleMusic.add(Uri.parse(resUriHead+R.raw.battle7));
	        battleMusic.add(Uri.parse(resUriHead+R.raw.battle8));
	        battleMusic.add(Uri.parse(resUriHead+R.raw.battle9));
	        
	        if (shopMusicPlayer == null) {
		        shopMusicPlayer = new MusicHandler(getBaseContext());
		        shopMusicPlayer.load(R.raw.shop1,true);
	        }
	        
	        if (battleMusicPlayer == null) {
		        battleMusicPlayer = new MusicHandler(getBaseContext());
		        battleMusicPlayer.setSong(battleMusic.get(mRand.nextInt(battleMusic.size())), 
						false,
						musicHandler,
						MUSICDONEMESSAGE);
				battleMusicPlayer.play(FADE_AMT);
	        }
	        else { battleMusicPlayer.resume(FADE_AMT); }
        }
        
        /* FRAGMENT HANDLING */
        characterfrag = new FragmentCharacters();
        gamefrag = new FragmentMainGame();
        helperfrag = new FragmentHelpers();
        
        if(characterfrag == null){
        	Log.d("EPIC FAILURE","FRAG ONE NULL");
        }
        
        //And shoving them inside an array
        fragarr = new Fragment[3];
        fragarr[0] = gamefrag;
        fragarr[1] = characterfrag;
        fragarr[2] = helperfrag;
        
        //Set our custom adapter to a new one, passing in the fragment manager.
        myFragmentPagerAdapter = new MyAdapter(getSupportFragmentManager());
        
        //Setting up the view pager.
        myViewPager = (ViewPager) findViewById(R.id.pager);
        
        //We need to load how many pages can be offscreen, and then set our adapter.
        myViewPager.setOffscreenPageLimit(2); 
        myViewPager.setAdapter(myFragmentPagerAdapter);
        
        //here we set up our ActionBar 
        setupTabs();
        
        myViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
            	if (position == CHARACTER_TAB || position == HELPER_TAB) {
            		if (position == CHARACTER_TAB) { characterfrag.updateGameData(rg); }
            		else { helperfrag.updateGameData(rg); }
            		gamefrag.stopDriver();
            		
            		if (playGameMusic) {
	            		Message m = new Message();
	            		m.what = ONSHOPMESSAGE;
	            		musicHandler.sendMessage(m);
            		}
            	}
            	else {
            		gamefrag.updateGameData(rg, fps, flipStrength, getCurrentCharacter());
            		gamefrag.startDriver();
            		
            		if (playGameMusic) {
	            		Message m = new Message();
	            		m.what = ONGAMEMESSAGE;
	            		musicHandler.sendMessage(m);
            		}
            	}
            	getActionBar().setSelectedNavigationItem(position);
            }
        });
    	
		return true;
	}
	
	private boolean destroy() {
		
		/* Free up references to be garbage collected */
		mRand = null;
		battleMusic = null;
		characterfrag = null;
		gamefrag = null;
		helperfrag = null;
		myViewPager = null;
		myFragmentPagerAdapter = null;
		helpers = null;
		defaultChar = null;
		characters = null;
		helperObjects = null;
		battleMusicPlayer = null;
		shopMusicPlayer = null;
		
		return false;
	}
        
    //THIS IS THE TAB LISTENER STUFF-----------------------------------------------------
	private void setupTabs() {
	   	 final ActionBar actionBar = getActionBar();
	   	 actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	   	 ActionBar.TabListener tabListener = new ActionBar.TabListener() {
	   	   
	   	  @Override
	   	  public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	   	   
	   	  }
	   	 
	   	  @Override
	   	  public void onTabSelected(Tab tab, FragmentTransaction ft) {
	   	    myViewPager.setCurrentItem(tab.getPosition());
	   	  }
	   	   
	   	  @Override
	   	  public void onTabReselected(Tab tab, FragmentTransaction ft) {
	   	    
	   	  }
	    };
	    
	    actionBar.addTab(actionBar.newTab().setText(R.string.game_tab).setTabListener(tabListener));
	    actionBar.addTab(actionBar.newTab().setText(R.string.characters_tab).setTabListener(tabListener));
	    actionBar.addTab(actionBar.newTab().setText(R.string.helpers_tab).setTabListener(tabListener));
	}
	//END OF TAB LISTENER STUFF ------------------------------------------------------
	
	
	//ADAPTER INNER CLASS
	public class MyAdapter extends FragmentPagerAdapter {
		 //Number of tabs/fragments we will have
		 private final int PAGE_COUNT = 3;
		  
		 //Constructor
		 public MyAdapter(FragmentManager fm) {
			 super(fm);
		 }
		  
		 //We're using an array of fragments to keep track of everything,
		 //so getting an item at position i is just the fragment in the frag
		 //array at index i
		 @Override
		 public Fragment getItem(int i) {
			 return fragarr[i];
		 }
		  
		 //Returns the number of pages we have
		 @Override
		 public int getCount() {
			 return PAGE_COUNT;
		 }
		  
		 // Used to populate the tab titles
		 @Override
		 public CharSequence getPageTitle(int position) {
			 switch(position) {
			 	case CHARACTER_TAB:
			 		return getResources().getString(R.string.characters_tab);
			 	case GAME_TAB:
			 		return getResources().getString(R.string.game_tab);
			 	case HELPER_TAB:
			 		return getResources().getString(R.string.helpers_tab);
		  
			 	default:
			 		return null;
			 }
		 }
	}
}