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
        $('#inputItemTypeCategory').val('');
        $('#inputItemTypeIconColor').val('');
        $('#inputItemTypeTextColor').val('');
        $('#inputItemTypeRowColor').val('');
        $('#inputNote').val('');

        $('#itemTypeUpdateFooter').addClass('hidden');
        $('#itemTypeRegisterFooter').removeClass('hidden');
        $('#inputModal').modal();
    }else{
        if($('.rowHoverSelectedColor').length > 0){
            var itemTypeId = $('.rowHoverSelectedColor .itemTypeId').html();
            $('#inputItemTypeId').val(itemTypeId);
            $('#inputItemTypeName').val($('#'+itemTypeId).find('.itemTypeName').text());
            $('#inputItemTypeCategory').val($('#'+itemTypeId).find('.itemTypeCategory').text());
            $('#inputItemTypeIconColor').val($('#'+itemTypeId).find('.itemTypeIconColor').text());
            $('#inputItemTypeTextColor').val($('#'+itemTypeId).find('.itemTypeTextColor').text());
            $('#inputItemTypeRowColor').val($('#'+itemTypeId).find('.itemTypeRowColor').text());
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

// 表示ボタンをクリックする時に発生するイベント
function viewBtnEvent(){
    var viewBtnElement = document.getElementById("itemTypeUpdateFooter")
        viewBtnElement.addEventListener('click', function(event) {
            // itemTypeId結果をfromへ設定
            var itemTypeFilterResult = $('#item_type option:selected').val();
            $('#inputItemTypeCategory').val(itemTypeFilterResult);
        });

    var viewBtnElement = document.getElementById("itemTypeRegisterFooter")
        viewBtnElement.addEventListener('click', function(event) {
            // itemTypeId結果をfromへ設定
            var itemTypeFilterResult = $('#item_type option:selected').val();
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
