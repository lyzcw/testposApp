package com.openunion.cordova.plugins.nlpos;


import com.openunion.cordova.plugins.nlpos.R;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;


/**
 * android原生音效
 * @author chenkh
 * @date 2015-7-29
 * @time 上午9:24:18
 *
 */
public class SoundPoolImpl {

	private static SoundPoolImpl INSTANCE;

	private SoundPool soundPool;


	public static SoundPoolImpl getInstance(){
		if (INSTANCE == null) {
			INSTANCE = new SoundPoolImpl();
		}
		return INSTANCE;
	}

	public void initLoad(Context context){
		soundPool= new SoundPool(3,AudioManager.STREAM_SYSTEM,5);
		soundPool.load(context.getApplicationContext(),
				R.raw.click1, 1);
	}

	public void play(){
		soundPool.play(1,1, 1, 0, 0, 1);
	}

}
