package com.denhartog.overhoorme;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends Activity {
	private String[] menuOptions;
	private DrawerLayout drawerMenu;
	private ListView drawerMenuList;
	private CharSequence drawerTitle;
	private CharSequence currTitle;
	private ActionBarDrawerToggle drawerToggle;
	private Integer pausedOn = null;
	
	//Constants
	public static final String ARG_IMG_URI = "img_uri";
	public static final String rotatedImgDir = "/Pictures/OverhoorMe";
	public static final String tmpImgDirName = "imgtmp";
	public static final String tmpImgFileName = "om_tmp.png";
	public static final int PAGE_HOME = 0;
	public static final int PAGE_EDIT = 1;
	public static final int PAGE_HELP = 2;
	public static final int PAGE_ABOUT = 3;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        drawerTitle = getResources().getString(R.string.drawer_title);
        currTitle = getTitle();
        menuOptions = getResources().getStringArray(R.array.menu_options);
        drawerMenu = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerMenuList = (ListView) findViewById(R.id.left_drawer);
        
        drawerMenuList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, menuOptions));
        drawerMenuList.setOnItemClickListener(new DrawerItemClickListener());
        
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        drawerToggle = new ActionBarDrawerToggle(this, drawerMenu, R.string.drawer_open, R.string.drawer_close) {
        	public void onDrawerClosed(View view) {
        		super.onDrawerClosed(view);
        		getActionBar().setTitle(currTitle);
                invalidateOptionsMenu();
        	}
        	
        	public void onDrawerOpened(View view) {
        		super.onDrawerOpened(view);
        		getActionBar().setTitle(drawerTitle);
                invalidateOptionsMenu();
        	}
        };
        
        drawerMenu.setDrawerListener(drawerToggle);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		Fragment frag = getFragmentManager().findFragmentById(R.id.content_frame);
		if(frag != null) {
			getFragmentManager().beginTransaction().remove(frag).commit();
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
    	if(pausedOn != null) {
			Log.i("OM", "onStart calling selectItem from pause.");
			selectItem(pausedOn);
		} else {
			selectItem(getIntent().getStringExtra(ARG_IMG_URI) != null ? PAGE_EDIT : PAGE_HOME);
		}
	}
	
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			selectItem(position);
		}
	}
	
	//Switch fragments & change title & mark item as checked when menu item is tapped
	private void selectItem(int position) {
		Fragment frag = null;
		pausedOn = position;
		
		switch(position) {
		case PAGE_HOME:
			frag = new HomeFragment();
			break;
		case PAGE_EDIT:
			frag = formImageFragment();
			break;
		default:
			frag = new HomeFragment();
			break;
		}
		
		if(frag != null) {
			Log.i("OM", "MainActivity going to load a "+ frag.getClass().getSimpleName());
			getFragmentManager().beginTransaction().replace(R.id.content_frame, frag).commit();
		}
		
		drawerMenuList.setItemChecked(position, true);
		setTitle(menuOptions[position]);
		drawerMenu.closeDrawer(drawerMenuList);
	}
	
	public Fragment formImageFragment() {
		Fragment frag = null;
		Intent in = getIntent();

		//Let's check for an existing save with this uri
		if(in.getStringExtra(ARG_IMG_URI) != null) {
			try {
				SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
				JSONArray arr = null;
				
				String str = prefs.getString(getString(R.string.preference_vakjes_array), null);
				if(str != null) {
					arr = new JSONArray(str);
					int len = arr.length() > 0 ? arr.length() : 0;
					if(len > 0) {
						for(int i = 0; i < len; i++) {
							JSONObject obj = arr.getJSONObject(i);
							String[] tmp = in.getStringExtra(ARG_IMG_URI).split("/");
							String imgId = tmp[tmp.length - 1].replace("image:", "");
							Log.v("OM", "Looking for '"+ obj.getString("imgId") +"' with ARG_IMG_URI imgId '"+ imgId +"'");
							if(obj.getString("imgId").equals(imgId)) {
								Log.v("OM", "Save match found for save '"+ obj.getString("imgId") +"' with ARG_IMG_URI imgId '"+ imgId +"'");
								frag = new ImageFragment(Uri.parse(in.getStringExtra(ARG_IMG_URI)), obj);
								break;
							}
						}
					}
				}
			} catch(JSONException e) {
				Log.e("OM", "Loading SavedPreferences failed.");
			}
			
			if(frag == null) {
				Log.i("OM", "No save found, starting new ImageFragment with "+ in.getStringExtra(ARG_IMG_URI));
				frag = new ImageFragment(Uri.parse(in.getStringExtra(ARG_IMG_URI)));
			}
		} else {
			//If all else fails, return to file browser for new img
			Intent i = new Intent(getApplicationContext(), FileSelectionActivity.class);
			startActivity(i);
		
			return null;
		}
		
		return frag;
	}

	@Override
	protected void onPostCreate(Bundle savedInstance) {
		super.onPostCreate(savedInstance);
		drawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		drawerToggle.onConfigurationChanged(newConfig);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(drawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	//Menu key = toggle nav
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent e) {
	    if(keyCode == KeyEvent.KEYCODE_MENU) {
		    if(drawerMenu.isDrawerOpen(drawerMenuList)) {
		    	drawerMenu.closeDrawer(drawerMenuList);
			    return true;
		    } else {
		    	drawerMenu.openDrawer(drawerMenuList);
			    return true;
		    }
	    }
	    
	    return super.onKeyDown(keyCode, e);
	}
	
	@Override
	public void onBackPressed() {
	    if(drawerMenu.isDrawerOpen(drawerMenuList)) {
	    	drawerMenu.closeDrawer(drawerMenuList);
	    	
		    return;
	    }
	    
	    //Go from ImageFragment to HomeFragment with back key
	    Fragment f = getFragmentManager().findFragmentById(R.id.content_frame);
	    if(f instanceof ImageFragment) {
	    	selectItem(0);
			return;
	    }

	    super.onBackPressed();
	    return;
	}
	
	public void setTitle(CharSequence title) {
		currTitle = title;
		getActionBar().setTitle(title);
	}
}

