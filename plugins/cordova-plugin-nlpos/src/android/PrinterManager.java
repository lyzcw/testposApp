package com.openunion.cordova.plugins.nlpos;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.newland.mtype.log.DeviceLogger;
import com.newland.mtype.log.DeviceLoggerFactory;
import com.newland.mtype.module.common.printer.PrintContext;
import com.newland.mtype.module.common.printer.Printer;
import com.newland.mtype.module.common.printer.PrinterResult;
import com.newland.mtype.util.ISOUtils;

 public class PrinterManager {
    private DeviceLogger logger=DeviceLoggerFactory.getLogger(PrinterManager.class);
	private static final int timeOut=30;
	int lineLength = 32;
	private static PrinterManager printerManager;

	private PrinterManager(){

	}

	public static synchronized PrinterManager getInstance(){
		if(printerManager==null){
			printerManager=new PrinterManager();
		}
		return printerManager;
	}

   /**
    * 立即打印文本内容（左右对齐模式），打印完成后自动回车换行（注：超过打印宽度自动换行）
    * @param textLeftAlign： 左边对齐的文本
    * @param textRightAlign：右边对齐的文本
    * @param fontProperty: 参考字体属性定义
    * @return ret 0：成功
    *            -1：失败
    *          -999: 不支持该功能（可以不支持）
    */
    public int prtMultiText(Printer printer,String textLeftAlign, String textRightAlign, int fontProperty){
    	int result = -999;
    	printer.init();
		String fontSet = setFont(fontProperty);
		logger.info("fontSet====="+fontSet);
		logger.info("lineLen============"+lineLength);
		int leftLen = getStringLen(textLeftAlign);
		int rightChineseNum = getChinStringLen(textRightAlign);//右边的中文个数
		String totalString = "";
		String str = ISOUtils.padleft(textRightAlign, (lineLength-leftLen)-rightChineseNum, ' ');
		totalString = textLeftAlign + str;

		try {
			PrinterResult rslt = printer.printByScript(PrintContext.defaultContext(), (fontSet+"*text l "+totalString+"\n").getBytes("GBK"), timeOut, TimeUnit.SECONDS);
			if(rslt==PrinterResult.SUCCESS){
				result=0;
			}
		} catch (Exception e) {
			result = -1;
			e.printStackTrace();
			logger.error("-----------printMultiText-----error-----"+e );
		}
		return result;
    }
    /**
     * 立即打印文本内容（左中右对齐模式），打印完成后自动回车换行（注：超过打印宽度自动换行）中间是左右打印的中间
     * @param textLeftAlign： 左边对齐的文本
     * @param textCenterAlign：中间对齐文本
     * @param textRightAlign：右边对齐的文本
     * @param fontProperty: 参考字体属性定义
     * @return ret 0：成功
     *            -1：失败
     *          -999: 不支持该功能（可以不支持）
     */
     public int prtMultiText2(Printer printer,String textLeftAlign,String textCenterAlign, String textRightAlign, int fontProperty){
     	int result = -999;
     	printer.init();
 		String fontSet = setFont(fontProperty);
 		logger.info("fontSet====="+fontSet);
 		logger.info("lineLen============"+lineLength);
 		int leftLen = getStringLen(textLeftAlign);
 		int rightChineseNum = getChinStringLen(textRightAlign);//右边的中文个数
 		int centerChineseNum = getChinStringLen(textCenterAlign);//中间的中文个数
 		int centerNum = getStringLen(textCenterAlign);//中间总个数
 		int rightNum = getStringLen(textRightAlign);//右边总个数
 		String totalString = "";

 		String str = ISOUtils.padleft(textRightAlign, lineLength-leftLen-rightChineseNum, ' ');
 		int i=(str.length()-(rightNum-rightChineseNum)-centerNum)/2;
 		StringBuffer b=new StringBuffer(str);
		StringBuffer strc = b.replace(i,centerNum+i,textCenterAlign);
 		totalString = textLeftAlign+strc;
 		try {
 			PrinterResult rslt = printer.printByScript(PrintContext.defaultContext(), (fontSet+"*text l "+totalString+"\n").getBytes("GBK"), timeOut, TimeUnit.SECONDS);
 			if(rslt==PrinterResult.SUCCESS){
 				result=0;
 			}
 		} catch (Exception e) {
 			result = -1;
 			e.printStackTrace();
 			logger.error("-----------printMultiText-----error-----"+e );
 		}
 		return result;
     }
     /**
      * 立即打印文本内容（左中右对齐模式），打印完成后自动回车换行（注：超过打印宽度自动换行） 中间是打印纸的中间
      * @param textLeftAlign： 左边对齐的文本
      * @param textCenterAlign：中间对齐文本
      * @param textRightAlign：右边对齐的文本
      * @param fontProperty: 参考字体属性定义
      * @return ret 0：成功
      *            -1：失败
      *          -999: 不支持该功能（可以不支持）
      */
      public int prtMultiText3(Printer printer,String textLeftAlign,String textCenterAlign, String textRightAlign, int fontProperty){
      	int result = -999;
      	printer.init();
  		String fontSet = setFont(fontProperty);
  		logger.info("fontSet====="+fontSet);
  		logger.info("lineLen============"+lineLength);
  		int leftLen = getStringLen(textLeftAlign);
  		int rightChineseNum = getChinStringLen(textRightAlign);//右边的中文个数
  		int centerChineseNum = getChinStringLen(textCenterAlign);//中间的中文个数
  		int centerNum = getStringLen(textCenterAlign);//中间总个数
  		int rightNum = getStringLen(textRightAlign);//右边总个数
  		String totalString = "";

  		String str = ISOUtils.padleft(textRightAlign, lineLength-leftLen-rightChineseNum, ' ');

 		int i=(lineLength)/2-leftLen-(centerNum/2);
 		StringBuffer b=new StringBuffer(str);
		StringBuffer strc = b.replace(i,centerNum+i,textCenterAlign);
 		totalString = textLeftAlign+strc;

 		logger.debug("i="+i+"str"+str.length());
  		try {
  			PrinterResult rslt = printer.printByScript(PrintContext.defaultContext(), (fontSet+"*text l "+totalString+"\n").getBytes("GBK"), timeOut, TimeUnit.SECONDS);
  			if(rslt==PrinterResult.SUCCESS){
  				result=0;
  			}
  		} catch (Exception e) {
  			result = -1;
  			e.printStackTrace();
  			logger.error("-----------printMultiText-----error-----"+e );
  		}
  		return result;
      }
  	//设置打印字体fontProperty
  	private String setFont(int fontProperty) {
  		StringBuffer buffer = new StringBuffer();
  		String data=ISOUtils.hexString(ISOUtils.intToBytes(fontProperty, true));
  		logger.info("data="+data);
  		char fontSize = data.charAt(0);
  		char fontScale = data.charAt(1);

  		logger.info("fontSize="+fontSize+"fontScale="+fontScale);

  		if(fontSize=='1'){//普通字号,一行32个（按英文字符长度算）
  			try {
  					lineLength = 32;
  					buffer.append("!hz n\n"+"!asc n\n");
  			} catch (Exception e) {
  				e.printStackTrace();
  				logger.error("设置普通字号字体异常");
  			}
  		}else if(fontSize=='2'){//小字号,一行48个（按英文字符长度算）
  			try {
  					lineLength = 48;
  					buffer.append("!asc s\n"+"!hz s\n");
  			} catch (Exception e) {
  				e.printStackTrace();
  				logger.error("设置小号字体异常");
  			}
  		}else if(fontSize=='3'){//大字号,一行24个（按英文字符长度算）
  			try {
  					lineLength = 24;
  					buffer.append("!asc l\n"+"!hz l\n");
  			} catch (Exception e) {
  				e.printStackTrace();
  				logger.error("设置大号字体异常");
  			}
  		}


  		if(fontScale=='1'){//未缩放


  		}else if(fontScale=='2'){//双倍高
  			switch (fontSize) {
  			case '1'://普通字号
//  				buffer.append("!hz nl\n"+"!asc nl\n");
  				buffer.append("!NLFONT 1 12 2\n");
  				break;
  			case '2'://小号
//  				buffer.append("!hz sn\n"+"!asc sn\n");
  				buffer.append("!NLFONT 6 1 2\n");
  				break;

  			case '3'://大号
//  				buffer.append("!hz l\n"+"!asc l\n");
  				buffer.append("!NLFONT 3 13 2\n");
  				break;

  			default:
  				break;
  			}
  		}else if(fontScale=='3'){//双倍宽
  			switch (fontSize) {
  			case '1'://普通字号
  				lineLength = 24;
  				buffer.append("!NLFONT 1 12 1\n");
//  				buffer.append("!hz ln\n"+"!asc ln\n");
  				break;
  			case '2'://小号
  				lineLength = 32;
  				buffer.append("!NLFONT 6 1 1\n");
//  				buffer.append("!hz ns\n"+"!asc ns\n");
  				break;

  			case '3'://大号
  				lineLength = 24;
//  				buffer.append("!hz l\n"+"!asc l\n");
  				buffer.append("!NLFONT 3 13 1\n");
  				break;

  			default:
  				break;
  			}

  		}else if(fontScale=='4'){//双倍高.双倍宽
  			switch (fontSize) {
  			case '1'://普通字号
  				lineLength = 24;
  				buffer.append("!NLFONT 1 12 0\n");
//  				buffer.append("!hz l\n"+"!asc l\n");
  				break;
  			case '2'://小号
  				lineLength = 32;
  				buffer.append("!NLFONT 6 1 0\n");
//  				buffer.append("!hz n\n"+"!asc n\n");
  				break;

  			case '3'://大号
  				lineLength = 24;
//  				buffer.append("!hz l\n"+"!asc l\n");
  				buffer.append("!NLFONT 3 13 0\n");
  				break;

  			default:
  				break;
  			}
  		}
  		return buffer.toString();
  	}

  	//获取输入字符串的长度（按英文字符长度算）
  	private int getStringLen(String str) {
  		Pattern p = Pattern.compile("^[\\u4e00-\\u9fa5]$");
  		int i = 0;
  		for(char c : str.toCharArray()) {
  		Matcher m = p.matcher(String.valueOf(c));
  		i += m.find() ? 2 : 1;

  		}
  		return i;
  	}

  	//判断右边字符串有几个中文
  	private int getChinStringLen(String str) {
  		Pattern p = Pattern.compile("^[\\u4e00-\\u9fa5]$");
  		int i = 0;
  		for (char c : str.toCharArray()) {
  			Matcher m = p.matcher(String.valueOf(c));
  			if (m.find()) {
  				i++;
  			}
  		}
  		return i;
  	}
}
