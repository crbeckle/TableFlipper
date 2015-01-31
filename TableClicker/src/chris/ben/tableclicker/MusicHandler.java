package chris.ben.tableclicker;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

public class MusicHandler {
	
	private MediaPlayer mediaPlayer = null;
	private Context context;
	private int iVolume;

	private final static int INT_VOLUME_MAX = 100;
	private final static int INT_VOLUME_MIN = 0;
	private final static float FLOAT_VOLUME_MAX = 1;
	private final static float FLOAT_VOLUME_MIN = 0;

	public MusicHandler(Context context)
	{
	    this.context = context;
	}
	
	public boolean playing() {
		return mediaPlayer.isPlaying();
	}
	
	public void setSong(Uri u, boolean looping) {
		if (mediaPlayer == null) { 
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		}
		try {
			mediaPlayer.reset();
			mediaPlayer.setLooping(looping);
			mediaPlayer.setDataSource(context, u);
			mediaPlayer.prepare();
		}
		catch (Exception e) {}
	}
	
	public void setSong(Uri u, boolean looping, final Handler onFinishHandler, final int onFinishMessage) {
		this.setSong(u, looping);
		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
	    	@Override
	    	public void onCompletion(MediaPlayer mp) {
	    		Message m = new Message();
	    		m.what = onFinishMessage;
	    		onFinishHandler.sendMessage(m);
	    	}
	    });
	}

	public void load(String path, boolean looping)
	{
	    mediaPlayer = MediaPlayer.create(context, Uri.fromFile(new File(path)));
	    mediaPlayer.setLooping(looping);
	}
	
	public void load(String path, boolean looping, final Handler onFinishHandler, final int onFinishMessage)
	{
	    this.load(path, looping);
	    mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
	    	@Override
	    	public void onCompletion(MediaPlayer mp) {
	    		Message m = new Message();
	    		m.what = onFinishMessage;
	    		onFinishHandler.sendMessage(m);
	    	}
	    });
	}

	public void load(int address, boolean looping)
	{
	    mediaPlayer = MediaPlayer.create(context, address);
	    mediaPlayer.setLooping(looping);
	}
	
	public void load(int address, boolean looping, final Handler onFinishHandler, final int onFinishMessage)
	{
	    this.load(address, looping);
	    mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
	    	@Override
	    	public void onCompletion(MediaPlayer mp) {
	    		Message m = new Message();
	    		m.what = onFinishMessage;
	    		onFinishHandler.sendMessage(m);
	    	}
	    });
	}
	
	public void play(int fadeDuration)
	{
	    //Set current volume, depending on fade or not
	    if (fadeDuration > 0) 
	        iVolume = INT_VOLUME_MIN;
	    else 
	        iVolume = INT_VOLUME_MAX;

	    updateVolume(0);

	    //Play music
	    if(!mediaPlayer.isPlaying()) {
	    	mediaPlayer.seekTo(0);
	    	mediaPlayer.start();
	    }

	    //Start increasing volume in increments
	    if(fadeDuration > 0)
	    {
	        final Timer timer = new Timer(true);
	        TimerTask timerTask = new TimerTask() 
	        {
	            @Override
	            public void run() 
	            {
	                updateVolume(1);
	                if (iVolume == INT_VOLUME_MAX)
	                {
	                    timer.cancel();
	                    timer.purge();
	                }
	            }
	        };

	        // calculate delay, cannot be zero, set to 1 if zero
	        int delay = fadeDuration/INT_VOLUME_MAX;
	        if (delay == 0) delay = 1;

	        timer.schedule(timerTask, delay, delay);
	    }
	}

	public void resume(int fadeDuration)
	{
	    //Set current volume, depending on fade or not
	    if (fadeDuration > 0) 
	        iVolume = INT_VOLUME_MIN;
	    else 
	        iVolume = INT_VOLUME_MAX;

	    updateVolume(0);

	    //Play music
	    if(!mediaPlayer.isPlaying()) mediaPlayer.start();

	    //Start increasing volume in increments
	    if(fadeDuration > 0)
	    {
	        final Timer timer = new Timer(true);
	        TimerTask timerTask = new TimerTask() 
	        {
	            @Override
	            public void run() 
	            {
	                updateVolume(1);
	                if (iVolume == INT_VOLUME_MAX)
	                {
	                    timer.cancel();
	                    timer.purge();
	                }
	            }
	        };

	        // calculate delay, cannot be zero, set to 1 if zero
	        int delay = fadeDuration/INT_VOLUME_MAX;
	        if (delay == 0) delay = 1;

	        timer.schedule(timerTask, delay, delay);
	    }
	}

	public void pause(int fadeDuration)
	{
	    //Set current volume, depending on fade or not
	    if (fadeDuration > 0) 
	        iVolume = INT_VOLUME_MAX;
	    else 
	        iVolume = INT_VOLUME_MIN;

	    updateVolume(0);

	    //Start decreasing volume in increments
	    if(fadeDuration > 0)
	    {
	        final Timer timer = new Timer(true);
	        TimerTask timerTask = new TimerTask() 
	        {
	            @Override
	            public void run() 
	            {   
	                updateVolume(-1);
	                if (iVolume == INT_VOLUME_MIN)
	                {
	                    //Pause music
	                    if (mediaPlayer.isPlaying()) mediaPlayer.pause();
	                    timer.cancel();
	                    timer.purge();
	                }
	            }
	        };

	        // calculate delay, cannot be zero, set to 1 if zero
	        int delay = fadeDuration/INT_VOLUME_MAX;
	        if (delay == 0) delay = 1;

	        timer.schedule(timerTask, delay, delay);
	    }           
	}

	private void updateVolume(int change)
	{
	    //increment or decrement depending on type of fade
	    iVolume = iVolume + change;

	    //ensure iVolume within boundaries
	    if (iVolume < INT_VOLUME_MIN)
	        iVolume = INT_VOLUME_MIN;
	    else if (iVolume > INT_VOLUME_MAX)
	        iVolume = INT_VOLUME_MAX;

	    //convert to float value
	    float fVolume = 1 - ((float) Math.log(INT_VOLUME_MAX - iVolume) / (float) Math.log(INT_VOLUME_MAX));

	    //ensure fVolume within boundaries
	    if (fVolume < FLOAT_VOLUME_MIN)
	        fVolume = FLOAT_VOLUME_MIN;
	    else if (fVolume > FLOAT_VOLUME_MAX)
	        fVolume = FLOAT_VOLUME_MAX;     

	    mediaPlayer.setVolume(fVolume, fVolume);
	}
	
	public void stop(int fadeDuration)
	{
	    try {
	        // Set current volume, depending on fade or not
	        if (fadeDuration > 0)
	            iVolume = INT_VOLUME_MAX;
	        else
	            iVolume = INT_VOLUME_MIN;

	        updateVolume(0);

	        // Start decreasing volume in increments
	        if (fadeDuration > 0)
	        {
	            final Timer timer = new Timer(true);
	            TimerTask timerTask = new TimerTask()
	            {
	                @Override
	                public void run()
	                {
	                    updateVolume(-1);
	                    if (iVolume == INT_VOLUME_MIN)
	                    {
	                        // Pause music
	                        mediaPlayer.stop();
	                        timer.cancel();
	                        timer.purge();
	                    }
	                }
	            };

	            // calculate delay, cannot be zero, set to 1 if zero
	            int delay = fadeDuration / INT_VOLUME_MAX;
	            if (delay == 0)
	                delay = 1;

	            timer.schedule(timerTask, delay, delay);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	}

	public void stopAndRelease(int fadeDuration) {
	    try {
	        final Timer timer = new Timer(true);
	        TimerTask timerTask = new TimerTask()
	        {
	            @Override
	            public void run()
	            {
	                updateVolume(-1);
	                if (iVolume == INT_VOLUME_MIN)
	                {
	                    // Stop and Release player after Pause music
	                    mediaPlayer.stop();
	                    mediaPlayer.release();
	                    mediaPlayer = null;
	                    timer.cancel();
	                    timer.purge();
	                }
	            }
	        };

	        timer.schedule(timerTask, fadeDuration);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
}
