import { Component, ViewChild, Renderer } from '@angular/core';
import { NavController } from 'ionic-angular';
import { HomeModel } from './home-model';
import { Platform, Events } from 'ionic-angular';

declare let cordova: any;

@Component({
  selector: 'page-home',
  templateUrl: 'home.html'
})
export class HomePage {
  @ViewChild('response1') response1: any;

  //private events: Events,
  constructor(private platform: Platform, public navCtrl: NavController, public renderer: Renderer) {
    platform.ready().then((e) => {
      // window.addEventListener('readcard', function(e) {
      //   alert('有刷卡' + e);
      //   console.log('有刷卡');
      //   //let temp = this.homeModel.response;
      //   //this.homeModel.response = temp + "\r\n*******有刷卡事件*******\r\n" + "Event:" + e;// + "Info:" + data;
      // }, false );
      //window.addEventListener('readcard', this.onReadCard, false );

      //this.events.subscribe('readcard', () => this.onReadCard( e ));
      
    });

  }

  homeModel: HomeModel = {"response":"等待POS响应中..."};
  
  onReadCard( data ): void{
    //alert('有刷卡：' + data.info);
    console.log('有刷卡：' + data.info );
    let temp = this.homeModel.response;
    this.homeModel.response = "\r\n*******有刷卡事件*******\r\n" + "Event:" + data.info; // + "Info:" + data;
  }

  openCardReader(): void {
    
    let promise = new Promise((resolve, reject) => {
      cordova.plugins.nlpos.openCardReader( data => {
        let temp = this.homeModel.response;
        this.homeModel.response = data; //temp + "\r\n******************************************\r\n" + data;
        // this.renderer.setElementStyle(this.response1.nativeElement, 'scrollTop', this.response1.nativeElement.clientHeight + 'px');
        let timeout=240;
        const interval =setInterval(() => {
          console.log('取异步数据：' + timeout + ":次");
          if( timeout == 0){
            clearInterval(interval);
          }else{
            this.getAsynMsg().then(data => {
              console.log('取到异步数据2：' + data );
              if( ""!=data && data != undefined && null != data){
                this.homeModel.response = data;
                clearInterval(interval);
                resolve(data);
              }
            });
            
          }
          timeout--;
        }, 500);
        
      }, error => {
        reject(error);
      });
    });
  }

  getAsynMsg(): Promise<any> {

    let promise = new Promise((resolve, reject) => {
      cordova.plugins.nlpos.getAsynMsg( data => {
        resolve(data);
      }, error => {
        reject(error);
      });
    });  
    return promise;
   
  }

  closeCardReader(): void {
    
    let promise = new Promise((resolve, reject) => {
      cordova.plugins.nlpos.closeCardReader( data => {
        let temp = this.homeModel.response;
        this.homeModel.response = data; //temp + "\r\n******************************************\r\n" + data;
        this.renderer.setElementStyle(this.response1.nativeElement, 'scrollTop', this.response1.nativeElement.clientHeight + 'px');
        resolve(data);
      }, error => {
        reject(error);
      });
    });
    
  }

  scan(): void {
    let promise = new Promise((resolve, reject) => {
      cordova.plugins.nlpos.scan( data => {
        let temp = this.homeModel.response;
        this.homeModel.response = data; //temp + "\r\n******************************************\r\n" + data;
        let timeout=20;
        const interval0 =setInterval(() => {
          console.log('取扫码异步数据：' + timeout + ":次");
          if( timeout == 0){
            clearInterval(interval0);
          }else{
            this.getAsynMsg().then(data => {
              console.log('取到扫码异步数据2：' + data );
              if( ""!=data && data != undefined && null != data){
                this.homeModel.response = data;
                clearInterval(interval0);
                resolve(data);
              }
            });
            
          }
          timeout--;
        }, 500);

      }, error => {
        reject(error);
      });
    });
  }

  print(): void {
    let promise = new Promise((resolve, reject) => {
      let bill = "商户名称：开联支付\n";
      bill += "操作员号(OPERATOR NO.)：001\n";
      bill += "消费类型：消费 \n商户编号:123455432112345\n";
      bill += "-----------------------------\n";
      bill += "+++++++++++++++++++++++++++++\n";
      cordova.plugins.nlpos.print((bill), data => {
        let temp = this.homeModel.response;
        this.homeModel.response = data; //temp + "\r\n******************************************\r\n" + data;
        resolve(data);
      }, error => {
        reject(error);
      });
    });
  }

  clear(): void {
    this.homeModel.response = "等待POS响应中...";
  }
}
