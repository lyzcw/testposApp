package com.openunion.cordova.plugins.nlpos;

import com.newland.mtype.module.common.swiper.K21Swiper;
import com.newland.mtype.module.common.swiper.SwipResult;
import com.newland.mtype.module.common.swiper.SwipResultType;
import com.newland.mtype.module.common.swiper.SwiperReadModel;

import org.apache.cordova.LOG;

import java.util.HashMap;
import java.util.Map;
/**
 * Created by lyzcw on 2017/9/6.
 */

public class SwipRead {

  public K21Swiper swiper;
  private final static String LOG_TAG = "openunion.nlpos";
  private static final String SUCCESS = "success";
  private static final String FAILED = "failed";
  private String showMsg = "";
  private Map map = new HashMap();

  public Map readExpress(){

//    new Thread(new Runnable() {
//
//      @Override
//      public void run() {
        try {
          showMsg = "开始明文方式返回刷卡结果:" + "\r\n";
          LOG.d(LOG_TAG, showMsg);
          SwipResult swipRslt = swiper.readPlainResult(new SwiperReadModel[] {
            SwiperReadModel.READ_SECOND_TRACK, SwiperReadModel.READ_THIRD_TRACK });
          if (null != swipRslt && swipRslt.getRsltType() == SwipResultType.SUCCESS) {
            byte[] secondTrack = swipRslt.getSecondTrackData();
            byte[] thirdTrack = swipRslt.getThirdTrackData();
            showMsg = "二磁道:" + new String(secondTrack,"gbk");
            LOG.d(LOG_TAG, showMsg);
            showMsg = "三磁道:" +(thirdTrack==null?"null": new String(thirdTrack,"gbk"));
            LOG.d(LOG_TAG, showMsg);
            map.put("status", SUCCESS);
            map.put("msg", "读取磁条卡数据成功");
            Map map0 = new HashMap();
            map0.put("track-2", new String(secondTrack,"gbk"));
            map0.put("track-3", thirdTrack==null?"null": new String(thirdTrack,"gbk"));
            map.put("data", map0 );
          } else {
            showMsg = "刷卡结果  空了" + "\r\n";
            LOG.d(LOG_TAG, showMsg);
          }

        } catch (Exception e) {
          showMsg = "获取磁道明文异常：" + e + "\r\n";
          showMsg = "是否已经加载主密钥、工作秘钥、AID、公钥！";
          showMsg = "是否已经开启读卡器并刷卡！";
          map.put("status", FAILED);
          map.put("msg", showMsg );
        }
//      }
//    }).start();
    return map;
  }
}
