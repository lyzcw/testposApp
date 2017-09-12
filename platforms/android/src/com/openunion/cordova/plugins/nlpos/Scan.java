package com.openunion.cordova.plugins.nlpos;

import android.content.Intent;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.newland.mtype.module.common.scanner.BarcodeScanner;

import com.openunion.cordova.plugins.nlpos.ScanViewActivity;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lyzcw on 2017/9/7.
 */

public class Scan {
  private Activity cordovaActivity;
  public N900Device n900Device;
  public static BarcodeScanner scanner = null;
  private ScanDecodeMode scanDecodeMdoe = ScanDecodeMode.FRONT;
  private final static String LOG_TAG = "openunion.nlpos";
  private static final String SUCCESS = "success";
  private static final String FAILED = "failed";
  private String showMsg = "";
  private Map map = new HashMap();

  public Scan( N900Device n900Device, Activity activity){
    this.n900Device = n900Device;
    this.cordovaActivity = activity;
    this.scanner = n900Device.getBarcodeScanner();
  }
  public void scan(){
    Intent intent = new Intent(this.cordovaActivity, ScanViewActivity.class);
    intent.putExtra("scanType", 0x00);
    this.cordovaActivity.startActivity(intent);
  }

}
