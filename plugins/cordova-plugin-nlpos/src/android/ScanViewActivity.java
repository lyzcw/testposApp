package com.openunion.cordova.plugins.nlpos;

import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.newland.SettingsManager;
import android.newland.content.NlContext;
import android.os.Bundle;
import android.os.Message;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import io.ionic.starter.R;
import com.openunion.cordova.plugins.nlpos.Const.ScanType;
import com.newland.mtype.log.DeviceLogger;
import com.newland.mtype.log.DeviceLoggerFactory;
import com.newland.mtype.module.common.scanner.ScannerListener;

public class ScanViewActivity extends Activity{

	private SurfaceView surfaceView;
	private Context context;
	private int scanType;
	private int timeout;
	private static DeviceLogger logger = DeviceLoggerFactory.getLogger(ScanViewActivity.class);
	private ImageView scanIV;
	private Button frontBtn;
	private Button backBtn;
	private SoundPoolImpl spi;
	private LinearLayout frontLL;
	private boolean isFinish=true;
	private AnimationDrawable scanAnim;
	private FrameLayout backFL;
	private SettingsManager settingManager;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.sacn_view);
		context=this;
//		//屏蔽任务键
//		settingManager=(SettingsManager) getSystemService(NlContext.SETTINGS_MANAGER_SERVICE);
//		settingManager.setAppSwitchKeyEnabled(false);

//		SettingsManager settingsManager=(SettingsManager) getSystemService(NlContext.SETTINGS_MANAGER_SERVICE);
//		settingsManager.setSettingVpnDispley(0); vpn显示


		spi = SoundPoolImpl.getInstance();
		spi.initLoad(this);
		init();
	}



	private void init() {
		scanType=getIntent().getIntExtra("scanType", 0x00);//默认后置扫码
//		timeout=getIntent().getIntExtra("timeout", 60);
		timeout=10;
		surfaceView=(SurfaceView) findViewById(R.id.surfaceView);
//		scanIV=(ImageView) findViewById(R.id.iv_scan);
//		frontLL=(LinearLayout) findViewById(R.id.ll_front);
//		frontBtn=(Button) findViewById(R.id.btn_switch_front);
//		backBtn=(Button) findViewById(R.id.btn_switch_back);
//		backFL=(FrameLayout) findViewById(R.id.fl_back);
		startScan();
//		frontBtn.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				logger.debug("---------------切换前置---------");
//				isFinish=false;
//				Scan.scanner.stopScan();
//				scanType=ScanType.FRONT;
//				try {
//					Thread.sleep(200);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//				startScan();
//			}
//		});

//		backBtn.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				logger.debug("---------------切换后置---------");
//				isFinish=false;
//				Scan.scanner.stopScan();
//				scanType=ScanType.BACK;
//				try {
//					Thread.sleep(200);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//				startScan();
//			}
//		});

	}

	private void startScan(){
		boolean issu=Scan.scanner.isSupScanCode(ScanType.FRONT);
		System.out.println("isu-----"+issu);
		try{
			if(scanType==ScanType.BACK){//后置的
				//frontLL.setVisibility(View.GONE);
				//backFL.setVisibility(View.VISIBLE);
        Scan.scanner.initScanner(context,surfaceView,scanType);
//			}else if(scanType==ScanType.FRONT){
//				backFL.setVisibility(View.GONE);
//				frontLL.setVisibility(View.VISIBLE);
//				scanAnim = (AnimationDrawable)scanIV.getDrawable();
//				 if (scanAnim != null && !scanAnim.isRunning()) {
//			            scanAnim.start();
//			        }
//        Scan.scanner.initScanner(context,null,scanType);
			}else{
				finish();
				Message scanMsg = new Message();
				scanMsg.what = Const.ScanResult.SCAN_ERROR;
				Bundle scanBundle = new Bundle();
				scanBundle.putInt("errorCode", 0x00);
				scanBundle.putString("errormessage","不支持的扫描类型");
				scanMsg.setData(scanBundle);
        nlpos.getScanEventHandler().sendMessage(scanMsg);
			}
      Scan.scanner.startScan(timeout, TimeUnit.SECONDS, new ScannerListener() {

				@Override
				public void onResponse(String[] barcodes) {
					logger.debug("---------------onResponse---------"+barcodes[0]+"barcodes"+barcodes.length);
					spi.play();
					isFinish=true;
					Message scanMsg = new Message();
					scanMsg.what = Const.ScanResult.SCAN_RESPONSE;
					Bundle scanBundle = new Bundle();
					scanBundle.putStringArray("barcodes", barcodes);
					scanMsg.setData(scanBundle);
          nlpos.getScanEventHandler().sendMessage(scanMsg);

				}

				@Override
				public void onFinish() {
					logger.debug("------onFinish--------"+isFinish);
					if(isFinish){
						finish();
						logger.debug("---------------onFinish---------");
						Message scanMsg = new Message();
						scanMsg.what = Const.ScanResult.SCAN_FINISH;
            nlpos.getScanEventHandler().sendMessage(scanMsg);
					}
				}
			},true);
		}catch(Exception e){
			logger.debug("---------------Exception---------"+e.getMessage());
			finish();
			e.getStackTrace();
			Message scanMsg = new Message();
			scanMsg.what = Const.ScanResult.SCAN_ERROR;
			Bundle scanBundle = new Bundle();
			scanBundle.putInt("errorCode", 0);
			scanBundle.putString("errormessage", e.getMessage());
			scanMsg.setData(scanBundle);
			nlpos.getScanEventHandler().sendMessage(scanMsg);
		}
	}
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		isFinish=true;
	}
	//任务键点击以及home键点击在onPause里面做释放
	@Override
	protected void onPause() {
		isFinish=true;
		System.out.println("-----onPause--------------");
    Scan.scanner.stopScan();
		if (scanAnim != null && scanAnim.isRunning()) {
	           scanAnim.stop();
	    }
		super.onPause();
	}
}
