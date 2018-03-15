// モーダル画面の表示
function showInputModal(){
    $('#inputModal').modal();
}

var inputModalTimer_touch_time = 0;
var inputModalTimer;
// マウス・タッチの動作のバインド
function bindMouseAndTouch(){
    var ua = navigator.userAgent;
    if (ua.indexOf('iPad') > 0 || ua.indexOf('Android') > 0 || ua.indexOf('iPhone') > 0 || ua.indexOf('iPod') > 0){
        // タッチデバイスの場合
        var touched = false;
        var touch_time = 0;
        $(".reserveTdHover").bind({
            'touchstart': function(e) {
                $(this).addClass('reserveTdHoverColor');
                inputModalTimer = setTimeout(function(){
                    inputModalTimer_touch_time += 100;
                    if (inputModalTimer_touch_time == common.longTapTime) {
                        // ロングタップ時の処理
                        showInputModal();
                    }
                }, 100);
            },
            'touchend': function(e) {
                $(this).removeClass('reserveTdHoverColor');
                inputModalTimer_touch_time = 0;
                clearTimeout(inputModalTimer);
            },
            'touchmove': function(e) {
                $(this).removeClass('reserveTdHoverColor');
                inputModalTimer_touch_time = 0;
                clearTimeout(inputModalTimer);
            }
        });

    }else{
        // PCブラウザの場合
        $(".reserveTdHover").bind({
            'mouseover': function(e) {
                $(this).addClass('reserveTdHoverColor');
            },
            'mouseout': function(e) {
                $(this).removeClass('reserveTdHoverColor');
            },
            'click': function(e) {
                $(this).removeClass('reserveTdHoverColor');
                showInputModal();
            },
        });
    }
}

// 予約テーブルの固定
function fixTable(){
    // 予約表テーブルの固定
    var h = $("#reserveTable").height();
    var w = $('.mainSpace').width() * 0.97;
    $('#reserveTable').tablefix({width: w, height: h, fixCols: 3, fixRows: 2});

    // 複製テーブルのドラッグ＋ドロップは無効に
    $('.crossTableDiv, .rowTableDiv, .colTableDiv').find('table').removeAttr('id');
    $('.crossTableDiv, .rowTableDiv, .colTableDiv').find('th').removeAttr('id');
    $('.crossTableDiv, .rowTableDiv, .colTableDiv').find('td').removeAttr('id data-company data-name data-floor');
    $('.crossTableDiv, .rowTableDiv, .colTableDiv').find('span').removeAttr('id data-originalId data-carNo data-before');

    $('.crossTableDiv, .rowTableDiv, .colTableDiv').find('td').removeClass('drop-able draggable removable reserveTdHover');
    $('.crossTableDiv, .rowTableDiv, .colTableDiv').find('th').removeClass('drop-able draggable removable');
    $('.crossTableDiv, .rowTableDiv, .colTableDiv').find('tr').removeClass('drop-able draggable removable reserveRow');
}
// 予約テーブルのクリア
function removeTable(){
    var clonedTable = $('.bodyTableDiv').find('table').clone();
    $(clonedTable).attr('style', '');
    $('.baseDiv').remove();
    $('#table-responsive-body').append(clonedTable.prop("outerHTML"));
}

// ドラッグ設定
function setDraggable(){
    // ドラッグ可能にする
    $('.draggable').draggable({
          connectToSortable: '.drop-able, .removable'
        , helper: 'clone'                     // clone: 複製、original：移動
        , revert: false                       // true：範囲外の場合は元に戻す、false：範囲外の場合は消す
        , opacity: 0.5                        // ドラッグ中の透明度
        , stop: function(event,ui){
            ui.helper.attr('id',$(this).attr('id'));
        }
    });
}
// ドロップ設定(削除)
function setRemovable(){
    // ドロップ可能にする(削除ゾーン)
    $('.removable').sortable( {
        revert: false
        , stop: function(event, ui) {
            $(ui.item).remove();
            setColor();
        }
    });
}

var partTapTime = 0;
var partTapTimer;

// ドロップ設定(削除)
function setSortable(){
    // ドロップ可能にする
    $('.drop-able').sortable( {
            revert: false                   // ドロップ時のアニメーション
          , stop: function(event, ui) {     // ドロップして、ソートした後の時の動き
                // 複製されたコマ
                var clonedItem = $(ui.item);

                var ua = navigator.userAgent;
                if (ua.indexOf('iPad') > 0 || ua.indexOf('Android') > 0 || ua.indexOf('iPhone') > 0 || ua.indexOf('iPod') > 0){
                    // タッチデバイス
                    clonedItem.find('.badgeCls').on('touchstart', function(e) {
                        clonedItem.remove();
                        setColor();
                        return false;
                    });
                    clonedItem.bind({
                        'touchstart': function(e) {
                            partTapTimer = setTimeout(function(){
                                partTapTime += 100;
                            }, 100)
                            return false;
                        },
                        'touchend': function(e) {
                            if (partTapTime < 200) {
                                if(clonedItem.find('.badgeCls').attr('data-badge-top-right')){
                                    clonedItem.find('.badgeCls').removeAttr('data-badge-top-right');
                                }else{
                                    clonedItem.find('.badgeCls').attr('data-badge-top-right', '1');
                                }
                            }
                            clearTimeout(partTapTimer);
                            partTapTime = 0;
                            return false;
                        },
                    });
                }else{
                    // PCデバイス
                    clonedItem.find('.badgeCls').on('click', function(e) {
                        clonedItem.remove();
                        setColor();
                        return false;
                    });
                    clonedItem.on('click', function(e) {
                        if(clonedItem.find('.badgeCls').attr('data-badge-top-right')){
                            clonedItem.find('.badgeCls').removeAttr('data-badge-top-right');
                        }else{
                            clonedItem.find('.badgeCls').attr('data-badge-top-right', '1');
                        }
                        return false;
                    });
                }

                var originalId = clonedItem.attr('data-originalId');

                // ドロップ箇所に同じ作業車がある場合は消す
                var carNo = clonedItem.attr('data-carNo');
                if($(this).find('[data-carNo="' + carNo + '"]').length > 1){
                    clonedItem.remove();
                }else{
                    clonedItem.addClass('cloned');
                    clonedItem.removeClass('original');

                    // 色付け
                    setColor();

                    // 幅を調整
                    var no = $(this).attr('data-company');
                    var minWidth = $(this).outerWidth()*1.1 + "px";
                    $('[data-th="th_' + no + '"]').css("min-width", '');
                    $('[data-th="th_' + no + '"]').css("min-width", minWidth);
                }

                // 受け取ったクローンをさらにドラッグ可能にする
                clonedItem.draggable({
                    connectToSortable: '.drop-able, .removable'
                    , helper: 'clone'
                    , revert: false
                    , opacity: 0.5
                    , stop: function(event,ui){
                        var parent = $(this).parent();//.outerWidth() + "px";
                        var no = parent.attr('data-company');

                        // 生成したクローンをさらにドラッグした時の動作
                        $(this).remove();   // 元を消去

                        // 色付け
                        setColor();

                        $('[data-th="th_' + no + '"]').css("min-width", '');
                        $('[data-th="th_' + no + '"]').css("min-width", parent.outerWidth()*1.1 + "px");
                    }
                });
          }
    });
}

// 全体の色付け
function setColor(){
    // 予約のコマの色付け
    var rsvObjArray = $('.bodyTableDiv').find('.reserveRow').find('.cloned');

    // 一旦色を消す
    $(rsvObjArray).removeClass("reserveNormal reserveDuplicate reserveDiff reserveNone reserveDone");

    $.each(rsvObjArray, function(i, rsvObj) {
        var cn = $(rsvObj).attr('data-carNo');
        var rsvObjects = $(".reserveRow").find('[data-carNo="' + cn + '"]')

        if($(rsvObjects).length > 1){
            // 予約の行に同じ作業車がある場合は、「予約重複」
            $(rsvObjects).addClass('reserveDuplicate');

        }else if($(rsvObjects).length == 1){
            if($(rsvObjects).parent().attr('data-name') != $(rsvObjects).attr('data-before')){
                // 前日と異なる階 / 業者で予約
                $(rsvObjects).addClass('reserveDiff');
            }else if($(rsvObjects).parent().attr('data-name') == $(rsvObjects).attr('data-before')){
                // 前日と同じ場合「予約希望」
                $(rsvObjects).addClass('reserveNormal');
            }
        }
    });

    // 稼働のコマの色付け
    var useObjArray = $('.bodyTableDiv').find('td.useTd').find('.original');

    // 一旦色を消す
    $(useObjArray).removeClass("reserveNormal reserveDuplicate reserveDiff reserveNone reserveDone");

    $.each(useObjArray, function(i, useObj) {

        // 予約の行に同じ作業車があるかをチェック
        var cn = $(useObj).attr('data-carNo');
        var useObjects = $(".reserveRow").find('[data-carNo="' + cn + '"]')

        if(useObjects.length > 0){
            // 「予約済み」
            $(useObj).addClass('reserveDone');
        }else{
            // 「予約なし」
            $(useObj).addClass('reserveNone');
        }
    });
}

$(function(){
    // テーブルを固定
    fixTable();
    // マウス・タッチの動作のバインド
    bindMouseAndTouch();
    // ドラッグ設定
    setDraggable();
    // ドロップ可能にする(削除)
    setRemovable();
    // ドロップ可能にする
    setSortable();

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
            setDraggable();
            setRemovable();
            setSortable();
        }, 200);
    });
});
