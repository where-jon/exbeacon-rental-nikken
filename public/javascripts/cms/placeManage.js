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
// モーダル画面の表示
function showInputModal(){
    $('#inputModal').modal();
}

function showUpdateModal(){
    var selectLine = $('.rowHoverSelectedColor');
    if(selectLine.length > 0){
        $('#updatePlaceId').val(selectLine.attr('data-placeId'));
        $('#updatePlaceName').val(selectLine.attr('data-placeName'));
        $('#updatePlaceStatus').val(selectLine.attr('data-statusCode'));
        $('#updateModal').modal();
    }
}

function showPasswordModal(){
    var selectLine = $('.rowHoverSelectedColor');
    if(selectLine.length > 0){
        $('#pwPlaceId').val(selectLine.attr('data-placeId'));
        $('#upUserId').val(selectLine.attr('data-userEmail'));
        $('#passwordModal').modal();
    }
}

function showDeleteModal(){
    if($('.rowHoverSelectedColor').length > 0){
        $('#deleteModal').modal();
    }
}
//

function deleteSelectPlace() {
    if($('.rowHoverSelectedColor').length > 0){
        var placeId = $('.rowHoverSelectedColor').attr('data-placeId');
        $('#deletePlaceId').val(placeId);
        $('#deleteForm').submit();
    }
}

// 予約テーブルの固定
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
        var w = $('.mainSpace').width()*1.001; // ヘッダー右側ボーダーが切れる為*1.001
        if ($('.mainSpace').height() > h) {
            w = $('.mainSpace').width()-5;
            $('.itemTable').tablefix({width:w, height: h, fixRows: 2});
            w = $('.mainSpace').width()-14;
            $('.rowTableDiv').width(w);
        } else {
            $('.rowTableDiv').width(w);
        }
    }
    $('.bodyTableDiv').find('.itemTable').css('margin-bottom','0');
    $('.colTableDiv').css("width","");

}
// 予約テーブルのクリア
function removeTable(){
    var clonedTable = $('.bodyTableDiv').find('table').clone();
    $(clonedTable).attr('style', '');
    $('.baseDiv').remove();
    $('#table-responsive-body').append(clonedTable.prop("outerHTML"));
}

$(function(){
    // テーブルを固定
    fixTable();
    bindMouseAndTouch();

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
            touched = false;
            touch_time = 0;
            clearInterval(document.interval);
        }, 200);
    });
});
