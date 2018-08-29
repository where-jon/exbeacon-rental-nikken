var arCheckBoxIndex = []
var gReserveCheck = true;

// サブミット
function getFilterCheck(){
    var inputItemType = document.getElementById("itemTypeId")
    if(inputItemType!=null){
         $('#ITEM_TYPE_FILTER').val(inputItemType.value)
    }
    var inputWorkTypeName = document.getElementById("workTypeName")
    if(inputWorkTypeName!=null){
         $('#WORK_TYPE_FILTER').val(inputWorkTypeName.value)
    }

    var inputDate = document.getElementById("inputDate")
    if(inputDate!=null){
         $('#RESERVE_DATE').val(inputDate.value)
    }
}

function dbExecuteManager(routeUrl){
    var formElement = $("#viewForm")
    formElement[0].action = routeUrl
    //formElement[0].action = "../site/itemCarReserveUpdate"
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
     gDatePicker.dayClickEvent();
     gDatePicker.htmlDayClickEvent();
    // DatePickerの設定 end-----------------------------------------


    //　予約登録ボタン
　　var registerBtnElement = document.getElementById("itemRegisterFooter")
    registerBtnElement.addEventListener('click', function(event) {
        if(gReserveCheck){
            gReserveCheck = false;

            // itemTypeId結果をfromへ設定
            var itemTypeFilterResult = $('#ITEM_TYPE_FILTER option:selected').val();
            var inputItemType = document.getElementById("itemTypeId")
            inputItemType.value = itemTypeFilterResult

            // companyName結果をfromへ設定
            var companyNameFilterResult = $('#COMPANY_NAME_FILTER option:selected').val();
            var inputCompanyName = document.getElementById("companyName")
            inputCompanyName.value = companyNameFilterResult

            // floorName結果をfromへ設定
            var floorNameFilterResult = $('#FLOOR_NAME_FILTER option:selected').val();
            var inputFloorName = document.getElementById("floorName")
            inputFloorName.value = floorNameFilterResult

            // work_type_name結果をfromへ設定
            var workTypeNameFilterResult = $('#WORK_TYPE_FILTER option:selected').val();
            var inputWorkTypeName = document.getElementById("workTypeName")
            inputWorkTypeName.value = workTypeNameFilterResult

            // inputDate結果をfromへ設定
            var inputDate = document.getElementById("inputDate")
            inputDate.value = gDatePicker.startSqlTime

            dbExecuteManager("../site/itemCarReserve/reserve")
        }
    });

    //　予約へボタン
    var reserveBtnElement = document.getElementById("reserveBtn")
    reserveBtnElement.addEventListener('click', function(event) {

         $('#inputModal').modal();
         // modal値設定
        // item種別
        var itemTypeFilterResult = $('#ITEM_TYPE_FILTER option:selected')[0].text;
        var mItemTypeName = document.getElementById("mItemTypeName")
        if(itemTypeFilterResult != ""){
            mItemTypeName.textContent = itemTypeFilterResult
        }

        // companyName結果をfromへ設定
        var companyNameFilterResult = $('#COMPANY_NAME_FILTER option:selected').val();
        var mCompanyName = document.getElementById("mCompanyName")
        if(companyNameFilterResult != ""){
            mCompanyName.textContent = companyNameFilterResult
        }


        // floorName結果をfromへ設定
        var floorNameFilterResult = $('#FLOOR_NAME_FILTER option:selected').val();
        var mReserveFloorName = document.getElementById("mReserveFloorName")
        if(floorNameFilterResult != ""){
            mReserveFloorName.textContent = floorNameFilterResult
        }

        // work_type_name結果をfromへ設定
        var workTypeNameFilterResult = $('#WORK_TYPE_FILTER option:selected').val();
        if(workTypeNameFilterResult != ""){
            mWorkTypeName.textContent = workTypeNameFilterResult
        }

        // inputDate結果をfromへ設定
        var mReserveDate = document.getElementById("mReserveDate")
        var inputDate = document.getElementById("inputDate")
        if(inputDate.value != ""){
            mReserveDate.textContent = gDatePicker.startSqlTime
        }


        // 選択
//        var mArSelectId = document.getElementById("mArSelectId")
//        mArSelectId.textContent = "";
//        $("input[name=current_proudct]:checked").each(function() {
//          arCheckBoxIndex.push(this.value)
//          mArSelectId.textContent += this.value
//        });


    });

    var viewBtnElement = document.getElementById("viewBtn")
    viewBtnElement.addEventListener('click', function(event) {
        // itemTypeId結果をfromへ設定
        var itemTypeFilterResult = $('#ITEM_TYPE_FILTER option:selected').val();
        var inputItemType = document.getElementById("itemTypeId")
        inputItemType.value = itemTypeFilterResult

        // work_type_name結果をfromへ設定
        var workTypeNameFilterResult = $('#WORK_TYPE_FILTER option:selected').val();
        var inputWorkTypeName = document.getElementById("workTypeName")
        inputWorkTypeName.value = workTypeNameFilterResult

        // inputDate結果をfromへ設定
        var inputDate = document.getElementById("inputDate")
        inputDate.value = gDatePicker.startSqlTime
        dbExecuteManager("../site/itemCarReserve")
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

    // 画面サイズ変更による再調整
    gInitView.tableResize();

});
