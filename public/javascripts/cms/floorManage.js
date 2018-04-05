// マウス・タッチの動作のバインド
function bindMouseAndTouch(){
    var ua = navigator.userAgent;
    if (ua.indexOf('iPad') > 0 || ua.indexOf('Android') > 0 || ua.indexOf('iPhone') > 0 || ua.indexOf('iPod') > 0){
        // タッチデバイスの場合
        $(".rowHover").bind({
            'touchstart': function(e) {
                if(e.originalEvent.touches.length > 1){
                }else if(e.originalEvent.touches.length == 1){
                    $(this).addClass('rowHoverColor');
                    var floorId = $(this).attr('id');
                    touched = true;
                    touch_time = 0;
                    document.interval = setInterval(function(){
                        touch_time += 100;
                        if (touch_time == common.longTapTime) {
                            // ロングタップ時の処理
                            showFloorModal(floorId);
                        }
                    }, 100)
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
    if(floorId == ""){
        // 新規
        $('#inputFloorId').val('');
        $('#inputExbDeviceIdListComma').val('');
        $('#inputFloorName').val('');
        $('#inputDeviceId').val('');
        $('.cloned').remove();
        // ボタン表示の切り替え
        $('#floorUpdateFooter').removeClass('hidden');
        $('#floorRegisterFooter').addClass('hidden');
    }else{
        $('.cloned').remove();
        $('#inputFloorId').val(floorId);
        $('#inputExbDeviceIdListComma').val('');
        $('#inputFloorName').val($('#'+floorId).find('.floorName').text());
        $('#inputDeviceId').val('');

        var spanObjList = $('#'+floorId).find('span');
        $(spanObjList).each(function(index, element){
            var clonedRow = $('.template').clone();
            clonedRow.addClass('cloned');
            clonedRow.removeClass('template');
            var deviceIds = $.trim($(element).text());
            if (deviceIds != "") {
                clonedRow.find('span.inputDeviceIdSpan').text(deviceIds);
                $('.template').before(clonedRow);
                var value = $('#inputExbDeviceIdListComma').val();
                if (value != "") {
                    value = value + "-";
                }
                $('#inputExbDeviceIdListComma').val(value + $.trim($(element).text()))

                clonedRow.removeClass('hidden');
            }
        });

        // ボタン表示の切り替え
        $('#floorUpdateFooter').addClass('hidden');
        $('#floorRegisterFooter').removeClass('hidden');
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
        $('#floorDeleteModal').modal();
    }
}

function deleteFloor(){
    if($('.rowHoverSelectedColor').length > 0){
        var floorId = $('.rowHoverSelectedColor').attr('data-floorId');
        $('#deleteFloorId').val(floorId)
        $('#deleteForm').submit()
    }
}

// 入力モーダルのEXBデバイスの行を追加
function addTagRow(){
    if($('#inputDeviceId').val() != ''){
        if($('#inputDeviceId').val().match(/[0-9a-zA-Z]/)){
            var duplicateFlg = false;
            $('.cloned').each(function(index, element){
                if($(element).find('span').text() == $('#inputDeviceId').val()){
                    duplicateFlg = true;
                    return false;
                }
            });

            if(duplicateFlg){
                $('#inputDeviceId').val('');
                return false;
            }

            var clonedRow = $('.template').clone();
            clonedRow.addClass('cloned');
            clonedRow.removeClass('template');
            // 表示文字列の設定
            clonedRow.find('span.inputDeviceIdSpan').text($.trim($('#inputDeviceId').val()));
            // 値の設定
            var value = $('#inputExbDeviceIdListComma').val();
            if (value != "") {
                value = value + "-";
            }
            $('#inputExbDeviceIdListComma').val(value + $.trim($('#inputDeviceId').val()));

            clonedRow.removeClass('hidden');
            $('.template').before(clonedRow);
            $('#inputDeviceId').val('');
        }
    }
}

// 入力モーダルのEXBデバイスの行の削除
function removeTagRow(obj){
    var clonedRow = $(obj).parent().parent();
    var value = $.trim(clonedRow.find('span.inputDeviceIdSpan').text());
    var originalValue = $('#inputExbDeviceIdListComma').val();
    var replaceReg = new RegExp("(-" + value + "|" + value + "-|" + value + ")", '');
    $('#inputExbDeviceIdListComma').val(originalValue.replace(replaceReg, ''));
    clonedRow.remove();
}

// 初期表示
$(function(){
    bindMouseAndTouch();
});
