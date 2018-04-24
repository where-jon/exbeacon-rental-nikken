//function bindSortable(){
//    $('.sortable').bind('sortstop', function (e, ui) {
//        // ソートが完了したら実行される。
//        var rows;
//        if($('.bodyTableDiv').length > 0){
//            rows = $('.bodyTableDiv').find('.sortable').find('tr');
//        }else{
//            rows = $('.sortable').find('tr');
//        }
//
//        var floorIdComma = "";
//        $(rows).each(function(index, r){
//            if(index == 0){
//                floorIdComma += $(r).attr('id');
//            }else{
//                floorIdComma += "," + $(r).attr('id');
//            }
//        });
//
//        $.ajax({
//            type: "POST",
//            url: window.location.pathname + "/sort",
//            data: JSON.stringify({floorIdComma: floorIdComma}),
//            contentType: 'application/json', // リクエストの Content-Type
//            dataType: "json",           // レスポンスをJSONとしてパースする
//            success: function(json_data) {   // 200 OK時
//                console.log("sort - OK");
//            },
//            error: function(e) {         // HTTPエラー時
//                alert("エラーが発生");
//                console.log("sort - NG");
//            },
//            complete: function() {      // 成功・失敗に関わらず通信が終了した際の処理
//            }
//        });
//    })
//}

// マウス・タッチの動作のバインド
function bindMouseAndTouch(){
    var ua = navigator.userAgent;
    if (ua.indexOf('iPad') > 0 || ua.indexOf('Android') > 0 || ua.indexOf('iPhone') > 0 || ua.indexOf('iPod') > 0){
        // タッチデバイスの場合
        $(".rowHover").bind({
            'touchstart': function(e) {
                if(e.originalEvent.touches.length > 1){
                }else if(e.originalEvent.touches.length == 1){
                    $(".rowHoverSelectedColor").removeClass('rowHoverSelectedColor');
                    $(this).addClass('rowHoverSelectedColor');
                }
            },
        });
    }else{
        // PCブラウザの場合
        $(".rowHover").bind({
            'mouseover': function(e) {
                $(this).addClass('rowHoverColor');
            },
            'mouseout': function(e) {
                $(this).removeClass('rowHoverColor');
            },
            'click': function(e) {
                $(".rowHoverSelectedColor").removeClass('rowHoverSelectedColor');
                $(this).addClass('rowHoverSelectedColor');
            },
        });
    }
}

// フロア追加・更新モーダル画面の表示
function showFloorModal(floorId){
    if(!floorId){
        // 新規
        $('#inputFloorId').val('');
        $('#inputExbDeviceNoListComma').val('');
        $('#inputFloorName').val('');
        $('#inputDeviceNo').val('');
        $('.cloned').remove();
        // ボタン表示の切り替え
        $('#floorUpdateFooter').addClass('hidden');
        $('#floorRegisterFooter').removeClass('hidden');
    }else{
        $('.cloned').remove();
        $('#inputFloorId').val(floorId);
        $('#inputExbDeviceNoListComma').val('');
        $('#inputFloorName').val($('#'+floorId).find('.floorName').text());
        $('#inputDeviceNo').val('');

        var spanObjList = $('#'+floorId).find('span');
        $(spanObjList).each(function(index, element){
            var clonedRow = $('.template').clone();
            clonedRow.addClass('cloned');
            clonedRow.removeClass('template');
            var deviceIds = $.trim($(element).text());
            if (deviceIds != "") {
                clonedRow.find('span.inputDeviceNoSpan').text(deviceIds);
                $('.template').before(clonedRow);
                var value = $('#inputExbDeviceNoListComma').val();
                if (value != "") {
                    value = value + "-";
                }
                $('#inputExbDeviceNoListComma').val(value + $.trim($(element).text()))

                clonedRow.removeClass('hidden');
            }
        });

        // ボタン表示の切り替え
        $('#floorUpdateFooter').removeClass('hidden');
        $('#floorRegisterFooter').addClass('hidden');
    }
    $('#floorUpdateModal').modal();
}

function showFloorUpdateModal(isRegister){
    if(isRegister){
        showFloorModal();
    }else{
        if($('.rowHoverSelectedColor').length > 0){
            var floorId = $('.rowHoverSelectedColor').attr('data-floorId');
            showFloorModal(floorId);
        }
    }
}

function showFloorDeleteModal(){
    if($('.rowHoverSelectedColor').length > 0){
        var floorId = $('.rowHoverSelectedColor').attr('data-floorId');
        $('#deleteFloorId').val(floorId)
        $('#floorDeleteModal').modal();
    }
}

// 入力モーダルのEXBデバイスの行を追加
function addTagRow(){
    if($('#inputDeviceId').val() != ''){
        if($('#inputDeviceNo').val().match(/[0-9a-zA-Z]/)){
            var duplicateFlg = false;
            $('.cloned').each(function(index, element){
                if($(element).find('span').text() == $('#inputDeviceNo').val()){
                    duplicateFlg = true;
                    return false;
                }
            });

            if(duplicateFlg){
                $('#inputDeviceNo').val('');
                return false;
            }

            var clonedRow = $('.template').clone();
            clonedRow.addClass('cloned');
            clonedRow.removeClass('template');
            // 表示文字列の設定
            clonedRow.find('span.inputDeviceNoSpan').text($.trim($('#inputDeviceNo').val()));
            // 値の設定
            var value = $('#inputExbDeviceNoListComma').val();
            if (value != "") {
                value = value + "-";
            }
            $('#inputExbDeviceNoListComma').val(value + $.trim($('#inputDeviceNo').val()));

            clonedRow.removeClass('hidden');
            $('.template').before(clonedRow);
            $('#inputDeviceNo').val('');
        }
    }
}

// 入力モーダルのEXBデバイスの行の削除
function removeTagRow(obj){
    var clonedRow = $(obj).parent().parent();
    var value = $.trim(clonedRow.find('span.inputDeviceNoSpan').text());
    var originalValue = $('#inputExbDeviceNoListComma').val();
    var replaceReg = new RegExp("(-" + value + "|" + value + "-|" + value + ")", '');
    $('#inputExbDeviceNoListComma').val(originalValue.replace(replaceReg, ''));
    clonedRow.remove();
}

// テーブルの固定
function fixTable(){
    // テーブルの固定
    var h = $(window).height()*0.7;
    // テーブルの調整
    var ua = navigator.userAgent;
    if (ua.indexOf('iPad') > 0 || ua.indexOf('Android') > 0 || ua.indexOf('iPhone') > 0 || ua.indexOf('iPod') > 0){
        // タッチデバイス
        if ($('.mainSpace').height() > h) {
            var w = $('.mainSpace').width()*0.99;
            $('.itemTable').tablefix({width:w, height: h, fixRows: 2});
        } else {
            var w = $('.mainSpace').width();
            $('.itemTable').tablefix({width:w, fixRows: 2});
        }
    }else{
        // PCブラウザ
        var w = $('.mainSpace').width();
        $('.itemTable').tablefix({height: h, fixRows: 2});
        $('.rowTableDiv').width(w);
    }
    $('.bodyTableDiv').find('.itemTable').css('margin-bottom','0');
    $('.colTableDiv').css("width","");

}
// テーブルのクリア
function removeTable(){
    var clonedTable = $('.bodyTableDiv').find('table').clone();
    $(clonedTable).attr('style', '');
    $('.baseDiv').remove();
    $('.floorTable-responsive-body').append(clonedTable.prop("outerHTML"));
}

// 初期表示
$(function(){
    // テーブルを固定
    fixTable();
    // マウス操作とタップ操作をバインド
    bindMouseAndTouch();

//    // ソート設定
//    $('.sortable').sortable();
//    $('.sortable').disableSelection();
//    bindSortable();

    // リサイズ対応
    var timer = false;
    $(window).resize(function() {
        if (timer !== false) {
            clearTimeout(timer);
        }
        timer = setTimeout(function() {
            // 処理の再実行
            removeTable();
            fixTable();
            bindMouseAndTouch();
            $('#sortable').sortable();
            $('#sortable').disableSelection();
            bindSortable();

        }, 200);
    });
});
