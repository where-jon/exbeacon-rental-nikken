// 予約テーブルの固定
function fixTable(){
    // 予約表テーブルの固定
    var h = $("#reserveTable").height();
    var w = $('.mainSpace').width() * 0.95;
    $('#reserveTable').tablefix({width: w, height: h, fixCols: 2, fixRows: 2});
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
