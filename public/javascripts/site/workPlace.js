var TotalBeaconsData = [];
var curPos = 0;
var bCheckUpdate = false;

$( window ).resize(function() {
	location.reload();
});


var clonePosNew = function(pos_id) {
    var result;
    gBeaconPosition.getPosition().forEach(function(p) {
        if (p.id == -1 || p.id == pos_id) {
            result = {
                posId : p.id,
                floor : p.floor,
                margin : p.margin,
                viewType : p.viewType,
                visible : p.visible,
                y : p.y,
                x : p.x
            };
        }
    });
    return result;
}

$(function () {

    console.log("workPlace.js")
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


    var addr = "../site/workPlace/getData"
    $.ajax({
        cache:false,
        type : "GET",
        url : addr,
        success : function(d) {
            //d.length = 15;
            TotalBeaconsData = d.map(function(beaconPosition) {
                 //alert(beaconPosition.pos_id);
                return {
                    id : beaconPosition.btx_id,
                    //pos : clonePosNew(beaconPosition.pos_id)
                };
            });
        },
        error : function(e) {
            console.dir(e);
        }
    });

});