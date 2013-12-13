package com.ru426.android.xposed.parts.lockscreen.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.ru426.android.xposed.parts.lockscreen.R;

public class XExtendsSeekBar extends LinearLayout {
	protected SharedPreferences pref;
	protected Context mContext;
	protected String summary;
	protected String summaryExtra;
	protected int summaryExtraUnit;
	protected String prefKey;
	public String getPrefKey() {
		return prefKey;
	}
	public void setPrefKey(String prefKey) {
		this.prefKey = prefKey;
	}
	protected int progressExtra;
	protected int progressAmount;
	public SeekBar seekBar;
	protected TextView summaryView;
	
	protected OnXExtendsSeekBarChangeListener mOnXExtendsSeekBarChangeListener;
	public interface OnXExtendsSeekBarChangeListener{
		public void onProgressChanged(XExtendsSeekBar mXExtendsSeekBar, SeekBar seekBar, int progress, boolean fromUser);
		public void onStartTrackingTouch(XExtendsSeekBar mXExtendsSeekBar, SeekBar seekBar);
		public void onStopTrackingTouch(XExtendsSeekBar mXExtendsSeekBar, SeekBar seekBar);
	}
	public void setOnXExtendsSeekBarChangeListener(OnXExtendsSeekBarChangeListener listener){
		mOnXExtendsSeekBarChangeListener = listener;
	}
	
	@SuppressLint("Recycle")
	public XExtendsSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflate(context, R.layout.extends_seekbar, this);
		setOrientation(LinearLayout.VERTICAL);
		mContext = context;
		pref = PreferenceManager.getDefaultSharedPreferences(mContext);
		TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.XExtendsSeekBar);

		summary = array.getString(0);
		int max = array.getInt(1, 0);
		prefKey = array.getString(2);
		progressExtra = array.getInt(3, 0);
		summaryExtra = array.getString(4);
		progressAmount = array.getInt(5, 1);
		summaryExtraUnit = array.getResourceId(6, 0);
		
		if(summary == null) summary = "";
		if(prefKey == null) prefKey = "";
		if(summaryExtra == null) summaryExtra = "";
		
		seekBar = (SeekBar) findViewById(R.id.seekbar);
		seekBar.setMax(max);
		
		setInitialProgress();
				
		Button prev = (Button) findViewById(R.id.prev);
		Button next = (Button) findViewById(R.id.next);
		prev.setOnClickListener(onClickListener);
		next.setOnClickListener(onClickListener);
	}
	
	protected void setInitialProgress(){
		int progress = pref.getInt(prefKey, 0);
		seekBar.setProgress(progressExtra + progress);
		if(summaryExtra.length() != 0 && summaryExtraUnit != 0) summaryExtra = getResources().getQuantityString(summaryExtraUnit, seekBar.getProgress());
		seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
		summaryView = (TextView) findViewById(R.id.summary);		
		summaryView.setText(String.format("%s : %s%s", summary, getExtraProgress(seekBar.getProgress()), summaryExtra));
	}
	
	public void setProgress(int progress){
		if(seekBar != null){
			seekBar.setProgress(progressExtra + progress);
		}
	}
	
	protected OnSeekBarChangeListener onSeekBarChangeListener = new OnSeekBarChangeListener(){
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			if(!fromUser){
				progress = (int)(Math.round((double)progress/progressAmount)*progressAmount);
				seekBar.setProgress(progress);
			}
			if(summaryExtra.length() != 0 && summaryExtraUnit != 0) summaryExtra = getResources().getQuantityString(summaryExtraUnit, seekBar.getProgress());
			summaryView.setText(String.format("%s : %s%s", summary, getExtraProgress(seekBar.getProgress()), summaryExtra));
			if(mOnXExtendsSeekBarChangeListener != null)
				mOnXExtendsSeekBarChangeListener.onProgressChanged(XExtendsSeekBar.this, seekBar, seekBar.getProgress(), fromUser);
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			if(mOnXExtendsSeekBarChangeListener != null)
				mOnXExtendsSeekBarChangeListener.onStartTrackingTouch(XExtendsSeekBar.this, seekBar);
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			pref.edit().putInt(prefKey, (Integer) getExtraProgress(seekBar.getProgress())).commit();
			if(mOnXExtendsSeekBarChangeListener != null)
				mOnXExtendsSeekBarChangeListener.onStopTrackingTouch(XExtendsSeekBar.this, seekBar);
		}		
	};
	
	private OnClickListener onClickListener = new OnClickListener(){
		@Override
		public void onClick(View v) {
			switch(v.getId()){
			case R.id.prev:
				seekBar.setProgress(seekBar.getProgress()-progressAmount);
				break;
			case R.id.next:
				seekBar.setProgress(seekBar.getProgress()+progressAmount);
				break;
			}
		}		
	};
	
	protected Object getExtraProgress(int progress){
		if (progress > progressExtra) {
			return (progress - progressExtra);
		}else{
			return (-progressExtra + progress);
		}
	}
}
