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
}

/*
function dbExecuteManager2(routeUrl){
    var formElement = $("#viewForm2")
    formElement[0].action = routeUrl
    // 送信ボタン生成
    var vButton = document.createElement("button");
    vButton.id = "dbExecuteBtn"
    vButton.className = "btn hidden";
    formElement[0].appendChild(vButton);

    $("#dbExecuteBtn").trigger( "click" );
}
*/

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
            var floorNameFilterResult = $('#FLOOR_NAME_FILTER option:selected').val();
            var inputFloorName = document.getElementById("floorName")
            inputFloorName.value = floorNameFilterResult
            dbExecuteManager("../site/newItemCarReserve/reserve","reserve")
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
        dbExecuteManager("../site/newItemCarReserve","search")
    });

}


// テーブルの固定
function fixTable(){

    var vHeight = $(window).height()*0.80;
    $('#tableDiv')[0].style.height = vHeight + "px"


    $('#myTable').stickyTable({overflowy: true});

    $('#destroyBtn').click(function() {
        //removes sticky table classes and elements
        $('#myTable').stickyTable('destroy');
    });

    $('#initBtn').click(function() {
        $('#myTable').stickyTable();
    });

//    // テーブルの固定
//    var h = $(window).height()*0.85;
//    // テーブルの調整
//    var ua = navigator.userAgent;
//    if (ua.indexOf('iPad') > 0 || ua.indexOf('Android') > 0 || ua.indexOf('iPhone') > 0 || ua.indexOf('iPod') > 0){
//        // タッチデバイス
//        if ($('.mainSpace').height() > h) {
//            var w = $('.mainSpace').width()*0.99;
//            $('.itemTable').tablefix({width:w, height: h, fixRows: 4});
//        } else {
//            var w = $('.mainSpace').width();
//            $('.itemTable').tablefix({width:w, fixRows: 4});
//        }
//    }else{
//        // PCブラウザ
//        var w = $('.mainSpace').width();
//        $('.itemTable').tablefix({height: h, fixRows: 4});
//        $('.rowTableDiv').width(w);
//    }
//    $('.bodyTableDiv').find('.itemTable').css('margin-bottom','0');
//    $('.colTableDiv').css("width","");

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

// テーブルのクリア
function removeTable(){
    var clonedTable = $('.bodyTableDiv').find('table').clone();
    $(clonedTable).attr('style', '');
    $('.baseDiv').remove();
    $('.table-responsive-body').append(clonedTable.prop("outerHTML"));
}

$(function(){

//    $('#specialstam').loading({
//        message: '読込中...'
//    });

    // filter値確認
    getFilterCheck();
    // 表示ボタンをクリック
    btnEvent();

    fixTable();
    bindMouseAndTouch();

 // リサイズ対応
//    var timer = false;
//    $(window).resize(function() {
//        if (timer !== false) {
//            clearTimeout(timer);
//        }
//        timer = setTimeout(function() {
//            // 処理の再実行
//            removeTable();
//            fixTable();
//            bindMouseAndTouch();
//        }, 200);
//    });

});


