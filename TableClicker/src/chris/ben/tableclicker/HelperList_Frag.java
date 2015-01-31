package chris.ben.tableclicker;

import java.util.ArrayList;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class HelperList_Frag extends Fragment{
	
	private ArrayList<TFHelper> helperList;
	private ArrayList<Integer> helperData;
	private ArrayList<String> datalist;
	private double currentMoney;
	private double currentFPS;
	private ListView helper_list;
	private ArrayAdapter<String> arrayAdpt;
	private Context c;
	private TFHelper selectedHelper;
	private int itemSelected = 0;
	
	private TextView helpername;
	private TextView helperlevel;
	private TextView helper_curmoney;
	private ImageView helperimage;
	private TextView helperinfo;
	private TextView helperfps;
	private TextView helpercost;
	private Button purchase;

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		TableClickerActivity act = (TableClickerActivity) getActivity();
		c = getActivity().getApplicationContext();
		
		currentMoney = act.getCurrency();
		currentFPS = act.getFPS();
		helperList = act.getHelperObjects();
		helperData = act.getHelpers();
		if (helperData != null) { Log.d("Helper List", "From activity: "+helperData.toString()); }
		if (helperData == null || helperData.size() != helperList.size()) { 
			helperData = new ArrayList<Integer>();
			for (int h = 0; h < helperList.size(); h++) {
				helperData.add(0);
			}
			Log.d("Helper List", "Constructed new helper levels: "+helperData.toString());
		}
		datalist = new ArrayList<String>();
		for (int i = 0; i < helperList.size(); i++) {
			datalist.add(helperList.get(i).getName());
		}
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState){
		// Inflate the layout for this fragment
		View rootView = inflater.inflate(R.layout.helperlist_frag, container, false);
		
		//Setting up the listview for this fragment
		helper_list = (ListView)rootView.findViewById(R.id.helper_list);
		arrayAdpt = new ArrayAdapter<String>(c,R.layout.purchaselist_item,datalist);
		helper_list.setAdapter(arrayAdpt);
		
	    //onclick listener for pressing items in the listview
	    helper_list.setOnItemClickListener(new OnItemClickListener() {       	
	    	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	    		/* Remember the selected position and character */
	    		itemSelected = position;
	    		selectedHelper = helperList.get(position);
	    		
	    		// Grab a handle to all of the widgets
	    		helpername = (TextView)getActivity().findViewById(R.id.helpername);
	    		helperlevel = (TextView)getActivity().findViewById(R.id.helperlevel);
	    		helper_curmoney = (TextView)getActivity().findViewById(R.id.helper_curmoney);
	    		helperimage = (ImageView)getActivity().findViewById(R.id.helperimage);
	    		helperinfo = (TextView)getActivity().findViewById(R.id.helperinfo);
	    		helperfps = (TextView)getActivity().findViewById(R.id.helperfps);
	    		helpercost = (TextView)getActivity().findViewById(R.id.helpercost);
	    		purchase = (Button)getActivity().findViewById(R.id.purchasehelper);
	    		
				/* Update the widgets */
				helpername.setText(selectedHelper.getName());
				helperlevel.setText("Level "+helperData.get(itemSelected));
				helper_curmoney.setText(String.format("Your Gold:\n%d",(long)currentMoney));
				helperimage.setImageBitmap(selectedHelper.getImage());
				helperinfo.setText(selectedHelper.getDescription());
				// TODO Update FPS and Cost properly
				helperfps.setText(String.format("FPS: %d",
						(long)(selectedHelper.getFPS()*(helperData.get(itemSelected)+1))));
				helpercost.setText(String.format("Cost: %d",
						(long)(selectedHelper.getCost()*(helperData.get(itemSelected)+1))));
				purchase.setText("Purchase");
				
				/* Set the button listener */ 
				purchase.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						TableClickerActivity act = (TableClickerActivity) getActivity();
						double calcCost = selectedHelper.getCost() * (helperData.get(itemSelected) + 1);
						if (calcCost > currentMoney) {
							Toast.makeText(act.getBaseContext(), 
									"Insufficient funds!", Toast.LENGTH_SHORT).show();
						}
						else {
							currentFPS += (selectedHelper.getFPS() * (helperData.get(itemSelected) + 1));
							currentMoney -= calcCost;
							helperData.set(itemSelected, Integer.valueOf(helperData.get(itemSelected)+1));
							act.helperUpdate(helperData,currentFPS,currentMoney);
							helperlevel.setText("Level "+helperData.get(itemSelected));
							helper_curmoney.setText(String.format("Your Gold:\n%d",(long)currentMoney));
							helperfps.setText(String.format("FPS: %d",
									(long)(selectedHelper.getFPS()*(helperData.get(itemSelected)+1))));
							helpercost.setText(String.format("Cost: %d",
									(long)(selectedHelper.getCost()*(helperData.get(itemSelected)+1))));
						}
					}
				});
	    	}

	    });
		
		return rootView;
	}
	
	public void updateGameData(double moneyUpdate) {
		currentMoney = moneyUpdate;
		helper_curmoney.setText(String.format("Your Gold:\n%d",(long)currentMoney));
	}
	
	@Override
	public void onResume() {
		super.onResume();
		helper_list.performItemClick(helper_list.getAdapter().getView(0, null, null), 0, helper_list.getItemIdAtPosition(0));
	}
}
