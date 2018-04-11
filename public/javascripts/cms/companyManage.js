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
            }
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
        var w = $('.mainSpace').width()*0.995;
        if ($('.mainSpace').height() > h) {
            $('.itemTable').tablefix({width:w, height: h, fixRows: 2});
        } else {
            $('.itemTable').tablefix({width:w, fixRows: 2});
        }
    }else{
        // PCブラウザ
        var w = $('.mainSpace').width()*1.001; // ヘッダー右側ボーダーが切れる為*1.001
        if ($('.mainSpace').height() > h) {
            $('.itemTable').tablefix({height: h, fixRows: 2});
            $('.rowTableDiv').width(w);
        } else {
            $('.rowTableDiv').width(w);
        }
    }
    $('.bodyTableDiv').find('.itemTable').css('margin-bottom','0');
    $('.colTableDiv').css("width","");
}
// テーブルのクリア
function removeTable(){
    var clonedTable = $('.bodyTableDiv').find('table').clone();
    $(clonedTable).attr('style', '');
    $('.baseDiv').remove();
    $('.table-responsive-body').append(clonedTable.prop("outerHTML"));
}

// モーダル画面の表示
function showInputModal(isRegister){
    if(isRegister){
        $('#inputCompanyId').val('');
        $('#inputCompanyName').val('');
        $('#inputNote').val('');

        $('#companyUpdateFooter').addClass('hidden');
        $('#companyRegisterFooter').removeClass('hidden');
        $('#inputModal').modal();
    }else{
        if($('.rowHoverSelectedColor').length > 0){
            var companyId = $('.rowHoverSelectedColor .companyId').html();
            $('#inputCompanyId').val(companyId);
            $('#inputCompanyName').val($('#'+companyId).find('.companyName').text());
            $('#inputNote').val($('#'+companyId).find('.note').text());

            $('#companyUpdateFooter').removeClass('hidden');
            $('#companyRegisterFooter').addClass('hidden');
            $('#inputModal').modal();
        }
    }
}

function showDeleteModal(){
    if($('.rowHoverSelectedColor').length > 0){
        $('#deleteModal').modal();
    }
}

function deleteCompany() {
    if($('.rowHoverSelectedColor').length > 0){
        var companyId = $('.rowHoverSelectedColor .companyId').html();
        $('#deleteCompanyId').val(companyId);
        $('#deleteForm').submit();
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
