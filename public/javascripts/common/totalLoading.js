// 現在画面タイトル名を取得する
function getSerfaceText(element){
    var text = "";
    for (var i = 0; i < element.childNodes.length; i++) {
        if (element.childNodes[i].toString() == '[object Text]'){
            text +=
                element.childNodes[i].data;
        }
    }
    return text.trim();
}
// 画面loadingが終わってから走る
$(window).load(function() {
    $('#load').hide();
    // bodyにあるスクロールバーを削除
    document.body.style.overflow = "hidden"

    // 左サイドメニュークリックしたら活性化させる .start
    var vElement = $(".mainSpace").find('.form-group')
    var vTitle = getSerfaceText(vElement[0])
    var selectedElement = [].slice.call(document.querySelectorAll(".menuBtn"))
    selectedElement.forEach(function(selected, pos) {
        selected.addEventListener('click', function() {
            selectedElement.forEach(function(selected, pos) {
                 $(selected).removeClass('btnSelected');
            });
        });
    });

    selectedElement.forEach(function(selected, pos) {
    if(selected.textContent.trim() == vTitle)
         $(selected).addClass('btnSelected');
    });
    // 左サイドメニュークリックしたら活性化させる .end
});
