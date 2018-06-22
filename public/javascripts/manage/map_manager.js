
var curPos = 0;
var bCheckUpdate = false;

$( window ).resize(function() {
	/*gInit.setScrollSize("exbViewer")*/
	location.reload();
});


$(function () {

    // 一覧ボタンイベント
    //gInit.backBtnStopEvent();

    // 初期表示
    var beaconMapFrame = document.getElementById("beaconMap-1");
    beaconMapFrame.classList.remove("hidden");

    var vMapElement = document.getElementsByClassName("level")
        for (var i = 0;i< vMapElement.length; ++i){
            vMapElement[i].addEventListener('click', function(event) {
                       //alert(i)
                       var x = (event.offsetX || event.clientX) - (gIconWidth/2);
                       var y = (event.offsetY || event.clientY) - (gIconHeight/2);
                       //console.dir("x :" + x + "\n" +  "y :" + y + "\n")
                       console.dir("x :" + x + "\n" +  "y :" + y + "\n")
                       //alert("x :" + x + "\n" +  "y :" + y + "\n")
            });
    }
    var viewHidden = function() {
        for (var i = 0;i< vMapElement.length; ++i){
            vMapElement[i].classList.add("hidden")
        }
    }


     // 画面サイズチェック
     var selectMapElement = [].slice.call(document.querySelectorAll(".level"));
     selectMapElement.forEach(function(map, pos) {
        var vWidth = map.clientWidth
        var vHeight = map.clientHeight
        console.dir("index" + pos + "vWidth :" + vWidth + "\n" +  "vHeight :" + vHeight + "\n")
     });

    var viewHidden = function() {
        for (var i = 0;i< vMapElement.length; ++i){
            vMapElement[i].classList.add("hidden");
        }
     }
    var vMapElement = document.getElementsByClassName("level")
     var floorFrame = $('#floor-category');
         if(floorFrame!=null){
         // 管理者用selectbox value取得
         $('#floor-category').change(function() {
             viewHidden();
             var result = $('#floor-category option:selected').val();
             vMapElement[result].classList.remove("hidden");
         });
     }


        var fileBtn = [].slice.call(document.querySelectorAll(".map__image--input"));
        fileBtn.forEach(function(fileBtn, pos) {
            fileBtn.addEventListener('change', function() {

                // 画像の変更
                var file = fileBtn.files;
                if(file[0].size > 1024 * 1024){
                    alert("1MBを超えてます。");
                    return;
                }

                var reader = new FileReader();
                reader.onload = function(rst){
                    console.log(this.width);
                    imgData = rst.target.result;
                     $('[name=base64text]').val(rst.target.result);
                     var getImgElement = document.getElementById("imgFrame-" + pos);
                     var vWidth = getImgElement.clientWidth
                     var vHeight = getImgElement.clientHeight
                     //getImgElement.style.width = vWidth + "px"
                     //getImgElement.style.height = vHeight + "px"
                     getImgElement.src = imgData;
                     var getImgElement2 = document.getElementById("imgFrame-" + pos);
                     console.log(getImgElement2.clientWidth);

                     bInputCheck = true;
                     var vTemp = document.getElementById("inputNum-" + pos).value;
                     var vFileElement = document.getElementById("inputNum-" + pos)
                     var vFile = $(vFileElement)[0].files[0]
                     var cloneElement = $(vFileElement).clone()
                     cloneElement[0].id = "input_map_image-" + pos
                     var vTempElement = document.getElementById("mapImage-" + pos)
                    vTempElement.appendChild(cloneElement[0]);
                }
                reader.readAsDataURL(file[0])
                getImgElement = document.getElementById("imgFrame-" + pos);
                 console.log(getImgElement.offsetWidth);

            });
        });



        // db結果がある場合
        gDatabase.resultCheck();

         /* btnイベント .start*/
        // 更新
        //updateBtnEvent();
        /* btnイベント .end*/


        //gTopMenu.setMenuSelect();

        gResize.mapCenterMove();

});
/* 更新処理 */
function updateBtnEvent() {
	var vUpdateBtn = document.getElementById("update-btn");
	vUpdateBtn.addEventListener('click', function() {
	    console.log("更新ボタン");
        bCheckUpdate = true;
        var check = formCheck("update");
        if(check){
            // modal処理
            gModal.confirm(gTitle.update,gMessage.update,"../mapManager/updateMapManager")
        }else{
           bCheckUpdate = false;
        }
	});
}
/* formをチェックする処理 */
function formCheck(type) {
    var vMessage = "default";
    var vResult = false;
    var checkLength = document.getElementsByClassName("layer-beacon").length
    if (type == "update"){
        // 更新処理
        vMessage = "「更新」"
        vResult = true;

    }

    return vResult;
}