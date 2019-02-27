var gMapPos = 1;
$( window ).resize(function() {
	location.reload();
});

$(function () {
    // 初期表示
    var beaconMapFrame = document.getElementById("beaconMap-" + gMapPos);
    beaconMapFrame.classList.remove("hidden");

    // 画面サイズチェック
    var selectMapElement = [].slice.call(document.querySelectorAll(".level"));
        selectMapElement.forEach(function(map, pos) {
        var vWidth = map.clientWidth;
        var vHeight = map.clientHeight;
        console.dir("index" + pos + "vWidth :" + vWidth + "\n" +  "vHeight :" + vHeight + "\n");
    });

    var viewHidden = function() {
        for (var i = 0;i< vMapElement.length; ++i){
            vMapElement[i].classList.add("hidden");
        }
    }
    var vMapElement = document.getElementsByClassName("level");
    var floorFrame = $('#floor-category');
    if(floorFrame!=null){
        // 管理者用selectbox value取得
        $('#floor-category').change(function() {
         viewHidden();
         var result = $('#floor-category option:selected').val();
         gMapPos = result;
         document.getElementById("beaconMap-" + result).classList.remove("hidden");
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
                imgData = rst.target.result;
                 $('[name=base64text]').val(rst.target.result);
                 var getImgElement = document.getElementById("imgFrame-" + pos);
                 getImgElement.src = imgData;
                 bInputCheck = true;
                 var vFileElement = document.getElementById("inputNum-" + pos);
                 var cloneElement = $(vFileElement).clone();
                 cloneElement[0].id = "input_map_image-" + pos;
                 var vTempElement = document.getElementById("mapImage-" + pos);
                vTempElement.appendChild(cloneElement[0]);
            }
            reader.readAsDataURL(file[0]);
            getImgElement = document.getElementById("imgFrame-" + pos);
            console.log(getImgElement.offsetWidth);
        });
    });

    // db結果がある場合
    gDatabase.resultCheck();
    gResize.mapCenterMove();

});