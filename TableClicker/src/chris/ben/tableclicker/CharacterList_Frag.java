package chris.ben.tableclicker;

import java.util.ArrayList;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.OnItemClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class CharacterList_Frag extends Fragment{
	
	private ArrayList<TFCharacter> charList;
	private ArrayList<String> datalist;
	private double currentMoney;
	private int upgradeLevel;
	private ListView character_list;
	private ArrayAdapter<String> arrayAdpt;
	private Context c;
	private TFCharacter selectedChar;
	private int itemSelected = 0;
	
	private TextView charname;
	private TextView char_curmoney;
	private ImageView charimage;
	private TextView charinfo;
	private TextView charfs;
	private TextView charcost;
	private Button purchase;

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		TableClickerActivity act = (TableClickerActivity) getActivity();
		c = getActivity().getApplicationContext();
		
		currentMoney = act.getCurrency();
		upgradeLevel = act.getUpgradeLevel();
		charList = act.getCharacters();
		datalist = new ArrayList<String>();
		for (int i = 0; i < charList.size(); i++) {
			datalist.add(charList.get(i).getName());
		}
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState){
		// Inflate the layout for this fragment
		View rootView = inflater.inflate(R.layout.characterlist_frag, container, false);
		
		//Setting up the listview for this fragment
		character_list = (ListView)rootView.findViewById(R.id.character_list);
		arrayAdpt = new ArrayAdapter<String>(c,R.layout.purchaselist_item,datalist);
		character_list.setAdapter(arrayAdpt);
		
	    //onclick listener for pressing items in the listview
	    character_list.setOnItemClickListener(new OnItemClickListener() {       	
	    	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	    		/* Remember the selected position and character */
	    		itemSelected = position;
	    		selectedChar = charList.get(position);
	    		
	    		// Grab a handle to all of the widgets
	    		charname = (TextView)getActivity().findViewById(R.id.charname);
	    		char_curmoney = (TextView)getActivity().findViewById(R.id.char_curmoney);
	    		charimage = (ImageView)getActivity().findViewById(R.id.charimage);
	    		charinfo = (TextView)getActivity().findViewById(R.id.charinfo);
	    		charfs = (TextView)getActivity().findViewById(R.id.charfs);
	    		charcost = (TextView)getActivity().findViewById(R.id.charcost);
	    		purchase = (Button)getActivity().findViewById(R.id.purchasechar);
	    		
				/* Update the widgets */
				charname.setText(selectedChar.getName());
				char_curmoney.setText(String.format("Your Gold:\n%d",(long)currentMoney));
				charimage.setImageBitmap(selectedChar.getWholeChar());
				charinfo.setText(selectedChar.getDescription());
				charfs.setText(String.format("Flip Strength: %d",(long)selectedChar.getFlipStrength()));
				charcost.setText(String.format("Cost: %d",(long)selectedChar.getCost()));
				if (upgradeLevel >= (position+1)) {
					purchase.setText("Already Purchased");
				}
				else {
					purchase.setText("Purchase");
				}
				
				/* Set the button listener */ 
				purchase.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						TableClickerActivity act = (TableClickerActivity) getActivity();
						if (upgradeLevel >= (itemSelected+1)) {
							Toast.makeText(act.getBaseContext(),
									"Already purchased!", Toast.LENGTH_SHORT).show();
						}
						else if (selectedChar.getCost() > currentMoney) {
							Toast.makeText(act.getBaseContext(), 
									"Insufficient funds!", Toast.LENGTH_SHORT).show();
						}
						else {
							upgradeLevel = itemSelected+1;
							currentMoney -= selectedChar.getCost();
							act.characterUpdate(upgradeLevel,selectedChar.getFlipStrength(),currentMoney);
							purchase.setText("Already purchased");
							char_curmoney.setText(String.format("Your Gold:\n%d",(long)currentMoney));
						}
					}
				});
	    	}

	    });
		
		return rootView;
	}
	
	public void updateGameData(double moneyUpdate) {
		currentMoney = moneyUpdate;
		char_curmoney.setText(String.format("Your Gold:\n%d",(long)currentMoney));
	}
	
	@Override
	public void onResume() {
		super.onResume();
		character_list.performItemClick(character_list.getAdapter().getView(0, null, null), 0, character_list.getItemIdAtPosition(0));
	}
	
}