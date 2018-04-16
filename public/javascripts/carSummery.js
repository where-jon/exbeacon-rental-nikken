// テーブルの固定
function fixTable(){
    // 表テーブルの固定
    var h = $("#reserveTable").height();
    var w = $('.mainSpace').width() * 0.95;
    $('#reserveTable').tablefix({width: w, height: h, fixCols: 3, fixRows: 2});

    // 複製テーブルのドラッグ＋ドロップは無効に
    $('.crossTableDiv, .rowTableDiv, .colTableDiv').find('table').removeAttr('id');
    $('.crossTableDiv, .rowTableDiv, .colTableDiv').find('th').removeAttr('id');
    $('.crossTableDiv, .rowTableDiv, .colTableDiv').find('td').removeAttr('id data-company data-name data-floor');
    $('.crossTableDiv, .rowTableDiv, .colTableDiv').find('span').removeAttr('id data-originalId data-carNo data-before');

    $('.crossTableDiv, .rowTableDiv, .colTableDiv').find('td').removeClass('reserveTdHover');
    $('.crossTableDiv, .rowTableDiv, .colTableDiv').find('tr').removeClass('reserveRow');
}

// テーブルのクリア
function removeTable(){
    var clonedTable = $('.bodyTableDiv').find('table').clone();
    $(clonedTable).attr('style', '');
    $('.baseDiv').remove();
    $('#table-responsive-body').append(clonedTable.prop("outerHTML"));
}

//
function drawCar(){
    // APIからデータを取得
    $.ajax({
        type: "GET",
        url: window.location.pathname + "/getPlotInfo",
        cache: false,
        datatype: 'json',
        success: function (json) {
            // 予約情報
            $.each(json.reserveInfoList, function(i, record){
                var htmlStr = '<span class="part reserveContent">'+record.carNo+'</span>';
                if(record.carNo.length == 1){
                    htmlStr = '<span class="part reserveContent">&nbsp;'+record.carNo+'&nbsp;</span>';
                }
                var id = 'reserveTd_' + record.floorId + '_' + record.companyId;
                $("#" + id).append(htmlStr);
                $('[data-id="th_'+ record.companyId +'"]').css("min-width", $("#" + id).outerWidth() + "px");
            });

            // 稼働情報
            $.each(json.workInfoList, function(i, record){
                var clsNm = "";
                if(record.companyId == ""){
                    //予約なし
                    clsNm = "reserveNone";
                }else{
                    if(record.isWorking){
                        clsNm = "useWorking";
                    }else{
                        clsNm = "useNotWorking";
                    }
                }
                var htmlStr = '<span class="part '+ clsNm + '">'+record.carNo+'</span>';
                if(record.carNo.length == 1){
                    htmlStr = '<span class="part '+ clsNm + '">&nbsp;'+record.carNo+'&nbsp;</span>';
                }
                var id = 'useTd_' + record.floorId + '_' + record.companyId;
                $("#" + id).append(htmlStr);
                $('[data-id="th_'+ record.companyId +'"]').css("min-width", $("#" + id).outerWidth() + "px");
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
    // テーブルの中身を描画
    drawCar();

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
    //setInterval("location.reload();", 5000);
});
