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
        $('#inputItemTypeId').val('');
        $('#other_type').val("0");
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
            // 種別の初期表示を指定
            document.itemForm.other_type.selectedIndex = parseInt($('#'+itemOtherId).find('.itemTypeOrder').text()) - 1;
            $('#inputItemOtherBtxId').val($('#'+itemOtherId).find('.itemOtherBtxId').text());
            $('#inputItemOtherNo').val($('#'+itemOtherId).find('.itemOtherNo').text());
            $('#inputItemOtherName').val($('#'+itemOtherId).find('.itemOtherName').text());
            $('#inputItemNote').val($('#'+itemOtherId).find('.itemOtherNote').text());

            // ボタン表示の切り替え
            $('#itemUpdateFooter').removeClass('hidden');
            $('#itemRegisterFooter').addClass('hidden');

            $('#inputModal').modal();
        }
    }
}

function showDeleteModal(){
    if($('.rowHoverSelectedColor').length > 0){
        var itemOtherId = $('.rowHoverSelectedColor').attr('data-itemOtherId');
        $('.cloned').remove();
        $('#deleteItemTypeId').val($('#'+itemOtherId).find('.itemTypeId').text());
        $('#deleteModal').modal();
    }
}

function deleteItemOtherId(){
    if($('.rowHoverSelectedColor').length > 0){
        var itemOtherId = $('.rowHoverSelectedColor').attr('data-itemOtherId');
        $('#deleteItemOtherId').val(itemOtherId);
        $('#deleteForm').submit();
    }
}

// 表示ボタンをクリックする時に発生するイベント
function viewBtnEvent(){
    var viewBtnElement = document.getElementById("itemUpdateFooter");
        viewBtnElement.addEventListener('click', function(event) {
            // itemTypeId結果をfromへ設定
            var itemTypeFilterResult = $('#other_type option:selected').val();
            $('#inputItemTypeId').val(itemTypeFilterResult);
            var itemTypeNameFilterResult = $('#other_type option:selected').text();
            $('#inputItemTypeName').val(itemTypeNameFilterResult);
        });

    var viewBtnElement = document.getElementById("itemRegisterFooter");
        viewBtnElement.addEventListener('click', function(event) {
            // itemTypeId結果をfromへ設定
            var itemTypeFilterResult = $('#other_type option:selected').val();
            $('#inputItemTypeId').val(itemTypeFilterResult);
            var itemTypeNameFilterResult = $('#other_type option:selected').text();
            $('#inputItemTypeName').val(itemTypeNameFilterResult);
        });
}

$(function(){
    var otherTypeFrame = $('#other_type');
        if(otherTypeFrame!=null){
            // 管理者用selectbox value取得
            $('#other_type').change(function() {
            var itemType = $('#other_type option:selected').val();
        });
    }

    // テーブルを固定
    gInitView.fixTable();

    // 表示ボタンをクリック
    viewBtnEvent();

    // マウス操作とタップ操作をバインド
    gInitView.bindMouseAndTouch();

    // 画面サイズ変更による再調整
    gInitView.tableResize();

});
