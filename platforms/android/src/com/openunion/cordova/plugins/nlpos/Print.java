package com.openunion.cordova.plugins.nlpos;

import android.app.Dialog;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.newland.mtype.module.common.printer.FontSettingScope;
import com.newland.mtype.module.common.printer.FontType;
import com.newland.mtype.module.common.printer.LiteralType;
import com.newland.mtype.module.common.printer.Printer;
import com.newland.mtype.module.common.printer.PrinterResult;
import com.newland.mtype.module.common.printer.PrinterStatus;

import org.apache.cordova.LOG;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by lyzcw on 2017/9/7.
 */

public class Print {
  private WaitThreat waitThreat;
  private LiteralType literalType;
  private FontSettingScope fontSettingScope;
  private FontType fontType;
  public N900Device n900Device;
  private Printer printer;
  private PrinterManager printManager;
  private final static String LOG_TAG = "openunion.nlpos";
  private static final String SUCCESS = "success";
  private static final String FAILED = "failed";
  private String showMsg = "";
  private Map map = new HashMap();

  public Map print( String bill ){
    waitThreat = new WaitThreat();
    printManager=PrinterManager.getInstance();
    if(n900Device.isDeviceAlive()){
      printer = n900Device.getPrinter();
      // 缺纸
      if (printer.getStatus() == PrinterStatus.OUTOF_PAPER) {
        showMsg = "打印失败！打印机缺纸";
        map.put("status", FAILED);
        map.put("msg", showMsg);
        LOG.d(LOG_TAG, showMsg);
        return map;
      }
      if (printer.getStatus() != PrinterStatus.NORMAL) {
        showMsg = "打印失败！打印机状态不正常";
        map.put("status", FAILED);
        map.put("msg", showMsg);
        LOG.d(LOG_TAG, showMsg);
        return map;
      }

      //if(initPrinter().get("status").equals(SUCCESS)){
        printer.init();
        printer.setLineSpace(Integer.parseInt("2"));
        //Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.big_picture);
        Bitmap bitmap = BitmapFactory.decodeStream(getClass().getResourceAsStream(Constant.icon_path));
        PrinterResult printerResult = printer.print(0, bitmap, 30, TimeUnit.SECONDS);
        showMsg = "图片打印结果：" + printerResult.toString();
        printer.setDensity(10);
        PrinterResult printerResult0 = printer.print(bill, 30, TimeUnit.SECONDS);
        showMsg += "默认字体打印结果：" + printerResult0.toString();
        map.put("status", SUCCESS);
        map.put("msg", showMsg);
        LOG.d(LOG_TAG, showMsg);
        return map;
      //}

    }
    return map;

  }

  private Map initPrinter(){
    new Thread(new Runnable() {

      @Override
      public void run() {
        try {
          printer.init();
          showMsg = "打印机初始化成功" ;
          map.put("status", SUCCESS);
          map.put("msg", showMsg);
          LOG.d(LOG_TAG, showMsg);
        } catch (Exception e) {
          e.printStackTrace();
          showMsg = "打印机初始化异常：";
          map.put("status", FAILED);
          map.put("msg", showMsg);
          LOG.d(LOG_TAG, showMsg);
        }

      }
    }).start();

    return map;
  }
  private Map getPrinterState(){
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          PrinterStatus printerStatus = printer.getStatus();
          showMsg = "打印机状态：" + printerStatus;
          map.put("status", SUCCESS);
          map.put("msg", showMsg);
          map.put("state", printerStatus.toString());
          LOG.d(LOG_TAG, showMsg);
        } catch (Exception e) {
          e.printStackTrace();
          showMsg = "获取打印机状态异常：" + e;
          map.put("status", FAILED);
          map.put("msg", showMsg);
          LOG.d(LOG_TAG, showMsg);
        }
      }
    }).start();
    return map;
  }

  /**
   * 线程等待、唤醒
   *
   */
  private class WaitThreat {
    Object syncObj = new Object();

    void waitForRslt() throws InterruptedException {
      synchronized (syncObj) {
        syncObj.wait();
      }
    }

    void notifyThread() {
      synchronized (syncObj) {
        syncObj.notify();
      }
    }
  }
}
