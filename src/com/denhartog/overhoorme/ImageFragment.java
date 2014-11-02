package com.denhartog.overhoorme;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class ImageFragment extends Fragment {
	private Uri imgUri = null;
	private Uri tmpImgUri = null;
	private JSONObject argObj = null;
	private WebView web = null;
	
	public ImageFragment(Uri u, JSONObject o) {
		argObj = o;
		imgUri = u;
	}
	
	public ImageFragment(Uri u) {
		imgUri = u;
	}
	
	@SuppressLint({ "SetJavaScriptEnabled", "NewApi" })
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	super.onCreateView(inflater, container, savedInstanceState);

		File mDir = new ContextWrapper(getActivity()).getDir(MainActivity.tmpImgDirName, Context.MODE_PRIVATE);
		mDir.mkdirs();
		File tmpImgFile = new File(mDir, MainActivity.tmpImgFileName);
		tmpImgUri = Uri.fromFile(tmpImgFile);

		if(imgUri == null || tmpImgUri == null) {
			Log.e("OM", "ImageFragment failed to assign imgUri.");
    		return null;
    	}

		View root = inflater.inflate(R.layout.fragment_image, container, false);
		web = (WebView) root.findViewById(R.id.webView1);
		web.setWebViewClient(new WebViewClient() {
			public void onPageFinished(WebView view, String url) {
				if(tmpImgUri == null) {
					Log.e("OM", "ImageFragment failed to load tmpImgUri!");
					return;
				}
				
				if(argObj == null) {
					try {
						argObj = new JSONObject();
						argObj.put("uri", imgUri.toString());
					} catch(JSONException e) {
						e.printStackTrace();
						Log.e("OM", "ImageFragment failed form argObj.");
						return;
					}
				}
				
				try {
					argObj.put("tmpImgUri", tmpImgUri);
					web.loadUrl("javascript:init(JSON.parse('"+ argObj.toString() +"'));");
					Log.i("OM", "ImageFragment loaded with argObj.uri: "+ argObj.getString("uri"));
					Log.v("OM", "ImageFragment loaded with argObj: "+ argObj.toString());
				} catch (JSONException e) {
					Log.e("OM", "Failed to load ImageFragment with argObj.");
					e.printStackTrace();
				}
			}
		});
		web.setWebChromeClient(new WebChromeClient());
		web.getSettings().setJavaScriptEnabled(true);
		web.getSettings().setSupportZoom(true);
		web.getSettings().setBuiltInZoomControls(true);
		web.addJavascriptInterface(new WebAppInterface(root.getContext()), "Android");

		return root;
    }
	
	@Override
	public void onStart() {
		super.onStart();
		
		web.loadUrl("file:///android_asset/edit.html");
	}
	
	public static void saveVakjes(Context c, String stringifiedJSON) {
		Activity a = (Activity) c;

		try {
			JSONObject result = new JSONObject(stringifiedJSON);
			SharedPreferences prefs = a.getPreferences(Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = prefs.edit();
			JSONArray arr = null;
			
			String str = prefs.getString(a.getString(R.string.preference_vakjes_array), null);
			if(str != null) {
				arr = new JSONArray(str);
				
				int len = arr.length() > 0 ? arr.length() : 0;
				if(len > 0) {
					for(int i = 0; i < len; i++) {
						JSONObject obj = arr.getJSONObject(i);
						if(obj.getString("uri").equals(result.getString("uri"))) {
							arr.put(i, result);
							editor.putString(a.getString(R.string.preference_vakjes_array), arr.toString());
							editor.commit();
							Log.i("OM", "Saved vakjes in SharedPreferences for: "+ result.getString("imgId"));
							Log.v("OM", "Saved vakjes in SharedPreferences for: "+ result.toString());

							Toast.makeText(a.getApplicationContext(), R.string.save_success, Toast.LENGTH_SHORT).show();
							return;
						}
					}
				}
			} else {
				Log.e("OM", "SharedPreferences preference_vakjes_array is empty. Creating!");
				arr = new JSONArray();
			}
			
			arr.put(result);
			editor.putString(a.getString(R.string.preference_vakjes_array), arr.toString());
			editor.commit();
			
			Log.i("OM", "Saved vakjes in SharedPreferences for: "+ result.getString("imgId"));
			Log.v("OM", "Saved vakjes in SharedPreferences for: "+ result.toString());
			Toast.makeText(a.getApplicationContext(), R.string.save_success, Toast.LENGTH_SHORT).show();
		} catch(Exception e) {
			Log.e("OM", "Saving failed: "+ e.getMessage());
			e.printStackTrace();
			Toast.makeText(a.getApplicationContext(), R.string.save_error, Toast.LENGTH_SHORT).show();
		}
	}
	
	public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Uri u;
        Cursor cursor = context.getContentResolver().query(
			MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
			new String[] { MediaStore.Images.Media._ID },
			MediaStore.Images.Media.DATA + "=? ",
			new String[] { filePath },
			null
        );
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            u = Uri.parse("content://media/external/images/media/" + Integer.toString(id));
            Log.v("OM", "Got img uri for "+ filePath +" from MediaStore. _ID: "+ Integer.toString(id) +", URI: "+ u.toString());
            return u;
        } else {
        	if(imageFile.exists()) {
        		ContentValues values = new ContentValues();
        		values.put(MediaStore.Images.Media.DATA, filePath);
        		u = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        		Log.v("OM", "Put image into MediaStore. filePath: "+ filePath +", URI: "+ u.toString());
        		return u;
        	}
            return null;
        }
    }
	
	public static void onImageRotate(Activity a, Uri u) {
		if(u != null) {
			Toast.makeText(a.getApplicationContext(), R.string.rotate_saved, Toast.LENGTH_SHORT).show();
			a.getIntent().putExtra(MainActivity.ARG_IMG_URI, u.toString());
		} else {
			Toast.makeText(a.getApplicationContext(), R.string.rotate_failed, Toast.LENGTH_SHORT).show();
		}

		Log.v("OM", "Reloading ImageFragment post-rotate.");
		AsyncTmpImgCreation atic = new AsyncTmpImgCreation(a);
		atic.execute(u);
	}
	
	public static void onTmpImageCreate(Activity a, Uri fromUri) {
		if(fromUri == null) {
			return;
		}
		
		Intent i = new Intent(a, MainActivity.class);
		try {
			i.putExtra(MainActivity.ARG_IMG_URI, URLDecoder.decode(fromUri.toString(), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		Log.v("OM", "Loading MainActivity after tmp image was created.");
		a.startActivity(i);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		web.clearCache(true);
		web.destroy();
		argObj = null;
		imgUri = null;
		tmpImgUri = null;
	}
}
