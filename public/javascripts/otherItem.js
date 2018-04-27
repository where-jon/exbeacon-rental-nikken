// マウス・タッチの動作のバインド
function bindMouseAndTouch(){
    var ua = navigator.userAgent;
    if (ua.indexOf('iPad') > 0 || ua.indexOf('Android') > 0 || ua.indexOf('iPhone') > 0 || ua.indexOf('iPod') > 0){
        // タッチデバイスの場合
        $(".rowHover").bind({
            'touchstart': function(e) {
                $(this).find('td').addClass('summerySelected');
            },
            'touchend': function(e) {
                $(this).find('td').removeClass('summerySelected');
                var floorId = $(this).attr('id');
                moveTo(window.location.pathname + "?floorId=" + floorId);
            },
            'touchmove': function(e) {
                $(this).find('td').removeClass('summerySelected');
                var floorId = $(this).attr('id');
                moveTo(window.location.pathname + "?floorId=" + floorId);
            }
        });
    }else{
        // PCブラウザの場合
        $(".rowHover").bind({
            'mouseover': function(e) {
                $(this).find('td').addClass('rowHoverColor');
            },
            'mouseout': function(e) {
                $(this).find('td').removeClass('rowHoverColor');
            },
            'click': function(e) {
                var floorId = $(this).attr('id');
                moveTo(window.location.pathname + "?floorId=" + floorId);
            },
        });
    }
}

// テーブルの固定
function fixTable(){
    // 表テーブルの固定
    var h = $(".pc-side-nav").height()*0.97;
    $('#itemTable').tablefix({height: h, fixRows: 2});

    // 複製テーブルのドラッグ＋ドロップは無効に
    $('.crossTableDiv, .rowTableDiv, .colTableDiv').find('table').removeAttr('id');
    $('.crossTableDiv, .rowTableDiv, .colTableDiv').find('th').removeAttr('id');
    $('.crossTableDiv, .rowTableDiv, .colTableDiv').find('td').removeAttr('id data-company data-name data-floor');
    $('.crossTableDiv, .rowTableDiv, .colTableDiv').find('span').removeAttr('id data-originalId data-carNo data-before');
}
// テーブルのクリア
function removeTable(){
    var clonedTable = $('.bodyTableDiv').find('table').clone();
    $(clonedTable).attr('style', '');
    $(clonedTable).find('span').remove();
    $('.baseDiv').remove();
    $('#table-responsive-body').append(clonedTable.prop("outerHTML"));
}

// 仮設材マークの描画
function drawItem(){
    // APIからデータを取得
    $.ajax({
        type: "GET",
        url: window.location.pathname + "/getPlotInfo?floorId=" + $('#floorId').val(),
        cache: false,
        datatype: 'json',
        success: function (json) {
            // 集計情報を表示
            $.each(json.summeryInfo, function(i, record){
                // 行の生成
                var row = $('.template').clone();
                $(row).removeClass('template');
                $(row).find('.floorNameTd').text(record.floorName);
                if(record.count > 0){
                    var unit = $(row).find('.itemCountSpan').attr('data-unit');
                    $(row).find('.itemCountSpan').text(record.count + unit);
                }
                // 色付け
                if(record.floorId == Number($('#floorId').val())){
                    $(row).find('.floorNameTd').addClass("summerySelected");
                    $(row).find('.itemTd').addClass("summerySelected");
                }else{
                    $(row).find('.floorNameTd').css("cursor", "pointer");
                    $(row).find('.itemTd').css("cursor", "pointer");
                }

                $(row).attr('id', record.floorId);
                $(row).removeClass('hidden');
                $('.template').before(row);
            });
            // 合計の表示
            $('#totalCountSpan').text(json.allCount);

            // 仮設材情報を表示
            $.each(json.itemInfo, function(i, record){
                var htmlStr = '<span class="part">'+record.itemNo+'</span>';
                if(record.itemNo.length == 1){
                    htmlStr = '<span class="part">&nbsp;'+record.itemNo+'&nbsp;</span>';
                }
                var id = 'td_' + record.itemKindId;
                $("#" + id).append(htmlStr);
            });
            // 作業車が多い場合は改行をつけて見やすくする
            $.each($('.plottedTd'), function(i, td){
                $.each($(td).find('span'), function(idx, span){
                    if(idx == 10){
                        $(span).before('<br/><br/>');
                    }
                });
            });

            // クリック可能にする
            bindMouseAndTouch();
        },
        error: function (e) {
            console.dir(e);
        }
    });
}

$(function(){
    // テーブルを固定
    fixTable();
    drawItem();

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

    // 10分毎にリロード
    setInterval("location.reload();", 1000 * 60 * 10);
});
