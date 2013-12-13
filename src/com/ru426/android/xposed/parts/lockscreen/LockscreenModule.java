package com.ru426.android.xposed.parts.lockscreen;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.ru426.android.xposed.library.ModuleBase;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class LockscreenModule extends ModuleBase {
	private static final String TAG = LockscreenModule.class.getSimpleName();
	public static final String STATE_CHANGE = LockscreenModule.class.getName() + ".intent.action.STATE_CHANGE";
	public static final String STATE_EXTRA_IS_HOOK = LockscreenModule.class.getName() + ".intent.extra.STATE_EXTRA_IS_HOOK";
	public static final String STATE_EXTRA_TOP_MARGIN = LockscreenModule.class.getName() + ".intent.extra.STATE_EXTRA_TOP_MARGIN";
	public static final String STATE_EXTRA_LEFT_MARGIN = LockscreenModule.class.getName() + ".intent.extra.STATE_EXTRA_LEFT_MARGIN";
	
	public static final String STATE_EXTRA_TOOLS_ALPHA = LockscreenModule.class.getName() + ".intent.extra.STATE_EXTRA_TOOLS_ALPHA";
	public static final String STATE_EXTRA_ALARM_VISIBILITY = LockscreenModule.class.getName() + ".intent.extra.STATE_EXTRA_ALARM_VISIBILITY";
	public static final String STATE_EXTRA_CARRIER_VISIBILITY = LockscreenModule.class.getName() + ".intent.extra.STATE_EXTRA_CARRIER_VISIBILITY";
	public static final String STATE_EXTRA_BATTERY_VISIBILITY = LockscreenModule.class.getName() + ".intent.extra.STATE_EXTRA_BATTERY_VISIBILITY";
	public static final String STATE_EXTRA_HINT_VISIBILITY = LockscreenModule.class.getName() + ".intent.extra.STATE_EXTRA_HINT_VISIBILITY";
	
	private boolean isHook;
	private int topMargin;
	private int leftMargin;
	private boolean alarmVisibility = true;
	private boolean carrierVisibility = true;
	private boolean batteryVisibility = true;
	private boolean hintVisibility = true;
	private float toolsAlpha;
	
	@Override
	public void init(XSharedPreferences prefs, ClassLoader classLoader, boolean isDebug) {
		super.init(prefs, classLoader, isDebug);
		isHook = (Boolean) xGetValue(prefs, xGetString(R.string.is_hook_lockscreen_key), false);
		topMargin = (Integer) xGetValue(prefs, xGetString(R.string.settings_hook_lockscreen_clock_top_margin_key), (int) xModuleResources.getDimension(R.dimen.lockscreen_widget_slider_margin_top));
		leftMargin = (Integer) xGetValue(prefs, xGetString(R.string.settings_hook_lockscreen_clock_left_margin_key), 0);
		carrierVisibility = (Boolean) xGetValue(prefs, xGetString(R.string.settings_hook_lockscreen_carrier_key), true);
		batteryVisibility = (Boolean) xGetValue(prefs, xGetString(R.string.settings_hook_lockscreen_battery_key), true);
		alarmVisibility = (Boolean) xGetValue(prefs, xGetString(R.string.settings_hook_lockscreen_next_alarm_key), true);
		hintVisibility = (Boolean) xGetValue(prefs, xGetString(R.string.settings_hook_lockscreen_hint_key), true);
		toolsAlpha = (Float) xGetValue(prefs, xGetString(R.string.settings_hook_lockscreen_tool_key), 1.0f);
		Class<?> xUxpNxtLockScreen = XposedHelpers.findClass("com.sonyericsson.lockscreen.uxpnxt.UxpNxtLockScreen", classLoader);
		XposedBridge.hookAllConstructors(xUxpNxtLockScreen, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				super.afterHookedMethod(param);
				try{
					xLog(TAG + " : " + "afterHookedMethod hookAllConstructors");
					if(isHook){
						mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
						IntentFilter intentFilter = new IntentFilter();
						intentFilter.addAction(STATE_CHANGE);
						xRegisterReceiver(mContext, intentFilter);						
					}
				} catch (Throwable throwable) {
					XposedBridge.log(throwable);
				}
			}
		});
		
		Object callback[] = new Object[1];
		callback[0] = new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				super.afterHookedMethod(param);
				try {
					xLog(TAG + " : " + "afterHookedMethod onResume");
					if(!isHook) return;
					Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
					RelativeLayout mRootView = (RelativeLayout) XposedHelpers.getObjectField(param.thisObject, "mRootView");
					RelativeLayout mTopSlider = (RelativeLayout) XposedHelpers.getObjectField(param.thisObject, "mTopSlider");
					LinearLayout mClockWidget = (LinearLayout) XposedHelpers.getObjectField(param.thisObject, "mClockWidget");
					TextView mCarrier = (TextView) XposedHelpers.getObjectField(param.thisObject, "mCarrier");
					TextView mBatteryInfo = (TextView) XposedHelpers.getObjectField(param.thisObject, "mBatteryInfo");
					TextView mNextAlarm = (TextView) XposedHelpers.getObjectField(param.thisObject, "mNextAlarm");
					LinearLayout mHint = (LinearLayout) mRootView.findViewById(mContext.getResources().getIdentifier("hint", "id",  mContext.getPackageName()));
					try{
						RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
						params.topMargin = topMargin;
						params.leftMargin = leftMargin;
						mClockWidget.setLayoutParams(params);
					} catch (Throwable throwable) {
						XposedBridge.log(throwable);
					}
					try{
						if(!carrierVisibility) mCarrier.setVisibility(View.INVISIBLE);
						if(!batteryVisibility) mBatteryInfo.setVisibility(View.INVISIBLE);
						if(!alarmVisibility) mNextAlarm.setVisibility(View.INVISIBLE);
						if(!hintVisibility) mHint.setVisibility(View.INVISIBLE);
					} catch (Throwable throwable) {
						XposedBridge.log(throwable);
					}
					try{
	                    mTopSlider.setAlpha(toolsAlpha);
					} catch (Throwable throwable) {
						XposedBridge.log(throwable);
					}
				} catch (Throwable throwable) {
					XposedBridge.log(throwable);
				}
			}
		};
		xHookMethod(xUxpNxtLockScreen, "onResume", callback, isHook);
	}

	@Override
	protected void xOnReceive(Context context, Intent intent) {
		super.xOnReceive(context, intent);
		xLog(TAG + " : " + intent.getAction());
		if (intent.getAction().equals(STATE_CHANGE)) {
			isHook = intent.getBooleanExtra(STATE_EXTRA_IS_HOOK, false);
			topMargin = intent.getIntExtra(STATE_EXTRA_TOP_MARGIN, (int) xModuleResources.getDimension(R.dimen.lockscreen_widget_slider_margin_top));
			leftMargin = intent.getIntExtra(STATE_EXTRA_LEFT_MARGIN, 0);
			carrierVisibility = intent.getBooleanExtra(STATE_EXTRA_CARRIER_VISIBILITY, true);
			batteryVisibility = intent.getBooleanExtra(STATE_EXTRA_BATTERY_VISIBILITY, true);
			alarmVisibility = intent.getBooleanExtra(STATE_EXTRA_ALARM_VISIBILITY, true);
			hintVisibility = intent.getBooleanExtra(STATE_EXTRA_HINT_VISIBILITY, true);
			toolsAlpha = intent.getFloatExtra(STATE_EXTRA_TOOLS_ALPHA, 1.0f);
		}
	}
	
}
