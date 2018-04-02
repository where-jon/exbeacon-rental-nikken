// マウス・タッチの動作のバインド
function bindMouseAndTouch(){
    var ua = navigator.userAgent;
    if (ua.indexOf('iPad') > 0 || ua.indexOf('Android') > 0 || ua.indexOf('iPhone') > 0 || ua.indexOf('iPod') > 0){
        // タッチデバイスの場合
        $(".rowHover").bind({
            'touchstart': function(e) {
                if(e.originalEvent.touches.length > 1){
                }else if(e.originalEvent.touches.length == 1){
<<<<<<< HEAD
                    $(this).addClass('rowHoverColor');
                    var itemKindId = $(this).attr('id');
                    touched = true;
                    touch_time = 0;
                    document.interval = setInterval(function(){
                        touch_time += 100;
                        if (touch_time == common.longTapTime) {
                            // ロングタップ時の処理
                            showInputModal(itemKindId);
                        }
                    }, 100)
=======
                    $(".rowHoverSelectedColor").removeClass('rowHoverSelectedColor');
                    $(this).addClass('rowHoverSelectedColor');
>>>>>>> design
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
<<<<<<< HEAD
                var itemKindId = $(this).attr('id');
                showInputModal(itemKindId);
=======
                $(".rowHoverSelectedColor").removeClass('rowHoverSelectedColor');
                $(this).addClass('rowHoverSelectedColor');
>>>>>>> design
            },
        });
    }
}


<<<<<<< HEAD
// モーダル画面の表示
function showInputModal(itemKindId){
    if(itemKindId == ""){
        // 新規
        $('#inputItemKindId').val('');
        $('#actualItemInfoStr').val('');
        $('#inputItemKindName').val('');
        $('#inputNote').val('');
        $('#inputItemNo').val('');
        $('#inputItemBtxId').val('');
        $('.cloned').remove();
        // ボタン表示の切り替え
        $('#itemUpdateFooter').addClass('hidden');
        $('#itemRegisterFooter').removeClass('hidden');
    }else{
        $('.cloned').remove();
        $('#inputItemKindId').val(itemKindId);
        $('#actualItemInfoStr').val('');
        $('#inputItemKindName').val($('#'+itemKindId).find('.itemKindName').text());
        $('#inputNote').val($('#'+itemKindId).find('.note').text());
        $('#inputItemNo').val('');
        $('#inputItemBtxId').val('');

        var spanObjList = $('#'+itemKindId).find('span.item');
        $(spanObjList).each(function(index, element){
            var clonedRow = $('.template').clone();
            clonedRow.addClass('cloned');
            clonedRow.removeClass('template');
            clonedRow.find('span.inputItemNoSpan').text($(element).attr('data-itemNo'));
            clonedRow.find('span.inputItemBtxIdSpan').text($(element).attr('data-itemBtxId'));
            $('.template').before(clonedRow);
            var value = $('#actualItemInfoStr').val();
            $('#actualItemInfoStr').val(value + "-" + $(element).attr('data-itemNo')
                                                    + ',' + $(element).attr('data-itemBtxId')
                                                    )
            clonedRow.removeClass('hidden');
        });

        // ボタン表示の切り替え
        $('#itemUpdateFooter').removeClass('hidden');
        $('#itemRegisterFooter').addClass('hidden');
    }
    $('#inputModal').modal();
}

=======
>>>>>>> design
// テーブルの固定
function fixTable(){
    // 表テーブルの固定
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

<<<<<<< HEAD
=======
// モーダル画面の表示
function showInputModal(isRegister){
    if(isRegister){
        $('#inputModal').modal();
    }else{
        if($('.rowHoverSelectedColor').length > 0){
            $('#inputModal').modal();
        }
    }
}
function showDeleteModal(){
    if($('.rowHoverSelectedColor').length > 0){
        $('#deleteModal').modal();
    }
}
>>>>>>> design

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

// サブミット
function doSubmit(formId, action){
    $('#' + formId).attr('action', action)
    $('#' + formId).submit();
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
