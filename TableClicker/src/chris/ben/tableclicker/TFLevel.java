package chris.ben.tableclicker;

import java.util.ArrayList;
import java.util.Random;

import android.graphics.Bitmap;

public class TFLevel {
	
	/* The number of enemies in a level */
	private static final int levelSize = 10;
	
	private ArrayList<TableEnemy> level = null;
	private Random rand = null;
	private int curEnemy = 0;
	
	public TFLevel(int levelNum, ArrayList<Bitmap> enemyImages) {
		this.curEnemy = 0;
		this.rand = new Random();
		this.level = new ArrayList<TableEnemy>();
		for (int i = 0; i < levelSize; i++) {
			this.level.add(new TableEnemy((double)rand.nextInt(10*levelNum)+10.0*levelNum,
					enemyImages.get(rand.nextInt(enemyImages.size()))));
		}
	}
	
	/* Return the next enemy to fight */
	public TableEnemy nextEnemy() {
		if (curEnemy == level.size()) {
			return null;
		}
		curEnemy++;
		return level.get(curEnemy - 1);
	}
	
	/* Return the percentage of progress made through the level */
	public double getProgress() {
		double curPlace = (curEnemy == 0) ? 0.0 : (double) (curEnemy - 1);
		return (curPlace / ((double) level.size()));
	}
}