// サブミット
function doSubmit(formId, action){
    $('#' + formId).attr('action', action)
    $('#' + formId).submit();
}

// モーダル画面の表示
function showPasswordUpdateModal(){
    $('#passwordUpdateModal').modal();
}
function showPlaceUpdateModal(){
    $('#placeUpdateModal').modal();
}
function showPlaceDeleteModal(){
    $('#placeDeleteModal').modal();
}

function showPasswordModal(){
    $('#passwordModal').modal();
}
