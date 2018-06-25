
var curPos = 0;
var bCheckUpdate = false;

$( window ).resize(function() {
	location.reload();
});


$(function () {

    console.log("workSite.js")
    // 初期表示
    var beaconMapFrame = document.getElementById("beaconMap-1");
    beaconMapFrame.classList.remove("hidden");


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

    gResize.mapCenterMove();

});