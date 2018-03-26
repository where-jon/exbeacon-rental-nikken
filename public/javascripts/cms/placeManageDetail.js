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
            },
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


// モーダル画面の表示
function showInputModal(){
    $('#inputModal').modal();
}
function showPasswordModal(){
    $('#passwordModal').modal();
}
function showFloorUpdateModal(isRegister){
    if(isRegister){
        $('#floorUpdateModal').modal();
    }else{
        if($('.rowHoverSelectedColor').length > 0){
            $('#floorUpdateModal').modal();
        }
    }
}
function showFloorDeleteModal(){
    if($('.rowHoverSelectedColor').length > 0){
        $('#floorDeleteModal').modal();
    }
}

// 入力モーダルのTxタグの行を追加
function addTagRow(){
    var clonedRow = $('.template').clone();
    clonedRow.addClass('cloned');
    clonedRow.removeClass('template');
    clonedRow.removeClass('hidden');
    $('.template').before(clonedRow);
}

// 入力モーダルのTxタグの行の削除
function removeTagRow(obj){
    $(obj).parent().parent().remove();
}

$(function(){
    bindMouseAndTouch();
});
