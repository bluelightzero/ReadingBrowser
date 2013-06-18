package me.bluelightzero.readingbrowser;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

import android.preference.PreferenceActivity;
import android.content.Intent;

public class SettingsActivity extends PreferenceActivity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
}
