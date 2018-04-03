// モーダル画面の表示
function showInputModal(){
    clearInterval(document.interval);
    $('#inputModal').modal();
}

// マウス・タッチの動作のバインド
function bindMouseAndTouch(){
    var ua = navigator.userAgent;
    if (ua.indexOf('iPad') > 0 || ua.indexOf('Android') > 0 || ua.indexOf('iPhone') > 0 || ua.indexOf('iPod') > 0){
        // タッチデバイスの場合
        var touched = false;
        var touch_time = 0;
        $(".reserveTdHover").bind({
            'touchstart': function(e) {
                if(e.originalEvent.touches.length > 1){
                    touch_time = 0;
                    touched = false;
                    clearInterval(document.interval);
                }else if(e.originalEvent.touches.length == 1){
                    $(this).addClass('reserveTdHoverColor');
                    touched = true;
                    touch_time = 0;
                    document.interval = setInterval(function(){
                        touch_time += 500;
                        if (touch_time == 500) {
                            // ロングタップ時の処理
                            showInputModal();
                            touch_time = 0;
                            clearInterval(document.interval);
                        }
                    }, 500);
                }
            },
            'touchend': function(e) {
                $(this).removeClass('reserveTdHoverColor');
                touched = false;
                touch_time = 0;
                clearInterval(document.interval);
            },
            'touchmove': function(e) {
                $(this).removeClass('reserveTdHoverColor');
                touched = false;
                touch_time = 0;
                clearInterval(document.interval);
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
            'drag': function(e) {
                $(this).removeClass('reserveTdHoverColor');
            },
            'click': function(e) {
                $(this).removeClass('reserveTdHoverColor');
                showInputModal();
            },
        });
    }
}
// 削除バッジの設定
function setDeleteBadge(obj){
    var ua = navigator.userAgent;
    if (ua.indexOf('iPad') > 0 || ua.indexOf('Android') > 0 || ua.indexOf('iPhone') > 0 || ua.indexOf('iPod') > 0){
        // タッチデバイス
        $(obj).find('.badgeCls').on('touchstart', function(e) {
            clearInterval(document.interval);
            var parent = $(obj).parent();
            var no = parent.attr('data-company');

            $(obj).remove();
            setColor();

            $('#company_th_' + no).css("min-width", '');
            $('#company_th_' + no).css("min-width", parent.outerWidth() + "px");

            return false;
        });
        $(obj).bind({
            'touchstart': function(e) {
                clearInterval(document.interval);
                partTapTimer = setTimeout(function(){
                    partTapTime += 100;
                }, 100)
            },
            'touchend': function(e) {
                clearInterval(document.interval);
                if (partTapTime < 200) {
                    if($(obj).find('.badgeCls').attr('data-badge-top-right')){
                        $(obj).find('.badgeCls').removeAttr('data-badge-top-right');
                    }else{
                        $(obj).find('.badgeCls').attr('data-badge-top-right', '1');
                    }
                }
                clearTimeout(partTapTimer);
                partTapTime = 0;
                return false;
            },
        });
    }else{
        // PCデバイス
        $(obj).find('.badgeCls').on('click', function(e) {
            var parent = $(obj).parent();
            var no = parent.attr('data-company');

            $(obj).remove();
            setColor();

            $('#company_th_' + no).css("min-width", '');
            $('#company_th_' + no).css("min-width", parent.outerWidth() + "px");

            return false;
        });
        $(obj).on('click', function(e) {
            if($(obj).find('.badgeCls').attr('data-badge-top-right')){
                $(obj).find('.badgeCls').removeAttr('data-badge-top-right');
            }else{
                $(obj).find('.badgeCls').attr('data-badge-top-right', '1');
            }
            return false;
        });
    }
}

// 予約テーブルの固定
function fixTable(){
    // 予約表テーブルの固定
    var h = $("#reserveTable").height();
    var x = $("#reserveTable").width();
    var w = $('.mainSpace').width() * 0.96;
    $('#reserveTable').tablefix({width: w, height: h, fixCols: 3, fixRows: 2});

    // 複製テーブルのドラッグ＋ドロップは無効に
    $('.crossTableDiv, .rowTableDiv, .colTableDiv').find('table').removeAttr('id');
    $('.crossTableDiv, .rowTableDiv, .colTableDiv').find('th').removeAttr('id');
    $('.crossTableDiv, .rowTableDiv, .colTableDiv').find('td').removeAttr('id data-company data-pos data-floor');
    $('.crossTableDiv, .rowTableDiv, .colTableDiv').find('span').removeAttr('id data-originalId data-carNo data-before');

    $('.crossTableDiv, .rowTableDiv, .colTableDiv').find('span').removeClass('draggable reserveNone original badgeCls');

    $('.crossTableDiv, .rowTableDiv, .colTableDiv').find('td').removeClass('drop-able removable reserveTdHover');
    $('.crossTableDiv, .rowTableDiv, .colTableDiv').find('th').removeClass('drop-able removable');
    $('.crossTableDiv, .rowTableDiv, .colTableDiv').find('tr').removeClass('drop-able removable reserveRow');
}
// 予約テーブルのクリア
function removeTable(){
    var clonedTable = $('.bodyTableDiv').find('table').clone();
    $(clonedTable).attr('style', '');

    $('.baseDiv').remove();
    $('#table-responsive-body').append(clonedTable.prop("outerHTML"));
}

// ドラッグ設定
var parentOnDrag;
function setDraggableOriginal(){
    // ドラッグ可能にする
    $('.original').draggable({
          connectToSortable: '.drop-able, .removable'
        , helper: 'clone'                     // clone: 複製、original：移動
        , revert: false                       // true：範囲外の場合は元に戻す、false：範囲外の場合は消す
        , opacity: 0.5                        // ドラッグ中の透明度
        , drag: function(event,ui){
//            // 予約の行にドラッグ中の場合はフロアと業者名を表示
//            if(parentOnDrag != ui.helper.parent()){
//                // 親要素のtdを表示
//                var name = ui.helper.parent().attr('data-pos');
//                if(name){
//                    ui.helper.attr('data-dragDisp',name);
//                    parentOnDrag = ui.helper.parent()
//                }else{
//                    ui.helper.removeAttr('data-dragDisp');
//                    parentOnDrag = null;
//                }
//            }
//            $('.drop-able').attr('drop-waiting', '1');
        }
        , stop: function(event,ui){
//            // ドラッグ中の表示を終了
//            ui.helper.removeAttr('data-dragDisp');
//            parentOnDrag = null;
//            $('.drop-able').removeAttr('drop-waiting');

            ui.helper.attr('id',$(this).attr('id'));
        }
    });
}
// ドラッグ設定（クローン用）
function setDraggableClone(paramObj){
    var target;
    if(paramObj){
        target = $(paramObj);
    }else{
        target = $('.clone');
    }
    // ドラッグ可能にする
    $(target).draggable({
        connectToSortable: '.drop-able, .removable'
        , helper: 'clone'
        , revert: false
        , opacity: 0.5
        , drag: function(event,ui){
//            // 予約の行にドラッグ中の場合はフロアと業者名を表示
//            if(parentOnDrag != ui.helper.parent()){
//                // 親要素のtdを表示
//                var name = ui.helper.parent().attr('data-pos');
//                if(name){
//                    ui.helper.attr('data-dragDisp',name);
//                    parentOnDrag = ui.helper.parent()
//                }else{
//                    ui.helper.removeAttr('data-dragDisp');
//                    parentOnDrag = null;
//                }
//            }

        }
        , stop: function(event,ui){
//            // ドラッグ中の表示を終了
//            ui.helper.removeAttr('data-dragDisp');
//            parentOnDrag = null;

            var parent = $(this).parent();//.outerWidth() + "px";
            var no = parent.attr('data-company');
            // 生成したクローンをさらにドラッグした時の動作
            $(this).remove();   // 元を消去
            // 色付け
            setColor();
            // 幅の調整
            $('#company_th_' + no).css("min-width", '');
            $('#company_th_' + no).css("min-width", parent.outerWidth()*1 + "px");
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

                // 削除バッジ設定
                setDeleteBadge(clonedItem);

                var originalId = clonedItem.attr('data-originalId');

                // ドロップ箇所に同じ作業車がある場合
                var carNo = clonedItem.attr('data-carNo');
                if($(this).find('[data-carNo="' + carNo + '"]').length > 1){
                    if(clonedItem.hasClass('original')){
                        // originalから足す時
                        var parent = $(clonedItem).parent();//.outerWidth() + "px";
                        var no = parent.attr('data-company');
                        clonedItem.remove();
                        // 色付け
                        setColor();
                        // 幅の調整
                        $('#company_th_' + no).css("min-width", '');
                        $('#company_th_' + no).css("min-width", parent.outerWidth()*1 + "px");
                    }else{
                        // ただ同じ場所に移動した時
                        clonedItem.addClass('cloned');
                        clonedItem.removeClass('original');
                        // 色付け
                        setColor();
                        // 幅を調整
                        var no = $(this).attr('data-company');
                        var minWidth = $(this).outerWidth() + "px";
                        $('#company_th_' + no).css("min-width", '');
                        $('#company_th_' + no).css("min-width", minWidth);
                        // 受け取ったクローンをさらにドラッグ可能にする
                        setDraggableClone(clonedItem);
                    }

                }else{
                    clonedItem.addClass('cloned');
                    clonedItem.removeClass('original');
                    // 色付け
                    setColor();
                    // 幅を調整
                    var no = $(this).attr('data-company');
                    var minWidth = $(this).outerWidth() + "px";
                    $('#company_th_' + no).css("min-width", '');
                    $('#company_th_' + no).css("min-width", minWidth);
                    // 受け取ったクローンをさらにドラッグ可能にする
                    setDraggableClone(clonedItem);
                }
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
            if($(rsvObjects).parent().attr('data-pos') != $(rsvObjects).attr('data-before')){
                // 前日と異なる階 / 業者で予約
                $(rsvObjects).addClass('reserveDiff');
            }else if($(rsvObjects).parent().attr('data-pos') == $(rsvObjects).attr('data-before')){
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

// 初期表示時の処理
$(function(){
    // テーブルを固定
    fixTable();
    // マウス・タッチの動作のバインド
    bindMouseAndTouch();
    // ドラッグ設定
    setDraggableOriginal();
    // ドロップ可能にする(削除)
    setRemovable();
    // ドロップ可能にする
    setSortable();
    // ドラッグ設定(クローン用)
    setDraggableClone(null);

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
            setDraggableOriginal();
            setRemovable();
            setSortable();
            setDraggableClone(null);
        }, 200);
    });
});
