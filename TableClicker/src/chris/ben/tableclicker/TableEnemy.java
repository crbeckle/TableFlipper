package chris.ben.tableclicker;

import android.graphics.Bitmap;

public class TableEnemy {
	private double maxHP = 0.0;
	private double curHP = 0.0;
	private double goldReward = 0.0;
	private Bitmap enemy = null;
	
	public TableEnemy(double maxHP, Bitmap enemy) {
		this.maxHP = this.curHP = this.goldReward = maxHP;
		this.enemy = enemy;
	}
	
	/* Attack with the given damage and return the remaining HP percentage */
	public double attack(double damage) {
		if ((curHP - damage) < 0.0) {
			curHP = 0.0;
		}
		else { curHP -= damage; }
		return (curHP / maxHP);
	}
	
	public double getReward() {
		return goldReward;
	}
	
	public double getMaxHp() {
		return maxHP;
	}
	
	public Bitmap getEnemy() {
		return this.enemy;
	}
	
	public float getPose() {
		if (curHP == 0) {
			return (float) 180;
		}
		else {
			double percentRotate = 1.0 - (curHP / maxHP);
			return (float) (percentRotate * 45);
		}
	}
}