var TotalBeaconsData = [];
var VIEWTYPE = false;    // false(アイコンがスムーズに動くバージョン)
var gMapPos = 1;
var gInfoAllFrame = null;
var MARGIN_HEIGHT = 20
$( window ).resize(function() {
	location.reload();
});


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

     // 文字サイズ再調整
     gResize.textResize(vElement,beaconData.pos.size)

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
    var getColor = null
    // 色の設定
    //var getColor = setTagNameAfterColor(beaconData);
    //var getColor = "2vmin solid " + gBeaconPosition.getColorUi(beaconData.depName)
    if(beaconData.totalCount >= VIEW_COUNT){
        getColor = "2vmin solid " + "black"
    }else{
        getColor = "2vmin solid " + beaconData.iconColor
    }

    var typeName = ""
    afterFrame.className = ""
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

    if(beaconData.totalCount >= VIEW_COUNT){
       afterFrame.style.visibility = "hidden"
    }
    $(infoTag).append(afterFrame);
}

function setFrame() {
    gAddPositionMargin(TotalBeaconsData);

    var vClone = $('.infoAllFrame').clone()
    gInfoAllFrame = vClone[0]
    gInfoAllFrame.id = "cloneInfoAllFrame"
    gInfoAllFrame.children[1].children[0].id ="cloneTbodyId"
    var vMainFrame = document.getElementById("map__main--frame")
    vMainFrame.appendChild(gInfoAllFrame)

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
        pinFrame.id = "user_id_" + beaconData.id;

        pinFrame.style.top = beaconData.pos.y + "px";
        pinFrame.style.left = beaconData.pos.x + "px";

        var spanFrame = document.createElement('span');

        var infoTag = document.createElement('div');
        var contentTag = document.createElement('div');
        contentTag.className = "user__tag--content"


        var picText1 = document.createElement('div');
        picText1.className = "user__tag--pic--text--1"
        picText1.textContent = "仮設材種別："+beaconData.typeName;

        var picTextHuri = document.createElement('div');
        picTextHuri.className = "user__tag--pic--text--huri"
        picTextHuri.textContent = "仮設材名称："+beaconData.itemName ;

        var picText2 = document.createElement('div');
        picText2.className = "user__tag--pic--text--2"
        picText2.textContent = "Tag番号："　+ beaconData.btxId;

        var picText3 = document.createElement('div');
        picText3.className = "user__tag--pic--text--3"
        picText3.textContent =  "予約業者："+beaconData.companyName;

         if(beaconData.totalCount >= VIEW_COUNT){
            infoTag.className = "user__tag__all--info"
            pinFrame.style.background = "black";
            pinFrame.style.color = "white";
            infoTag.style.background = "black";
            infoTag.style.color = "white";
            infoTag.id = "infoTagAll-" + beaconData.id;

            var vTbody = document.getElementById("cloneTbodyId")
            var vTr = document.createElement('tr');
            vTr.id = "trPosId-" + beaconData.posId
            vTr.className = "trAll trPosId-" + beaconData.posId
            vTr.style.background = beaconData.iconColor;
            vTr.style.color = beaconData.textColor;
            var vTd1 = document.createElement('td');
            vTd1.className = "td--frame"
            vTd1.textContent = beaconData.typeName
            var vTd2 = document.createElement('td');
            vTd2.className = "td--frame"
            vTd2.textContent = beaconData.itemNo
            var vTd3 = document.createElement('td');
            vTd3.className = "td--frame"
            vTd3.textContent = beaconData.companyName
            var vTd4 = document.createElement('td');
            vTd4.className = "td--frame"
            vTd4.textContent = beaconData.posId

            vTr.appendChild(vTd1);
            vTr.appendChild(vTd2);
            vTr.appendChild(vTd3);
            //vTr.appendChild(vTd4);
            vTbody.appendChild(vTr);
            spanFrame.textContent = "A";
        }else{
             infoTag.id = "infoTag-" + beaconData.id;
             infoTag.className = "user__tag--info";
             //infoTag = gBeaconPosition.setColorUi(infoTag,"iconStyle",beaconData.depName)
             infoTag.style.background = beaconData.iconColor;
             infoTag.style.color = beaconData.textColor;

              // text結合
             contentTag.appendChild(picText1);
             contentTag.appendChild(picTextHuri);
             contentTag.appendChild(picText2);
             contentTag.appendChild(picText3);
             spanFrame.textContent = beaconData.btxId;
        }
        userTag.push(infoTag);
        setTagNamePosition(pinFrame,vFloor,beaconData,infoTag);
        infoTag.appendChild(contentTag);
        pinFrame.style.visibility = "visible";
        pinFrame.appendChild(infoTag);
        pinFrame.appendChild(spanFrame);

        // floorによるpin結合
        //gMapFrame[vMapIndex].appendChild(pinFrame);
        var vMapElement = document.getElementById("beaconMap-" + beaconData.pos.floor);
        if(vMapElement!=null)
            vMapElement.appendChild(pinFrame);

        // 表示・非表示
        if(beaconData.pos.visible == "false"){
            pinFrame.style.visibility = "hidden";
        }
        // アイコンサイズ
        pinFrame.style.width = (beaconData.pos.size) * (MARGIN_BASE) + "px";
        pinFrame.style.height =(beaconData.pos.size) * (MARGIN_BASE)+ "px";

         // 文字サイズ再調整
         gResize.textResize(pinFrame,beaconData.pos.size)

        // myNum = getCookie('pos-num');
        if (beaconData.id == myNum ) {
            if(beaconData.finishPos == "在席"){
            classie.add(pinFrame, 'pin--active');
            // 強調する。
            if(beaconData.depName == "その他（物品）"){
                // classie.add(pinFrame, 'pin--active--tri');
            }
            var infoTag = pinFrame.getElementsByClassName("user__tag--info");
            vInfoTagElement = document.getElementById("infoTag-" +  beaconData.id);
            if(vInfoTagElement.style.visibility == "visible"){
                myNum = -1;
                vInfoTagElement.style.visibility = "hidden";
                setActivePinColor("off",beaconData.depName);
                classie.remove(preClickElement, 'pin--active');

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
                if(userTag.children[2]!=null){
                    userTag.children[2].style.visibility = "hidden";
                }
            });
        }
        vInfoTagElement = document.getElementById("infoTag-" +  TotalBeaconsData[pos].id);
        vInfoAllTagElement = document.getElementById("infoTagAll-" +  TotalBeaconsData[pos].id);
        var vInfoAllFrame = document.getElementById("cloneInfoAllFrame")
        if(vInfoTagElement!=null){
            if(vInfoTagElement.style.visibility == "visible"){
                myNum = -1;
                vInfoTagElement.style.visibility = "hidden";

            }else{
                vInfoTagElement.style.visibility = "visible";
                classie.add(level, 'pin--active');
                myNum = TotalBeaconsData[pos].id;
            }
        }
        if(vInfoAllTagElement!=null){
            var vTrAllElement = [].slice.call(document.querySelectorAll(".trAll"))
            for (var i = 0;i<vTrAllElement.length;++i){
                if(vTrAllElement[i] != null){
                    classie.add(vTrAllElement[i], 'hidden');
                }
            }
            // 該当になるものだけ表示する
            var vTrSelectAllElement = [].slice.call(document.querySelectorAll(".trPosId-"+TotalBeaconsData[pos].posId))
            for (var i = 0;i<vTrSelectAllElement.length;++i){
                if(vTrSelectAllElement[i] != null){
                    classie.remove(vTrSelectAllElement[i], 'hidden');
                }
            }
            //alert("click pos ::"+ TotalBeaconsData[pos].id)
            myNum = -1;
            //classie.remove(document.getElementById("specialstam"),'levels--hidden')
            //$('#inputModal').modal();

            if(vInfoAllTagElement.style.visibility == "visible"){

                myNum = -1;
                vInfoAllTagElement.style.visibility = "hidden";
                vInfoAllFrame.style.visibility = "hidden";

            }else{
                vInfoAllTagElement.style.visibility = "visible";
                classie.remove(vInfoAllFrame, 'hidden');
                vInfoAllFrame.style.visibility = "visible";
                vInfoAllTagElement.appendChild(vInfoAllFrame)
                classie.add(level, 'pin--active');
                myNum = TotalBeaconsData[pos].id;
//                var ua = navigator.userAgent;
//                if (ua.indexOf('iPad') > 0 || ua.indexOf('Android') > 0 || ua.indexOf('iPhone') > 0 || ua.indexOf('iPod') > 0){
//                    vInfoAllFrame.children[0].width = "100%"
//                }else{
//                    vInfoAllFrame.children[0].width = "97%"
//                }
                  var vElementHeight = document.getElementById("cloneInfoAllFrame").offsetHeight
                  var vAllInfoElement = document.getElementById("allInfo")
                  vAllInfoElement.style.height = vElementHeight - MARGIN_HEIGHT + "px"

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
            //d.length = 20;
            TotalBeaconsData = d.map(function(beaconPosition,index) {
                 //alert(beaconPosition.pos_id);
                 //if(index < 6){
                     return {
                        id : beaconPosition.btx_id,
                        overCheck : false,
                        // new .Start
                        totalCount : -1,
                        posId : -1,
                        btxId : beaconPosition.item_btx_id,
                        typeName : beaconPosition.item_type_name,
                        itemName : beaconPosition.item_name,
                        itemNo : beaconPosition.item_no,
                        iconColor : beaconPosition.item_type_icon_color,
                        textColor : beaconPosition.item_type_text_color,
                        companyName : beaconPosition.company_name,
                        // new .End
                        //pos : clonePosNew(1),
                        pos : clonePosNew(beaconPosition.pos_id),
                        finishPos:beaconPosition.cur_pos_name,
                        updateTime : beaconPosition.updatetime,
                        show:"hidden"
                    };
//                 }else{
//                     return {
//                        id : beaconPosition.btx_id,
//                        overCheck : false,
//                        // new .Start
//                        totalCount : -1,
//                        posId : -1,
//                        btxId : beaconPosition.item_btx_id,
//                        typeName : beaconPosition.item_type_name,
//                        itemName : beaconPosition.item_name,
//                        itemNo : beaconPosition.item_no,
//                        iconColor : beaconPosition.item_type_icon_color,
//                        textColor : beaconPosition.item_type_text_color,
//                        companyName : beaconPosition.company_name,
//                        // new .End
//                        // test code .start
//                        //pos : clonePosNew(2),
//                        // pos : clonePosNew(beaconPosition.btx_id),
//                        // test code .end
//                        pos : clonePosNew(beaconPosition.pos_id),
//                        finishPos:"無",
//                        updateTime : beaconPosition.updatetime,
//                        show:"hidden"
//                    };
//                 }
            });


            if(!ChangeCheck){
                workerBtn = [];
                userTag = []
                setFrame();
                // user iconクリックイベント
                personBtnEvent();
                ChangeCheck = false;
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

                            for (var i = 0;i<gDrawer.length;++i){
                                 var vMapIndex = b.pos.floor
                                 if(gDrawer[i].map ==  document.getElementById("beaconMap-" + vMapIndex).id){
                                    gDrawer[i].drawBeacon(b,gDrawer[i].map,equalCheck);
                                 }
                            }

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

        // ある時の削除
        delElement = [].slice.call(document.querySelectorAll(".user__tag--name"))
        for (var i = 0;i<delElement.length;++i){
            if(delElement[i] != null){
                $(delElement[i]).remove();
            }else{
            }
        }
        delTrElement = [].slice.call(document.querySelectorAll(".trAll"))
        for (var i = 0;i<delTrElement.length;++i){
            if(delTrElement[i] != null){
                $(delTrElement[i]).remove();
            }else{
            }
        }

        gInfoAllFrame = null;

        updating = true;
        gInit.spinAnimationStart();
        getJson();
    }
}
var finishUpdate = function() {
    gInit.spinAnimationEnd(updating);
    updating = false;
    workerBtnCheck = true;

}


var viewHidden = function() {
    var vMapElement = document.getElementsByClassName("level")
    for (var i = 0;i< vMapElement.length; ++i){
        vMapElement[i].classList.add("hidden");
    }
 }

$(function () {
    console.log("workPlace.js")

    try {
        VIEW_COUNT = Number($(document.getElementById("viewCount")).attr("data-count"));
    }
    catch(exception){
        VIEW_COUNT = -1; // エラーの場合
    }

    // 初期表示
    var beaconMapFrame = document.getElementById("beaconMap-" + gMapPos);
    beaconMapFrame.classList.remove("hidden");


    var updateBtn = [].slice.call(document.querySelectorAll(".btn__icon--style"))
    updateBtn[0].addEventListener('click', function() {
        startUpdate();
    });

    var floorFrame = $('#floor-category');
     if(floorFrame!=null){
     // 管理者用selectbox value取得
     $('#floor-category').change(function() {
         viewHidden();
         var result = $('#floor-category option:selected').val();
         console.log("floor:" + result)
         gMapPos = result;
         document.getElementById("beaconMap-" + result).classList.remove("hidden");
     });
    }


    // exbViewer取得
    gObjData.setElementBuildingData();

    gResize.viewDataMotoChange();

    // exbViewer再取得
    //gObjData.setElementBuildingData();


	 // mapの中央整列
    gResize.mapCenterMove();

	// 画面サイズチェック
    gResize.viewSizeCheck();


    // form内容の変更
    if(gResizeCheck){
        //gResize.reWriteForm();
    }

    var vMapLevel = document.getElementsByClassName("level")
    for (var i = 0;i< vMapLevel.length; ++i){
        gMapFrame[i] = vMapLevel[i]
        gDrawer[i] =  new Drawer(gMapFrame[i].id)
    }

	var secUpdateUnit
    try {
        secUpdateUnit = Number(document.getElementById("updateSec").textContent　+ "000");
    }
    catch(exception){
        secUpdateUnit = 300000; // 5分
    }

	// 定期更新
	setInterval(function() {
		startUpdate();
	}, secUpdateUnit);

    startUpdate();

});