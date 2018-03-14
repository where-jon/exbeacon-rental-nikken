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
                    touched = true;
                    touch_time = 0;
                    document.interval = setInterval(function(){
                        touch_time += 100;
                        if (touch_time == common.longTapTime) {
                            // ロングタップ時の処理
                            showInputModal();
                        }
                    }, 100);
                }
            },
            'touchend': function(e) {
                $(this).removeClass('rowHoverColor');
                touched = false;
                touch_time = 0;
                clearInterval(document.interval);
            },
            'touchmove': function(e) {
                $(this).removeClass('rowHoverColor');
                touched = false;
                touch_time = 0;
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
                showInputModal();
            },
        });
    }
}

// テーブルの固定
function fixTable(){
    // テーブルの固定
    var h = $(window).height()*0.8;
    // テーブルの調整
    var ua = navigator.userAgent;
    if (ua.indexOf('iPad') > 0 || ua.indexOf('Android') > 0 || ua.indexOf('iPhone') > 0 || ua.indexOf('iPod') > 0){
        // タッチデバイス
        var w = $('.mainSpace').width()*0.993;
        $('.itemTable').tablefix({width:w, height: h, fixRows: 2});
        //$('.rowTableDiv').width(w);
    }else{
        // PCブラウザ
        var w = $('.mainSpace').width();
        $('.itemTable').tablefix({height: h, fixRows: 2});
        $('.rowTableDiv').width(w);
    }
    if(hasVerticalScrollBar($('.bodyTableDiv'), $('.bodyTableDiv').find('table')) == false){
        removeTable();
    }else{
        $('.bodyTableDiv').find('.itemTable').css('margin-bottom','0');
        $('.colTableDiv').css("width","");
    }

}
// テーブルのクリア
function removeTable(){
    var clonedTable = $('.bodyTableDiv').find('table').clone();
    $(clonedTable).attr('style', '');
    $(clonedTable).find('span').remove();
    $('.baseDiv').remove();
    $('.table-responsive-body').append(clonedTable.prop("outerHTML"));
}

// モーダル画面の表示
function showInputModal(){
    $('#inputModal').modal();
}
function showDeleteModal(){
    $('#deleteModal').modal();
}

$(function(){
    // テーブルを固定
    fixTable();
    // マウス操作とタップ操作をバインド
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
        }, 200);
    });
});
