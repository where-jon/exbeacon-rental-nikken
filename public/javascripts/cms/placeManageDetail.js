// マウス・タッチの動作のバインド
function bindMouseAndTouch(){
    var ua = navigator.userAgent;
    if (ua.indexOf('iPad') > 0 || ua.indexOf('Android') > 0 || ua.indexOf('iPhone') > 0 || ua.indexOf('iPod') > 0){
        // タッチデバイスの場合
        var touched = false;
        var touch_time = 0;
        $(".rowHover").bind({
            'touchstart': function(e) {
                if(e.originalEvent.touches.length > 1){
                    touch_time = 0;
                    touched = false;
                    clearInterval(document.interval);
                }else if(e.originalEvent.touches.length == 1){
                    $(this).addClass('rowHoverColor');
                    touched = true;
                    touch_time = 0;
                    document.interval = setInterval(function(){
                        touch_time += 100;
                        if (touch_time == common.longTapTime) {
                            // ロングタップ時の処理
                            showFloorModal();
                        }
                    }, 100)
                }
            },
            'touchend': function(e) {
                $(this).removeClass('rowHoverColor');
                touch_time = 0;
                touched = false;
                clearInterval(document.interval);
            },
            'touchmove': function(e) {
                $(this).removeClass('rowHoverColor');
                touch_time = 0;
                touched = false;
                clearInterval(document.interval);
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
                showFloorModal();
            },
        });
    }
}


// モーダル画面の表示
function showInputModal(){
    $('#inputModal').modal();
}
function showDeleteModal(){
    $('#deleteModal').modal();
}
function showFloorModal(){
    $('#floorModal').modal();
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
