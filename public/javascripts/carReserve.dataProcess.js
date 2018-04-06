// データ削除
function deleteReserve(obj){
    var parent = $(obj).parent();
    var no = parent.attr('data-company');
    var reserveId = $(obj).attr("data-reserveId");
    var data = {
        reserveId: Number(reserveId)
    };
    $.ajax({
        type: "POST",
        url: window.location.pathname + "/delete",
        data:JSON.stringify(data),
        contentType: 'application/json', // リクエストの Content-Type
        dataType: "json",           // レスポンスをJSONとしてパースする
        success: function(json_data) {   // 200 OK時
            // 削除
            $(obj).remove();
            // 枠の調整
            $('.company_th_' + no).css("min-width", '');
            $('.company_th_' + no).css("min-width", parent.outerWidth() + "px");
            // 色付け
            setColor();

            console.log(json_data);
        },
        error: function(e) {         // HTTPエラー時
            alert("予約削除にてHTTPエラーが発生");
            console.log(e);
        },
        complete: function() {      // 成功・失敗に関わらず通信が終了した際の処理
        }
    });
}
// データ予約
function updateReserve(reserveId, floorId, companyId){
    var data = {
       reserveId: Number(reserveId),
       carId: null,
       floorId: Number(floorId),
       companyId: Number(companyId),
       reserveDate: $('#reserveDate').val()
    };

    $.ajax({
        type: "POST",
        url: window.location.pathname + "/update",
        data:JSON.stringify(data),
        contentType: 'application/json',
        dataType: "json",
        success: function(json_data) {
            console.log(json_data);
        },
        error: function(e) {
            alert("予約更新にてHTTPエラーが発生");
            console.log(e);
        },
        complete: function() {
        }
    });
}
// データ登録
function registerReserve(carId, floorId, companyId, obj){
    var data = {
       reserveId: null,
       carId: Number(carId),
       floorId: Number(floorId),
       companyId: Number(companyId),
       reserveDate: $('#reserveDate').val()
    };

    $.ajax({
        type: "post",
        url: window.location.pathname + "/register",
        data: JSON.stringify(data),
        contentType: 'application/json',
        dataType: "json",
        success: function(result) {
            console.log(result);
            $(obj).attr('data-reserveid', result.reserveId);
        },
        error: function(e) {
            alert("予約登録にてHTTPエラーが発生");
            console.log(e);
        },
        complete: function() {
        }
    });
}
