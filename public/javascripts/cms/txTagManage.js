// テーブルの固定
function fixTable(){
    // テーブルの固定
    var h = $(window).height()*0.8;
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
//        // PCブラウザ
//        var w = $('.mainSpace').width()*1.001; // ヘッダー右側ボーダーが切れる為*1.001
//        if ($('.mainSpace').height() > h) {
//            w = $('.mainSpace').width()-5;
//            $('.itemTable').tablefix({width:w, height: h, fixRows: 2});
//            w = $('.mainSpace').width()-14;
//            $('.rowTableDiv').width(w);
//        } else {
//            $('.rowTableDiv').width(w);
//        }

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
    $('.table-responsive-body').append(clonedTable.prop("outerHTML"));
}

$(function(){
    // テーブルを固定
    fixTable();

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
        }, 200);
    });
});
