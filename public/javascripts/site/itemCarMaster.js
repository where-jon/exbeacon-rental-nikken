
// サブミット
function doSubmit(formId, action){
    $('#' + formId).attr('action', action)
    $('#' + formId).submit();
}


// サブミット
function getFilterCheck(){
    var inputItemType = document.getElementById("itemTypeId")
    if(inputItemType!=null){
         $('#ITEM_TYPE_FILTER').val(inputItemType.value)
    }
    var inputCompanyName = document.getElementById("companyName")
    if(inputCompanyName!=null){
         $('#COMPANY_NAME_FILTER').val(inputCompanyName.value)
    }
    var inputFloorName = document.getElementById("floorName")
    if(inputFloorName!=null){
         $('#FLOOR_NAME_FILTER').val(inputFloorName.value)
    }
    var inputWorkTypeName = document.getElementById("workTypeName")
    if(inputWorkTypeName!=null){
         $('#WORK_TYPE_FILTER').val(inputWorkTypeName.value)
    }
}
// 表示ボタンをクリックする時に発生するイベント
function viewBtnEvent(){
    var viewBtnElement = document.getElementsByClassName("btn__view--frame")[0];
    viewBtnElement.addEventListener('click', function(event) {
        // itemTypeId結果をfromへ設定
        var itemTypeFilterResult = $('#ITEM_TYPE_FILTER option:selected').val();
        var inputItemType = document.getElementById("itemTypeId")
        inputItemType.value = itemTypeFilterResult
        // floorName結果をfromへ設定
        var floorNameFilterResult = $('#FLOOR_NAME_FILTER option:selected').val();
        var inputFloorName = document.getElementById("floorName")
        inputFloorName.value = floorNameFilterResult

        // companyName結果をfromへ設定
        var companyNameFilterResult = $('#COMPANY_NAME_FILTER option:selected').val();
        var inputCompanyName = document.getElementById("companyName")
        inputCompanyName.value = companyNameFilterResult

        // work_type_name結果をfromへ設定
        var workTypeNameFilterResult = $('#WORK_TYPE_FILTER option:selected').val();
        var inputWorkTypeName = document.getElementById("workTypeName")
        inputWorkTypeName.value = workTypeNameFilterResult

        var formElement = $("#viewForm")
        formElement[0].action = "../site/itemCarMaster"
        // 送信ボタン生成
        var vButton = document.createElement("button");
        vButton.id = "dbExecuteBtn"
        vButton.className = "btn hidden";
        formElement[0].appendChild(vButton);

        $("#dbExecuteBtn").trigger( "click" );
    });

}
$(function(){
    // filter値確認
    getFilterCheck();
    // 表示ボタンをクリック
    viewBtnEvent();

    // テーブルを固定
    gInitView.fixTableNoBtn();

    // マウス操作とタップ操作をバインド
    gInitView.bindMouseAndTouch();

    // 画面サイズ変更による再調整
    gInitView.tableResize("NoBtn");
});
