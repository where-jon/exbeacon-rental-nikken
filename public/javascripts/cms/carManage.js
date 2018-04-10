// マウス・タッチの動作のバインド
function bindMouseAndTouch(){
    var ua = navigator.userAgent;
    if (ua.indexOf('iPad') > 0 || ua.indexOf('Android') > 0 || ua.indexOf('iPhone') > 0 || ua.indexOf('iPod') > 0){
        // タッチデバイスの場合
        $(".rowHover").bind({
            'touchstart': function(e) {
                if(e.originalEvent.touches.length > 1){
                }else if(e.originalEvent.touches.length == 1){
                    $(".rowHoverSelectedColor").removeClass('rowHoverSelectedColor');
                    $(this).addClass('rowHoverSelectedColor');
                }
            },
        });
    }else{
        // PCブラウザの場合
        $(".rowHover").bind({
            'mouseover': function(e) {
                $(this).addClass('rowHoverColor');
            },
            'mouseout': function(e) {
                $(this).removeClass('rowHoverColor');
            },
            'click': function(e) {
                $(".rowHoverSelectedColor").removeClass('rowHoverSelectedColor');
                $(this).addClass('rowHoverSelectedColor');
            },
        });
    }
}

// テーブルの固定
function fixTable(){
    // テーブルの固定
    var h = $(window).height()*0.7;
    // テーブルの調整
    var ua = navigator.userAgent;
    if (ua.indexOf('iPad') > 0 || ua.indexOf('Android') > 0 || ua.indexOf('iPhone') > 0 || ua.indexOf('iPod') > 0){
        // タッチデバイス
        var w = $('.mainSpace').width()*0.993;
        $('.itemTable').tablefix({width:w, height: h, fixRows: 2});

    }else{
        // PCブラウザ
        var w = $('.mainSpace').width();
        $('.itemTable').tablefix({height: h, fixRows: 2});
        $('.rowTableDiv').width(w);
    }
    $('.bodyTableDiv').find('.itemTable').css('margin-bottom','0');
    $('.colTableDiv').css("width","");

}
// テーブルのクリア
function removeTable(){
    var clonedTable = $('.bodyTableDiv').find('table').clone();
    $(clonedTable).attr('style', '');
    $(clonedTable).find('span').remove();
    $('.baseDiv').remove();
    $('.table-responsive-body').append(clonedTable.prop("outerHTML"));
}

// サブミット
function doSubmit(formId, action){
    $('#' + formId).attr('action', action)
    $('#' + formId).submit();
}

// モーダル画面の表示
function showInputModal(isRegister){
    if(isRegister){
        $('#inputCarId').val('');
        $('#inputCarNo').val('');
        $('#inputCarName').val('');
        $('#inputCarBtxId').val('');
        $('#inputCarKeyBtxId').val('');

        // ボタン表示の切り替え
        $('#carRegisterFooter').removeClass('hidden');
        $('#carUpdateFooter').addClass('hidden');
        $('#inputModal').modal();
    }else{
        if($('.rowHoverSelectedColor').length > 0){
            var carId = $('.rowHoverSelectedColor').attr('data-carId');
            $('#inputCarId').val(carId);
            $('#inputCarNo').val($('#'+carId).find('.carNo').text());
            $('#inputCarName').val($('#'+carId).find('.carName').text());
            $('#inputCarBtxId').val($('#'+carId).find('.carBtxId').text());
            $('#inputCarKeyBtxId').val($('#'+carId).find('.carKeyBtxId').text());

            // ボタン表示の切り替え
            $('#carUpdateFooter').removeClass('hidden');
            $('#carRegisterFooter').addClass('hidden');
            $('#inputModal').modal();
        }
    }
}

function showDeleteModal(){
    if($('.rowHoverSelectedColor').length > 0){
        $('#deleteModal').modal();
    }
}

function deleteCar(){
    if($('.rowHoverSelectedColor').length > 0){
        var carId = $('.rowHoverSelectedColor').attr('data-carId');
        $('#deleteCarId').val(carId)
        $('#deleteForm').submit()
    }
}

$(function(){
    // テーブルを固定
    fixTable();
    // マウス操作とタップ操作をバインド
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
