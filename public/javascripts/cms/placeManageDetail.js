// マウス・タッチの動作のバインド
function bindMouseAndTouch(){
    var ua = navigator.userAgent;
    if (ua.indexOf('iPad') > 0 || ua.indexOf('Android') > 0 || ua.indexOf('iPhone') > 0 || ua.indexOf('iPod') > 0){
        // タッチデバイスの場合
        var touched = false;
        var touch_time = 0;
        $(".rowHover").bind({
            'touchstart': function(e) {
                $(this).addClass('rowHoverColor');
                touched = true;
                touch_time = 0;
                document.interval = setInterval(function(){
                    touch_time += 100;
                    if (touch_time == 300) {
                        // ロングタップ(タップから約0.3秒)時の処理
                        showFloorModal();
                    }
                }, 100)
            },
            'touchend': function(e) {
                $(this).removeClass('rowHoverColor');
                touched = false;
                clearInterval(document.interval);
            },
            'touchmove': function(e) {
                $(this).removeClass('rowHoverColor');
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
                showFloorModal();
            },
        });
    }
}

//// 予約テーブルの固定
//function fixTable(){
//    // 予約表テーブルの固定
//    var h = $(window).height()*0.7;
//    var w = $('#table-responsive-body').width();
//    $('#itemTable').tablefix({height: h, fixRows: 2});
//
//    // 複製テーブルのドラッグ＋ドロップは無効に
//    $('.crossTableDiv, .rowTableDiv, .colTableDiv').find('table').removeAttr('id');
//
//    // Chromeのみ
//    if(window.navigator.userAgent.indexOf('Chrome') !== -1 ){
//        $('.rowTableDiv').width(w);
//    }
//    $('.bodyTableDiv').find('.itemTable').css('margin-bottom','0');
//
//}
//// 予約テーブルのクリア
//function removeTable(){
//    var clonedTable = $('.bodyTableDiv').find('table').clone();
//    $(clonedTable).attr('style', '');
//    $(clonedTable).find('span').remove();
//    $('.baseDiv').remove();
//    $('#table-responsive-body').append(clonedTable.prop("outerHTML"));
//}

// モーダル画面の表示
function showInputModal(){
    $('#inputModal').modal();
}
function showDeleteModal(){
    $('#deleteModal').modal();
}
function showFloorModal(){
    $('#floorModal').modal();
}

$(function(){
//    // テーブルを固定
//    fixTable();

    bindMouseAndTouch();

    // リサイズ対応
    var timer = false;
    $(window).resize(function() {
        if (timer !== false) {
            clearTimeout(timer);
        }
        timer = setTimeout(function() {
            // 処理の再実行
//            removeTable();
//            fixTable();
//            bindMouseAndTouch();
        }, 200);
    });
});
