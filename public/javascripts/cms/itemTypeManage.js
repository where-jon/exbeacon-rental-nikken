var clickPos;
var colorBtn;
var rgbClickId;
var clickElement;
var arPkey = [];
var arSelectIndex = [];
var arInsert = [];
var arAddFrame = [];
var bCheckInsert = false;
var vPickerPos = 0;
var vPrePickerPos = -1;
var defaultBtn
var bCheckConfirm = false
var bCheckDelete = false
var bCheckUpdate = false
var bNodataCountCheck
var vCompDepCode
var vDelCheck;
$( window ).resize(function() {
	gInit.setScrollSize("itemTypeManage")
});

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
        $('#inputItemTypeId').val('');
        $('#inputItemTypeName').val('');
        $('#inputItemTypeCategoryName').val('');
        $('#inputItemTypeCategory').val('');
        $('#item_type').val("0");
        $('#inputItemTypeIconColor').val("rgb(255,255,255");
        $('#background-color').val("#FFFFFF");
        $('#inputItemTypeTextColor').val("rgb(0,0,0");
        $('#text-color').val("#000000");
        $('#inputItemTypeRowColor').val("rgb(0,0,0");
        $('#row-color').val("#000000");
        $('#inputNote').val('');

        $('#itemTypeUpdateFooter').addClass('hidden');
        $('#itemTypeRegisterFooter').removeClass('hidden');
        $('#inputModal').modal();
    }else{
        if($('.rowHoverSelectedColor').length > 0){
            var itemTypeId = $('.rowHoverSelectedColor .itemTypeId').html();
            $('#inputItemTypeId').val(itemTypeId);
            $('#inputItemTypeName').val($('#'+itemTypeId).find('.itemTypeName').text());
            $('#inputItemTypeCategory').val($('#'+itemTypeId).find('.itemTypeCategoryId').text());
            $('#item_type').val((parseInt($('#'+itemTypeId).find('.itemTypeCategoryId').text()) + 1).toString());

            $('#inputItemTypeIconColor').val($('#'+itemTypeId).find('.itemTypeIconColor').text());
            var rgbEditIcon = colorEdit($('#'+itemTypeId).find('.itemTypeIconColor').text());
            $('#background-color').val(rgbEditIcon);
            $('#inputItemTypeTextColor').val($('#'+itemTypeId).find('.itemTypeTextColor').text());
            var rgbEditText = colorEdit($('#'+itemTypeId).find('.itemTypeTextColor').text());
            $('#text-color').val(rgbEditText);
            $('#inputItemTypeRowColor').val($('#'+itemTypeId).find('.itemTypeRowColor').text());
            var rgbEditRow = colorEdit($('#'+itemTypeId).find('.itemTypeRowColor').text());
            $('#row-color').val(rgbEditRow);
            $('#inputNote').val($('#'+itemTypeId).find('.itemNote').text());

            $('#itemTypeUpdateFooter').removeClass('hidden');
            $('#itemTypeRegisterFooter').addClass('hidden');
            $('#inputModal').modal();
        }
    }
}

function showDeleteModal(){
    if($('.rowHoverSelectedColor').length > 0){
        $('#deleteModal').modal();
    }
}

function deleteItemType() {
    if($('.rowHoverSelectedColor').length > 0){
        var itemTypeId = $('.rowHoverSelectedColor .itemTypeId').html();
        $('#deleteItemTypeId').val(itemTypeId);
        $('#deleteForm').submit();
    }
}

// カラーコード変換
function colorEdit(rgbColor) {
    var rgbEdit = rgbColor.slice(4);
    rgbEdit = rgbEdit.replace(")", "");
    var listRgb = rgbEdit.split(",");
    var retRgb = "#";
    if(listRgb.length == 3) {
        retRgb = retRgb + ("00" + parseInt(listRgb[0]).toString(16)).slice(-2);
        retRgb = retRgb + ("00" + parseInt(listRgb[1]).toString(16)).slice(-2);
        retRgb = retRgb + ("00" + parseInt(listRgb[2]).toString(16)).slice(-2);
    }else{
        retRgb = retRgb + "000000";
    }
    return retRgb;
}

// 表示ボタンをクリックする時に発生するイベント
function viewBtnEvent(){
    var viewBtnElement = document.getElementById("itemTypeUpdateFooter")
        viewBtnElement.addEventListener('click', function(event) {
            // itemTypeId結果をfromへ設定
            var itemTypeFilterResult = $('#item_type option:selected').val() - 1;
            $('#inputItemTypeCategory').val(itemTypeFilterResult);
        });

    var viewBtnElement = document.getElementById("itemTypeRegisterFooter")
        viewBtnElement.addEventListener('click', function(event) {
            // itemTypeId結果をfromへ設定
            var itemTypeFilterResult = $('#item_type option:selected').val() - 1;
            $('#inputItemTypeCategory').val(itemTypeFilterResult);
        });
}

$(function(){
    // テーブルを固定
    fixTable();
    // 表示ボタンをクリック
    viewBtnEvent();
    // マウス操作とタップ操作をバインド
    bindMouseAndTouch();

    $('#background-color').on('change', function(e) {
        var colorEdit = $(this).val();
        colorEdit = colorEdit.slice(1);
        var r = parseInt(colorEdit.substring(0, 2), 16);
        var g = parseInt(colorEdit.substring(2, 4), 16);
        var b = parseInt(colorEdit.substring(4, 6), 16);
        var colorRgb = "rgb(" + r + "," + g + "," + b + ")";
        $('#inputItemTypeIconColor').val(colorRgb);
      });
      $('#text-color').on('change', function(e) {
        var colorEdit = $(this).val();
        colorEdit = colorEdit.slice(1);
        var r = parseInt(colorEdit.substring(0, 2), 16);
        var g = parseInt(colorEdit.substring(2, 4), 16);
        var b = parseInt(colorEdit.substring(4, 6), 16);
        var colorRgb = "rgb(" + r + "," + g + "," + b + ")";
        $('#inputItemTypeTextColor').val(colorRgb);
      });
      $('#row-color').on('change', function(e) {
        var colorEdit = $(this).val();
        colorEdit = colorEdit.slice(1);
        var r = parseInt(colorEdit.substring(0, 2), 16);
        var g = parseInt(colorEdit.substring(2, 4), 16);
        var b = parseInt(colorEdit.substring(4, 6), 16);
        var colorRgb = "rgb(" + r + "," + g + "," + b + ")";
        $('#inputItemTypeRowColor').val(colorRgb);
      });

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
