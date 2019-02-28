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
    gInitView.fixTable();

    // マウス操作とタップ操作をバインド
    gInitView.bindMouseAndTouch();

    // 画面サイズ変更による再調整
    gInitView.tableResize();
});
