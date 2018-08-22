
// サブミット
function getFilterCheck(){
    var inputItemType = document.getElementById("itemTypeId")
    if(inputItemType!=null){
         $('#ITEM_TYPE_FILTER').val(inputItemType.value)
    }
    var inputFloorNameName = document.getElementById("floorName")
    if(inputFloorNameName!=null){
         $('#FLOOR_NAME_FILTER').val(inputFloorNameName.value)
    }

    var inputDate = document.getElementById("inputDate")
    if(inputDate!=null){
         $('#DETECT_DATE').val(inputDate.value)
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
     gDatePicker.todayClickEvent();
     gDatePicker.htmlDayClickEvent();
    // DatePickerの設定 end-----------------------------------------


    var viewBtnElement = document.getElementById("viewBtn")
    viewBtnElement.addEventListener('click', function(event) {
        // itemTypeId結果をfromへ設定
        var itemTypeFilterResult = $('#ITEM_TYPE_FILTER option:selected').val();
        var inputItemType = document.getElementById("itemTypeId")
        inputItemType.value = itemTypeFilterResult

        // floorName結果をfromへ設定
        var floorNameFilterResult = $('#FLOOR_NAME_FILTER option:selected').val();
        var inputFloorName = document.getElementById("floorName")
        inputFloorName.value = floorNameFilterResult

        // inputDate結果をfromへ設定
        var inputDate = document.getElementById("inputDate")
        inputDate.value = gDatePicker.startSqlTime
        dbExecuteManager("../site/unDetected")
    });

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
