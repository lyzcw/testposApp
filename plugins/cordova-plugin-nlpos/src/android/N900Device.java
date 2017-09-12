package com.openunion.cordova.plugins.nlpos;

import android.newland.SettingsManager;
import android.newland.content.NlContext;
import android.os.Handler;
import android.util.Log;

import com.newland.me.ConnUtils;
import com.newland.me.DeviceManager;
import com.newland.mtype.ConnectionCloseEvent;
import com.newland.mtype.Device;
import com.newland.mtype.ExModuleType;
//import com.newland.mtype.ExModuleType;
import com.newland.mtype.ModuleType;
import com.newland.mtype.event.DeviceEventListener;
import com.newland.mtype.module.common.cardreader.CardReader;
import com.newland.mtype.module.common.cardreader.K21CardReader;
import com.newland.mtype.module.common.emv.EmvModule;
import com.newland.mtype.module.common.externalPin.ExternalPinInput;
import com.newland.mtype.module.common.externalrfcard.ExternalRFCard;
import com.newland.mtype.module.common.externalsignature.ExternalSignature;
import com.newland.mtype.module.common.iccard.ICCardModule;
import com.newland.mtype.module.common.light.IndicatorLight;
import com.newland.mtype.module.common.pin.K21Pininput;
import com.newland.mtype.module.common.printer.Printer;
import com.newland.mtype.module.common.rfcard.K21RFCardModule;
import com.newland.mtype.module.common.rfcard.RFCardModule;
import com.newland.mtype.module.common.scanner.BarcodeScanner;
import com.newland.mtype.module.common.scanner.BarcodeScannerManager;
import com.newland.mtype.module.common.security.SecurityModule;
import com.newland.mtype.module.common.serialport.SerialModule;
import com.newland.mtype.module.common.sm.SmModule;
//import com.newland.mtype.module.common.serialport.SerialModule;6
import com.newland.mtype.module.common.storage.Storage;
import com.newland.mtype.module.common.swiper.K21Swiper;
import com.newland.mtypex.nseries.NSConnV100ConnParams;
import com.newland.mtypex.nseries3.NS3ConnParams;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class N900Device extends AbstractDevice {
	private final static String LOG_TAG = "openunion.nlpos";
	private static final String K21_DRIVER_NAME = "com.newland.me.K21Driver";
	private static final String SUCCESS = "success";
	private static final String FAILED = "failed";
	private static CordovaInterface cordova = null;
	private static N900Device n900Device=null;
	private static DeviceManager deviceManager=null;
	public  static CallbackContext callbackContext=null;

	Map map = new HashMap();

	private N900Device(CordovaInterface cordova ) {
    	N900Device.cordova = cordova;
  	}

	public static N900Device getInstance(CordovaInterface cordova) {
		if (n900Device == null) {
			synchronized (N900Device.class) {
				if (n900Device == null) {
					n900Device = new N900Device( cordova );
				}
			}
		}
		N900Device.cordova = cordova;
		return n900Device;
	}

	@Override
	public Map connectDevice(){
		// Map map = new HashMap();
		// map.put("status", SUCCESS);
		// map.put("msg", "设备连接成功");
		// return map;

		try {
			deviceManager = ConnUtils.getDeviceManager();
			deviceManager.init(cordova.getActivity(), K21_DRIVER_NAME, new NS3ConnParams(), new DeviceEventListener<ConnectionCloseEvent>() {
				@Override
				public void onEvent(ConnectionCloseEvent event, Handler handler) {
					if (event.isSuccess()) {
						map.put("status", FAILED);
						map.put("msg", "设备被客户主动断开！");
					}
					if (event.isFailed()) {
						map.put("status", FAILED);
						map.put("msg", "设备链接异常断开！");
					}

					callbackContext.success((new JSONObject(map)).toString());

				}

				@Override
				public Handler getUIHandler() {
					return null;
				}
			});
			try {
				deviceManager.connect();
				deviceManager.getDevice().setBundle(new NS3ConnParams());
				map.put("status", SUCCESS);
				map.put("msg", "设备连接成功");
			} catch (Throwable e) {
				e.printStackTrace();
				map.put("status", FAILED);
				map.put("msg", "设备连接异常,请检查设备或重新连接...");
			}
		} catch (Exception e1) {
			e1.printStackTrace();
			map.put("status", FAILED);
			map.put("msg", "设备连接异常,请检查设备或重新连接");
		}
		return map;

	}

	@Override
	public Map disconnect(){
		Map map = new HashMap();
		// new Thread(new Runnable() {
		// 	@Override
		// 	public void run() {
				try {
					if (deviceManager != null) {
						deviceManager.disconnect();
						deviceManager = null;
						map.put("status", SUCCESS);
						map.put("msg", "设备已断开");
					}
				} catch (Exception e ) {
					map.put("status", FAILED);
					map.put("msg", "设备断开异常:" +  e.getMessage() );
				}
		//	}
		// }).start();
		return map;
	}

	@Override
	public boolean isDeviceAlive() {
		boolean ifConnected = ( deviceManager== null ? false : deviceManager.getDevice().isAlive());
        return ifConnected;
	}

	@Override
	public K21CardReader getCardReaderModuleType() {
		K21CardReader cardReader=(K21CardReader) deviceManager.getDevice().getStandardModule(ModuleType.COMMON_CARDREADER);
		return cardReader;
	}

	@Override
	public EmvModule getEmvModuleType() {
		EmvModule emvModule=(EmvModule) deviceManager.getDevice().getExModule("EMV_INNERLEVEL2");
		return emvModule;
	}

	@Override
	public ICCardModule getICCardModule() {
		ICCardModule iCCardModule=(ICCardModule) deviceManager.getDevice().getStandardModule(ModuleType.COMMON_ICCARDREADER);
		return iCCardModule;
	}

	@Override
	public IndicatorLight getIndicatorLight() {
		IndicatorLight indicatorLight=(IndicatorLight) deviceManager.getDevice().getStandardModule(ModuleType.COMMON_INDICATOR_LIGHT);
		return indicatorLight;
	}

	@Override
	public K21Pininput getK21Pininput() {
		K21Pininput k21Pininput=(K21Pininput) deviceManager.getDevice().getStandardModule(ModuleType.COMMON_PININPUT);
		return k21Pininput;
	}

	@Override
	public Printer getPrinter() {
		Printer printer=(Printer) deviceManager.getDevice().getStandardModule(ModuleType.COMMON_PRINTER);
		printer.init();
		return printer;
	}

	@Override
	public K21RFCardModule getRFCardModule() {
		K21RFCardModule rFCardModule=(K21RFCardModule) deviceManager.getDevice().getStandardModule(ModuleType.COMMON_RFCARDREADER);
		return rFCardModule;
	}

	@Override
	public BarcodeScanner getBarcodeScanner() {
		BarcodeScannerManager barcodeScannerManager=(BarcodeScannerManager) deviceManager.getDevice().getStandardModule(ModuleType.COMMON_BARCODESCANNER);
		BarcodeScanner scanner = barcodeScannerManager.getDefault();
		return scanner;
	}

	@Override
	public SecurityModule getSecurityModule() {
		SecurityModule securityModule=(SecurityModule) deviceManager.getDevice().getStandardModule(ModuleType.COMMON_SECURITY);
		return securityModule;
	}

	@Override
	public Storage getStorage() {
		Storage storage=(Storage) deviceManager.getDevice().getStandardModule(ModuleType.COMMON_STORAGE);
		return storage;
	}

	@Override
	public K21Swiper getK21Swiper() {
		K21Swiper k21Swiper=(K21Swiper) deviceManager.getDevice().getStandardModule(ModuleType.COMMON_SWIPER);
		return k21Swiper;
	}

	@Override
	public Device getDevice() {
		return deviceManager.getDevice();
	}

	@Override
	public SerialModule getUsbSerial() {
		SerialModule k21Serial=(SerialModule) deviceManager.getDevice().getExModule(ExModuleType.USBSERIAL);
		return k21Serial;
	}

	@Override
	public SmModule getSmModule() {
		SmModule smModule = (SmModule)deviceManager.getDevice().getStandardModule(ModuleType.COMMON_SM);
		return smModule;
	}

}
