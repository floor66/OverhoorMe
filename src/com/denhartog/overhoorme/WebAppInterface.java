package com.denhartog.overhoorme;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.webkit.JavascriptInterface;

public class WebAppInterface {
	Context mContext;
	Activity a;
	
	WebAppInterface(Context c) {
		mContext = c;
		a = (Activity) mContext;
	}
	
	@JavascriptInterface
	public void openFileBrowser() {
		if(a instanceof MainActivity) {
			if(a.getIntent().getStringExtra(MainActivity.ARG_IMG_URI) != null) {
				new AlertDialog.Builder(mContext)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle(R.string.warning)
					.setMessage(R.string.img_open_are_you_sure)
					.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface Dialog, int which) {
							Intent i = new Intent(mContext, FileSelectionActivity.class);
							mContext.startActivity(i);
						}
					})
					.setNegativeButton(R.string.no, null)
					.show();
			} else {
				Intent i = new Intent(mContext, FileSelectionActivity.class);
				mContext.startActivity(i);
			}
		}
	}
	
	@JavascriptInterface
	public void saveVakjes(String stringifiedJSON) {
		ImageFragment.saveVakjes(mContext, stringifiedJSON);
	}
	
	@JavascriptInterface
	public void rotateImg(String rotation) {
		Log.i("OM", "Android.rotateImg called with "+ rotation);
		AsyncRotation mRot = new AsyncRotation(a, Integer.parseInt(rotation));
		mRot.execute();
	}
	
	@JavascriptInterface
	public void reloadPage() {
		if(a instanceof MainActivity) {
			Fragment frag = ((MainActivity) a).formImageFragment();
			if(frag != null) {
				Log.i("OM", "Reloading ImageFragment from JSInterface call.");
				a.getFragmentManager().beginTransaction().replace(R.id.content_frame, frag).commit();
			}
		}
	}
}
