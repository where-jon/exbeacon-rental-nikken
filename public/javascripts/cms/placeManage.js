// マウス・タッチの動作のバインド
function bindMouseAndTouch(){
    var ua = navigator.userAgent;
    if (ua.indexOf('iPad') > 0 || ua.indexOf('Android') > 0 || ua.indexOf('iPhone') > 0 || ua.indexOf('iPod') > 0){
        // タッチデバイスの場合
        $(".rowHover").bind({
            'touchstart': function(e) {
                if(e.originalEvent.touches.length > 1){
                }else if(e.originalEvent.touches.length == 1){
<<<<<<< HEAD
                    $(this).addClass('rowHoverColor');
                    var placeId = $(this).attr('data-placeId');
                    touched = true;
                    touch_time = 0;
                    document.interval = setInterval(function(){
                        touch_time += 100;
                        if (touch_time == common.longTapTime) {
                            // ロングタップ時の処理
                            moveTo('/cms/placeManage/detail?placeId=' + placeId);
                        }
                    }, 100);
=======
                    $(".rowHoverSelectedColor").removeClass('rowHoverSelectedColor');
                    $(this).addClass('rowHoverSelectedColor');
>>>>>>> design
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
<<<<<<< HEAD
                var placeId = $(this).attr('data-placeId');
                moveTo('/cms/placeManage/detail?placeId=' + placeId);
=======
                $(".rowHoverSelectedColor").removeClass('rowHoverSelectedColor');
                $(this).addClass('rowHoverSelectedColor');
>>>>>>> design
            },
        });
    }
}

// モーダル画面の表示
function showPlaceRegisterModal(){
    $('#placeRegisterModal').modal();
}
function showDeleteModal(){
    if($('.rowHoverSelectedColor').length > 0){
        $('#deleteModal').modal();
    }
}
//
function moveToSelected(){
    if($('.rowHoverSelectedColor').length > 0){
        moveTo('/cms/placeManage/detail');
    }
}

// 予約テーブルの固定
function fixTable(){
    // 予約表テーブルの固定
    var h = $(window).height()*0.7;
    var w = $('#table-responsive-body').width();
    $('#itemTable').tablefix({height: h, fixRows: 2});

    // 複製テーブルのドラッグ＋ドロップは無効に
    $('.crossTableDiv, .rowTableDiv, .colTableDiv').find('table').removeAttr('id');

    // Chromeのみ
    if(window.navigator.userAgent.indexOf('Chrome') !== -1 ){
        $('.rowTableDiv').width(w);
    }
    $('.bodyTableDiv').find('.itemTable').css('margin-bottom','0');

}
// 予約テーブルのクリア
function removeTable(){
    var clonedTable = $('.bodyTableDiv').find('table').clone();
    $(clonedTable).attr('style', '');
    $(clonedTable).find('span').remove();
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
