package com.denhartog.overhoorme;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

//Fragments are loaded into the displaying part of the drawer layout
public class HomeFragment extends Fragment {
	private WebView web;
	
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_home, container, false);
		
		web = (WebView) root.findViewById(R.id.webView1); 
		web.getSettings().setJavaScriptEnabled(true);
		web.addJavascriptInterface(new WebAppInterface(root.getContext()), "Android");
		
		return root;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		web.loadUrl("file:///android_asset/index.html");
	}
}

