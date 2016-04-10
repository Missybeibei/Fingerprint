package ma.fprint;

//import com.android.settings.R;
import ma.fprint.R;
import android.app.Activity; 
import android.app.AlertDialog;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;

public class Prompt {               
	public static void e(final Activity atv, String content) {   
		 final AlertDialog dlg = new AlertDialog.Builder(atv).create();
		 dlg.show();		
		 Window wnd = dlg.getWindow();		
		 wnd.setContentView(R.layout.ma_dialog);
		 TextView textv = (TextView) wnd.findViewById(R.id.ma_dlg_content);
		 textv.setText(content);
		 Button ok = (Button) wnd.findViewById(R.id.ma_dlg_confirm);
		 ok.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				atv.finish();				
			}
		});	 	        
	}
	
	// warnning
	public static void w(final Activity atv, String content) { 
		 final AlertDialog dlg = new AlertDialog.Builder(atv).create();
		 dlg.show();
		 Window wnd = dlg.getWindow();		
		 wnd.setContentView(R.layout.ma_dialog);
		 ImageView iv = (ImageView) wnd.findViewById(R.id.ma_dlg_ticon);
		 iv.setBackgroundResource(R.drawable.ic_ma_warn);
		 TextView tt = (TextView) wnd.findViewById(R.id.ma_dlg_ttext);
		 tt.setText(atv.getResources().getString(R.string.ma_dlg_warn));
		 TextView tv = (TextView) wnd.findViewById(R.id.ma_dlg_content);
		 tv.setText(content);
		 Button ok = (Button) wnd.findViewById(R.id.ma_dlg_confirm);
		 ok.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				dlg.dismiss();
			}
		});		  
	}	 
}



