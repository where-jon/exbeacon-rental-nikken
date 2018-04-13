// モーダル画面の表示
function showInputModal(obj){
    clearInterval(document.interval);

    $('#inputFloorId').val($(obj).attr('data-floor'));
    $('#floorSpan').text($(obj).attr('data-floorNameStr'));
    $('#inputCompanyId').val($(obj).attr('data-company'));
    $('#companySpan').text($(obj).attr('data-companyNameStr'));
    $('#inputCarNo').val('');
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
                    var targetTd = $(this);
                    touched = true;
                    touch_time = 0;
                    document.interval = setInterval(function(){
                        touch_time += 500;
                        if (touch_time == 500) {
                            // ロングタップ時の処理
                            showInputModal($(targetTd));
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
                showInputModal($(this));
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
            // 予約の削除
            deleteReserve($(obj));
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
            deleteReserve($(obj));// 実際の削除
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
        $(obj).find('.carNoTxtCls').on('click', function(e) {
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
    var w = $('.mainSpace').width() * 0.96;
    $('#reserveTable').tablefix({width: w, height: h, fixCols: 3, fixRows: 2});

    // 複製テーブルのドラッグ＋ドロップは無効に
    var divs = '.crossTableDiv, .rowTableDiv, .colTableDiv';
    $(divs).find('table').removeAttr('id');
    $(divs).find('th').removeAttr('id');
    $(divs).find('td').removeAttr('id data-company data-pos data-floor');
    $(divs).find('span').removeAttr('id data-originalId data-carNo data-before');
    $(divs).find('tr').removeAttr('data-floorId');

    $(divs).find('span').removeClass('draggable reserveNone original badgeCls');
    $(divs).find('td').removeClass('drop-able removable reserveTdHover');
    $(divs).find('th').removeClass('drop-able removable');
    $(divs).find('tr').removeClass('drop-able removable reserveRow workRow');
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
            // 予約の行にドラッグ中の場合はフロアと業者名を表示
            if(parentOnDrag != ui.helper.parent()){
                // 親要素のtdを表示
                var floorName = ui.helper.parent().attr('data-floorNameStr');
                var companyName = ui.helper.parent().attr('data-companyNameStr');
                if(floorName && companyName){
                    ui.helper.attr('data-dragDisp',floorName+"/"+companyName);
                    parentOnDrag = ui.helper.parent();
                }else{
                    ui.helper.removeAttr('data-dragDisp');
                    parentOnDrag = null;
                }
            }
//            $('.drop-able').attr('drop-waiting', '1');
        }
        , stop: function(event,ui){
            // ドラッグ中の表示を終了
            ui.helper.removeAttr('data-dragDisp');
            parentOnDrag = null;
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
        target = $('.cloned');
    }
    // ドラッグ可能にする
    $(target).draggable({
        connectToSortable: '.drop-able, .removable'
        , helper: 'clone'
        , revert: false
        , opacity: 0.5
        , drag: function(event,ui){
            // 予約の行にドラッグ中の場合はフロアと業者名を表示
            if(parentOnDrag != ui.helper.parent()){
                // 親要素のtdを表示
                var floorName = ui.helper.parent().attr('data-floorNameStr');
                var companyName = ui.helper.parent().attr('data-companyNameStr');
                if(floorName && companyName){
                    ui.helper.attr('data-dragDisp',floorName+"/"+companyName);
                    parentOnDrag = ui.helper.parent()
                }else{
                    ui.helper.removeAttr('data-dragDisp');
                    parentOnDrag = null;
                }
            }
        }
        , stop: function(event,ui){
            // ドラッグ中の表示を終了
            ui.helper.removeAttr('data-dragDisp');
            parentOnDrag = null;

            var parent = $(this).parent();
            var no = parent.attr('data-company');
            // 生成したクローンをさらにドラッグした時の動作
            $(this).remove();   // 元を消去
            // 色付け
            setColor();
            // 幅の調整
            $('.company_th_' + no).css("min-width", '');
            $('.company_th_' + no).css("min-width", parent.outerWidth()*1 + "px");
        }
    });
}
// ドロップ設定(削除)
function setRemovable(){
    // ドロップ可能にする(削除ゾーン)
    $('.removable').sortable( {
        revert: false
        , stop: function(event, ui) {
            var clonedItem = $(ui.item);
            // 削除
            if($(clonedItem).attr("data-reserveId")){
                deleteReserve($(clonedItem));// 実際の削除
            }else{
                clonedItem.remove();
            }
        }
    });
}

var partTapTime = 0;
var partTapTimer;

// ドロップ設定
function setSortable(){
    // ドロップ可能にする
    $('.drop-able').sortable( {
            revert: false                   // ドロップ時のアニメーション
          , stop: function(event, ui) {     // ドロップして、ソートした後の時の動き
                // 複製されたコマ
                var clonedItem = $(ui.item);

                // 削除バッジ設定
                setDeleteBadge(clonedItem);

                // ドロップ箇所に同じ作業車がある場合 -> データ的には何もしない
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
                        $('.company_th_' + no).css("min-width", '');
                        $('.company_th_' + no).css("min-width", parent.outerWidth() + "px");
                    }else{
                        var dc = clonedItem.attr('data-current');
                        if($(this).find('[data-current="' + dc + '"]').length > 1){
                            // ただ同じ場所に移動した時
                            clonedItem.addClass('cloned');
                            clonedItem.removeClass('original');
                            // 色付け
                            setColor();
                            // 幅を調整
                            var no = $(this).attr('data-company');
                            var minWidth = $(this).outerWidth() + "px";
                            $('.company_th_' + no).css("min-width", '');
                            $('.company_th_' + no).css("min-width", minWidth);
                            // 受け取ったクローンをさらにドラッグ可能にする
                            setDraggableClone(clonedItem);
                        }else{
                            // 違う場所から移動 -> 重複、となった場合
//                            var parent = $(clonedItem).parent();//.outerWidth() + "px";
//                            var no = parent.attr('data-company');
                            deleteReserve($(clonedItem));
//                            // 色付け
//                            setColor();
//                            // 幅の調整
//                            $('[data-th="th_' + no + '"]').css("min-width", '');
//                            $('[data-th="th_' + no + '"]').css("min-width", parent.outerWidth()*1 + "px");
                        }

                    }
                }else{  // ドロップ箇所に同じ作業車がない場合 -> データ的に処理する
                    var floorId = $(this).attr('data-floor');
                    var companyId = $(this).attr('data-company');
                    if(clonedItem.hasClass('original')){
                        // originalから足す時は新規登録
                        var carId = clonedItem.attr('data-carId');
                        registerReserve(carId, floorId, companyId, clonedItem);
                    }else{
                        // 更新
                        var carNo = clonedItem.attr('data-carNo');
                        if($(this).find('[data-carId="' + carNo + '"]').length == 0){
                            var reserveId = clonedItem.attr('data-reserveId');
                            updateReserve(reserveId, floorId, companyId);
                        }
                    }
                    clonedItem.addClass('cloned');
                    clonedItem.removeClass('original');
                    var value = $(this).attr('data-floor') +"_"+ $(this).attr('data-company');

                    clonedItem.attr('data-current', value);
                    // 色付け
                    setColor();
                    // 幅を調整
                    var no = $(this).attr('data-company');
                    var minWidth = $(this).outerWidth() + "px";
                    $('.company_th_' + no).css("min-width", '');
                    $('.company_th_' + no).css("min-width", minWidth);
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
    var rsvObjArray_length = rsvObjArray.length;

    // 一旦色を消す
    $(rsvObjArray).removeClass("reserveNormal reserveDuplicate reserveDiff reserveNone reserveDone");

    for(var i = 0; i < rsvObjArray_length; i++){
        var rsvObj = rsvObjArray[i];
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
    }

    // 稼働のコマの色付け
    var useObjArray = document.getElementsByClassName("original");
    var useObjArray_length = useObjArray.length;

    // 一旦色を消す
    $(useObjArray).removeClass("reserveNormal reserveDuplicate reserveDiff reserveNone reserveDone");

    for(var i = 0; i < useObjArray_length; i++){
        var useObj = useObjArray[i];
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
    }

    // サマリー表の色付け
    var isTotalDuplicate = false;
    $('.todayReserveCount').removeClass("summeryDuplicate summeryColoredTd");

    var reserveRowList = document.getElementsByClassName("reserveRow");
    var reserveRowList_length = reserveRowList.length;
    for(var i = 0; i < reserveRowList_length; i++){
        var row = reserveRowList[i];
        var floorId = $(row).attr('data-floorId');
        if($(row).find(".reserveDuplicate").length > 0){
            $('#summery_floorId_'+floorId+' > .todayReserveCount').addClass('summeryDuplicate');
            isTotalDuplicate = true;
        }else{
            $('#summery_floorId_'+floorId+' > .todayReserveCount').addClass('summeryColoredTd');
        }
    }
    if(isTotalDuplicate){
        $('#summery_floorId_total > .todayReserveCount').addClass('summeryDuplicate');
    }else{
        $('#summery_floorId_total > .todayReserveCount').addClass('summeryColoredTd');
    }

    setSummeryCount();
}

// サマリーの集計
function setSummeryCount(){
    // 稼働
    var total = 0;
    var rowList = document.getElementsByClassName("workRow");
    var rowList_length = rowList.length;
    for(var i = 0; i < rowList_length; i++){
        var row = rowList[i];
        var floorId = $(row).attr('data-floorId');
        var count = $(row).find(".original").length;
        total += count;
        if(count > 0){
            $('#summery_floorId_'+floorId+' > .carExistCountStr > span').text(count + "台");
        }else{
            $('#summery_floorId_'+floorId+' > .carExistCountStr > span').text("");
        }
    }
    $('#summery_floorId_total > .carExistCountStr > span').text(total);

    // 予約
    total = 0;
    rowList = document.getElementsByClassName("reserveRow");
    rowList_length = rowList.length;
    for(var i = 0; i < rowList_length; i++){
        var row = rowList[i];
        var floorId = $(row).attr('data-floorId');
        var count = $(row).find(".cloned").length;
        total += count;
        if(count > 0){
            $('#summery_floorId_'+floorId+' > .todayReserveCount > span').text(count + "台");
        }else{
            $('#summery_floorId_'+floorId+' > .todayReserveCount > span').text("");
        }
    }
    $('#summery_floorId_total > .todayReserveCount > span').text(total);
}

// 初期表示時の処理
var dispFlg = false;
$(function(){

    $('.datePickerArea').datepicker({
        language: "ja",
        orientation: "bottom auto",
        clearBtn: true,
        autoclose: true,
    }).on('changeDate', function(e){
        if(e.format('yyyymmdd') != ""){
            location.href = window.location.pathname + "?reserveDate=" + e.format('yyyymmdd')
        }

    });
//    $('.datePickerArea').on('click', function(){
//        if(dispFlg){
//            $('#inputReserveDate').datepicker("hide");
//            dispFlg = false;
//        }else{
//            $('#inputReserveDate').datepicker("show");
//            dispFlg = true;
//        }
//    });

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
    // 削除バッチ設定
    var objArray = document.getElementsByClassName('cloned');
    var length = objArray.length;
    for(var i = 0; i < length; i++){
        setDeleteBadge(objArray[i]);
    }
    // 色付け
    setColor();

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
            // 削除バッチ設定
            for(var i = 0; i < length; i++){
                setDeleteBadge(objArray[i]);
            }
        }, 200);
    });
});
