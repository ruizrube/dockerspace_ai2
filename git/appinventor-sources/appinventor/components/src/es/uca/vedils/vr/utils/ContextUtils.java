package es.uca.vedils.vr.utils;

import android.content.ClipData;
import android.content.Context;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.widget.Toast;
import android.content.ClipboardManager;

public class ContextUtils 
{
	public static Context context;
	public static void init(Context c){
		context=c;
	}
	@RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
	public static  void CopytoClip(String x){

        ((ClipboardManager)context.getSystemService(context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("clipboard", x));    

		Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show(); 





	}
	
}
