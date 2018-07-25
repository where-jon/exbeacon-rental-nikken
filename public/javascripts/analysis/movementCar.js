var arCheckBoxIndex = []
var gReserveCheck = true;

// サブミット
function getFilterCheck(){
}

function dbExecuteManager(routeUrl){
}
// 表示ボタンをクリックする時に発生するイベント
function btnEvent(){

}

$(function(){
    // filter値確認
    getFilterCheck();
    // 表示ボタンをクリック
    btnEvent();

    // テーブルを固定
    gInitView.fixTable();

    // マウス操作とタップ操作をバインド
    gInitView.bindMouseAndTouch();

});
