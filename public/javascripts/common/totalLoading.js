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
    /*var vElement = $(".mainSpace").find('.form-group')
    if(vElement[0] === undefined){
    }else{
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
    }*/
    // 左サイドメニュークリックしたら活性化させる .end

    //  logoutボタンイベント.start
    var vLogOutElement = document.getElementsByClassName("btn btn-signout")
    var btnOut = vLogOutElement[0]
    if(btnOut === undefined){
    }else{
        btnOut.addEventListener('click', function(event) {
          document.cookie.split(';').forEach(function(c) {
            document.cookie = c.trim().split('=')[0] + '=;' + 'expires=Thu, 01 Jan 1970 00:00:00 UTC;';
          });
        });
    }
    //  logoutボタンイベント.end



    loadingBtnElement = [].slice.call(document.querySelectorAll(".loadingBtn"))
    loadingBtnElement.forEach(function(selected, pos) {
        selected.addEventListener('click', function() {
            $('#load')[0].style.display = ""
        });
    });

});
