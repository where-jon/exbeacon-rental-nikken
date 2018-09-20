var secUpdateUnit = 300000 // 5分更新
// 表示ボタンをクリックする時に発生するイベント
function btnEvent(){


    btnUpdate = $("#btn-update");
    btnUpdate.on("click", function() {
        gInit.reloadView();
    });

    var vTrAllElement = [].slice.call(document.querySelectorAll(".checkTd"))
    for (var i = 0;i<vTrAllElement.length;++i){
        if(vTrAllElement[i] != null){
            vTrAllElement[i].title=("最終受信時刻\n" + vTrAllElement[i].children[0].textContent)
        }
    }

}

$(function(){
    // 表示ボタンをクリック
    btnEvent();

    // テーブルを固定
    gInitView.fixTable("NoBtn");

    // マウス操作とタップ操作をバインド
    gInitView.bindMouseAndTouch();

    // 画面サイズ変更による再調整
    gInitView.tableResize("NoBtn");

    // 5分単位更新
    setInterval(function() {
     gInit.reloadView();
    }, secUpdateUnit);

});
