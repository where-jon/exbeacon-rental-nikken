// 鍵TagID 一時保存用
var btxIdBack = "-1";

// サブミット
function doSubmit(formId, action){
    $('#' + formId).attr('action', action);
    $('#' + formId).submit();
}

// モーダル画面の表示
function showInputModal(isRegister){
    if(isRegister){
        $('#inputCarId').val('');
        $('#inputCarNo').val('');
        $('#inputCarBtxId').val('');
        $('#inputCarKeyBtxIdDsp').val('');
        $('#inputCarKeyBtxId').val('');
        var ele = document.getElementById("inputCarKeyBtxIdDsp");
        ele.readOnly = false;
        btxIdBack = "";
        $('#inputCarTypeName').val('');
        $('#inputCarTypeId').val('');
        $('#inputCarTypeCategoryId').val(0);
        $('#car_type').val("0");
        $('#inputCarName').val('');
        $('#inputCarNote').val('');

        // ボタン表示の切り替え
        $('#carRegisterFooter').removeClass('hidden');
        $('#carUpdateFooter').addClass('hidden');
        $('#inputModal').modal();
    }else{
        if($('.rowHoverSelectedColor').length > 0){
            var carId = $('.rowHoverSelectedColor').attr('data-carId');
            $('#inputCarId').val(carId);
            // 種別の初期表示を指定
            document.carForm.car_type.selectedIndex = parseInt($('#'+carId).find('.itemTypeOrder').text()) - 1;
            $('#inputCarNo').val($('#'+carId).find('.carNo').text());
            $('#inputCarBtxId').val($('#'+carId).find('.carBtxId').text());
            $('#inputCarKeyBtxId').val($('#'+carId).find('.carKeyBtxId').text());
            btxIdBack = $('#inputCarKeyBtxId').val();
            $('#inputCarTypeName').val($('#'+carId).find('.carTypeName').text());
            $('#car_type').val($('#'+carId).find('.carTypeId').text());
            $('#inputCarTypeId').val($('#car_type').val());
            var ele = document.getElementById("inputCarKeyBtxIdDsp");
            // 立馬の場合は鍵TagIDは入力不可
            var categoryid = parseInt($('#'+carId).find('.itemTypeCategoryid').text());
            $('#inputCarTypeCategoryId').val(categoryid);
            if(categoryid == 1){
               $('#inputCarKeyBtxIdDsp').val("無");
               $('#inputCarKeyBtxId').val("9999999999");
               ele.readOnly = true;
            }else{
               $('#inputCarKeyBtxIdDsp').val($('#'+carId).find('.carKeyBtxId').text());
               ele.readOnly = false;
            }
            $('#inputCarName').val($('#'+carId).find('.carName').text());
            $('#inputCarNote').val($('#'+carId).find('.carNote').text());

            // ボタン表示の切り替え
            $('#carUpdateFooter').removeClass('hidden');
            $('#carRegisterFooter').addClass('hidden');
            $('#inputModal').modal();
        }
    }
}

function showDeleteModal(){
    if($('.rowHoverSelectedColor').length > 0){
        var carId = $('.rowHoverSelectedColor').attr('data-carId');
        $('#deleteCarTypeId').val($('#'+carId).find('.carTypeId').text());
        $('#deleteModal').modal();
    }
}

function deleteCar(){
    if($('.rowHoverSelectedColor').length > 0){
        var carId = $('.rowHoverSelectedColor').attr('data-carId');
        $('#deleteCarId').val(carId);
        $('#deleteForm').submit();
    }
}

// サブミット
function getFilterCheck(){
    var inputItemType = document.getElementById("inputCarTypeName");
    if(inputItemType!=null){
         $('#ITEM_TYPE_FILTER').val(inputItemType.value);
    }
}

// 表示ボタンをクリックする時に発生するイベント
function viewBtnEvent(){
var floorFrame = $('#car_type');
        if(floorFrame!=null){
            // 管理者用selectbox value取得
            $('#car_type').change(function() {
            var itemType = $("#car_type option:selected").data();
            var ele = document.getElementById("inputCarKeyBtxIdDsp");
            // カテゴリID設定
            $('#inputCarTypeCategoryId').val(itemType.categoryid);
            // 立馬の場合は鍵TagIDは入力不可
            if(itemType.categoryid == 1){
               $('#inputCarKeyBtxIdDsp').val("無");
               $('#inputCarKeyBtxId').val("9999999999");
                ele.readOnly = true;
            }else{
                $('#inputCarKeyBtxIdDsp').val(btxIdBack);
                $('#inputCarKeyBtxId').val(btxIdBack);
                ele.readOnly = false;
            }
        });
    }

    var viewBtnElement = document.getElementById("carUpdateFooter")
        viewBtnElement.addEventListener('click', function(event) {
            // itemTypeId結果をfromへ設定
            var itemTypeFilterResult = $('#car_type option:selected').val();
            $('#inputCarTypeId').val(itemTypeFilterResult);
            var itemTypeNameFilterResult = $('#car_type option:selected').text();
            $('#inputCarTypeName').val(itemTypeNameFilterResult);
        });

    var viewBtnElement = document.getElementById("carRegisterFooter")
        viewBtnElement.addEventListener('click', function(event) {
            // itemTypeId結果をfromへ設定
            var itemTypeFilterResult = $('#car_type option:selected').val();
            $('#inputCarTypeId').val(itemTypeFilterResult);
            var itemTypeNameFilterResult = $('#car_type option:selected').text();
            $('#inputCarTypeName').val(itemTypeNameFilterResult);
        });
}

$(function(){
    // filter値確認
    getFilterCheck();

    // 表示ボタンをクリック
    viewBtnEvent();

    // 鍵TagID 設定
    $('#inputCarKeyBtxIdDsp').on('change', function(e) {
        $('#inputCarKeyBtxId').val($('#inputCarKeyBtxIdDsp').val());
    });

    // テーブルを固定
    gInitView.fixTable();

    // 表示ボタンをクリック
    viewBtnEvent();

    // マウス操作とタップ操作をバインド
    gInitView.bindMouseAndTouch();

    // 画面サイズ変更による再調整
    gInitView.tableResize();
});
