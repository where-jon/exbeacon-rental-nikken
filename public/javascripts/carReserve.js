$(function(){

//    $('#reserveScheduleTable').tablefix({width: 600, height: 600, fixRows: 1, fixCols: 2});
//    $('.crossTableDiv, .rowTableDiv, .colTableDiv').find('.drop-able, .draggable').removeClass('.drop-able').removeClass('.draggable');

    // ドラッグ可能にする
    $('.draggable').draggable({
          connectToSortable: '.drop-able'
        , helper: 'clone'                     // clone: 複製、original：移動
        , revert: false                       // true：範囲外の場合は元に戻す、false：範囲外の場合は消す
        , opacity: 0.5                        // ドラッグ中の透明度
        , start: function(event, ui){
              //$(ui.helper).css('width', `${ $(event.target).width() }px`);
          }
    });
    // ドロップ可能にする
    $('.drop-able').sortable( {
            revert: false                     //ドロップ時のアニメーション
          , update: function(event, ui) {     // 受け取ったクローンをドラッグ可能にする
                $(ui.item).draggable({
                  connectToSortable: '.drop-able'
                  , helper: 'clone'
                  , revert: false
                  , opacity: 0.5
                  , stop: function(event,ui){
                      $(this).remove();
                      //$(ui.item).css('width', `${ $(event.target).width() }px`);
                  }
                });
            }
    });
    // テキスト選択を無効にする
    $('.draggable').disableSelection();
});
