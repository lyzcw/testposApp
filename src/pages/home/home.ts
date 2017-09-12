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
  constructor(private platform: Platform, private events: Events, public navCtrl: NavController, public renderer: Renderer) {
    platform.ready().then((e) => {
      
      window.addEventListener('readcard', this.onReadCard, false );
      window.addEventListener('scancode', this.onScanCode, false );
      //this.events.subscribe('readcard', () => this.onReadCard( e ));
      
    });

  }

  homeModel: HomeModel = {"response":"等待POS响应中..."};
  
  onReadCard( data ): void{
    alert('刷卡信息：' + data.info);
    console.log('有刷卡：' + data.info );
    let temp = this.homeModel.response;
    this.homeModel.response = "\r\n*******有刷卡事件*******\r\n" + data.info; // + "Info:" + data;
  }

  onScanCode( data ): void{
    alert('扫码信息：' + data.info);
    console.log('扫码：' + data.info );
    let temp = this.homeModel.response;
    this.homeModel.response = "\r\n*******有扫码事件*******\r\n" + data.info; // + "Info:" + data;
  }

  openCardReader(): void {
    
    let promise = new Promise((resolve, reject) => {
      cordova.plugins.nlpos.openCardReader( data => {
        this.homeModel.response = data;
        resolve(data);
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
        this.homeModel.response = data;
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
        this.homeModel.response = data;
        resolve(data);
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
