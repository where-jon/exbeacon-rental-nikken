
// 表示ボタンをクリックする時に発生するイベント
function btnEvent(){

}

$(function(){
    // 表示ボタンをクリック
    btnEvent();

    // テーブルを固定
    gInitView.fixTable();

    // マウス操作とタップ操作をバインド
    gInitView.bindMouseAndTouch();

});
