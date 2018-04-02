// マウス・タッチの動作のバインド
function bindMouseAndTouch(){
    var ua = navigator.userAgent;
    if (ua.indexOf('iPad') > 0 || ua.indexOf('Android') > 0 || ua.indexOf('iPhone') > 0 || ua.indexOf('iPod') > 0){
        // タッチデバイスの場合
        $(".rowHover").bind({
            'touchstart': function(e) {
                if(e.originalEvent.touches.length == 1){
<<<<<<< HEAD
                    var floorId = $(this).attr('id');
                    moveTo(window.location.pathname + "?floorId" + floorId);
=======
                    $(this).find('td').addClass('rowHoverColor');
                    //showInputModal();
>>>>>>> design
                }
            },
            'touchend': function(e) {
                $(this).find('td').removeClass('rowHoverColor');
            },
            'touchmove': function(e) {
                $(this).find('td').removeClass('rowHoverColor');
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

<<<<<<< HEAD
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
=======

// テーブルのクリア
function addDummyItem(){
    $("#td_0").append('<span class="part">001</span>');
    $("#td_0").append('<span class="part">002</span>');
    $("#td_0").append('<span class="part">003</span>');
    $("#td_0").append('<span class="part">004</span>');
    $("#td_1").append('<span class="part">005</span>');
    $("#td_1").append('<span class="part">006</span>');
    $("#td_2").append('<span class="part">007</span>');
    $("#td_3").append('<span class="part">008</span>');
    $("#td_4").append('<span class="part">009</span>');
>>>>>>> design
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
