// サブミット
function doSubmit(formId, action){
    $('#' + formId).attr('action', action);
    $('#' + formId).submit();
}

// モーダル画面の表示
function showCreateModal() {
    $('#createModal').modal();
}

function showUpdateModal(){
    var selectLine = $('.rowHoverSelectedColor');
    if(selectLine.length > 0){
        $('#updateUserId').val(selectLine.attr('data-userId'));
        $('#updateUserName').val(selectLine.attr('data-userName'));
        $('#updateUserLoginId').val(selectLine.attr('data-userLoginId'));
        $('#updateUserLevel').val(selectLine.attr('data-userLevel'));
        $('#updateModal').modal();
    }
}

function showPasswordModal(){
    var selectLine = $('.rowHoverSelectedColor');
    if(selectLine.length > 0){
        $('#passwordUpdateUserId').val(selectLine.attr('data-userId'));
        $('#passwordModal').modal();
    }
}

function showDeleteModal(){
    var selectLine = $('.rowHoverSelectedColor');
    if(selectLine.length > 0){
        $('#deleteUserId').val(selectLine.attr('data-userId'));
        $('#deleteModal').modal();
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
