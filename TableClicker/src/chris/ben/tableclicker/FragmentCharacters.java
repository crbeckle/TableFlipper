package chris.ben.tableclicker;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FragmentCharacters extends Fragment{
	
	private CharacterList_Frag frag1;
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
	}
	
	public void updateGameData(double moneyUpdate) {
		frag1.updateGameData(moneyUpdate);
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState){
		// Inflate the layout for this fragment
		View temp_view = inflater.inflate(R.layout.purchase_frag_splitter, container, false);
		
		
		frag1 = (CharacterList_Frag) getChildFragmentManager().findFragmentById(R.id.characterlistfrag_layout);
	    CharacterSheet_Frag frag2 = (CharacterSheet_Frag) getChildFragmentManager().findFragmentById(R.id.charactersheetfrag_layout);
	    if (null == frag1) {
	        frag1 = new CharacterList_Frag();
	        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
	        transaction.add(R.id.list_frag, frag1).addToBackStack(null).commit();
	    }

	     if (null == frag2) {
	        frag2 = new CharacterSheet_Frag();
	        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();

	        transaction.add(R.id.infopage_frag, frag2).addToBackStack(null).commit();
	    }
		
	    return temp_view;
	}
}
