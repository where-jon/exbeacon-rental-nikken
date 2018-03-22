// マウス・タッチの動作のバインド
function bindMouseAndTouch(){
    var ua = navigator.userAgent;
    if (ua.indexOf('iPad') > 0 || ua.indexOf('Android') > 0 || ua.indexOf('iPhone') > 0 || ua.indexOf('iPod') > 0){
        // タッチデバイスの場合
        var touched = false;
        var touch_time = 0;
        $(".rowHover").bind({
            'touchstart': function(e) {
                if(e.originalEvent.touches.length > 1){
                    touch_time = 0;
                    touched = false;
                    clearInterval(document.interval);
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
                }
            },
            'touchend': function(e) {
                $(this).removeClass('rowHoverColor');
                touch_time = 0;
                touched = false;
                clearInterval(document.interval);
            },
            'touchmove': function(e) {
                $(this).removeClass('rowHoverColor');
                touch_time = 0;
                touched = false;
                clearInterval(document.interval);
            }
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
                var floorId = $(this).attr('id');
                showFloorModal(floorId);
            },
        });
    }
}

// サブミット
function doSubmit(formId, action){
    $('#' + formId).attr('action', action)
    $('#' + formId).submit();
}

// モーダル画面の表示
function showPasswordUpdateModal(){
    $('#passwordUpdateModal').modal();
}
function showPlaceUpdateModal(){
    $('#placeUpdateModal').modal();
}
function showPlaceDeleteModal(){
    $('#placeDeleteModal').modal();
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
        $('#floorUpdateFooter').addClass('hidden');
        $('#floorRegisterFooter').removeClass('hidden');
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
            clonedRow.find('span.inputDeviceIdSpan').text($.trim($(element).text()));
            $('.template').before(clonedRow);
            var value = $('#inputExbDeviceIdListComma').val();
            $('#inputExbDeviceIdListComma').val(value + "-" + $.trim($(element).text()))

            clonedRow.removeClass('hidden');
        });

        // ボタン表示の切り替え
        $('#floorUpdateFooter').removeClass('hidden');
        $('#floorRegisterFooter').addClass('hidden');
    }
    $('#floorModal').modal();
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
            $('#inputExbDeviceIdListComma').val(value + "," + $.trim($('#inputDeviceId').val()));

            clonedRow.removeClass('hidden');
            $('.template').before(clonedRow);
            $('#inputDeviceId').val('');
        }
    }
}

// 入力モーダルのEXBデバイスの行の削除
function removeTagRow(obj){
    var clonedRow = $(obj).parent().parent();
    var value = "-" + $.trim(clonedRow.find('span.inputDeviceIdSpan').text());
    var originalValue = $('#inputExbDeviceIdListComma').val();
    originalValue = originalValue.replace(value, '');
    $('#inputExbDeviceIdListComma').val(originalValue);
    clonedRow.remove();
}

// 初期表示
$(function(){
    bindMouseAndTouch();
});
