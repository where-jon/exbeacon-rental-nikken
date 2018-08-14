
var gPosX
var gPosY
var gClickPos = null;
var gMovePos = -1;
var gMapPos = 1;
var preClickPos = -1;

var bCheckUpdate = false;
var VIEW_COUNT = 1000

var workerBtn;

$( window ).resize(function() {
	location.reload();
});


var newExbViewerData;
var vExbViewerData;


$(function () {

     var initManager = function() {
         var viewHidden = function() {
            for (var i = 0;i< vMapElement.length; ++i){
                vMapElement[i].classList.add("hidden");
            }
         }

         //
        var reloadManager = function() {
            // 画面クリア
            var delElement = [].slice.call(document.querySelectorAll(".exb__viewer--frame"));
             $(delElement).remove();
            // 画面再描画
            gViewerManage.reDrawExbeacon(vExbViewerData);
            initClickFrame();
        }

        var vMapTestElemet = document.getElementById("map__main--frame")
        vMapTestElemet.addEventListener('click', function(event) {
            console.log("levelclick")
            console.log("clientX:" +  (event.clientX))
            console.log("offsetY:" +  (event.offsetY))
        });
        var vMapElement = document.getElementsByClassName("level")
            for (var i = 0;i< vMapElement.length; ++i){
                vMapElement[i].addEventListener('click', function(event) {
                        // 設定pullDown位置を表示
                       var getDisplayOrder =  document.getElementsByClassName("item-" + gClickPos)[0].textContent
                        $('#input-floor-category-' + gClickPos ).val(getDisplayOrder);
                       if(preClickPos == gClickPos){
                            //alert("同じところ")
                            var vTempElement = document.getElementById("viewer-list-" + gClickPos)
                            vTempElement.classList.add("hidden");
                            gClickPos = null;
                            gViewer.pinRemove();

                       }else if (gClickPos != null){
                            var vIconSize = Number(document.getElementById("input_viewer_pos_size-" + gClickPos).value)
                               gPosX = (event.clientX) - (vIconSize/2);
                               gPosY = (event.offsetY) - (vIconSize/2);

                               //console.dir("x :" + gPosX + "\n" +  "y :" + gPosY + "\n")
                               if( gPosX > 10 && gPosY > 10 && gClickPos != -1 && !vInputCheck){
                                    var vGetElementX = document.getElementById("input_viewer_pos_x-" + gClickPos);
                                    var vGetElementY = document.getElementById("input_viewer_pos_y-" + gClickPos);
                                    vGetElementX.value = gPosX + "";
                                    vGetElementY.value = gPosY + "";

                                    vExbViewerData.forEach(function(exbData, pos) {
                                        if(exbData.id == gClickPos){
                                            var vClickElement = document.getElementById("exb_id_" + gClickPos)
                                            vClickElement.style.left = gPosX + "px"
                                            vClickElement.style.top = gPosY + "px"
                                            vClickElement.classList.remove("exb__pin--active");
                                            vClickElement.classList.add("exb__pin--move");

                                            exbData.x = gPosX
                                            exbData.y = gPosY

                                           // 画面クリア
                                            var delElement = [].slice.call(document.querySelectorAll(".exb__viewer--frame"));
                                            $(delElement).remove();
                                            // 画面再描画
                                            gViewerManage.reDrawExbeacon(vExbViewerData);
                                        }
                                    });
                                  initClickFrame();
                               }
                       }

                });
        }

        var floorFrame = $('#floor-category');
            if(floorFrame!=null){
            // 管理者用selectbox value取得
            $('#floor-category').change(function() {
                viewHidden();
                var result = $('#floor-category option:selected').val();
                console.log("floor:" + result)
                gMapPos = result;
                //vMapElement[result].classList.remove("hidden");
                document.getElementById("beaconMap-" + result).classList.remove("hidden");
            });
        }

         /* input formのselectBoxクリックイベント.start*/
            // posNum
           var vInputNum = [].slice.call(document.querySelectorAll(".inputNum"));
           vInputNum.forEach(function(vNum, pos) {
             vNum.addEventListener('change', function() {
                  var result = $(vNum).val();
                  console.dir("result::" + result);
                  gClickPos = Number(result);
                    gViewer.inputViewerRemove();
                    gViewer.pinRemove();
                    gViewer.inputSelectPosNum();
                    //vViewerListElement[gClickPos].classList.remove("hidden");
                    //document.getElementById("viewer-list-" + gClickPos ).classList.remove("hidden");

                      // exbの処理選択されたもの
                    var pinElement = document.getElementById("exb_id_" + gClickPos)
                    pinElement.classList.add("exb__pin--active");

                    // input変更イベントをoff
                    var vTitle = document.getElementsByClassName("manager__data--frame")[0]
                    vTitle.classList.remove("hidden");
                    gPosX = -1;
                    gPosY = -1;

                    viewHidden();
                    var vFloor = document.getElementById("input_viewer_pos_floor-" + gClickPos ).value
                    gMapPos = Number(vFloor)
                    document.getElementById("beaconMap-" + vFloor).classList.remove("hidden");

             });
           });

         // visible
          var vInputVisible = [].slice.call(document.querySelectorAll(".inputVisible"));
          vInputVisible.forEach(function(vVisible, pos) {
            vVisible.addEventListener('change', function() {
                 var result = $(vVisible).val();
                 console.dir("result::" + result);
                 var vElement = document.getElementById("input_viewer_visible-" + gClickPos)
                 vElement.value = result;
                 vExbViewerData[pos].visible = result;
                 reloadManager();

            });
          });
        // floor
          var vInputFloor = [].slice.call(document.querySelectorAll(".inputFloor"));
          vInputFloor.forEach(function(vFloor, pos) {
            vFloor.addEventListener('change', function() {
                var result = $(vFloor).val();
                gMapPos = result;
                console.dir("result::" + result);
                viewHidden();
                document.getElementById("beaconMap-" + result).classList.remove("hidden");

                var vElement = document.getElementById("input_viewer_pos_floor-" + gClickPos)
                vElement.value =  $('.dataFloorId-' + result).attr('data-floorId');
                vExbViewerData[pos].displayOrder = result;
                document.getElementsByClassName("item-" + gClickPos)[0].textContent = result
                reloadManager();

            });

          });
        // posTyppe
          var vInputPosType = [].slice.call(document.querySelectorAll(".inputPosType"));
          vInputPosType.forEach(function(vPosType, pos) {
            vPosType.addEventListener('change', function() {
                 var result = $(vPosType).val();
                 console.dir("result::" + result);
                 var vElement = document.getElementById("input_viewer_pos_type-" + gClickPos)
                 vElement.value = result;
                 vExbViewerData[pos].viewType = result;
                 reloadManager();
            });
          });

          // posSize
        var vInputPosSize = [].slice.call(document.querySelectorAll(".inputPosSize"));
            vInputPosSize.forEach(function(vPosSize, pos) {
            vPosSize.addEventListener('change', function() {
               var result = $(vPosSize).val();
               console.dir("result::" + result);
               var vElement = document.getElementById("input_viewer_pos_size-" + gClickPos)
               vElement.value = result;
               vExbViewerData[pos].size = result;

               reloadManager();
            });
        });


         /* input formのselectイベント.end*/
         // 現在位置表示
         if(gClickPos > -1){
             var vElement = document.getElementById("exb_id_"+ gClickPos)
             //vElement.classList.add("exb__pin--active");
         }

    }

    var gViewer = {
        pinActive : function() {
            var selectedElement = document.getElementById("exb_id_" + gClickPos);

            if( selectedElement != null &&　!vInputCheck){
                selectedElement.classList.add("exb__pin--active");
            }
        },

        pinRemove : function() {
            var exbElement = [].slice.call(document.querySelectorAll(".exb__viewer--frame"));
             for (var i = 0;i< exbElement.length; ++i){
                exbElement[i].classList.remove("exb__pin--active");
            }
        },
        inputViewerRemove : function() {
            var vViewerListElement = document.getElementsByClassName("pos-search");
             for (var i = 0;i< vViewerListElement.length; ++i){
                     vViewerListElement[i].classList.add("hidden");
             }

              var vTempElement = document.getElementById("viewer-list-" + gClickPos)
             vTempElement.classList.remove("hidden");
        },

        inputSelectPosNum : function() {
            var selectPosNum = document.getElementById("input-num-category-" + gClickPos);
             for (var i = 0;i< selectPosNum.length; ++i){
                    if(Number(selectPosNum[i].value) == gClickPos ){
                         selectPosNum[i].selected = true;
                    }else{
                         selectPosNum[i].selected = false;
                    }

             }
        },
    }
      /* exbのクリックイベント */
    var initClickFrame = function() {
        var exbElement = [].slice.call(document.querySelectorAll(".exb__viewer--frame"));
        gViewer.pinActive();
        exbElement.forEach(function(exb, pos) {
            exb.addEventListener('click', function() {
                    preClickPos = gClickPos;
                    gClickPos = Number(exb.textContent);
                    gMovePos = pos;
                    gViewer.inputViewerRemove();
                    gViewer.pinRemove();
                    // exbの処理選択されたもの
                    exb.classList.add("exb__pin--active");

                    // input変更イベントをoff
                    vInputCheck = false;

                    var vTitle = document.getElementsByClassName("manager__data--frame")[0]
                    vTitle.classList.remove("hidden");
                    gPosX = -1;
                    gPosY = -1;

                    // ataiを設定する
                    console.log("ataiを設定する")

            });
         });
     }
    // 初期表示
    var beaconMapFrame = document.getElementById("beaconMap-" + gMapPos);
    beaconMapFrame.classList.remove("hidden");
    // mapの中央整列
    gResize.mapCenterMove();


    // db結果がある場合
    gDatabase.resultCheck();

    gTopMenu.setMenuSelect();
    /*gInit.setScrollSize("exbViewer")　*/

    // 部署マスター取得
	gObjData.setElementDepData();

	// exbViewer データ結合
    gObjData.setElementExbViewerData();


    // 画面サイズチェック
    gResize.viewSizeCheck();

    // form内容の変更
    if(gResizeCheck){
        gResize.reWriteForm();
    }

    // 画面に描画
    gViewerManage.drawExbeacon();

    // 新配列にデータコピー
    newExbViewerData = gExbViewerUiData;
    vExbViewerData = gExbViewerData;

    // 初期に必要なところを呼び出し
    initManager();


    // exb__viewer--frameのクリックイベント結合
    initClickFrame();

    /* btnイベント .start*/
     // 更新
    updateBtnEvent();


    // 復元
    var vResetBtn = document.getElementById("reset-btn");
    vResetBtn.addEventListener('click', function() {
//        // 画面再更新
//        var delElement = [].slice.call(document.querySelectorAll(".exb__viewer--frame"));
//         $(delElement).remove();
//        // 画面再描画
//        gViewerManage.drawExbeacon();
//
//        initManager();

        // 画面再更新
        location.reload();
    });

    // input変更の時
    var vInputCheck = false;
    $("input").change(function(event) {
        console.log("値変更がありました");
        vInputCheck = true;
        event.currentTarget.value = $(event.currentTarget).val()
        event.currentTarget.textContent = $(event.currentTarget).val()

        // 変更されたらまたデータ最新に更新する
        for(var i = 0; i < vExbViewerData.length ; ++i){
            if(vExbViewerData[i].id == gClickPos){
                // id
                var id = vExbViewerData[i].id
                vExbViewerData[i].id = Number(id)

                //var result = $('#input-visible-category-' + gClickPos).val();
                // visble
                var vElement = document.getElementById("input_viewer_visible-" + gClickPos)
                vExbViewerData[i].visible = vElement.value;

                // floor
                var viewer_pos_floor = document.getElementById("input_viewer_pos_floor-" + gClickPos).value;
                vExbViewerData[i].floor = Number(viewer_pos_floor);

                // size
                var viewer_pos_size = document.getElementById("input_viewer_pos_size-" + gClickPos).value;
                vExbViewerData[i].size = Number(viewer_pos_size);

                // pos_type
                  var viewType = document.getElementById("input_viewer_pos_type-" + gClickPos).value;
                vExbViewerData[i].viewType = viewType;

                // x
                var viewer_pos_x = document.getElementById("input_viewer_pos_x-" + gClickPos).value;
                vExbViewerData[i].x = Number(viewer_pos_x);
                // y
                var viewer_pos_y = document.getElementById("input_viewer_pos_y-" + gClickPos).value;
                vExbViewerData[i].y = Number(viewer_pos_y);

                // margin
                var viewer_pos_margin = document.getElementById("input_viewer_pos_margin-" + gClickPos).value;
                vExbViewerData[i].margin = Number(viewer_pos_margin);
                // pos_num
                var viewer_pos_num = document.getElementById("input_viewer_pos_num-" + gClickPos).value;
                vExbViewerData[i].posNum = Number(viewer_pos_num);
                // count
                var viewer_pos_count = document.getElementById("input_viewer_pos_count-" + gClickPos).value;
                vExbViewerData[i].posCount = Number(viewer_pos_count);
//
//                // floor
//                var viewer_pos_display_order = document.getElementById("viewer_pos_display_order-" + gClickPos).value;
//                vExbViewerData[i].displayOrder = Number(viewer_pos_display_order);


            }
        }
        // 画面クリア
        var delElement = [].slice.call(document.querySelectorAll(".exb__viewer--frame"));
         $(delElement).remove();
        // 画面再描画
        gViewerManage.reDrawExbeacon(vExbViewerData);

        //initManager();
        initClickFrame();

        //vInputCheck = false;
    });
    /* btnイベント .end*/

});

/* 更新処理 */
function updateBtnEvent() {
	var vUpdateBtn = document.getElementById("update-btn");
	vUpdateBtn.addEventListener('click', function() {
	    console.log("更新ボタン");
        bCheckUpdate = true;

        var check = formCheck("update");
        if(check){
            // 基準サイズかをチェックして基準の値に変換する。
            gResize.viewDataMotoChange();
            // modal処理
            gModal.confirm(gTitle.update,gMessage.update,"../manage/updateExbViewer")
        }else{
           bCheckUpdate = false;
        }
	});
}
/* formをチェックする処理 */
function formCheck(type) {
    var vMessage = "default";
    var vResult = false;
    var checkLength = document.getElementsByClassName("pos-search").length
    if (type == "update"){
        // 更新処理
        vMessage = "「更新」"
        //vUrl ="../exbViewer/updateExbViewer"
        vResult = true;

    }

    return vResult;
}