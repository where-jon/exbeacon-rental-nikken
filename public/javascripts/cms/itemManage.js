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
        if ($('.mainSpace').height() > h) {
            var w = $('.mainSpace').width()*0.99;
            $('.itemTable').tablefix({width:w, height: h, fixRows: 2});
        } else {
            var w = $('.mainSpace').width();
            $('.itemTable').tablefix({width:w, fixRows: 2});
        }
    }else{
        // PCブラウザ
//        var w = $('.mainSpace').width()*1.001; // ヘッダー右側ボーダーが切れる為*1.001
//        if ($('.mainSpace').height() > h) {
//            w = $('.mainSpace').width()-5;
//            $('.itemTable').tablefix({width:w, height: h, fixRows: 2});
//            w = $('.mainSpace').width()-14;
//            $('.rowTableDiv').width(w);
//        } else {
//            $('.rowTableDiv').width(w);
//        }

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
    $('.baseDiv').remove();
    $('.table-responsive-body').append(clonedTable.prop("outerHTML"));
}

// モーダル画面の表示
function showInputModal(isRegister){
    if(isRegister){
        // 新規
        $('#inputItemOtherId').val('');
        $('#inputItemOtherBtxId').val('');
        $('#inputItemOtherNo').val('');
        $('#inputItemOtherName').val('');
        $('#inputItemNote').val('');
        $('#inputItemTypeName').val('');
        $('.cloned').remove();
        // ボタン表示の切り替え
        $('#itemUpdateFooter').addClass('hidden');
        $('#itemRegisterFooter').removeClass('hidden');

        $('#inputModal').modal();
    }else{
        if($('.rowHoverSelectedColor').length > 0){
            var itemOtherId = $('.rowHoverSelectedColor').attr('data-itemOtherId');
            $('.cloned').remove();
            $('#inputItemOtherId').val(itemOtherId);
            $('#inputItemOtherBtxId').val($('#'+itemOtherId).find('.itemOtherBtxId').text());
            $('#inputItemOtherNo').val($('#'+itemOtherId).find('.itemOtherNo').text());
            $('#inputItemOtherName').val($('#'+itemOtherId).find('.itemOtherName').text());
            $('#inputItemNote').val($('#'+itemOtherId).find('.itemOtherNote').text());
            $('#inputItemTypeName').val($('#'+itemOtherId).find('.itemTypeName').text());

            // ボタン表示の切り替え
            $('#itemUpdateFooter').removeClass('hidden');
            $('#itemRegisterFooter').addClass('hidden');

            $('#inputModal').modal();
        }
    }
}

function showDeleteModal(){
    if($('.rowHoverSelectedColor').length > 0){
        $('#deleteModal').modal();
    }
}

function deleteItemOtherId(){
    if($('.rowHoverSelectedColor').length > 0){
        var itemOtherId = $('.rowHoverSelectedColor').attr('data-itemOtherId');
        $('#deleteItemOtherId').val(itemOtherId)
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
