

// 予約テーブルの固定
function fixTable(){
    // 予約表テーブルの固定
    var h = $(".pc-side-nav").height()*0.97;
    $('#itemTable').tablefix({height: h, fixRows: 2});

    // 複製テーブルのドラッグ＋ドロップは無効に
    $('.crossTableDiv, .rowTableDiv, .colTableDiv').find('table').removeAttr('id');
    $('.crossTableDiv, .rowTableDiv, .colTableDiv').find('th').removeAttr('id');
    $('.crossTableDiv, .rowTableDiv, .colTableDiv').find('td').removeAttr('id data-company data-name data-floor');
    $('.crossTableDiv, .rowTableDiv, .colTableDiv').find('span').removeAttr('id data-originalId data-carNo data-before');

//    $('.crossTableDiv, .rowTableDiv, .colTableDiv').find('tr').removeClass('reserveRow');
}
// 予約テーブルのクリア
function removeTable(){
    var clonedTable = $('.bodyTableDiv').find('table').clone();
    $(clonedTable).attr('style', '');
    $(clonedTable).find('span').remove();
    $('.baseDiv').remove();
    $('#table-responsive-body').append(clonedTable.prop("outerHTML"));
}


// 予約テーブルのクリア
function addDummyItem(){
    $("#td_0").append('<span class="itemPart">001</span>');
    $("#td_0").append('<span class="itemPart">002</span>');
    $("#td_0").append('<span class="itemPart">003</span>');
    $("#td_0").append('<span class="itemPart">004</span>');
    $("#td_1").append('<span class="itemPart">005</span>');
    $("#td_1").append('<span class="itemPart">006</span>');
    $("#td_2").append('<span class="itemPart">007</span>');
    $("#td_3").append('<span class="itemPart">008</span>');
    $("#td_4").append('<span class="itemPart">009</span>');
}

$(function(){
    // テーブルを固定
    fixTable();
    addDummyItem();

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
            addDummyItem();
        }, 200);
    });
});
