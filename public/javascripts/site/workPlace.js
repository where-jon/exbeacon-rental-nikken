var TotalBeaconsData = [];
var VIEWTYPE = false;    // false(アイコンがスムーズに動くバージョン)

$( window ).resize(function() {
	location.reload();
});



$(function () {
    var ChangeCheck = false;
    var PreBeaconsData = [];
    var myFloor;
    var updating = false;
    var errorCheck = false;
    var preClickElement = "";
    var workerBtn;
    var userTag;
    var workerBtnCheck = false;
    var vInfoTagElement;
    var myNum;
    var INIT_POS = -5;
    var INIT_FLOOR = 3;
    var gMapFrame = []
    var gDrawer = []
    var Drawer = function(map) {
        this.map = map;
    };
    Drawer.prototype = {
    drawBeacon : function(beaconData,targetMap,eqaulCheck) {
        var vElement = document.getElementById("user_id_" + beaconData.id )
        //var getColor = gBeaconPosition.setTagNameAfterColor(beaconData);
        var getColor = "2vmin solid " + gBeaconPosition.getColorUi(beaconData.depName)
        var vInfoElement = vElement.childNodes[0]
        var vInfoAfterFrame = vInfoElement.childNodes[0]

         // アイコンサイズ
         vElement.style.width = (beaconData.pos.size) * (MARGIN_BASE) + "px";
         vElement.style.height =(beaconData.pos.size) * (MARGIN_BASE)+ "px";

        if(!eqaulCheck){
            var vFloor = "test"
            if(beaconData.pos.floor == 3){
                vFloor = "18F東へ移動"
            }else if (beaconData.pos.floor == 2){
                vFloor = "18F西へ移動"
            }else if (beaconData.pos.floor == 1){
                vFloor = "16Fへ移動"
            }

            if( vElement.children[0].style.visibility == "visible"){
                //alert("クリックid" + beaconData.id  + "//" +beaconData.name + "移動先:" + vFloor +"しました。")
                console.dir("クリックid" + beaconData.id  + "//" +beaconData.name + "移動先:" + vFloor +"しました。");
            }
            var vTargetMap = document.getElementById(targetMap)
            vTargetMap.appendChild(vElement)

            // 表示、非表示
            if(beaconData.finishPos == "未検知"){
                vInfoElement.style.visibility = "hidden"
            }else if (beaconData.finishPos == "不在"){
                vInfoElement.style.visibility = "hidden"
            }
        }
        vElement.style.top = beaconData.pos.y + "px";
        vElement.style.left = beaconData.pos.x + "px";

        // 表示・非表示
        if(beaconData.pos.visible == "false"){
            pinFrame.style.visibility = "hidden";
        }
        // taginfo位置変更
        $(vInfoAfterFrame)[0].style = null;
        $('#afterFrame-' + beaconData.id).removeAttr('style');
        if(beaconData.pos.y >=0 && beaconData.pos.y < (CUR_MAP_HEIGHT/2) && beaconData.pos.x >=0  && beaconData.pos.x <=(CUR_MAP_WIDTH/2)){
            vInfoElement.className = "user__tag--info user__tag--11zi";
            vInfoAfterFrame.className = "user__tag--11zi--after";
            $(vInfoAfterFrame).css('border-bottom', getColor);
        }else if (beaconData.pos.y >=0 && beaconData.pos.y < (CUR_MAP_HEIGHT/2) && beaconData.pos.x >=(CUR_MAP_WIDTH/2)  && beaconData.pos.x <=CUR_MAP_WIDTH){
            vInfoElement.className = "user__tag--info user__tag--1zi";
            vInfoAfterFrame.className = "user__tag--1zi--after";
            $(vInfoAfterFrame).css('border-bottom', getColor);
        }else if (beaconData.pos.y >=(CUR_MAP_HEIGHT/2) && beaconData.pos.y <= CUR_MAP_HEIGHT && beaconData.pos.x >=0  && beaconData.pos.x <=(CUR_MAP_WIDTH/2)){
            vInfoElement.className = "user__tag--info user__tag--7zi";
            vInfoAfterFrame.className = "user__tag--7zi--after";
            $(vInfoAfterFrame).css('border-top', getColor);
            }else if (beaconData.pos.y >=(CUR_MAP_HEIGHT/2) && beaconData.pos.y <= CUR_MAP_HEIGHT && beaconData.pos.x >=(CUR_MAP_WIDTH/2)  && beaconData.pos.x <=CUR_MAP_WIDTH){
            vInfoElement.className = "user__tag--info user__tag--5zi";
            vInfoAfterFrame.className = "user__tag--5zi--after";
            $(vInfoAfterFrame).css('border-top', getColor);
        }

        // 表示、非表示
        if(beaconData.finishPos == "未検知"){
            vElement.style.visibility = "hidden"
        }else if (beaconData.finishPos == "不在"){
            vElement.style.visibility = "hidden"
        }else　if (beaconData.finishPos == "在席"){
            vElement.style.visibility = "visible"
        }else{
            alert("例外")
        }

      }
    }

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
    function setTagNamePosition(pinFrame ,vFloor, beaconData,infoTag) {
        if(VIEWTYPE){
            pinFrame.className = "user__floor--" + vFloor + "" + " user__tag--name";
        }else{
            pinFrame.className = "user__floor--" + vFloor + "" + " user__tag--name animation--frame";
        }

        if(beaconData.finishPos == "在席"){

        }else if (beaconData.finishPos == "未検知"){
            pinFrame.style.visibility = "hidden";
        }else{	// 不在の場合
            pinFrame.style.visibility = "hidden";
        }

        var afterFrame = document.createElement('div');
        // 色の設定
        //var getColor = setTagNameAfterColor(beaconData);
        //var getColor = "2vmin solid " + gBeaconPosition.getColorUi(beaconData.depName)
        var getColor = "2vmin solid " + beaconData.iconColor
        if(beaconData.pos.y >=0 && beaconData.pos.y < (CUR_MAP_HEIGHT/2) && beaconData.pos.x >=0  && beaconData.pos.x <=(CUR_MAP_WIDTH/2)){
            infoTag.className += " user__tag--11zi";
            afterFrame.className = "user__tag--11zi--after";
            $(afterFrame).css('border-bottom', getColor);
        }else if (beaconData.pos.y >=0 && beaconData.pos.y < (CUR_MAP_HEIGHT/2) && beaconData.pos.x >=(CUR_MAP_WIDTH/2)  && beaconData.pos.x <=CUR_MAP_WIDTH){
            infoTag.className += " user__tag--1zi"
            afterFrame.className = "user__tag--1zi--after";
            $(afterFrame).css('border-bottom', getColor);
        }else if (beaconData.pos.y >=(CUR_MAP_HEIGHT/2) && beaconData.pos.y <= CUR_MAP_HEIGHT && beaconData.pos.x >=0  && beaconData.pos.x <=(CUR_MAP_WIDTH/2)){
            infoTag.className += " user__tag--7zi"
            afterFrame.className = "user__tag--7zi--after";
            $(afterFrame).css('border-top', getColor);
        }else if (beaconData.pos.y >=(CUR_MAP_HEIGHT/2) && beaconData.pos.y <= CUR_MAP_HEIGHT && beaconData.pos.x >=(CUR_MAP_WIDTH/2)  && beaconData.pos.x <=CUR_MAP_WIDTH){
            infoTag.className += " user__tag--5zi"
            afterFrame.className = "user__tag--5zi--after";
            $(afterFrame).css('border-top', getColor);
        }

        $(infoTag).append(afterFrame);
    }

    function setFrame() {
        gAddPositionMargin(TotalBeaconsData);

        var wakauFrame;
        var wakauFrameCount = 0;
        TotalBeaconsData.forEach(function(beaconData, i) {

            // ここから描画関数
            // alert(b);
            var vFloor = beaconData.pos.floor;
            var vMapIndex = vFloor - 1;
            var pinFrame = document.createElement('div');

            workerBtn.push(pinFrame);
            pinFrame.style.background = beaconData.iconColor;
            pinFrame.style.color = beaconData.textColor;
            //pinFrame = gBeaconPosition.setColorUi(pinFrame,"iconStyle",beaconData.depName)
            //var gg = gBeaconPosition.getTextColor(beaconData.depName);
            //pinFrame.style.color = "white";
            pinFrame.id = "user_id_" + beaconData.id;

            pinFrame.style.top = beaconData.pos.y + "px";
            pinFrame.style.left = beaconData.pos.x + "px";

            var spanFrame = document.createElement('span');
            //spanFrame = gBeaconPosition.setColorUi(spanFrame,"textStyle",beaconData.depName)

            // 仮 id表示
            //spanFrame.textContent = beaconData.id;
            spanFrame.textContent = beaconData.btxId;

            if(beaconData.depName == "その他（物品）"||beaconData.depName == "企画総務部"){
                // classie.add(spanFrame, 'user__tag--text--tri');
                //spanFrame.style.color = "black";
            }

            var infoTag = document.createElement('div');
            var contentTag = document.createElement('div');
            contentTag.className = "user__tag--content"
            infoTag.className = "user__tag--info";
            //infoTag = gBeaconPosition.setColorUi(infoTag,"iconStyle",beaconData.depName)
            infoTag.style.background = beaconData.iconColor;
            infoTag.style.color = beaconData.textColor;

            infoTag.id = "infoTag-" + beaconData.id;
            userTag.push(infoTag);

            setTagNamePosition(pinFrame,vFloor,beaconData,infoTag);

            var picTag = document.createElement('div');
            picTag.className = "user__tag--pic"

            var imgElement = document.createElement("img");
            imgElement.className = "user__tag--pic--Style";
            imgElement.setAttribute("id", "js_koneta_01-01");
//            if(beaconData.imgPath == ""){
//                imgElement.setAttribute("src", "");
//            }else{
//                imgElement.setAttribute("src", beaconData.imgPath);
//            }

            var picText1 = document.createElement('div');
            picText1.className = "user__tag--pic--text--1"
            picText1.textContent = beaconData.typeName;

            var picTextHuri = document.createElement('div');
            picTextHuri.className = "user__tag--pic--text--huri"
            picTextHuri.textContent = beaconData.itemName ;

            var picText2 = document.createElement('div');
            picText2.className = "user__tag--pic--text--2"
            picText2.textContent = beaconData.itemNo;

            var picText3 = document.createElement('div');
            picText3.className = "user__tag--pic--text--3"
            picText3.textContent =  "業者　"+beaconData.companyName;

            if(beaconData.depName == "その他（物品）"){
                // classie.remove(pinFrame, 'user__tag--name');
                // classie.add(pinFrame, 'user__tag--square');
                // 物品の内線番号を表示しない
                picText3.style.visibility = "hidden";
                // picText3Bg.style.visibility = "hidden";

            }

                // img 結合
            picTag.appendChild(imgElement);
                // text結合
            contentTag.appendChild(picText1);
            contentTag.appendChild(picTextHuri);
            contentTag.appendChild(picText2);
            contentTag.appendChild(picText3);

            infoTag.appendChild(picTag);
            infoTag.appendChild(contentTag);


            pinFrame.appendChild(infoTag);
            pinFrame.appendChild(spanFrame);

            pinFrame.style.visibility = "visible";
            // floorによるpin結合
            gMapFrame[vMapIndex].appendChild(pinFrame);

            // 表示・非表示
            if(beaconData.pos.visible == "false"){
                pinFrame.style.visibility = "hidden";
            }
            // アイコンサイズ
            pinFrame.style.width = (beaconData.pos.size) * (MARGIN_BASE) + "px";
            pinFrame.style.height =(beaconData.pos.size) * (MARGIN_BASE)+ "px";

            // myNum = getCookie('pos-num');
            if (beaconData.id == myNum ) {
                if(beaconData.finishPos == "在席"){
                classie.add(pinFrame, 'pin--active');
                // 強調する。
                if(beaconData.depName == "その他（物品）"){
                    // classie.add(pinFrame, 'pin--active--tri');
                }
                var infoTag = pinFrame.getElementsByClassName("user__tag--info");
                // infoTag[0].removeClass = ("user__tag--info--hidden");
                // beaconData.show = ""

                vInfoTagElement = document.getElementById("infoTag-" +  beaconData.id);
                if(vInfoTagElement.style.visibility == "visible"){
                    myNum = -1;
                    vInfoTagElement.style.visibility = "hidden";
                    setActivePinColor("off",beaconData.depName);
                    classie.remove(preClickElement, 'pin--active');

                    // classie.remove(preClickElement.childNodes[1],
                    // 'user__tag--small--tri');

                    // TotalBeaconData[pos].show = "hidden"
                }else{
                    vInfoTagElement.style.visibility = "visible";
                    // TotalBeaconData[pos].show = "visible"
                }
                //cookies = "pos-num" + '=' + myNum;
                //document.cookie = cookies;

                // if(beaconData.finishPos == "在席"){
                // beaconData.show = "visible"
                // //classie.remove(infoTag[0], 'user__tag--info--hidden');
                // //infoTag[0].style.visibility = "visible";
                // }else{
                // //infoTag[0].style.visibility = "hidden";
                // beaconData.show = "hidden"
                // }
                preClickElement = pinFrame;
                }
            }
        });


//        var kanriFrame = $('#kanri-category');
//        if(kanriFrame!=null){
//        // 管理者用selectbox value取得
//        $('#kanri-category').change(function() {
//            var result = $('#kanri-category option:selected').val();
//                $(location).attr('href', result);
//        });
//        }


    }

    function personBtnEvent() {
        // 現在クリックしてるアイコンとbtxIdで紐づくものだけ表示
        workerBtn.forEach(function(level, pos) {
        // マウスクリック
        level.addEventListener('click', function() {
            if(myNum!= INIT_POS && preClickElement){
                classie.remove(preClickElement, 'pin--active');
                // classie.remove(preClickElement.childNodes[1],
                // 'user__tag--small--tri');
            }

            if(preClickElement.id != level.id){
                // alert("another");
                userTag.forEach(function(userTag, pos) {
                    userTag.style.visibility = "hidden";
                });
            }
            vInfoTagElement = document.getElementById("infoTag-" +  TotalBeaconsData[pos].id);

            if(vInfoTagElement.style.visibility == "visible"){
                myNum = -1;
                vInfoTagElement.style.visibility = "hidden";

            }else{
                vInfoTagElement.style.visibility = "visible";
                classie.add(level, 'pin--active');
                myNum = TotalBeaconsData[pos].id;
                // 強調する。
                if(TotalBeaconsData[pos].depName == "その他（物品）"){
                    // classie.add(level, 'pin--active--tri');
                    // classie.add(level.childNodes[1], 'user__tag--small--tri');
                }
            }

        cookies = "pos-num" + '=' + myNum;
        document.cookie = cookies;
        preClickElement = level;
            });
        });

    }
    function getJson() {
     var addr = "../site/workPlace/getData"
        $.ajax({
            cache:false,
            type : "GET",
            url : addr,
            success : function(d) {
                d.length = 15;
                TotalBeaconsData = d.map(function(beaconPosition) {
                     //alert(beaconPosition.pos_id);
                    return {
                        id : beaconPosition.btx_id,
                        name : beaconPosition.btx_name,
                        // old.start
                        telNum: beaconPosition.btx_tel_num,
                        depName: beaconPosition.department_name,
                        tantou: beaconPosition.btx_tantou,
                        hurigana: beaconPosition.btx_furigana,
                        iconName: beaconPosition.btx_icon_name,
                        imgPath : beaconPosition.btx_icon_image,
                        // old.end

                        // new .Start
                        btxId : beaconPosition.item_btx_id,
                        typeName : beaconPosition.item_type_name,
                        itemName : beaconPosition.item_name,
                        itemNo : beaconPosition.item_no,
                        iconColor : beaconPosition.item_type_icon_color,
                        textColor : beaconPosition.item_type_text_color,
                        companyName : beaconPosition.company_name,
                        // new .End
                        // test code .start
                        pos : clonePosNew(1),
                        // pos : clonePosNew(beaconPosition.btx_id),
                        // test code .end
                        //pos : clonePosNew(beaconPosition.pos_id),
                        finishPos:"無",
                        updateTime : beaconPosition.updatetime,
                        show:"hidden"
                    };
                });


                if(!ChangeCheck){
                    workerBtn = [];
                    userTag = []
                    setFrame();
                    // user iconクリックイベント
                    personBtnEvent();
                    ChangeCheck = true;
                    // データを以前データとして管理
                    PreBeaconsData = TotalBeaconsData;
                    finishUpdate();
                } else{
                    if(VIEWTYPE){
                        // 画面を一回クリアする。
                        gInit.refreshFadeOut();
                    }
                    setTimeout(function() {
                        // 現在位置設定
                        gAddPositionMargin(TotalBeaconsData);
                        //comparePos();
                        TotalBeaconsData.forEach(function(b,pos) {
                            var equalCheck = false;
                            try {
                                if(PreBeaconsData[pos].pos.floor != b.pos.floor){
                                } else{
                                    equalCheck = true;
                                }
                                var vMapIndex = b.pos.floor - 1
                                gDrawer[vMapIndex].drawBeacon(b,gDrawer[vMapIndex].map,equalCheck);
                            }catch(exception){
                                //alert("b.pos.floor:" + b.pos.floor +"になってる")
                            }

                         });
                        if(VIEWTYPE){
                          // 画面を再表示する
                          gInit.refreshFadeIn();
                        }
                        // データを以前データとして管理
                        PreBeaconsData = TotalBeaconsData;
                        finishUpdate();
                    }, 500);
                }

            },
            error : function(e) {
                console.dir(e);
            }
        });

    }

    var startUpdate = function() {
        if(errorCheck){
            // ネットワーク切断およびjson取得失敗によるエラーの場合
            errorCheck = false;
            updating = false;
            gInit.spinAnimationReDraw();
        }
        if (!updating) {
            updating = true;
            gInit.spinAnimationStart();
            getJson();
        }
    }
    var finishUpdate = function() {
        gInit.spinAnimationEnd(updating);
        updating = false;
        workerBtnCheck = true;

        // levels表示
        //classie.remove(mallLevelsEl,'levels--hidden')


    }

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


    // exbViewer取得
    gObjData.setElementBuildingData();

	 // mapの中央整列
    gResize.mapCenterMove();

	// 画面サイズチェック
    gResize.viewSizeCheck();


    var vMapLevel = document.getElementsByClassName("level")
    for (var i = 0;i< vMapLevel.length; ++i){
        gMapFrame[i] = vMapLevel[i]
        gDrawer[i] =  new Drawer(gMapFrame[i].id)
    }
    startUpdate();

});