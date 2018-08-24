

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

// サブミット
function getFilterCheck(){
    var inputItemType = document.getElementById("itemTypeId")
    if(inputItemType!=null){
         $('#ITEM_TYPE_FILTER').val(inputItemType.value)
    }
    var inputPowerValue = document.getElementById("powerValue")
    if(inputPowerValue!=null){
         $('#POWER_FILTER').val(inputPowerValue.value)
    }
}

// 表示ボタンをクリックする時に発生するイベント
function btnEvent(){

    var viewBtnElement = document.getElementById("viewBtn")
    viewBtnElement.addEventListener('click', function(event) {
        // itemTypeId結果をfromへ設定
        var powerFilterResult = $('#POWER_FILTER option:selected').val();
        var inputPowerValue = document.getElementById("powerValue")
        inputPowerValue.value = powerFilterResult

        var itemTypeFilterResult = $('#ITEM_TYPE_FILTER option:selected').val();
        var inputItemTypeId = document.getElementById("itemTypeId")
        inputItemTypeId.value = itemTypeFilterResult

        dbExecuteManager("../site/txTagManage ")
    });

}
$(function(){
    // filter値確認
    getFilterCheck();
    // 表示ボタンをクリック
    btnEvent();

    // テーブルを固定
    gInitView.fixTableNoBtn();
});
