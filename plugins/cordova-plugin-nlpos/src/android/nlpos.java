package com.openunion.cordova.plugins.nlpos;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.newland.mtype.ModuleType;
import com.newland.mtype.ProcessTimeoutException;
import com.newland.mtype.event.DeviceEventListener;
import com.newland.mtype.module.common.cardreader.K21CardReader;
import com.newland.mtype.module.common.cardreader.K21CardReaderEvent;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import java.util.concurrent.TimeUnit;
/**
 * This class echoes a string called from JavaScript.
 */
public class nlpos extends CordovaPlugin {
  private final static String LOG_TAG = "openunion.nlpos";
  private static final String SUCCESS = "success";
  private static final String FAILED = "failed";
  protected static CallbackContext posCallbackContext;
  private String showMsg = "";
  public N900Device n900Device;
  private K21CardReader cardReader;
  private Map map = new HashMap();

  /**
   * Constructor.
   */
  public nlpos() {

  }

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    Log.d(LOG_TAG, "Execute:" + action + " with :" + args.toString());

    if (action.equals("openCardReader")) {
      if (posCallbackContext != null) {
        //callbackContext.success( "NLPos监听器正在运行");
        //return true;
      }else {
        posCallbackContext = callbackContext;
      }

      this.openCardReader( callbackContext );

      // Don't return any result now, since status results will be sent when events come in from broadcast receiver
      PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
      pluginResult.setKeepCallback(true);
      callbackContext.sendPluginResult(pluginResult);

      return true;
    }else if (action.equals("closeCardReader")) {
      this.closeCardReader( callbackContext );
      return true;
    }else if (action.equals("scan")) {
      this.scan( callbackContext );
      return true;
    }else if (action.equals("print")) {
      String bill = (String) args.get(0);
      this.print( callbackContext, bill );
      return true;
    }else if (action.equals("getAsynMsg")) {
      this.getAsynMsg( callbackContext );
      return true;
    }
    return false;
  }

  private void openCardReader( CallbackContext callbackContext) throws JSONException {

    n900Device=N900Device.getInstance(this.cordova);
    n900Device.callbackContext = callbackContext;

    if (!n900Device.isDeviceAlive()) {
      map = n900Device.connectDevice();
    }

    if( n900Device.isDeviceAlive()) {

      new Thread(new Runnable() {
        @Override
        public void run() {
          Constant.asynMsg = "";
          try {
            cardReader=n900Device.getCardReaderModuleType();
            Log.d(LOG_TAG, "开始：打开读卡器");
            cardReader.openCardReader("请刷卡或者插入IC卡", new ModuleType[] { ModuleType.COMMON_SWIPER, ModuleType.COMMON_ICCARDREADER, ModuleType.COMMON_RFCARDREADER }, false, true, 120, TimeUnit.SECONDS, new DeviceEventListener<K21CardReaderEvent>() {
              @Override
              public void onEvent(K21CardReaderEvent openCardReaderEvent, Handler handler) {
                Log.d(LOG_TAG, "监听到：刷卡事件");
                Map map1 = new HashMap();
                if (openCardReaderEvent.isSuccess()) {
                  Log.d(LOG_TAG, "监听到：刷卡成功");
                  switch (openCardReaderEvent.getOpenCardReaderResult().getResponseCardTypes()[0]) {
                    case MSCARD:
                      showMsg="读卡器识别到【磁条卡】";
                      boolean isCorrent = openCardReaderEvent.getOpenCardReaderResult().isMSDDataCorrectly();
                      if (!isCorrent) {
                        showMsg="刷卡姿势不对，获取的磁道数据不完整，请重刷！";
                      }else{
                        SwipRead swipdRead = new SwipRead();
                        swipdRead.swiper = n900Device.getK21Swiper();
                        Map map0 = new HashMap();
                        map0 = swipdRead.readExpress();
                        map0.put("event","readcard");
                        Log.d(LOG_TAG, (new JSONObject(map0)).toString() );
                        Constant.asynMsg = (new JSONObject(map0)).toString();
                        sendUpdate( new JSONObject(map0), true );
                      }
                      break;
                    case ICCARD:
                      showMsg="读卡器识别到【插卡】操作";
                      break;
                    case RFCARD:
                      switch (openCardReaderEvent.getOpenCardReaderResult().getResponseRFCardType()) {
                        case ACARD:
                        case BCARD:
                          showMsg="读卡器识别到非接CPU卡";
                          break;
                        case M1CARD:
                          byte sak = openCardReaderEvent.getOpenCardReaderResult().getSAK();
                          if (sak == 0x08) {
                            showMsg="读卡器识别到非接S50卡";
                            RFCardRead rfCardRead = new RFCardRead();
                            rfCardRead.rfCardModule = n900Device.getRFCardModule();
                            rfCardRead.m1CardPowerOn();
                            rfCardRead.authenticateByExtendKey();
                            Map map0 = new HashMap();
                            map0 = rfCardRead.readBlock();
                            map0.put("event","readcard");
                            Log.d(LOG_TAG, (new JSONObject(map0)).toString() );
                            Constant.asynMsg = (new JSONObject(map0)).toString();
                            sendUpdate( new JSONObject(map0), true );
                          } else if (sak == 0x18) {
                            showMsg="读卡器识别到非接S70卡";
                          } else if (sak == 0x28) {
                            showMsg="读卡器识别到非接S50_pro卡";
                          } else if (sak == 0x38) {
                            showMsg="读卡器识别到非接S70_pro卡";
                          }else{
                            showMsg="sak="+sak;
                            showMsg=showMsg+";读卡器识别到未定义的非接卡";
                          }
                          break;
                        default:
                          showMsg="读卡器识别到未定义的非接卡";
                          break;
                      }

                      break;
                    default:
                      break;
                  }
                  map1.put("status", SUCCESS);
                  map1.put("msg",showMsg);
                  map1.put("event","readcard");
                  Log.d(LOG_TAG, showMsg);
                  sendUpdate( new JSONObject(map1), true );
                  //asynMsg = new JSONObject(map1);
                } else if (openCardReaderEvent.isUserCanceled()) {
                  showMsg = "取消开启读卡器";
                } else if (openCardReaderEvent.isFailed()   ) {
                  if(openCardReaderEvent.getException() instanceof ProcessTimeoutException){
                    showMsg = "超时";
                  }
                  if(openCardReaderEvent.getException().getCause()  instanceof ProcessTimeoutException){
                    showMsg = "超时";
                  }
                  Log.d(LOG_TAG, "读卡器：开启失败");
                  Log.d(LOG_TAG, showMsg);
                  showMsg = "读卡器开启失败";
                  map1.put("status", FAILED);
                  map1.put("msg",showMsg);
                  map1.put("event","readcard");
                  sendUpdate( new JSONObject(map1), true );
                  //asynMsg = new JSONObject(map1);
                }
              }

              @Override
              public Handler getUIHandler() {
                return null;
              }
            });
          }catch ( Exception e ) {
            e.printStackTrace();
            showMsg = "读卡器开启异常：";
            map.put("status", FAILED);
            map.put("msg", showMsg + "\r\n" + e.getMessage() );
            map.put("event","readcard");
            sendUpdate( new JSONObject(map), true );
          }
        }
      }).start();
        // PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, (new JSONObject(map)).toString());
        // pluginResult.setKeepCallback(true);
        // callbackContext.sendPluginResult(pluginResult);
        map.put("event","readcard");
        sendUpdate( new JSONObject(map), true );

    }else {
      // PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, (new JSONObject(map)).toString());
      // pluginResult.setKeepCallback(true);
      // callbackContext.sendPluginResult(pluginResult);
      sendUpdate( new JSONObject(map), true );
    }
  }

  private void cancelCardReader( CallbackContext callbackContext) {
    Map map = new HashMap();
    // new Thread(new Runnable() {

    // 	@Override
    // 	public void run() {
    try {
      cardReader.cancelCardRead();
      showMsg = "撤销读卡操作：成功";
      map.put("status", SUCCESS);
      map.put("msg", showMsg );
    } catch (Exception e) {
      showMsg = "撤销读卡操作 异常：";
      map.put("status", FAILED);
      map.put("msg", showMsg );
    }
    // 	}
    // }).start();
    callbackContext.success( (new JSONObject(map)).toString() );
  }

  private void getAsynMsg( CallbackContext callbackContext) {
    try {
      //if( asynMsg.getString("status").equals(SUCCESS)){
        callbackContext.success( Constant.asynMsg );
        Log.d(LOG_TAG, "Constant.asynMsg："+Constant.asynMsg );
      //}
    } catch (Exception e) {
      showMsg = "读取异步消息 异常";
      map.put("status", FAILED);
      map.put("msg", showMsg );
      callbackContext.success( (new JSONObject(map)).toString() );
      Log.d(LOG_TAG, showMsg );
    }
  }

  private void closeCardReader( CallbackContext callbackContext) {
    Map map = new HashMap();
    //  new Thread(new Runnable() {

    //	@Override
    //	public void run() {
    try {
      cardReader.closeCardReader();
      showMsg = "关闭读卡器：成功";
      map.put("status", SUCCESS);
      map.put("msg", showMsg );
    } catch (Exception e) {
      showMsg = "关闭读卡器 异常：";
      map.put("status", FAILED);
      map.put("msg", showMsg );
    }
    //	}
    //}).start();
    callbackContext.success( (new JSONObject(map)).toString() );

  }

  private void scan( CallbackContext callbackContext) {
    this.n900Device=N900Device.getInstance(this.cordova);

    if (!n900Device.isDeviceAlive()) {
      map = n900Device.connectDevice();
    }
    if( n900Device.isDeviceAlive()) {
      Scan scan = new Scan(this.n900Device, this.cordova.getActivity());
      scan.scan();
    }
  }

  private void print( CallbackContext callbackContext, String bill) {
    n900Device=N900Device.getInstance(this.cordova);
    Print print = new Print();
    print.n900Device  = n900Device;

    if (!n900Device.isDeviceAlive()) {
      map = n900Device.connectDevice();
    }
    if( n900Device.isDeviceAlive()) {
      Map map0 = new HashMap();
      map0 = print.print( bill );
      if(map0.get("status").equals(FAILED)){
        callbackContext.success((new JSONObject(map0)).toString());
      }else{
        callbackContext.success((new JSONObject(map0)).toString());
      }
   }
  }

  public static Handler getScanEventHandler() {
    return scanEventHandler;
  }

  public static void setScanEventHandler(Handler scanEventHandler) {
    nlpos.scanEventHandler = scanEventHandler;
  }

  private static Handler scanEventHandler = new Handler(Looper.getMainLooper()) {

    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);
      switch (msg.what) {
        case Const.ScanResult.SCAN_FINISH: {
          Map map0 = new HashMap();
          map0.put("status", SUCCESS);
          map0.put("msg", "扫码结束");
          map0.put("event","scancode");
          Log.d(LOG_TAG, (new JSONObject(map0)).toString() );
          // Constant.asynMsg = (new JSONObject(map0)).toString();
          //sendUpdate( new JSONObject(map0), true );
          break;

        }
        case Const.ScanResult.SCAN_RESPONSE: {
          Bundle bundle = msg.getData();
          String[] barcodes = bundle.getStringArray("barcodes");
          //mainActivity.showMessage("-----扫码结果------"+ barcodes[0] + "\r\n", MessageTag.NORMAL);
          Map map0 = new HashMap();
          map0.put("status", SUCCESS);
          map0.put("msg", "扫码成功");
          Map map1 = new HashMap();
          map1.put("barcodes", barcodes[0]);
          map0.put("data", map1);
          Log.d(LOG_TAG, (new JSONObject(map0)).toString() );
          Constant.asynMsg = (new JSONObject(map0)).toString();
          map0.put("event","scancode");
          sendUpdate( new JSONObject(map0), true );
//				if (scanner != null) {
//					scanner.stopScan();
//				}
          break;
        }
        case Const.ScanResult.SCAN_ERROR: {
          Bundle bundle = msg.getData();
          int errorCode = bundle.getInt("errorCode");
          String errorMess = bundle.getString("errormessage");
          // mainActivity.showMessage("扫码异常，异常码：" + errorCode + ",异常信息：" + errorMess+ "\r\n", MessageTag.NORMAL);
          Map map0 = new HashMap();
          map0.put("status", FAILED);
          map0.put("msg", "扫码异常，异常码：" + errorCode + ",异常信息：" + errorMess);
          Log.d(LOG_TAG, (new JSONObject(map0)).toString() );
          Constant.asynMsg = (new JSONObject(map0)).toString();
          map0.put("event","scancode");
          sendUpdate( new JSONObject(map0), true );
          break;
        }

        default:
          break;
      }
    }

  };
  public CallbackContext getPosCallbackContext() {
    return posCallbackContext;
  }

  /**
   * Create a new plugin result and send it back to JavaScript
   *
   * @param*connection the network info to set as navigator.connection
   */
  private static void sendUpdate(JSONObject info, boolean keepCallback) {
    if (posCallbackContext != null) {
      Map map0 = new HashMap();
      map0.put("info", info.toString());
      PluginResult result = new PluginResult(PluginResult.Status.OK, new JSONObject(map0) );
      //PluginResult result = new PluginResult(PluginResult.Status.OK, info.toString() );
      result.setKeepCallback(keepCallback);
      posCallbackContext.sendPluginResult(result);
    }
  }
}
