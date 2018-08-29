
// フロア追加・更新モーダル画面の表示
function showExbModal(exbId){
    if(!exbId){
        // 新規
        $('#inputExbId').val('');
        $('#inputDeviceId').val('');
        $('#inputPreDeviceId').val('');
        $('#inputPosName').val('');
        $('#inputDeviceNo').val('');
        $('#inputDeviceName').val('');
        $("#PD_FLOOR").val(-1).prop("selected", true);
        $('#setupFloorId').val(-1);

        $('.cloned').remove();
        // ボタン表示の切り替え
        $('#floorUpdateFooter').addClass('hidden');
        $('#floorRegisterFooter').removeClass('hidden');
    }else{
        $('.cloned').remove();
        $('#inputExbId').val(exbId);
        $('#inputDeviceId').val($('#'+exbId).find('.deviceId').text());
        $('#inputPreDeviceId').val($('#'+exbId).find('.deviceId').text());
        $('#inputDeviceNo').val($('#'+exbId).find('.deviceNo').text());
        $('#inputDeviceName').val($('#'+exbId).find('.deviceName').text());
        $('#inputPosName').val($('#'+exbId).find('.posName').text());

        var vShow =$('#'+exbId).find('.setupFloorId').text()
        var vResult = -1
        if(vShow == "未設置"){
            vResult = -1
        }else{
            vResult = Number(vShow)
        }
        $("#PD_FLOOR").val(vResult).prop("selected", true);
        $('#setupFloorId').val(vResult);


        // ボタン表示の切り替え
        $('#floorUpdateFooter').removeClass('hidden');
        $('#floorRegisterFooter').addClass('hidden');
    }
    $('#exbUpdateModal').modal();
}

function showExbUpdateModal(isRegister){
    if(isRegister){
        showExbModal();
    }else{
        if($('.rowHoverSelectedColor').length > 0){
            var exbId = $('.rowHoverSelectedColor').attr('data-exbId');
            showExbModal(exbId);
        }
    }
}

function showExbDeleteModal(){
    if($('.rowHoverSelectedColor').length > 0){
        var exbId = $('.rowHoverSelectedColor').attr('data-exbId');
        var floorId = $('.rowHoverSelectedColor').attr('data-floorId');
        $('#deleteExbId').val(exbId)
        $('#deleteFloorId').val(floorId)
        $('#exbDeleteModal').modal();
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
    var pullDownFrame = $('#PD_FLOOR');
    if(pullDownFrame != null){
        $('#PD_FLOOR').change(function() {
             var result = $('#PD_FLOOR option:selected').val();
             console.log("select:" + result)
             var vActiveFlgElement = document.getElementById("setupFloorId");
             vActiveFlgElement.value = Number(result)
        });
    }
}
// 初期表示
$(function(){

    // ボタンをイベント
    btnEvent();

    // テーブルを固定
    gInitView.fixTable();

    // 画面更新
    gInitView.tableResize();

    // マウス操作とタップ操作をバインド
    gInitView.bindMouseAndTouch();
});
