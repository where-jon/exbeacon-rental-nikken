var common = {longTapTime : 400};//ボタンタップ長押しと判定する時間（ミリ秒）

$(function(){
    // ボタンのタップ時の動き
    $('a.btn').on('touchstart touchend', function(e) {
        if (e.type === 'touchstart') {
          $(this).addClass('btnTappedClass');
        } else {
          $(this).removeClass('btnTappedClass');
        }
    });
    // CMSサイドメニューリンクのタップ時の動き
    $('a.sideLinkNotSelected').on('touchstart touchend', function(e) {
        if (e.type === 'touchstart') {
          $(this).addClass('sideLinkSelected');
        } else {
          $(this).removeClass('sideLinkSelected');
        }
    });

});

function hasVerticalScrollBar(div, table){
    return $(div).height() <= $(table).height();
}

function moveTo(url){
    location.href = url;
}