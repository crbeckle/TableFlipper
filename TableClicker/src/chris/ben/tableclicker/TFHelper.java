package chris.ben.tableclicker;

import android.graphics.Bitmap;

public class TFHelper {
	
	private String helperName = null;
	private String helperDescription = null;
	private double helperCost = 0.0;
	private double baseFPS = 0.0;
	private Bitmap helperImage = null;
	
	public TFHelper(String name, String description, Bitmap image, double fps, double cost) {
		this.helperName = name;
		this.helperDescription = description;
		this.helperCost = cost;
		this.baseFPS = fps;
		this.helperImage = image;
	}
	
	public String getName() {
		return this.helperName;
	}
	
	public String getDescription() {
		return this.helperDescription;
	}
	
	public double getCost() {
		return this.helperCost;
	}
	
	public double getFPS() {
		return this.baseFPS;
	}
	
	public Bitmap getImage() {
		return this.helperImage;
	}
}
