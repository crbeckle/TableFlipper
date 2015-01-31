package chris.ben.tableclicker;

import android.graphics.Bitmap;

public class TFCharacter {
	
	private String charName = null;
	private String charDescription = null;
	private Bitmap charBody = null;
	private Bitmap charHand = null;
	private Bitmap charWhole = null;
	private double flipStrength = 0.0;
	private double cost = 0.0;

	public TFCharacter(String name, String description, Bitmap chBdy, Bitmap chHnd, Bitmap chWh, double fs, double c) {
		this.charName = name;
		this.charDescription = description;
		this.charBody = chBdy;
		this.charHand = chHnd;
		this.charWhole = chWh;
		this.flipStrength = fs;
		this.cost = c;
	}
	
	public String getDescription() {
		return this.charDescription;
	}
	
	public String getName() {
		return this.charName;
	}
	
	public double getFlipStrength() {
		return this.flipStrength;
	}
	
	public double getCost() {
		return this.cost;
	}
	
	public Bitmap getBody() {
		return this.charBody;
	}
	
	public Bitmap getWholeChar() {
		return this.charWhole;
	}
	
	public Bitmap getHand() {
		return this.charHand;
	}
	
	public float getHandPose(double percentEnemyHp) {
		if (percentEnemyHp == 0.0) {
			return (float) -90;
		}
		else {
			double percentRotate = 1.0 - percentEnemyHp;
			return (float) (percentRotate * -45);
		}
	}
}
