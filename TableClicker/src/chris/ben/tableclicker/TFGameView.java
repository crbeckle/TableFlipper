package chris.ben.tableclicker;

import java.util.ArrayList;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

public class TFGameView extends View {
	
	/* Object State Variable */
	private boolean destroyed = false;
	
	/* Screen Horizontal and Vertical Increments */
	private double xInc = 0.0;
	private double yInc = 0.0;
	
	/* Bitmaps and Rect positions of the character and enemy */
	private Bitmap charBody = null;
	private Bitmap charHand = null;
	private Bitmap enemy = null;
	private Bitmap deadEnemy = null;
	private Rect charBodyPos = new Rect();
	private RectF charHandPos = new RectF();
	private Matrix charHandMat = new Matrix();
	private RectF enemyPos = new RectF();
	private Matrix enemyMat = new Matrix();
	private RectF deadEnemyPos = new RectF();
	private Matrix deadEnemyMat = new Matrix();
	
	/* Rectangles used to draw the progress bars */
	private Paint progFramePaint = new Paint();
	private Paint progPaint = new Paint();
	private Rect enemyProgFrame = new Rect();
	private Rect enemyProg = new Rect();
	private Rect levelProgFrame = new Rect();
	private Rect levelProg = new Rect();
	
	/* Text to be drawn */
	private Paint textPaint = new Paint();
	private String currencyHeader = "Reddit Gold: ";
	private String fpsHeader = "FPS: ";
	private String flipStrengthHeader = "Flip Strength: ";
	
	/* Text drawing origins */
	private float currencyX,currencyY,fpsX,fpsY,flipStrengthX,flipStrengthY;
	
	/* Extra game fields passed in */
	private String currency = "";
	private String fps = "";
	private String flipStrength = "";
	
	/* Release as much memory as possible */
	public void destroy() {
		charBody = null;
		charHand = null;
		enemy = null;
		deadEnemy = null;
		charBodyPos = null;
		charHandPos = null;
		enemyPos = null;
		deadEnemyPos = null;
		progFramePaint = null;
		progPaint = null;
		enemyProgFrame = null;
		enemyProg = null;
		levelProgFrame = null;
		levelProg = null;
		textPaint = null;
		currencyHeader = null;
		fpsHeader = null;
		flipStrengthHeader = null;
		currency = null;
		fps = null;
		flipStrength = null;
		destroyed = true;
	}
	
	/* Constructor (two argument so that TFGameViews can be created via XML) */
	public TFGameView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	/* Initialization happens when the size of the layout is set */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w,h,oldw,oldh);
		
		/* Otherwise, this can get called on home button press */
		if (!destroyed) {
			
			/* Calculate dimensions to be used */
			double height = (double) h;
			double width = (double) w;
			xInc = width / 120.0;
			yInc = height / 120.0;
			
			/* Set up defaults for all the bitmaps */
			charBody = BitmapFactory.decodeResource(getResources(),R.drawable.default_body);
			charHand = BitmapFactory.decodeResource(getResources(),R.drawable.default_hand);
			enemy = BitmapFactory.decodeResource(getResources(), R.drawable.enemy_original);
			deadEnemy = null;
			
			/* Find image ratios */
			double charBodyRatio = (double) charBody.getWidth() / (double) charBody.getHeight();
			double charHandRatio = (double) charHand.getWidth() / (double) charHand.getHeight();
			double enemyRatio = (double) enemy.getWidth() / (double) enemy.getHeight();
			
			/* Set up the default image frames */
			double imageBottom = (height * 0.65) - yInc;
			double imageTop = (height * 0.35) + yInc;
			double imageHeight = imageBottom - imageTop;
			
			charBodyPos = new Rect((int)(width*0.1),(int)imageTop,(int)((width*0.1)+(imageHeight*charBodyRatio)),(int)imageBottom);
			charHandPos = new RectF((float)charBodyPos.right,(float)imageTop,(float)(charBodyPos.right+(imageHeight*charHandRatio)),(float)imageBottom);
			enemyPos = new RectF((float)charHandPos.right,(float)imageTop,(float)(charHandPos.right+(imageHeight*enemyRatio)),(float)imageBottom);
			deadEnemyPos = new RectF((float)(width-xInc-imageHeight*enemyRatio),(float)imageTop,(float)(width-xInc),(float)imageBottom);
			
			/* Set up the progress bars */
			enemyProgFrame = new Rect((int)(width*0.375),(int)(height*0.75+yInc),(int)(width*0.625),(int)(height*0.8));
			enemyProg = new Rect(enemyProgFrame.left+(int)xInc,enemyProgFrame.top+(int)yInc,
					enemyProgFrame.right-(int)xInc,enemyProgFrame.bottom-(int)yInc);
			levelProgFrame = new Rect((int)(width*0.375),(int)yInc,(int)(width*0.625),(int)(height*0.05 + yInc));
			levelProg = new Rect(levelProgFrame.left+(int)xInc,levelProgFrame.top+(int)yInc,
					levelProgFrame.right-(int)xInc,levelProgFrame.bottom-(int)yInc);
			progFramePaint.setColor(Color.rgb(17,242,46));
			progFramePaint.setStyle(Paint.Style.STROKE);
			progFramePaint.setStrokeWidth(5);
			progPaint.setColor(Color.rgb(17,242,46));
			progPaint.setStyle(Paint.Style.FILL);
			
			/* Set up text origins and paint object */
			flipStrengthX = (float) xInc;
			flipStrengthY = (float) (height * 0.1);
			fpsX = flipStrengthX;
			fpsY = (float) (height * 0.20 - yInc);
			currencyX = flipStrengthX;
			currencyY = (float) (height * 0.85);
			textPaint.setColor(Color.rgb(17,242,46));
			textPaint.setTextSize((float) 50);
			textPaint.setTextAlign(Paint.Align.LEFT);
			textPaint.setTypeface(Typeface.DEFAULT_BOLD);
		}
	}
	
	/* Update all game drawables */
	public void updateGame(Bitmap chBdy,Bitmap chHnd, float handRot, Bitmap emy, float emyRot, Bitmap dedEmy, float dedEmyRot, 
			double flpStr, double fpSec, double crncy, double lvlPrg, double emyPrg) {
		this.charBody = chBdy;
		this.charHand = chHnd;
		this.enemy = emy;
		this.deadEnemy = dedEmy;
		this.flipStrength = bigNumConverter(flpStr);
		this.fps = bigNumConverter(fpSec);
		this.currency = bigNumConverter(crncy);
		
		// Handle matrices
		this.charHandMat.setRectToRect(new RectF(0,0,charHand.getWidth(),charHand.getHeight()), charHandPos, Matrix.ScaleToFit.CENTER);
		this.charHandMat.postRotate(handRot, charHandPos.centerX(), charHandPos.centerY());
		this.enemyMat.setRectToRect(new RectF(0,0,enemy.getWidth(),enemy.getHeight()), enemyPos, Matrix.ScaleToFit.CENTER);
		this.enemyMat.postRotate(emyRot, enemyPos.centerX(), enemyPos.centerY());
		if (deadEnemy != null) {
			this.deadEnemyMat.setRectToRect(new RectF(0,0,deadEnemy.getWidth(),deadEnemy.getHeight()), deadEnemyPos, Matrix.ScaleToFit.CENTER);
			this.deadEnemyMat.postRotate(dedEmyRot, deadEnemyPos.centerX(), deadEnemyPos.centerY());
		}
		
		int levelProgLen = (int) ((levelProgFrame.right - levelProg.left - (int) xInc) * lvlPrg);
		this.levelProg.set(levelProg.left, levelProg.top, levelProg.left+levelProgLen, levelProg.bottom);
		
		int enemyProgLen = (int) ((enemyProgFrame.right - enemyProg.left - (int) xInc) * emyPrg);
		this.enemyProg.set(enemyProg.left, enemyProg.top, enemyProg.left+enemyProgLen, enemyProg.bottom);
	}
	
	private String bigNumConverter(double bigNum) {
		String result = "";
		long temp = (long) bigNum;
		ArrayList<String> charArray = new ArrayList<String>();
		int digitCount = 0;
		while (temp != 0) {
			charArray.add(""+ (temp % 10));
			temp = temp / 10;
			digitCount++;
		}
		
		if (digitCount < 4) {return result + (long) bigNum;}
		
		for (int i = charArray.size(); i > (charArray.size() - (charArray.size() % 3)); i--) {
			result += charArray.get(i-1);
		}
		
		if (digitCount > 9) {
			result += "B";
		}
		else if (digitCount > 6) {
			result += "M";
		}
		else {
			result += "K";
		}
		
		return result;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		/* Draw the bitmaps */
		canvas.drawBitmap(charBody, null, charBodyPos, null);
		canvas.drawBitmap(charHand, charHandMat, null);
		canvas.drawBitmap(enemy, enemyMat, null);
		if (deadEnemy != null) {
			canvas.drawBitmap(deadEnemy, deadEnemyMat, null);
		}
		
		/* Draw the text */
		canvas.drawText(currencyHeader+currency, currencyX, currencyY, textPaint);
		canvas.drawText(flipStrengthHeader+flipStrength, flipStrengthX, flipStrengthY, textPaint);
		canvas.drawText(fpsHeader+fps, fpsX, fpsY, textPaint);
		
		/* Draw the progress bars */
		canvas.drawRect(enemyProgFrame, progFramePaint);
		canvas.drawRect(enemyProg, progPaint);
		canvas.drawRect(levelProgFrame, progFramePaint);
		canvas.drawRect(levelProg, progPaint);
	}
}
