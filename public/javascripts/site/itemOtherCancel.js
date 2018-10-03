var arCheckBoxIndex = []
var gReserveCheck = true;
var gtitleName = "その他仮設材予約取消"

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

    var inputCompanyName = document.getElementById("companyName")
    if(inputCompanyName!=null){
         $('#COMPANY_NAME_FILTER').val(inputCompanyName.value)
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
     gDatePicker.dayClickEvent2();
      gDatePicker.htmlClickEvent();
    // DatePickerの設定 end-----------------------------------------

    //　checkboxクリックイベント
    gReserveCheck = gReserve.bCheckEvent()

    //　予約取消ボタン
　　var registerBtnElement = document.getElementById("itemRegisterFooter")
    registerBtnElement.addEventListener('click', function(event) {
        if(gReserveCheck){
            $('#load')[0].style.display = ""
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
            var inputDate2 = document.getElementById("inputDate2")
            inputDate2.value = gDatePicker.endSqlTime

            dbExecuteManager("../site/itemOtherCancel/cancel")
        }
    });

    //　取消へボタン
    var reserveBtnElement = document.getElementById("reserveBtn")
    reserveBtnElement.addEventListener('click', function(event) {
        gReserveCheck = gReserve.bCheckData()
        if(gReserveCheck){
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
            var mReserveStartDate = document.getElementById("mReserveStartDate")
            var inputDate = document.getElementById("inputDate")
            if(inputDate.value != ""){
                mReserveStartDate.textContent = gDatePicker.startSqlTime
            }
            var mReserveEndDate = document.getElementById("mReserveEndDate")
            var inputDate2 = document.getElementById("inputDate2")
            if(inputDate2.value != ""){
                mReserveEndDate.textContent = gDatePicker.endSqlTime
            }
        }

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

        // companyName結果をfromへ設定
        var companyNameFilterResult = $('#COMPANY_NAME_FILTER option:selected').val();
        var inputCompanyName = document.getElementById("companyName")
        inputCompanyName.value = companyNameFilterResult

        // inputDate結果をfromへ設定
        var inputDate = document.getElementById("inputDate")
        var inputDate2 = document.getElementById("inputDate2")
        inputDate.value = gDatePicker.startSqlTime
        inputDate2.value = gDatePicker.endSqlTime

        dbExecuteManager("../site/itemOtherCancel")

    });

}

$(function(){
    // テーブルを固定
    gInitView.newFixTable();
    gInitView.newTableResize();
    // マウス操作とタップ操作をバインド
    gInitView.bindMouseAndTouch();
    // filter値確認
    getFilterCheck();
    // 表示ボタンをクリック
    btnEvent();
});

