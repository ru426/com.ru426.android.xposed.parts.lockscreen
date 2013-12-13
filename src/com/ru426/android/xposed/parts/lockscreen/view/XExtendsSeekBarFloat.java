package com.ru426.android.xposed.parts.lockscreen.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.ru426.android.xposed.parts.lockscreen.R;

public class XExtendsSeekBarFloat extends XExtendsSeekBar {

	public XExtendsSeekBarFloat(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected void setInitialProgress(){
		float progress = pref.getFloat(prefKey, 1.0f);
		seekBar.setProgress((int)(progressExtra + (progress * 100)));
		if(summaryExtra.length() != 0 && summaryExtraUnit != 0) summaryExtra = getResources().getQuantityString(summaryExtraUnit, seekBar.getProgress());
		seekBar.setOnSeekBarChangeListener(null);
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(summaryExtra.length() != 0 && summaryExtraUnit != 0) summaryExtra = getResources().getQuantityString(summaryExtraUnit, seekBar.getProgress());
				summaryView.setText(String.format("%s : %s%s", summary, progress, summaryExtra));
				if(mOnXExtendsSeekBarChangeListener != null)
					mOnXExtendsSeekBarChangeListener.onProgressChanged(XExtendsSeekBarFloat.this, seekBar, progress, fromUser);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				if(mOnXExtendsSeekBarChangeListener != null)
					mOnXExtendsSeekBarChangeListener.onStartTrackingTouch(XExtendsSeekBarFloat.this, seekBar);
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				pref.edit().putFloat(prefKey, (Float) getExtraProgress(seekBar.getProgress())).commit();
				if(mOnXExtendsSeekBarChangeListener != null)
					mOnXExtendsSeekBarChangeListener.onStopTrackingTouch(XExtendsSeekBarFloat.this, seekBar);
			}		
		});
		summaryView = (TextView) findViewById(R.id.summary);		
		summaryView.setText(String.format("%s : %s%s", summary, seekBar.getProgress(), summaryExtra));
	}
	
	@Override
	protected Object getExtraProgress(int progress){
		return progress / 100.0f;
	}
}
