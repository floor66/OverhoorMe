package com.denhartog.overhoorme;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

public class AsyncRotation extends AsyncTask<Void, Integer, Uri> {
	private Activity currActivity;
	private int mRotation;
	private WeakReference<Bitmap> preImg;
	private WeakReference<Bitmap> rotatedImg;
	
	public AsyncRotation(Activity a, int rotation) {
		currActivity = a;
		mRotation = rotation;
	}
	
	@Override
	protected Uri doInBackground(Void... params) {
		Point size = new Point();
		File resultFile;
		
		System.gc();
		File mDir = new ContextWrapper(currActivity).getDir(MainActivity.tmpImgDirName, Context.MODE_PRIVATE);
		mDir.mkdirs();

		File tmpImgFile = new File(mDir, MainActivity.tmpImgFileName);
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inJustDecodeBounds = true;
		try {
			new WeakReference<Bitmap>(BitmapFactory.decodeFile(tmpImgFile.getAbsolutePath(), opt));
		} catch (Exception e) {
			Log.e("OM", "AsyncRotation | Failed to open tmp img.");
			e.printStackTrace();
			return null;
		}
		
		currActivity.getWindowManager().getDefaultDisplay().getSize(size);
		Log.v("OM", "AsyncRotation | outWidth: "+ Integer.toString(opt.outWidth) +", outHeight: "+ Integer.toString(opt.outHeight));
		Log.v("OM", "AsyncRotation | size.x: "+ Integer.toString(size.x) +", size.y: "+ Integer.toString(size.y));
		int sampleSize = 1;
		if(opt.outWidth > size.x || opt.outHeight > size.y) {
			Log.i("OM", "AsyncRotation | Resize is in order.");
			int halfWidth = (opt.outWidth / 2);
			int halfHeight = (opt.outHeight / 2);
			
			while(((halfWidth / sampleSize) > size.x) || ((halfHeight / sampleSize) > size.y)) {
				sampleSize *= 2;
			}
			
			Log.v("OM", "AsyncRotation | sampleSize: "+ Integer.toString(sampleSize));
		} else {
			Log.v("OM", "AsyncRotation | No need to resize.");
		}
		
		opt.inSampleSize = sampleSize;
		opt.inJustDecodeBounds = false;
		try {
			preImg = new WeakReference<Bitmap>(BitmapFactory.decodeFile(tmpImgFile.getAbsolutePath(), opt));
		} catch(Exception e) {
			e.printStackTrace();
			preImg = null;
		}
		
		if(preImg == null) {
			Log.e("OM", "AsyncRotation | Failed to open tmp img.");
			return null;
		}
		
		Matrix rotateMatrix = new Matrix();
		rotateMatrix.postRotate(mRotation);
		rotatedImg = new WeakReference<Bitmap>(Bitmap.createBitmap(preImg.get(), 0, 0, opt.outWidth, opt.outHeight, rotateMatrix, true));
		
		String dir = Environment.getExternalStorageDirectory().toString();
		mDir = new File(dir + MainActivity.rotatedImgDir);
		mDir.mkdirs();
		
		//Bugs occur when this folder doesn't exist..
		File tDir = new File(dir +"/DCIM/Camera");
		tDir.mkdirs();
		
		Random r = new Random();
		int n = 1000;
		n = r.nextInt(n);
		resultFile = new File(mDir, Integer.toString(n) +"_gedraaid.png");
		
		try {
			FileOutputStream out = new FileOutputStream(resultFile);
			rotatedImg.get().compress(Bitmap.CompressFormat.PNG, 100, out);
			out.flush();
			out.close();
		} catch(IOException e) {
			Log.e("OM", "AsyncRotation | Failed to write "+ resultFile.getAbsolutePath());
			e.printStackTrace();
			
			return null;
		}
		
		Log.i("OM", "AsyncRotation | Rotation successful!");
		Uri result = ImageFragment.getImageContentUri(currActivity, resultFile);
		MediaScannerWrapper m = new MediaScannerWrapper(currActivity, resultFile.getAbsolutePath(), "image/png");
		m.scan();
		System.gc();
		return result;
	}
	
	protected void onPostExecute(Uri result) {
		ImageFragment.onImageRotate(currActivity, result);
	}
}
