var arCheckBoxIndex = []
var gReserveCheck = false;

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

    var inputName = document.getElementById("inputName")
    if(inputName!=null){
         $('#ITEM_NAME_FILTER').val(inputName.value)
    }

    var inputFloorName = document.getElementById("floorName")
    if(inputFloorName!=null){
         $('#FLOOR_NAME_FILTER').val(inputFloorName.value)
    }
}

function dbExecuteManager(routeUrl,vBtnName){
    var formElement = null
    if(vBtnName =="search"){
        formElement = $("#viewFormSearch")
    }else if (vBtnName =="reserve"){
        formElement = $("#viewFormReserve")
    }
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
     gDatePicker.dayClickEvent();
    // DatePickerの設定 end-----------------------------------------

    //　予約登録ボタン
　　var registerBtnElement = document.getElementById("itemRegisterFooter")
    registerBtnElement.addEventListener('click', function(event) {
        if(gReserveCheck){
            $('#load')[0].style.display = ""
            gReserveCheck = false;
            // companyName結果をfromへ設定
            var companyNameFilterResult = $('#COMPANY_NAME_FILTER option:selected').val();
            var inputCompanyName = document.getElementById("companyName")
            inputCompanyName.value = companyNameFilterResult

            // floorName結果をfromへ設定
            var floorNameFilterResult = $('#FLOOR_NAME_FILTER_MODAL option:selected').val();
            var inputFloorName = document.getElementById("floorNameModal")
            inputFloorName.value = floorNameFilterResult
            dbExecuteManager("../site/reserveCar/reserve","reserve")
        }
    });

    //　予約へボタン
    var reserveBtnElement = document.getElementById("reserveBtn")
    reserveBtnElement.addEventListener('click', function(event) {
         gReserveCheck = true;
         $('#inputModal').modal();
    });

    var viewBtnElement = document.getElementById("viewBtn")
    viewBtnElement.addEventListener('click', function(event) {
         $('#load')[0].style.display = ""
        // inputDate結果をfromへ設定
        var inputDate = document.getElementById("inputDate")
        inputDate.value = gDatePicker.startSqlTime

        // itemTypeId結果をfromへ設定
        var itemTypeFilterResult = $('#ITEM_TYPE_FILTER option:selected').val();
        var inputItemType = document.getElementById("itemTypeId")
        inputItemType.value = itemTypeFilterResult

        // floorName結果をfromへ設定
        var floorNameFilterResult = $('#FLOOR_NAME_FILTER option:selected').val();
        var inputFloorName = document.getElementById("floorName")
        inputFloorName.value = floorNameFilterResult

        dbExecuteManager("../site/reserveCar","search")
    });

}

// マウス・タッチの動作のバインド
function bindMouseAndTouch(){
    var ua = navigator.userAgent;
    if (ua.indexOf('iPad') > 0 || ua.indexOf('Android') > 0 || ua.indexOf('iPhone') > 0 || ua.indexOf('iPod') > 0){
        // タッチデバイスの場合
        var touched = false;
        var touch_time = 0;
        $(".reserveTdHover").bind({
            'touchstart': function(e) {
                reserveClickEvent(this);
                if(e.originalEvent.touches.length > 1){
                    touch_time = 0;
                    touched = false;
                    clearInterval(document.interval);
                }else if(e.originalEvent.touches.length == 1){
                    $(this).addClass('reserveTdHoverColor');
                    var targetTd = $(this);
                    touched = true;
                    touch_time = 0;
                    document.interval = setInterval(function(){
                        touch_time += 500;
                        if (touch_time == 500) {
                            // ロングタップ時の処理
                            showInputModal($(targetTd));
                            touch_time = 0;
                            clearInterval(document.interval);
                        }
                    }, 500);
                }
            },
            'touchend': function(e) {
                $(this).removeClass('reserveTdHoverColor');
                touched = false;
                touch_time = 0;
                clearInterval(document.interval);
            },
            'touchmove': function(e) {
                $(this).removeClass('reserveTdHoverColor');
                touched = false;
                touch_time = 0;
                clearInterval(document.interval);
            }
        });
    }else{
        // PCブラウザの場合
        $(".reserveTdHover").bind({
            'mouseover': function(e) {
                $(this).addClass('reserveTdHoverColor');
            },
            'mouseout': function(e) {
                $(this).removeClass('reserveTdHoverColor');
            },
            'drag': function(e) {
                $(this).removeClass('reserveTdHoverColor');
            },
            'click': function(e) {
                reserveClickEvent(this);
            },
        });
    }
}
/* 予約に関するクリックイベント*/
function reserveClickEvent(vThis){
    var vCheck = $(vThis).attr('data-check');
    if(vCheck == "off"){
        $(vThis).addClass('reserveTdClickColor');
        vThis.setAttribute("data-check", "on");
        $(vThis.children[0]).prop('checked', true);
    }else{
        $(vThis).removeClass('reserveTdClickColor');
         vThis.setAttribute("data-check", "off");
         $(vThis.children[0]).prop('checked', false);
    }
}

$(function(){

    gInitView.newFixTable("noBtn");
    gInitView.newTableResize("noBtn");
    // filter値確認
    getFilterCheck();
    // 表示ボタンをクリック
    btnEvent();

    bindMouseAndTouch();

});