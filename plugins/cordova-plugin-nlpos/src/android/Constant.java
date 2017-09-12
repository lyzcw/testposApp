package com.openunion.cordova.plugins.nlpos;

import com.newland.mtype.module.common.rfcard.RFKeyMode;
import com.newland.mtype.util.ISOUtils;

/**
 * Created by lyzcw on 2017/9/6.
 */

public class Constant {
  //外部秘钥
  public static final byte[] key = ISOUtils.hex2byte("ffffffffffff");
  //认证的块号
  public static final int block = 2;
  //SNR序列号
  public static final byte[] snr = ISOUtils.hex2byte("8B7A84EF");
  //key模式
  public static final RFKeyMode qpKeyMode = RFKeyMode.KEYA_0X60;
  //异步消息
  public static String asynMsg = "";
  //打印
  public static final String icon_path = "/res/drawable/printicon.png";
  public static final String merchant_label = "商户名称(MERCHANT NAME)：";
  public static final String merchant_name = "钓鱼岛";
  public static final String merchant_code_label = "商户编号:：";
  public static final String merchant_code = "123455432112345";

  public static final String operator_label = "操作员号(OPERATOR NO.)：";
  public static final String operator_no = "001";
  public static final String consume_type_label ="消费类型：";
  public static final String consume_type ="消费";
  public static final String signl_line = "-----------------------------";
  public static final String plus_line = "+++++++++++++++++++++++++++++";
}
