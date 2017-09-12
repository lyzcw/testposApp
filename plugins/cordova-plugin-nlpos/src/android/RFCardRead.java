package com.openunion.cordova.plugins.nlpos;

import com.newland.mtype.DeviceInvokeException;
import com.newland.mtype.module.common.rfcard.K21RFCardModule;
import com.newland.mtype.module.common.rfcard.RFCardType;
import com.newland.mtype.module.common.rfcard.RFKeyMode;
import com.newland.mtype.module.common.rfcard.RFResult;
import com.newland.mtype.util.Dump;
import com.newland.mtype.util.ISOUtils;

import org.apache.cordova.LOG;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lyzcw on 2017/9/6.
 */

public class RFCardRead {
  public K21RFCardModule rfCardModule;
  private final static String LOG_TAG = "openunion.nlpos";
  private static final String SUCCESS = "success";
  private static final String FAILED = "failed";
  private String snr;
  private String showMsg = "";
  /**
   * M1卡上电.
   */
  public void m1CardPowerOn() {
    try {
      new Thread(new Runnable() {

        @Override
        public void run() {
          LOG.d(LOG_TAG, "M1卡上电，请贴非接卡！");
          try {
            List<RFCardType> cardTypeList=new ArrayList<RFCardType>();
            //if (arrayWorkingKeySelected[0]) {
              cardTypeList.add(RFCardType.ACARD);
            //}
            //if (arrayWorkingKeySelected[1]) {
              cardTypeList.add(RFCardType.BCARD);

            //}
            //if(arrayWorkingKeySelected[2]){
              cardTypeList.add(RFCardType.M1CARD);
            //}
            RFResult qPResult = rfCardModule.powerOn(cardTypeList.toArray(new RFCardType[cardTypeList.size()]),8, TimeUnit.SECONDS);
//										RFResult qPResult = rfCardModule.powerOn(RFCardType.ACARD, 8);		//卡类型可以为null,就包括了所以非接卡类型
            showMsg = "非接卡名:" + qPResult.getQpCardType() + "\r\n" +
                      "非接卡类型:" + qPResult.getQpCardType() + "\r\n";
            LOG.d(LOG_TAG, showMsg);
            if (qPResult.getCardSerialNo() == null) {
              showMsg="非接卡序列号:null" + "\r\n";
            } else {
              showMsg = "非接卡序列号:" + ISOUtils.hexString(qPResult.getCardSerialNo()) + "\r\n";
            }
            LOG.d(LOG_TAG, showMsg);
            snr = ISOUtils.hexString(qPResult.getCardSerialNo());
            LOG.d(LOG_TAG, "非接卡序列号：" + snr );
            if (qPResult.getATQA() == null) {
              showMsg = "非接卡ATQA:null" + "\r\n";
            } else {
              showMsg = "非接卡ATQA:" + Dump.getHexDump(qPResult.getATQA())+ "\r\n";
            }
            LOG.d(LOG_TAG, showMsg);
            showMsg = "寻卡上电完成" + "\r\n";
            LOG.d(LOG_TAG, showMsg);
          }catch(Exception e){e.fillInStackTrace();
            showMsg = "非接卡寻卡上电异常:" + e.getMessage() + "\r\n";
            LOG.d(LOG_TAG, showMsg);
          }
        }
      }).start();

    } catch (Exception e) {
      showMsg = "非接卡寻卡上电异常:" + e.getMessage() + "\r\n";
      LOG.d(LOG_TAG, showMsg);
    }

  }

  /**
   * 外认.
   */
  public void authenticateByExtendKey(){
    try {
      RFKeyMode qpKeyMode = Constant.qpKeyMode;
      int block = Constant.block;
      byte snr[] = Constant.snr;
      byte key[] = Constant.key;

      showMsg = "KEY模式:" + qpKeyMode + "\r\n";
      LOG.d(LOG_TAG, showMsg);
      showMsg = "SNR序列号:" + (snr==null?"null":ISOUtils.hexString(snr));
      LOG.d(LOG_TAG, showMsg);
      showMsg = "认证块号:" + block + "\r\n";
      LOG.d(LOG_TAG, showMsg);
      showMsg = "外部密钥:" + (key==null?"null":ISOUtils.hexString(key));
      LOG.d(LOG_TAG, showMsg);
      rfCardModule.authenticateByExtendKey(qpKeyMode, snr, block, key);
      showMsg = "非接卡使用外部密钥认证完成";
      LOG.d(LOG_TAG, showMsg);
    } catch (DeviceInvokeException e) {
      showMsg = e.getMessage();
      LOG.d(LOG_TAG, showMsg);
    } catch (Exception e) {
      showMsg = "非接卡外部密钥认证异常" + e ;
      LOG.d(LOG_TAG, showMsg);
    }
  }

  /**
   * 读块.
   */
  public Map readBlock() {
    Map map = new HashMap();

    try {
      int block = Constant.block;

      byte output[] = rfCardModule.readDataBlock(block);
      showMsg = "存储块:" + block;

      LOG.d(LOG_TAG, showMsg);
      showMsg = "数据:" + (output==null?"null":ISOUtils.hexString(output));

      LOG.d(LOG_TAG, showMsg);
      showMsg = "读块数据完成";
      LOG.d(LOG_TAG, showMsg);
      map.put("status", SUCCESS);
      map.put("msg", showMsg);
      Map map1 = new HashMap();
      map1.put("cardType", "M1CARD");
      map1.put("typeName", "非接S50");
      map1.put("block", block + "");
      map1.put("data", (output==null?"null":ISOUtils.hexString(output)) + "");
      map.put("data", map1);

    } catch (Exception e) {
      showMsg = "读块数据异常:" + e.getMessage();
      LOG.d(LOG_TAG, showMsg);
      showMsg = "请先确定非接卡已上电或该数据块已写入数据";
      LOG.d(LOG_TAG, showMsg);
      map.put("status", FAILED );
      map.put("msg", showMsg);
    }

    return map;
  }

}
