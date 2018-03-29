// マウス・タッチの動作のバインド
function bindMouseAndTouch(){
    var ua = navigator.userAgent;
    if (ua.indexOf('iPad') > 0 || ua.indexOf('Android') > 0 || ua.indexOf('iPhone') > 0 || ua.indexOf('iPod') > 0){
        // タッチデバイスの場合
        $(".rowHover").bind({
            'touchstart': function(e) {
                if(e.originalEvent.touches.length == 1){
                    var floorId = $(this).attr('id');
                    moveTo(window.location.pathname + "?floorId" + floorId);
                }
            },
            'touchend': function(e) {
                $(this).removeClass('rowHoverColor');
            },
            'touchmove': function(e) {
                $(this).removeClass('rowHoverColor');
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

//    $('.crossTableDiv, .rowTableDiv, .colTableDiv').find('tr').removeClass('reserveRow');
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
            // 仮設材情報を表示
            $.each(json, function(i, record){
                var htmlStr = '<span class="itemPart">'+record.itemNo+'</span>';
                var id = 'td_' + record.itemKindId;
                $("#" + id).append(htmlStr);
            });
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
