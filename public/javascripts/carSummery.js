// 予約テーブルの固定
function fixTable(){
    // 予約表テーブルの固定
    var h = $("#reserveTable").height();
    var w = $('.mainSpace').width() * 0.95;
    $('#reserveTable').tablefix({width: w, height: h, fixCols: 3, fixRows: 2});

    // 複製テーブルのドラッグ＋ドロップは無効に
    $('.crossTableDiv, .rowTableDiv, .colTableDiv').find('table').removeAttr('id');
    $('.crossTableDiv, .rowTableDiv, .colTableDiv').find('th').removeAttr('id');
    $('.crossTableDiv, .rowTableDiv, .colTableDiv').find('td').removeAttr('id data-company data-name data-floor');
    $('.crossTableDiv, .rowTableDiv, .colTableDiv').find('span').removeAttr('id data-originalId data-carNo data-before');

    $('.crossTableDiv, .rowTableDiv, .colTableDiv').find('td').removeClass('reserveTdHover');
//    $('.crossTableDiv, .rowTableDiv, .colTableDiv').find('th').removeClass('draggable');
    $('.crossTableDiv, .rowTableDiv, .colTableDiv').find('tr').removeClass('reserveRow');
}
// 予約テーブルのクリア
function removeTable(){
    var clonedTable = $('.bodyTableDiv').find('table').clone();
    $(clonedTable).attr('style', '');
    $(clonedTable).find('span').remove();
    $('.baseDiv').remove();
    $('#table-responsive-body').append(clonedTable.prop("outerHTML"));
}

function addDummyData(){
    $("#reserveTd_4_x").append('<span class="part reserveNone">005</span>');
    $("#useTd_3_x").append('<span class="part useNotWorking">008</span>');
    $("#reserveTd_3_x").append('<span class="part reserveNone">008</span>');

    $("#useTd_5_0").append('<span class="part useWorking">001</span>');
    $("#useTd_5_0").append('<span class="part useWorking">002</span>');
    $("#reserveTd_4_0").append('<span class="part reserveContent">001</span>');
    $("#reserveTd_4_0").append('<span class="part reserveContent">002</span>');
    $('[data-id="th_0"]').css("min-width", $("#useTd_5_0").outerWidth() + "px");

    $("#useTd_4_1").append('<span class="part useWorking">003</span>');
    $("#useTd_4_1").append('<span class="part useWorking">004</span>');
    $("#reserveTd_4_1").append('<span class="part reserveContent">003</span>');
    $("#reserveTd_4_1").append('<span class="part reserveContent">004</span>');
    $('[data-id="th_1"]').css("min-width", $("#useTd_4_1").outerWidth() + "px");

    $("#useTd_4_2").append('<span class="part useWorking">005</span>');
    $("#useTd_4_2").append('<span class="part useWorking">006</span>');
    $("#reserveTd_4_2").append('<span class="part reserveContent">006</span>');
    $('[data-id="th_2"]').css("min-width", $("#useTd_4_2").outerWidth() + "px");

    $("#useTd_4_3").append('<span class="part useWorking">007</span>');
    $("#reserveTd_4_3").append('<span class="part reserveContent">007</span>');
    $("#useTd_3_3").append('<span class="part useWorking">009</span>');
    $("#reserveTd_3_3").append('<span class="part reserveContent">009</span>');
    $('[data-id="th_3"]').css("min-width", $("#useTd_4_3").outerWidth() + "px");

    $("#useTd_3_4").append('<span class="part useWorking">010</span>');
    $("#reserveTd_3_4").append('<span class="part reserveContent">010</span>');
    $('[data-id="th_4"]').css("min-width", $("#useTd_3_4").outerWidth() + "px");

    $("#useTd_3_5").append('<span class="part useWorking">011</span>');
    $("#reserveTd_3_5").append('<span class="part reserveContent">011</span>');
    $("#useTd_2_5").append('<span class="part useWorking">013</span>');
    $("#reserveTd_2_5").append('<span class="part reserveContent">013</span>');
    $('[data-id="th_5"]').css("min-width", $("#useTd_3_5").outerWidth() + "px");

    $("#useTd_3_6").append('<span class="part useWorking">012</span>');
    $("#reserveTd_3_6").append('<span class="part reserveContent">012</span>');
    $("#useTd_2_6").append('<span class="part useNotWorking">014</span>');
    $("#reserveTd_2_6").append('<span class="part reserveContent">014</span>');
    $('[data-id="th_6"]').css("min-width", $("#useTd_3_6").outerWidth() + "px");

}

$(function(){
    // テーブルを固定
    fixTable();

    addDummyData();

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
            addDummyData();
        }, 200);
    });
});
