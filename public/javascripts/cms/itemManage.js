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

// モーダル画面の表示
function showInputModal(isRegister){
    if(isRegister){
        // 新規
        $('#inputItemOtherId').val('');
        $('#inputItemTypeId').val('');
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
            $('#inputItemTypeId').val('');
            $('#inputItemOtherBtxId').val($('#'+itemOtherId).find('.itemOtherBtxId').text());
            $('#inputItemOtherNo').val($('#'+itemOtherId).find('.itemOtherNo').text());;
            $('#inputItemOtherName').val($('#'+itemOtherId).find('.itemOtherName').text());
            $('#inputItemNote').val($('#'+itemOtherId).find('.itemOtherNote').text());;
            $('#inputItemTypeName').val($('#'+itemOtherId).find('.itemTypeName').text());

//            var spanObjList = $('#'+itemKindId).find('span.item');
//            $(spanObjList).each(function(index, element){
//                var clonedRow = $('.template').clone();
//                clonedRow.addClass('cloned');
//                clonedRow.removeClass('template');
//                clonedRow.find('span.inputItemNoSpan').text($(element).attr('data-itemNo'));
//                clonedRow.find('span.inputItemBtxIdSpan').text($(element).attr('data-itemBtxId'));
//                $('.template').before(clonedRow);
//                var value = $('#actualItemInfoStr').val();
//                $('#actualItemInfoStr').val(value + "-" + $(element).attr('data-itemNo')
//                                                        + ',' + $(element).attr('data-itemBtxId')
//                                                        )
//                clonedRow.removeClass('hidden');
//            });

            // ボタン表示の切り替え
            $('#itemUpdateFooter').removeClass('hidden');
            $('#itemRegisterFooter').addClass('hidden');

            $('#inputModal').modal();
        }
    }
}

function showDeleteModal(){
    if($('.rowHoverSelectedColor').length > 0){
        var itemKindId = $('.rowHoverSelectedColor .itemKindId').html();
        $('#deleteItemId').val(itemKindId);
        $('#deleteModal').modal();
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

// 入力モーダルのTxタグの行を追加
function addTagRow(){

    if($('#inputItemNo').val() != '' && $('#inputItemBtxId').val() != ''){
        if($('#inputItemNo').val().match(/[0-9a-zA-Z]/) && $('#inputItemBtxId').val().match(/[0-9a-zA-Z]/)){
            var duplicateFlg = false;
            $('.cloned').each(function(index, element){
                if($(element).find('span.inputItemNoSpan').text() == $('#inputItemNo').val()
                    || $(element).find('span.inputItemBtxIdSpan').text() == $('#inputItemBtxId').val()){
                    duplicateFlg = true;
                    return false;
                }
            });

            if(duplicateFlg){
                return false;
            }

            var clonedRow = $('.template').clone();
            clonedRow.addClass('cloned');
            clonedRow.removeClass('template');

            // 表示文字列の設定
            clonedRow.find('span.inputItemNoSpan').text($.trim($('#inputItemNo').val()));
            clonedRow.find('span.inputItemBtxIdSpan').text($.trim($('#inputItemBtxId').val()));
            // 値の設定
            var value = $('#actualItemInfoStr').val();
            $('#actualItemInfoStr').val(value + "-" + $("#inputItemNo").val()
                                                    + ',' + $("#inputItemBtxId").val()
                                                    )

            clonedRow.removeClass('hidden');
            $('.template').before(clonedRow);
            $('#inputItemNo').val('');
            $('#inputItemBtxId').val('');
        }
    }
}

// 入力モーダルのTxタグの行の削除
function removeTagRow(obj){
    var clonedRow = $(obj).parent().parent();
    var value = "-" + clonedRow.find('span.inputItemNoSpan').text() + ',' + clonedRow.find('span.inputItemBtxIdSpan').text();
    var originalValue = $('#actualItemInfoStr').val();
    originalValue = originalValue.replace(value, '');
    $('#actualItemInfoStr').val(originalValue);
    clonedRow.remove();
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
