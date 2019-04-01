// モーダル画面の表示
function showInputModal(){
    $('#inputModal').modal();
}

function showUpdateModal(){
    var selectLine = $('.rowHoverSelectedColor');
    if(selectLine.length > 0){
        $('#updatePlaceId').val(selectLine.attr('data-placeId'));
        $('#updatePlaceName').val(selectLine.attr('data-placeName'));
        $('#updatePlaceStatus').val(selectLine.attr('data-statusCode'));
        $('#updateModal').modal();
    }
}

function showPasswordModal(){
    var selectLine = $('.rowHoverSelectedColor');
    if(selectLine.length > 0){
        $('#pwPlaceId').val(selectLine.attr('data-placeId'));
        $('#upUserId').val(selectLine.attr('data-userEmail'));
        $('#passwordModal').modal();
    }
}

function showDeleteModal(){
    if($('.rowHoverSelectedColor').length > 0){
        $('#deleteModal').modal();
    }
}

function moveToSelected(){
    if($('.rowHoverSelectedColor').length > 0){
        var placeId = $('.rowHoverSelectedColor').attr('data-placeId');
        $('#inputPlaceId').val(placeId);
        $('#placeChangeForm').submit();
    }
}

function deleteSelectPlace() {
    if($('.rowHoverSelectedColor').length > 0){
        var placeId = $('.rowHoverSelectedColor').attr('data-placeId');
        $('#deletePlaceId').val(placeId);
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
