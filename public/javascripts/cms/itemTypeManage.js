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

// モーダル画面の表示
function showInputModal(isRegister){
    if(isRegister){
        $('#inputItemTypeId').val('');
        $('#inputItemTypeName').val('');
        $('#inputItemTypeCategoryName').val('');
        $('#inputItemTypeCategory').val('');
        $('#item_type').val('0');
        $('#inputItemTypeIconColor').val('rgb(106,106,106)');
        $('#background-color').val('#6a6a6a');
        $("#background-color").ColorPickerSliders({
            color: '#6a6a6a',
            placement: 'right',
            sliders: false,
            swatches: ['#F44336', '#E91E63', '#9C27B0', '#673AB7', '#3F51B5', '#2196F3', '#03A9F4', '#00BCD4', '#009688', '#4CAF50', '#8BC34A', '#CDDC39', '#FFEB3B', '#FFC107', '#FF9800', '#FF5722', '#795548', '#9E9E9E', '#607D8B', '#000000', '#FFFFFF'],
            hsvpanel: true
        });
        $('#inputItemTypeTextColor').val('rgb(106,106,106)');
        $('#text-color').val('#6a6a6a');
        $("#text-color").ColorPickerSliders({
            color: '#6a6a6a',
            placement: 'right',
            sliders: true,
            swatches: false,
            hsvpanel: true
        });
        $('#inputItemTypeRowColor').val('rgb(255,255,255)');
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
            $('#background-color').ColorPickerSliders({
                color: rgbEditIcon,
                placement: 'right',
                sliders: true,
                swatches: false,
                hsvpanel: true
            });
            $('#inputItemTypeTextColor').val($('#'+itemTypeId).find('.itemTypeTextColor').text());
            var rgbEditText = colorEdit($('#'+itemTypeId).find('.itemTypeTextColor').text());
            $('#text-color').val(rgbEditText);
            $('#text-color').ColorPickerSliders({
                color: rgbEditText,
                placement: 'right',
                sliders: true,
                swatches: false,
                hsvpanel: true
            });
            $('#inputItemTypeRowColor').val($('#'+itemTypeId).find('.itemTypeRowColor').text());
            var rgbEditRow = colorEdit($('#'+itemTypeId).find('.itemTypeRowColor').text());
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
    var viewBtnElement = document.getElementById("itemTypeUpdateFooter");
    viewBtnElement.addEventListener('click', function(event) {
        // itemTypeId結果をfromへ設定
        var itemTypeFilterResult = $('#item_type option:selected').val() - 1;
        $('#inputItemTypeCategory').val(itemTypeFilterResult);
        $('#inputItemTypeIconColor').val($('#background-color').val());
        $('#inputItemTypeTextColor').val($('#text-color').val());
    });
    var viewBtnElement = document.getElementById("itemTypeRegisterFooter");
    viewBtnElement.addEventListener('click', function(event) {
        // itemTypeId結果をfromへ設定
        var itemTypeFilterResult = $('#item_type option:selected').val() - 1;
        $('#inputItemTypeCategory').val(itemTypeFilterResult);
        $('#inputItemTypeIconColor').val($('#background-color').val());
        $('#inputItemTypeTextColor').val($('#text-color').val());
    });
}

$(function(){
    // テーブルを固定
    gInitView.fixTable();

    // 表示ボタンをクリック
    viewBtnEvent();

    // マウス操作とタップ操作をバインド
    gInitView.bindMouseAndTouch();

    // 画面サイズ変更による再調整
    gInitView.tableResize();
});
