package chris.ben.tableclicker;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class CharacterSheet_Frag extends Fragment{

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState){
		// Inflate the layout for this fragment
		View rootView = inflater.inflate(R.layout.charactersheet_frag, container, false);
		return rootView;
	}

	
}
