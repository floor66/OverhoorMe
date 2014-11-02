package com.denhartog.overhoorme;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class AsyncTmpImgCreation extends AsyncTask<Uri, Integer, Uri> {
	private Activity currActivity;
	private Uri imgUri;
	
	public AsyncTmpImgCreation(Activity a) {
		currActivity = a;
	}
	
	@Override
	protected Uri doInBackground(Uri... params) {
		Bitmap grabImg = null;
		Uri tmpImgUri = null;
		InputStream ips;
		Point size = new Point();
		imgUri = params[0];
		
		System.gc();
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inJustDecodeBounds = true;
		try {
			ips = currActivity.getContentResolver().openInputStream(imgUri);
			BitmapFactory.decodeStream(ips, null, opt);
			ips.close();
		} catch (FileNotFoundException e) {
			Log.e("OM", "AsyncTmpImgCreation | Failed to open InputStream to get to "+ imgUri.toString());
			e.printStackTrace();
			
			return null;
		} catch(IOException e) {
			Log.e("OM", "AsyncTmpImgCreation | Failed to open InputStream to get to "+ imgUri.toString());
			e.printStackTrace();
			
			return null;
		}
		
		currActivity.getWindowManager().getDefaultDisplay().getSize(size);
		Log.v("OM", "AsyncTmpImgCreation | outWidth: "+ Integer.toString(opt.outWidth) +", outHeight: "+ Integer.toString(opt.outHeight));
		Log.v("OM", "AsyncTmpImgCreation | size.x: "+ Integer.toString(size.x) +", size.y: "+ Integer.toString(size.y));
		int sampleSize = 1;
		if(opt.outWidth > size.x || opt.outHeight > size.y) {
			Log.i("OM", "AsyncTmpImgCreation | Resize is in order.");
			int halfWidth = (opt.outWidth / 2);
			int halfHeight = (opt.outHeight / 2);
			
			while(((halfWidth / sampleSize) > size.x) || ((halfHeight / sampleSize) > size.y)) {
				sampleSize *= 2;
			}
			
			Log.v("OM", "AsyncTmpImgCreation | sampleSize: "+ Integer.toString(sampleSize));
		} else {
			Log.v("OM", "AsyncTmpImgCreation | No need to resize.");
		}
		
		opt.inSampleSize = (sampleSize > 1 ? sampleSize - 1 : sampleSize);
		opt.inJustDecodeBounds = false;
		try {
			ips = currActivity.getContentResolver().openInputStream(imgUri);
			grabImg = BitmapFactory.decodeStream(ips, null, opt);
			ips.close();
		} catch(Exception e) {
			e.printStackTrace();
			grabImg = null;
		}
		
		if(grabImg == null) {
			Log.e("OM", "AsyncTmpImgCreation | Failed to grab image from "+ imgUri.toString());
			Toast.makeText(currActivity, R.string.loading_failed, Toast.LENGTH_SHORT).show();
			return null;
		}
		
		File mDir = new ContextWrapper(currActivity).getDir(MainActivity.tmpImgDirName, Context.MODE_PRIVATE);
		mDir.mkdirs();
		File tmpImgFile = new File(mDir, MainActivity.tmpImgFileName);
		
		try {
			FileOutputStream out = new FileOutputStream(tmpImgFile);
			grabImg.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.flush();
			out.close();
		} catch(FileNotFoundException e) {
			Log.e("OM", "AsyncTmpImgCreation | tmpImgFile not properly situated.");
			e.printStackTrace();
			
			return null;
		} catch (IOException e) {
			Log.e("OM", "AsyncTmpImgCreation | Failed to write "+ tmpImgFile.getAbsolutePath());
			e.printStackTrace();
			
			return null;
		}
		
		grabImg.recycle();
		tmpImgUri = Uri.parse(tmpImgFile.getAbsolutePath());
		Log.i("OM", "AsyncTmpImgCreation | tmpImgUri "+ tmpImgUri.toString() +" has been made from "+ imgUri.toString());
		return tmpImgUri;
	}
	
	protected void onPostExecute(Uri result) {
		ImageFragment.onTmpImageCreate(currActivity, imgUri);
	}
}
