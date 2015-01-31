package chris.ben.tableclicker;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.Button;

public class InstructionsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);
        
        /* SET UP THE WEB VIEW */
        WebView instrView = (WebView) findViewById(R.id.instr_intructionsview);
        instrView.loadUrl("file:///android_asset/Instructions.html");
        
        /* SET UP THE BUTTON HANDLER */
        Button b_done = (Button) findViewById(R.id.instr_donebutton);
        AdapterView.OnClickListener ocl_done;
        ocl_done = new AdapterView.OnClickListener() {
        	public void onClick(View v) {
        		// Just go back to the title screen
        		finish();
        	}
        };
        b_done.setOnClickListener(ocl_done);
	}
}
