function fixTable(){
    // 予約表テーブルの固定
    var h = $(window).height() * 0.78;
    var w = $('.mainSpace').width() * 0.9;
    $('#reserveTable').tablefix({width: w, height: h, fixRows: 2, fixCols: 2});
    // 複製テーブルのドラッグ＋ドロップは無効に
    $('.crossTableDiv, .rowTableDiv, .colTableDiv').find('.drop-able, .draggable').removeClass('.drop-able').removeClass('.draggable');
    $('.crossTableDiv, .rowTableDiv, .colTableDiv').find('table').removeAttr('id');
}
// テーブルのリサイズ
function resizeTable() {
    var w = $('.mainSpace').width() * 0.9;
    var h = $(window).height() * 0.78;
    // 横の設定
    $('.baseDiv').width(w);
    $('.crossTableDiv').width(w);
    $('.rowTableDiv').width(w);
    $('.colTableDiv').width(w);
    $('.bodyTableDiv').width(w);

    $('.baseDiv').height(h);
    $('.crossTableDiv').height(w);
    $('.rowTableDiv').height(w);
    $('.bodyTableDiv').height(w);

}
$(function(){

    // テーブルを固定
    fixTable();
    // リサイズ対応
    $(window).on("resize", resizeTable);

    // ドラッグ可能にする
    $('.draggable').draggable({
          connectToSortable: '.drop-able'
        , helper: 'clone'                     // clone: 複製、original：移動
        , revert: false                       // true：範囲外の場合は元に戻す、false：範囲外の場合は消す
        , opacity: 0.5                        // ドラッグ中の透明度
    });
    // ドロップ可能にする
    $('.drop-able').sortable( {
            revert: false                     //ドロップ時のアニメーション
          , update: function(event, ui) {     // 受け取ったクローンをさらにドラッグ可能にする
                $(ui.item).draggable({
                    connectToSortable: '.drop-able'
                  , helper: 'clone'
                  , revert: false
                  , opacity: 0.5
                  , stop: function(event,ui){
                      $(this).remove();
                  }
                });
            }
    });
});
