var arCheckBoxIndex = []
var gReserveCheck = true;

// 初期画面表示用
function getFilterCheck(){

    var inputDate = document.getElementById("inputDate")
    if(inputDate!=null){
        $('#DETECT_MONTH').val(inputDate.value)
    }
}

function dbExecuteManager(routeUrl){

    var formElement = $("#viewForm")
    formElement[0].action = routeUrl
    // 送信ボタン生成
    var vButton = document.createElement("button");
    vButton.id = "dbExecuteBtn"
    vButton.className = "btn hidden";
    formElement[0].appendChild(vButton);

    $("#dbExecuteBtn").trigger( "click" );


}
// 表示ボタンをクリックする時に発生するイベント
function btnEvent(){
    // DatePickerの設定 start---------------------------------------
    gDatePicker.monthClickEvent();
    gDatePicker.htmlDayClickEvent();
    // DatePickerの設定 end-----------------------------------------


    var viewBtnElement = document.getElementById("viewBtn")
    viewBtnElement.addEventListener('click', function(event) {
        // inputDate結果をfromへ設定
        var inputDate = document.getElementById("inputDate")
        inputDate.value = gDatePicker.startSqlTime
        dbExecuteManager("/analysis/movementCar")
    });
}

$(function(){
    // filter値確認
    getFilterCheck();
    // 表示ボタンをクリック
    btnEvent();

    // テーブルを固定
    gInitView.fixTableNoBtnOther();

    // マウス操作とタップ操作をバインド
    gInitView.bindMouseAndTouch();

    // 画面リサイズ
    gInitView.tableResizeOther();

});
