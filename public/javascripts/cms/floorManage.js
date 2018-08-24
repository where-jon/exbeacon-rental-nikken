
// フロア追加・更新モーダル画面の表示
function showFloorModal(floorId){
    if(!floorId){
        // 新規
        $('#inputFloorId').val('');
         $('#inputPreDisplayOrder').val('');
        $('#activeFlgDialog').val(false);
        $("#FLG_FILTER").val("1").prop("selected", true);
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
        $('#inputPreDisplayOrder').val($('#'+floorId).find('.displayOrder').text());
        $('#inputExbDeviceNoListComma').val('');

        var vShow = $('#'+floorId).find('.activeFlg').text()
        if(vShow == "表示"){
            $("#FLG_FILTER_DIALOG").val("1").prop("selected", true);
            $('#activeFlgDialog').val(true);
        }else{
            $("#FLG_FILTER_DIALOG").val("0").prop("selected", true);
            $('#activeFlgDialog').val(false);
        }

        $('#inputDisplayOrder').val($('#'+floorId).find('.displayOrder').text());
        $('#inputFloorName').val($('#'+floorId).find('.floorName').text());
        $('#inputDeviceNo').val('');

//        var spanObjList = $('#'+floorId).find('span');
//        $(spanObjList).each(function(index, element){
//            var clonedRow = $('.template').clone();
//            clonedRow.addClass('cloned');
//            clonedRow.removeClass('template');
//            var deviceIds = $.trim($(element).text());
//            if (deviceIds != "") {
//                clonedRow.find('span.inputDeviceNoSpan').text(deviceIds);
//                $('.template').before(clonedRow);
//                var value = $('#inputExbDeviceNoListComma').val();
//                if (value != "") {
//                    value = value + "-";
//                }
//                $('#inputExbDeviceNoListComma').val(value + $.trim($(element).text()))
//
//                clonedRow.removeClass('hidden');
//            }
//        });

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


// 表示ボタンをクリックする時に発生するイベント
function btnEvent(){
    var flgFrame = $('#FLG_FILTER');
    if(flgFrame != null){
        // 管理者用selectbox value取得
        $('#FLG_FILTER').change(function() {
             var result = $('#FLG_FILTER option:selected').val();
             console.log("select:" + result)
             var vActiveFlgElement = document.getElementById("activeFlg");
             var vResult = false
             if(result == 1 ) vResult = true

             vActiveFlgElement.value = vResult
        });
    }

    var flgFrame2 = $('#FLG_FILTER_DIALOG');
        if(flgFrame2 != null){
            // 管理者用selectbox value取得
            $('#FLG_FILTER_DIALOG').change(function() {
                 var result = $('#FLG_FILTER_DIALOG option:selected').val();
                 console.log("select:" + result)
                 var vActiveFlgElement = document.getElementById("activeFlgDialog");
                 var vResult = false
                 if(result == 1 ) vResult = true
                 vActiveFlgElement.value = vResult
            });
        }
}
// 初期表示
$(function(){

    // ボタンをイベント
    btnEvent();

    // テーブルを固定
    gInitView.fixTable();

    // マウス操作とタップ操作をバインド
    gInitView.bindMouseAndTouch();
});
