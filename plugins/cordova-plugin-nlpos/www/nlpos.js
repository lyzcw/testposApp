/**
 * This class contains information about the current pos status.
 * @constructor
 */
var cordova = require('cordova'),
    exec = require('cordova/exec');

var NLPos = function() {
    
    this.print = function( bill, success, error ) {
      return exec(success, error, 'nlpos', 'print', [bill]);
    };
    // Create new event handlers on the window (returns a channel instance)
    this.channels = {
      readcard:cordova.addWindowEventHandler("readcard"),
      scancode:cordova.addWindowEventHandler("scancode")
    };
    // for (var key in this.channels) {
    //     if( key=="readcard"){
    //       this.channels[key].onHasSubscribersChange = NLPos.onReadCard;
    //     }else if( key=="scancode"){
    //       this.channels[key].onHasSubscribersChange = NLPos.onScanCode;
    //     }
        
    // }
};

function handlers() {
    return nlpos.channels.readcard.numHandlers
        +nlpos.channels.scancode.numHandlers;
        // battery.channels.batterycritical.numHandlers;
}

/**
 * Event handlers for when callbacks getregistered for the pos.
 * Keep track of how many handlers wehave so we can start and stop the native pos listener
 * appropriately (and hopefully save pos life!).
 */
NLPos.onReadCard = function(readTimeout) {
  alert("onReadCard");
  exec(nlpos._status, nlpos._error,"nlpos", "openCardReader", [readTimeout]);
};

NLPos.onScanCode = function() {
  alert("onScanCode");
  exec(nlpos._status, nlpos._error,"nlpos", "scan", []);
};

NLPos.prototype.openCardReader = function(readTimeout, success, error) {
  return exec(nlpos._status, error, 'nlpos', 'openCardReader', [readTimeout]);
};

NLPos.prototype.closeCardReader = function(success, error) {
  return exec(success, error,'nlpos', 'closeCardReader', []);
};

NLPos.prototype.scan = function(success, error) {
  return exec(nlpos._status, error, 'nlpos', 'scan', []);
};

NLPos.prototype.getAsynMsg = function(success, error) {
  return exec(success, error,'nlpos', 'getAsynMsg', []);
};

/**
 * Callback for nlpos readcard status
 *
 * @param {Object} info           keys: info
 */
NLPos.prototype._status = function ( info ) {
  //Something changed. Fire ReadCard event
  
  var tempStr = info.info;
  var jsonObj = JSON.parse(tempStr);
  var eventStr = jsonObj.event;
  if( eventStr=="readcard" ){
    cordova.fireWindowEvent("readcard",info);
  }else if( eventStr=="scancode" ){
    cordova.fireWindowEvent("scancode",info);
  }else{
    console.log("不返回的消息: " + tempStr );
  }         
  
};

/**
 * Error callback for NLPos start
 */
NLPos.prototype._error = function(e) {
    console.log("Error initializing NLPos: " + e);
};

var nlpos = new NLPos(); // jshint ignore:line

module.exports = nlpos;
