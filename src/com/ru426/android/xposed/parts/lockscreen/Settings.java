package com.ru426.android.xposed.parts.lockscreen;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import com.ru426.android.xposed.parts.lockscreen.util.XUtil;
import com.ru426.android.xposed.parts.lockscreen.view.XExtendsSeekBar;
import com.ru426.android.xposed.parts.lockscreen.view.XExtendsSeekBar.OnXExtendsSeekBarChangeListener;
import com.ru426.android.xposed.parts.lockscreen.view.XExtendsSeekBarFloat;

public class Settings extends PreferenceActivity {
	private static Context mContext;
	private static SharedPreferences prefs;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mContext = this;
		prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		if(prefs.getBoolean(getString(R.string.ru_use_light_theme_key), false)){
			setTheme(android.R.style.Theme_DeviceDefault_Light);
		}
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings_fragment_base);
	    init();
	    initOption();
	}

	@Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch(item.getItemId()){
		case android.R.id.home:
			finish();
			break;
		}
        return super.onMenuItemSelected(featureId, item);
    }
	
	private static void showHomeButton(){
		if(mContext != null && ((Activity) mContext).getActionBar() != null){
			((Activity) mContext).getActionBar().setHomeButtonEnabled(true);
	        ((Activity) mContext).getActionBar().setDisplayHomeAsUpEnabled(true);
		}		
	}
	
	static void showRestartToast(){
		Toast.makeText(mContext, R.string.ru_restart_message, Toast.LENGTH_SHORT).show();
	}

	@SuppressWarnings("deprecation")
	private void init(){
		boolean isHook = prefs.getBoolean(mContext.getString(R.string.is_hook_lockscreen_key), false);
		String key = mContext.getString(R.string.settings_lockscreen_clock_key);
		PreferenceCategory prefCat = (PreferenceCategory) findPreference(key);
		prefCat.setEnabled(isHook);
		key = mContext.getString(R.string.settings_lockscreen_other_key);
		prefCat = (PreferenceCategory) findPreference(key);
		prefCat.setEnabled(isHook);
		
		key = mContext.getString(R.string.settings_hook_lockscreen_next_alarm_key);
		CheckBoxPreference pref = (CheckBoxPreference) findPreference(key);
		pref.setChecked(prefs.getBoolean(key, true));
		key = mContext.getString(R.string.settings_hook_lockscreen_battery_key);
		pref = (CheckBoxPreference) findPreference(key);
		pref.setChecked(prefs.getBoolean(key, true));
		key = mContext.getString(R.string.settings_hook_lockscreen_carrier_key);
		pref = (CheckBoxPreference) findPreference(key);
		pref.setChecked(prefs.getBoolean(key, true));
		key = mContext.getString(R.string.settings_hook_lockscreen_hint_key);
		pref = (CheckBoxPreference) findPreference(key);
		pref.setChecked(prefs.getBoolean(key, true));
		
		key = mContext.getString(R.string.settings_hook_lockscreen_clock_margin_key);
		Preference chooserPref = findPreference(key);
		chooserPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				FragmentManager manager = ((Activity) mContext).getFragmentManager();
		        LockScreenClockMarginChooser dialog = new LockScreenClockMarginChooser();
		        int theme = prefs.getBoolean(mContext.getString(R.string.ru_use_light_theme_key), false) ? android.R.style.Theme_DeviceDefault_Light_Dialog : android.R.style.Theme_DeviceDefault_Dialog;
		        dialog.setStyle(DialogFragment.STYLE_NO_FRAME, theme);
		        dialog.show(manager, "dialog");
				return false;
			}
		});
		
		key = mContext.getString(R.string.settings_hook_lockscreen_tool_key);
		chooserPref = findPreference(key);
		chooserPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				FragmentManager manager = ((Activity) mContext).getFragmentManager();
				LockScreenToolAlphaChooser dialog = new LockScreenToolAlphaChooser();
		        int theme = prefs.getBoolean(mContext.getString(R.string.ru_use_light_theme_key), false) ? android.R.style.Theme_DeviceDefault_Light_Dialog : android.R.style.Theme_DeviceDefault_Dialog;
		        dialog.setStyle(DialogFragment.STYLE_NO_FRAME, theme);
		        dialog.show(manager, "dialog");
				return false;
			}
		});
	}
	
	OnSharedPreferenceChangeListener mOnSharedPreferenceChangeListener = new OnSharedPreferenceChangeListener() {		
		@SuppressLint("WorldReadableFiles")
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			@SuppressWarnings("deprecation")
			SharedPreferences target = mContext.getSharedPreferences(Settings.class.getPackage().getName(), Context.MODE_WORLD_READABLE | Context.MODE_MULTI_PROCESS);
			XUtil.copyPreferences(sharedPreferences, target, key);
		}
	};
	
	@SuppressWarnings("deprecation")
	private void initOption(){
		showHomeButton();
		setPreferenceChangeListener(getPreferenceScreen());
		prefs.registerOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
	}
	
	@Override
	protected void onDestroy() {
		prefs.unregisterOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
		super.onDestroy();
	}

	private static void setPreferenceChangeListener(PreferenceScreen preferenceScreen){
		for(int i = 0; i < preferenceScreen.getPreferenceCount(); i++){
			if(preferenceScreen.getPreference(i) instanceof PreferenceCategory){
				for(int j = 0; j < ((PreferenceCategory) preferenceScreen.getPreference(i)).getPreferenceCount(); j++){
					((PreferenceCategory) preferenceScreen.getPreference(i)).getPreference(j).setOnPreferenceChangeListener(onPreferenceChangeListener);
				}
			}else{
				preferenceScreen.getPreference(i).setOnPreferenceChangeListener(onPreferenceChangeListener);				
			}
		}
	}
	
	private static OnPreferenceChangeListener onPreferenceChangeListener = new OnPreferenceChangeListener(){
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
	        if(prefs == null){
	        	prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
	        }
			switch(preference.getTitleRes()){
			case R.string.is_hook_lockscreen_title:
				if(!prefs.getBoolean(preference.getKey(), false) && (Boolean) newValue){
					showRestartToast();
				}
				preference.getPreferenceManager().findPreference(mContext.getString(R.string.settings_lockscreen_clock_key)).setEnabled((Boolean) newValue);
				preference.getPreferenceManager().findPreference(mContext.getString(R.string.settings_lockscreen_other_key)).setEnabled((Boolean) newValue);
				prefs.edit().putBoolean(preference.getKey(), (Boolean)newValue).commit();
				sendLockScreenIntent();
				break;
			case R.string.settings_hook_lockscreen_next_alarm_title:
				prefs.edit().putBoolean(preference.getKey(), (Boolean)newValue).commit();
				sendLockScreenIntent();
				break;
			case R.string.settings_hook_lockscreen_battery_title:
				prefs.edit().putBoolean(preference.getKey(), (Boolean)newValue).commit();
				sendLockScreenIntent();
				break;
			case R.string.settings_hook_lockscreen_carrier_title:
				prefs.edit().putBoolean(preference.getKey(), (Boolean)newValue).commit();
				sendLockScreenIntent();
				break;
			case R.string.settings_hook_lockscreen_hint_title:
				prefs.edit().putBoolean(preference.getKey(), (Boolean)newValue).commit();
				sendLockScreenIntent();
				break;
			default:
				return false;
			}
			return true;
		}		
	};
	
	static OnXExtendsSeekBarChangeListener mOnXExtendsSeekBarChangeListener = new OnXExtendsSeekBarChangeListener(){
		@Override
		public void onProgressChanged(XExtendsSeekBar mXExtendsSeekBar, SeekBar seekBar, int progress, boolean fromUser) { }
		@Override
		public void onStartTrackingTouch(XExtendsSeekBar mXExtendsSeekBar, SeekBar seekBar) { }
		@Override
		public void onStopTrackingTouch(XExtendsSeekBar mXExtendsSeekBar, SeekBar seekBar) {
			switch(mXExtendsSeekBar.getId()){
			case R.id.toolAlpha:
			case R.id.topMargin:
			case R.id.leftMargin:
				sendLockScreenIntent();
				break;
			}
		}		
	};
	
	private static void sendLockScreenIntent(){
		Intent intent = new Intent();
		intent.setAction(LockscreenModule.STATE_CHANGE);
		intent.putExtra(LockscreenModule.STATE_EXTRA_IS_HOOK, prefs.getBoolean(mContext.getString(R.string.is_hook_lockscreen_key), false));
		intent.putExtra(LockscreenModule.STATE_EXTRA_TOP_MARGIN, prefs.getInt(mContext.getString(R.string.settings_hook_lockscreen_clock_top_margin_key), (int) mContext.getResources().getDimension(R.dimen.lockscreen_widget_slider_margin_top)));
		intent.putExtra(LockscreenModule.STATE_EXTRA_LEFT_MARGIN, prefs.getInt(mContext.getString(R.string.settings_hook_lockscreen_clock_left_margin_key), 0));
		intent.putExtra(LockscreenModule.STATE_EXTRA_ALARM_VISIBILITY, prefs.getBoolean(mContext.getString(R.string.settings_hook_lockscreen_next_alarm_key), true));
		intent.putExtra(LockscreenModule.STATE_EXTRA_BATTERY_VISIBILITY, prefs.getBoolean(mContext.getString(R.string.settings_hook_lockscreen_battery_key), true));
		intent.putExtra(LockscreenModule.STATE_EXTRA_CARRIER_VISIBILITY, prefs.getBoolean(mContext.getString(R.string.settings_hook_lockscreen_carrier_key), true));
		intent.putExtra(LockscreenModule.STATE_EXTRA_HINT_VISIBILITY, prefs.getBoolean(mContext.getString(R.string.settings_hook_lockscreen_hint_key), true));
		intent.putExtra(LockscreenModule.STATE_EXTRA_TOOLS_ALPHA, prefs.getFloat(mContext.getString(R.string.settings_hook_lockscreen_tool_key), 1.0f));
		mContext.sendBroadcast(intent);
	}

	public static class LockScreenClockMarginChooser extends DialogFragment {
		@Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
        	Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getActivity().getString(R.string.settings_hook_lockscreen_clock_margin_title));
            builder.setMessage(getActivity().getString(R.string.settings_hook_lockscreen_clock_margin_summary));
            View layout = View.inflate(getActivity(), R.layout.lockscreen_margin_chooser, null);
			if(layout != null){
				final XExtendsSeekBar topMargin = (XExtendsSeekBar) layout.findViewById(R.id.topMargin);
				final XExtendsSeekBar leftMargin = (XExtendsSeekBar) layout.findViewById(R.id.leftMargin);
				topMargin.setOnXExtendsSeekBarChangeListener(mOnXExtendsSeekBarChangeListener);
				leftMargin.setOnXExtendsSeekBarChangeListener(mOnXExtendsSeekBarChangeListener);
				Button reset = (Button) layout.findViewById(R.id.reset);
				reset.setOnClickListener(new OnClickListener() {					
					@Override
					public void onClick(View v) {
				        if(prefs == null){
				        	prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
				        }
						prefs.edit().putInt(getString(R.string.settings_hook_lockscreen_clock_top_margin_key), (int) getActivity().getResources().getDimension(R.dimen.lockscreen_widget_slider_margin_top)).commit();
						prefs.edit().putInt(getString(R.string.settings_hook_lockscreen_clock_left_margin_key), 0).commit();
						topMargin.setProgress(prefs.getInt(getString(R.string.settings_hook_lockscreen_clock_top_margin_key), (int) getActivity().getResources().getDimension(R.dimen.lockscreen_widget_slider_margin_top)));
						leftMargin.setProgress(0);
					}
				});
				builder.setView(layout);
			}
        	
            builder.setPositiveButton(getActivity().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
            AlertDialog dialog = builder.create();
            return dialog;
        }
		@Override
		public void onResume() {
			showHomeButton();
			super.onResume();
		}
	}
	
	public static class LockScreenToolAlphaChooser extends DialogFragment {		
		@Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
        	Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getActivity().getString(R.string.settings_hook_lockscreen_tool_title));
            builder.setMessage(getActivity().getString(R.string.settings_hook_lockscreen_tool_summary));
            View layout = View.inflate(getActivity(), R.layout.lockscreen_tools_alpha_chooser, null);
            if(layout != null){
				final XExtendsSeekBarFloat toolAlpha = (XExtendsSeekBarFloat) layout.findViewById(R.id.toolAlpha);
				toolAlpha.setOnXExtendsSeekBarChangeListener(mOnXExtendsSeekBarChangeListener);
				Button reset = (Button) layout.findViewById(R.id.reset);
				reset.setOnClickListener(new OnClickListener() {					
					@Override
					public void onClick(View v) {
				        if(prefs == null){
				        	prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
				        }
						prefs.edit().putFloat(getString(R.string.settings_hook_lockscreen_tool_key), 1.0f).commit();
						toolAlpha.setProgress((int)((prefs.getFloat(getString(R.string.settings_hook_lockscreen_tool_key), 1.0f) * 100 )));
					}
				});
				builder.setView(layout);
			}
        	
            builder.setPositiveButton(getActivity().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
            AlertDialog dialog = builder.create();
            return dialog;
        }
		@Override
		public void onResume() {
			showHomeButton();
			super.onResume();
		}
	}
}
