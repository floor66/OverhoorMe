package com.denhartog.overhoorme;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class FileSelectionActivity extends Activity {
	private static final int READ_REQUEST_CODE = 42;
	private static final int KK_READ_REQUEST_CODE = 43;

	@SuppressLint("InlinedApi")
	protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        
    	Log.i("OM", "Starting FileSelectionActivity.");
    	if(Build.VERSION.SDK_INT < 19) {
	        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("image/*");
			
			startActivityForResult(Intent.createChooser(intent, "Kies een afbeelding:"), READ_REQUEST_CODE);
    	} else {
    		Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
			intent.addCategory(Intent.CATEGORY_OPENABLE);
			intent.setType("image/*");
			
			startActivityForResult(intent, KK_READ_REQUEST_CODE);
    	}
	}
	
	@SuppressLint("NewApi")
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
		super.onActivityResult(requestCode, resultCode, resultData);
		
		if(resultCode == Activity.RESULT_OK && resultData != null) {
			Uri u = resultData.getData();
			
			if(requestCode == KK_READ_REQUEST_CODE) {
				Log.v("OM", "KitKat (>= SDK 19) read request.");
				final int takeFlags = resultData.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION;
				getContentResolver().takePersistableUriPermission(u, takeFlags);
			}
			
			Log.i("OM", "FileSelectionActivity finished with URI: "+ u.toString());
			AsyncTmpImgCreation atic = new AsyncTmpImgCreation(this);
			atic.execute(u);
			Toast.makeText(getApplicationContext(), R.string.loading, Toast.LENGTH_LONG).show();
		}
	}
}
