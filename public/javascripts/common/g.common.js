// 不在になる時間（分）
var COMP_INTERVAL　= 20;
var allValue;
//var gArDepName = ["組織長","企画総務部","営業部","法人営業部","その他（サテライト）","その他（物品）"];
var gArPowerLevel = ["良好","減少","交換","-"];
var gArhankaku = ["ｼﾞ","ｶﾞ","ｻﾞ","ﾊﾞ","ﾊﾟ","ﾂﾞ","ｹﾞ","ｸﾞ","ｺﾞ","ｽﾞ","ｾﾞ","ｿﾞ","ｷﾞ","ﾀﾞ",
					  "ﾁﾞ","ﾃﾞ","ﾄﾞ","ﾋﾞ","ﾋﾟ","ﾌﾞ","ﾌﾟ","ﾋﾟ","ﾍﾞ","ﾍﾟ","ﾎﾞ","ﾎﾟ","ﾌﾞ"];
var gColorList = ["listType1","listType2","listType3","listType4","listType5","listType6"];

var gDepData;
var gExbData;
var gExbViewerData;
var gExbViewerUiData;
var gMapViewerData;

var INSERT_MAX = 10;

var TAG_MARGIN_TOP = 40
var TAG_MARGIN_BOTTOM = -132

var TEXT_SIZE_BASE = 10;
var TEXT_LINE_BASE = 2;

var MAP_WIDTH = 1366;
var MAP_HEIGHT = 574;

var CUR_MAP_WIDTH = 1920;
var CUR_MAP_HEIGHT = 1080;

var gIconWidth = 35;
var gIconHeight = 35;
var PIN_MARGIN_X = 8;
var PIN_MARGIN_Y = 7;
var MARGIN_BASE = 1;
var BASE_ASPECT = 2.67;

var VIEW_COUNT = -1

var gResizeCheck = false;

// ボタン有り（基本）
var BTN_HEIGHT_RAITO = 0.70;
var BTN_WIDTH_RAITO = 0.99;
var BTN_ROWS = 2;
// 画面下部にボタン無し
var NOBTN_HEIGHT_RAITO = 0.85;
var NOBTN_WIDTH_RAITO = 0.99;
var NOBTN_ROWS = 2;
// 画面下部にボタン無し（その他）
var NOBTN_OTHER_HEIGHT_RAITO = 0.85;
var NOBTN_OTHER_WIDTH_RAITO = 0.99;
var NOBTN_OTHER_ROWS = 3;

var gResize = {
    /* 文字サイズを再調整するロジック*/
    textResize : function(vElement,motoSize) {
        var vSize = (motoSize) * (MARGIN_BASE)
        $(vElement).css('font-size', vSize - TEXT_SIZE_BASE +"px");
        $(vElement).css('line-height', vSize - TEXT_LINE_BASE +"px");

    },

    mapCenterMove : function() {
        var vMainFrame= document.getElementById("map__main--frame")
        var vAspectHeight =  Math.round((vMainFrame.clientWidth/BASE_ASPECT)*1)/1
        var mapElement = document.getElementsByClassName("img__map__manager--style")
        for (var i = 0;i< mapElement.length; ++i){
            mapElement[i].style.height = vAspectHeight + "px"
        }

    },
    viewSizeCheck : function() {
          var vCheckMapElement = document.getElementById("beaconMap-1")
          if(vCheckMapElement!=null){
            var vWidth = vCheckMapElement.clientWidth;
            var vHeight = vCheckMapElement.clientHeight;
            if(MAP_WIDTH == vWidth && MAP_HEIGHT == vHeight){
                CUR_MAP_WIDTH = vWidth;
                CUR_MAP_HEIGHT= vHeight;
            }else{
                gResizeCheck = true;

                CUR_MAP_WIDTH = vWidth;
                CUR_MAP_HEIGHT= vHeight;

                // Marginサイズ
                PIN_MARGIN_X = gResize.getValueX(vWidth, PIN_MARGIN_X)
                PIN_MARGIN_Y = gResize.getValueY(vHeight, PIN_MARGIN_Y)

                // ピンサイズ
                MARGIN_BASE = gResize.getValueX(vWidth, MARGIN_BASE)

             for (var i = 0;i< gExbViewerData.length; ++i){
                // 座標位置
                gExbViewerData[i].x = gResize.getValueX(vWidth, gExbViewerData[i].x)
                gExbViewerData[i].y = gResize.getValueY(vHeight, gExbViewerData[i].y)
                //gExbViewerData[i].margin =  Math.floor(gExbViewerData[i].margin * (gResize.getValueX(vWidth, MARGIN_BASE))*10)/10
             }
            }
          }


    },
     viewDataMotoChange : function() {
          var vCheckMapElement = document.getElementById("beaconMap-" + gMapPos)
          var vWidth = vCheckMapElement.clientWidth;
          var vHeight = vCheckMapElement.clientHeight;
          if(MAP_WIDTH == vWidth && MAP_HEIGHT == vHeight){
             console.dir("基準サイズ")
             console.dir("vWidth:" + vWidth+ " vHieht:" + vHeight )
          }else{
            gResizeCheck = true;
            console.dir("変わったサイズ")
            console.dir("vWidth:" +vWidth+ " vHieht:" + vHeight )

             for (var i = 0;i< gExbViewerData.length; ++i){
                // 座標位置
                var vXelement = document.getElementById("input_viewer_pos_x-" + gExbViewerData[i].id)
                var vYelement = document.getElementById("input_viewer_pos_y-" + gExbViewerData[i].id)
                if(vXelement!=null && vYelement!=null){
                    var vXvalue = Number(vXelement.value);
                    var vYvalue = Number(vYelement.value);
                    vXelement.value = gResize.getMotoValueX(vWidth, vXvalue)
                    vYelement.value = gResize.getMotoValueY(vHeight, vYvalue)
                }
             }
          }

    },

    // 元に変更するX座標
    getMotoValueX : function(targetWidth,currentX) {
        var tempX = (currentX * MAP_WIDTH) / targetWidth;
        var resultX = Math.round(tempX*10)/10
        return resultX;
     },
        // 元に変更するY座標
       getMotoValueY : function(targetHeight,currentY) {
           var tempY = (currentY * MAP_HEIGHT) / targetHeight;
           var resultY = Math.round(tempY*10)/10
           return resultY;
       },

     // 比率に変更するX座標
     getValueX : function(targetWidth,currentX) {
        var tempX = (currentX * targetWidth) / MAP_WIDTH;
        var resultX = Math.round(tempX*10)/10
        return resultX;
     },

       // 比率に変更するY座標
     getValueY : function(targetHeight,currentY) {
         var tempY = (currentY * targetHeight) / MAP_HEIGHT;
         var resultY = Math.round(tempY*10)/10
         return resultY;
     },
      //reWriteForm
       reWriteForm : function() {
         for (var i = 0;i< gExbViewerData.length; ++i){
            // x
            document.getElementById("input_viewer_pos_x-" + gExbViewerData[i].id).value = gExbViewerData[i].x ;
            // y
            document.getElementById("input_viewer_pos_y-" + gExbViewerData[i].id).value = gExbViewerData[i].y ;
            // margin
            document.getElementById("input_viewer_pos_margin-" + gExbViewerData[i].id).value =  gExbViewerData[i].margin;
         }
       },
}


/*画面初期表示関連*/
var gInitView = {
    removeTable : function() {
        var clonedTable = $('.bodyTableDiv').find('table').clone();
        $(clonedTable).attr('style', '');
        $('.baseDiv').remove();
        $('.table-responsive-body').append(clonedTable.prop("outerHTML"));

    },
    tableResizeOther : function(vType) {
            // リサイズ対応
        var timer = false;
        $(window).resize(function() {
            if (timer !== false) {
                clearTimeout(timer);
            }
            timer = setTimeout(function() {
                // 処理の再実行
                gInitView.removeTable();
                gInitView.fixTable("NoBtnOther");
                gInitView.bindMouseAndTouch();
            }, 200);
        });

    },
    tableResize : function(vType) {
        // リサイズ対応
        var timer = false;
        $(window).resize(function() {
            if (timer !== false) {
                clearTimeout(timer);
            }
            timer = setTimeout(function() {
                // 処理の再実行
                gInitView.removeTable();
                if(vType == "NoBtn"){
                    gInitView.fixTable("NoBtn");
                } else{
                    gInitView.fixTable();
                }
                gInitView.bindMouseAndTouch();
            }, 200);
        });

    },

    // テーブルの固定
    // screenPattern：テーブル固定パターン
    // 　パターン１（画面下部にボタン有り）："Btn"、未設定、該当なしの場合
    // 　パターン２（画面下部にボタン無し）："NoBtn"
    // 　パターン３（画面下部にボタン無しでパターン２以外）："NoBtnOther"
    fixTable : function(screenPattern) {
        // 初期値はパターン１の画面下部にボタン有りのデフォルト値を設定
        var heightRaito = BTN_HEIGHT_RAITO;
        var widthRaito = BTN_WIDTH_RAITO;
        var fRows = BTN_ROWS;

        if(screenPattern !== undefined){
            if(screenPattern == "Btn"){
                // パターン１　初期値と同じ
            }else if(screenPattern == "NoBtn"){
                // パターン２
                heightRaito = NOBTN_HEIGHT_RAITO;
            }else if(screenPattern == "NoBtnOther"){
                // パターン３
                heightRaito = NOBTN_OTHER_HEIGHT_RAITO;
                fRows = NOBTN_OTHER_ROWS;
            }
        }

        // テーブルの固定
        var h = $(window).height()*heightRaito;
        // テーブルの調整
        var ua = navigator.userAgent;
        if (ua.indexOf('iPad') > 0 || ua.indexOf('Android') > 0 || ua.indexOf('iPhone') > 0 || ua.indexOf('iPod') > 0){
            // タッチデバイス
            if ($('.mainSpace').height() > h) {
                var w = $('.mainSpace').width()*widthRaito;
                $('.itemTable').tablefix({width:w, height: h, fixRows: fRows});
            } else {
                var w = $('.mainSpace').width();
                $('.itemTable').tablefix({width:w, fixRows: fRows});
            }
        }else{
            // PCブラウザ
            var w = $('.mainSpace').width();
            $('.itemTable').tablefix({height: h, fixRows: fRows});
            $('.rowTableDiv').width(w);
        }
        $('.bodyTableDiv').find('.itemTable').css('margin-bottom','0');
        $('.colTableDiv').css("width","");

    },

    bindMouseAndTouch : function() {
        var ua = navigator.userAgent;
        if (ua.indexOf('iPad') > 0 || ua.indexOf('Android') > 0 || ua.indexOf('iPhone') > 0 || ua.indexOf('iPod') > 0){
            // タッチデバイスの場合
            $(".rowHover").bind({
                'touchstart': function(e) {
                    if(e.originalEvent.touches.length > 1){
                    }else if(e.originalEvent.touches.length == 1){
                        $(".rowHoverSelectedColor").removeClass('rowHoverSelectedColor');
                        $(this).addClass('rowHoverSelectedColor');
                    }
                },
            });
        }else{
            // PCブラウザの場合
            $(".rowHover").bind({
                'mouseover': function(e) {
                    $(this).addClass('rowHoverColor');
                },
                'mouseout': function(e) {
                    $(this).removeClass('rowHoverColor');
                },
                'click': function(e) {
                    $(".rowHoverSelectedColor").removeClass('rowHoverSelectedColor');
                    $(this).addClass('rowHoverSelectedColor');
                },
            });
        }

    },

    removeTable : function() {
        var clonedTable = $('.bodyTableDiv').find('table').clone();
        $(clonedTable).attr('style', '');
        $('.baseDiv').remove();
        $('.table-responsive-body').append(clonedTable.prop("outerHTML"));
    }
}
var gDatePicker = {
    onChageClick : false,
    startDate : null,
    startTimeEpoch : null,
    startSqlTime : null,
    endDate : null,
    endSqlTime : null,
    endTimeEpoch : null,
    monthClickEvent : function() {
            var selectPinElement = [].slice.call(document.querySelectorAll(".inputClass"));
            selectPinElement.forEach(function(pin, pos) {
                $(pin).datetimepicker({
                     format : 'YYYY/MM',
                     locale: 'ja',
                 });
                gDatePicker.dateChangeEvent(pin)
                gDatePicker.dayDateTimeSetting();
            });
        },

    dayClickEvent : function() {
        var selectPinElement = [].slice.call(document.querySelectorAll(".inputClass"));
        selectPinElement.forEach(function(pin, pos) {
            $(pin).datetimepicker({
                 format : 'YYYY/MM/DD',
                 locale: 'ja',
             });
            gDatePicker.dateChangeEvent(pin)
            gDatePicker.dayDateTimeSetting();
        });
    },

     todayClickEvent : function() {
            var selectPinElement = [].slice.call(document.querySelectorAll(".inputClass"));
            selectPinElement.forEach(function(pin, pos) {
                $(pin).datetimepicker({
                     format : 'YYYY/MM/DD',
                     locale: 'ja',
                     maxDate: "now"
                 });
                gDatePicker.dateChangeEvent(pin)
                gDatePicker.dayDateTimeSetting();
            });
        },

    dayClickEvent2 : function() {
            var selectPinElement = [].slice.call(document.querySelectorAll(".inputClass"));
            selectPinElement.forEach(function(pin, pos) {
                $(pin).datetimepicker({
                     format : 'YYYY/MM/DD',
                     locale: 'ja',
                 });
                gDatePicker.dateChangeEvent2(pin)
                gDatePicker.dayDateTimeSetting2();
            });
        },

    clickEvent : function() {
        var selectPinElement = [].slice.call(document.querySelectorAll(".inputClass"));
        selectPinElement.forEach(function(pin, pos) {
            $(pin).datetimepicker({
                 format : 'YYYY/MM/DD HH:mm',
                 sideBySide: true,
                 locale: 'ja',
             });
            gDatePicker.dateChangeEvent(pin)
            gDatePicker.dateTimeSetting();
        });
    },
    dateChangeEvent : function(pickerElement) {
         $(pickerElement).on('dp.change', function(e){
            var date = pickerElement.value;
            gDatePicker.dayDateTimeSetting();
        });
    },
    dateChangeEvent2 : function(pickerElement) {
             $(pickerElement).on('dp.change', function(e){
                var date = pickerElement.value;
                gDatePicker.onChageClick = true;
                gDatePicker.dayDateTimeSetting2();
            });
        },
    dayDateTimeSetting2 : function() {
        gDatePicker.startDate = document.getElementById("inputDate").value
        gDatePicker.endDate = document.getElementById("inputDate2").value
        var startDate = Date.parse(gDatePicker.startDate);
        gDatePicker.startTimeEpoch = startDate/1000;
        gDatePicker.startSqlTime = gDatePicker.startDate.replace('/', '-').replace('/', '-')

        var endDate = Date.parse(gDatePicker.endDate);
        gDatePicker.endTimeEpoch = endDate/1000;
        gDatePicker.endSqlTime = gDatePicker.endDate.replace('/', '-').replace('/', '-')
    },

    dayDateTimeSetting : function() {
        gDatePicker.startDate = document.getElementById("inputDate").value
        var startDate = Date.parse(gDatePicker.startDate);
        gDatePicker.startTimeEpoch = startDate/1000;
        gDatePicker.startSqlTime = gDatePicker.startDate.replace('/', '-').replace('/', '-')
    },

    htmlDayClickEvent : function(vFunctionName) {
        $(document.getElementsByTagName("html")).click(function() {
            //if(gDatePicker.onChageClick){
            console.log("htmlDayClickEvent")
                //gDatePicker.onChageClick = false;
            //}
        });
    },

    htmlClickEvent : function(vFunctionName) {
        $(document.getElementsByTagName("html")).click(function() {
            if(gDatePicker.onChageClick){
                gDatePicker.dayDateTimeSetting2();
                 if (gDatePicker.endTimeEpoch < gDatePicker.startTimeEpoch){
                     //alert('スタート期間再確認');
                     //gModal.alert(gTitle.updateException,gMessage.replaceMessage("スタート期間再確認","「追加行」"))
                 }else{
                    if(vFunctionName!=null){
                        vFunctionName();
                    }else{
                        //console.log("define!! searchMoveData function")
                    }
                 }
                gDatePicker.onChageClick = false;
            }

        });
    },

    clickEventOnlyDate : function(pickerElement) {
            $(pickerElement).datetimepicker({
                 format : 'YYYY/MM/DD',
                 sideBySide: false,
                 locale: 'ja',
             });
    },
}

var gDefaultColor = {
	getColor : function() {
		return [ 
			{
				viewType : "colorType1",
				rgbColor : "rgb(0, 0, 0)",
				rgbR : 0,
				rgbG : 0,
				rgbB : 0,
				hueTop : "25px",
				cursorLeft : "0px",
				cursorTop : "150px",
			},
			{
				viewType : "colorType2",
				rgbColor : "rgb(240, 127, 9)",
				rgbR : 240,
				rgbG : 127,
				rgbB : 9,
				hueTop : "137px",
				cursorLeft : "144px",
				cursorTop : "8px",
			},
			{
				viewType : "colorType3",
				rgbColor : "rgb(0, 102, 204)",
				rgbR : 0,
				rgbG : 102,
				rgbB : 204,
				hueTop : "62px",
				cursorLeft : "150px",
				cursorTop : "30px",
			},
			{
				viewType : "colorType4",
				rgbColor : "rgb(0, 128, 128)",
				rgbR : 0,
				rgbG : 128,
				rgbB : 128,
				hueTop : "75px",
				cursorLeft : "150px",
				cursorTop : "74px",
			},
			{
				viewType : "colorType5",
				rgbColor : "rgb(192, 0, 0)",
				rgbR : 192,
				rgbG : 0,
				rgbB : 0,
				hueTop : "150px",
				cursorLeft : "150px",
				cursorTop : "37px",
			},
			{
				viewType : "colorType6",
				rgbColor : "rgb(255, 153, 204)",
				rgbR : 255,
				rgbG : 153,
				rgbB : 204,
				hueTop : "12px",
				cursorLeft : "60px",
				cursorTop : "0px",
			},
		];
	},
	getTextColor : function() {
		return [ 
			{
				viewType : "textType1",
				rgbColor : "rgb(0, 0, 0)",
				rgbR : 0,
				rgbG : 0,
				rgbB : 0,
				hueTop : "25px",
				cursorLeft : "0px",
				cursorTop : "150px",
			},
			{
				viewType : "textType2",
				rgbColor : "rgb(255, 255, 255)",
				rgbR : 255,
				rgbG : 255,
				rgbB : 255,
				hueTop : "25px",
				cursorLeft : "0px",
				cursorTop : "0px",
			},
		];
	},
	getListColor : function() {
		return [ 
			{
				viewType : "listType1",
				rgbColor : "rgb(217, 217, 217)",
				rgbR : 217,
				rgbG : 217,
				rgbB : 217,
				hueTop : "25px",
				cursorLeft : "0px",
				cursorTop : "22px",
			},
			{
				viewType : "listType2",
				rgbColor : "rgb(253, 229, 205)",
				rgbR : 253,
				rgbG : 229,
				rgbB : 205,
				hueTop : "137px",
				cursorLeft : "28px",
				cursorTop : "1px",
			},
			{
				viewType : "listType3",
				rgbColor : "rgb(196, 225, 242)",
				rgbR : 196,
				rgbG : 225,
				rgbB : 242,
				hueTop : "65px",
				cursorLeft : "28px",
				cursorTop : "7px",
			},
			{
				viewType : "listType4",
				rgbColor : "rgb(217, 234, 213)",
				rgbR : 217,
				rgbG : 234,
				rgbB : 213,
				hueTop : "104px",
				cursorLeft : "13px",
				cursorTop : "12px",
			},
			{
				viewType : "listType5",
				rgbColor : "rgb(225, 204, 204)",
				rgbR : 225,
				rgbG : 204,
				rgbB : 204,
				hueTop : "150px",
				cursorLeft : "14px",
				cursorTop : "17px",
			},
			{
				viewType : "listType6",
				rgbColor : "rgb(255, 204, 255)",
				rgbR : 255,
				rgbG : 204,
				rgbB : 255,
				hueTop : "39px",
				cursorLeft : "30px",
				cursorTop : "0px",
			},
		];
	}
}

var gObjData = {

          // 部署マスター　start
      	getDepatureData : function() {
      		return gDepData;
      	},

      	setDepatureData : function(getObj) {
      		gDepData = getObj;
      		//alert("specialstam");
      	},


      	setElementDepName : function() {
      		// dbからexbマスターを取得
      		gDepData=[];
      		var vDepElement = document.getElementsByClassName("depData");
      		for (var i = 0;i< vDepElement.length; ++i){
      			gDepData[i] = {};
      			var vDepCode = document.getElementById("depCode-" + i).textContent;
      			gDepData[i].code = vDepCode;
      		}

      	},

      	setElementDepData : function() {
      		// dbからexbマスターを取得
      		gDepData=[];
      		var vDepElement = document.getElementsByClassName("depData");
      		for (var i = 0;i< vDepElement.length; ++i){
      			gDepData[i] = {};
      			var vDepId = document.getElementById("depId-" + i).textContent;
      			gDepData[i].id = vDepId;
      			var vDepName = document.getElementById("depName-" + i).textContent;
      			gDepData[i].name = vDepName;

      			var vDepColor = document.getElementById("depColor-" + i).textContent;
      			gDepData[i].color = vDepColor;
      			var vDepListColor = document.getElementById("depListColor-" + i).textContent;
      			gDepData[i].listColor = vDepListColor;
      			var vDepTextColor = document.getElementById("depTextColor-" + i).textContent;
      			gDepData[i].textColor = vDepTextColor;
      		}

      	},

      	setElementMapViewerData : function() {
            // dbからexbマスターを取得
            gMapViewerData=[];
            var vMapViewerElement = document.getElementsByClassName("mapViewer");
            for (var i = 0;i< vMapViewerElement.length; ++i){

                gMapViewerData[i] = {};
                var id = document.getElementById("map_id-" + i).textContent;
                gMapViewerData[i].id = Number(id);

                var image = document.getElementById("map_image-" + i).textContent;
                gMapViewerData[i].image = image;

                var positionName = document.getElementById("map_position-" + i).textContent;
                gMapViewerData[i].positionName = positionName;

//                var imageWidth = document.getElementById("map_width-" + i).textContent;
//                gMapViewerData[i].imageWidth = imageWidth;
//
//                var imageHeight = document.getElementById("map_height-" + i).textContent;
//                gMapViewerData[i].imageHeight = imageHeight;

            }
        },
        setElementBuildingData : function() {
            // dbからexbマスターを取得
            var vCount = 0;
            gExbViewerData=[];
            gExbViewerData[vCount] = {};
            gExbViewerData[vCount].id = -1;
            gExbViewerData[vCount].visible = "false";
            gExbViewerData[vCount].viewType = "none";
            gExbViewerData[vCount].x = "-200";
            gExbViewerData[vCount].y = "-400";
            gExbViewerData[vCount].margin = 1;
            gExbViewerData[vCount].posNum = -1;
            gExbViewerData[vCount].posCount = 1;
            gExbViewerData[vCount].floor = 3;
            gExbViewerData[vCount].beaconId = "未検知";
            //gExbViewerData[vCount].display_limit = 50;
            vCount++;
            var vExbViewerElement = document.getElementsByClassName("exbViewer");
            for (var i = 0;i< vExbViewerElement.length; ++i){
                gExbViewerData[vCount] = {};
                var id = document.getElementById("viewer_id-" + i).textContent;
                gExbViewerData[vCount].id = Number(id);

                var visible = document.getElementById("viewer_visible-" + i).textContent;
                gExbViewerData[vCount].visible = visible;

                var viewType = document.getElementById("viewer_pos_type-" + i).textContent;
                gExbViewerData[vCount].viewType = viewType;

                var viewer_pos_x = document.getElementById("viewer_pos_x-" + i).textContent;
                gExbViewerData[vCount].x = Number(viewer_pos_x);

                var viewer_pos_y = document.getElementById("viewer_pos_y-" + i).textContent;
                gExbViewerData[vCount].y = Number(viewer_pos_y);

                var viewer_pos_margin = document.getElementById("viewer_pos_margin-" + i).textContent;
                gExbViewerData[vCount].margin = Number(viewer_pos_margin);

                var viewer_pos_num = document.getElementById("viewer_id-" + i).textContent;
                gExbViewerData[vCount].posNum = Number(viewer_pos_num);

                var viewer_pos_count = document.getElementById("viewer_pos_count-" + i).textContent;
                gExbViewerData[vCount].posCount = Number(viewer_pos_count);

                var viewer_pos_floor = document.getElementById("viewer_pos_floor-" + i).textContent;
                gExbViewerData[vCount].floor = Number(viewer_pos_floor);

                var viewer_pos_size = document.getElementById("viewer_pos_size-" + i).textContent;
                gExbViewerData[vCount].size = Number(viewer_pos_size);

                var exb_pos_name = document.getElementById("exb_pos_name-" + i).textContent;
                gExbViewerData[vCount].beaconId = exb_pos_name;

                var exb_name = document.getElementById("exb_name-" + i).textContent;
                gExbViewerData[vCount].exbName = exb_name;

//                if (document.getElementById("display_limit-" + i) != null ) {
//                    var display_limit = document.getElementById("display_limit-" + i).textContent;
//                    gExbViewerData[vCount].display_limit = Number(display_limit);
//                }

                vCount++;
            }

        },

      	setElementExbViewerData : function() {
            // dbからexbマスターを取得
            gExbViewerData=[];
            var vExbViewerElement = document.getElementsByClassName("exbViewer");
            for (var i = 0;i< vExbViewerElement.length; ++i){
                gExbViewerData[i] = {};
                var id = document.getElementById("viewer_id-" + i).textContent;
                gExbViewerData[i].id = Number(id);

                var visible = document.getElementById("viewer_visible-" + i).textContent;
                gExbViewerData[i].visible = visible;

                var viewType = document.getElementById("viewer_pos_type-" + i).textContent;
                gExbViewerData[i].viewType = viewType;

                var viewer_pos_x = document.getElementById("viewer_pos_x-" + i).textContent;
                gExbViewerData[i].x = Number(viewer_pos_x);

                var viewer_pos_y = document.getElementById("viewer_pos_y-" + i).textContent;
                gExbViewerData[i].y = Number(viewer_pos_y);

                var viewer_pos_margin = document.getElementById("viewer_pos_margin-" + i).textContent;
                gExbViewerData[i].margin = Number(viewer_pos_margin);

                var viewer_pos_num = document.getElementById("viewer_id-" + i).textContent;
                gExbViewerData[i].posNum = Number(viewer_pos_num);

                var viewer_pos_count = document.getElementById("viewer_pos_count-" + i).textContent;
                gExbViewerData[i].posCount = Number(viewer_pos_count);

                var viewer_pos_floor = document.getElementById("viewer_pos_floor-" + i).textContent;
                gExbViewerData[i].floor = Number(viewer_pos_floor);

                var viewer_pos_size = document.getElementById("viewer_pos_size-" + i).textContent;
                gExbViewerData[i].size = Number(viewer_pos_size);

                var exb_pos_name = document.getElementById("exb_pos_name-" + i).textContent;
                gExbViewerData[i].beaconId = exb_pos_name;
            }

        },
	
	// 部署マスター　end
	
	getExbData : function() {
		return gExbData;
	},
	
	setElementExbData : function() {
		// dbからexbマスターを取得
		gExbData=[];
		var vExbElement = document.getElementsByClassName("exbData");
		for (var i = 0;i< vExbElement.length; ++i){
			gExbData[i] = {};
			var vExbId = document.getElementById("exbId-" + i).textContent;
			gExbData[i].id = vExbId;
			var vExbPosName = document.getElementById("exbPosName-" + i).textContent;
			gExbData[i].posName = vExbPosName;

			var vExbDeviceId = document.getElementById("exbDevId-" + i).textContent;
            gExbData[i].devId = vExbDeviceId;
		}
		
		return gExbData;

	},
	
	getPosName : function(posNum) {
		var vResultPosName = ""
		for (var i = 0;i< gExbData.length; ++i){
			if(gExbData[i].id == posNum ){
				vResultPosName = gExbData[i].posName;
				break;
			}
		}
		return vResultPosName;

	},
	
	// exbマスター　end
	
	
	
	test2GetFunc : function(getValue) {
		
		//specialstam=getValue
		alert(getValue);
	},
	
	testSetFunc : function(getValue) {
		alert(getValue);

	},
}
var gDepartment = {
		getColor : function(szColor,checkColor) {
			var resultColor;
		 	var szTemp = szColor.split("rgb(");
		 	if(szTemp.length>1){
		 		
		 	
		 	var szRGBTemp = szTemp[1].split(",");
		 	var rColor = szRGBTemp[0].trim();
		 	var gColor = szRGBTemp[1].trim();
		 	var bColorTemp = szRGBTemp[2].split(")");
		 	var bColor = bColorTemp[0].trim();
		 	if(checkColor == "R"){
		 		resultColor = rColor;
		 	}else if (checkColor == "G"){
		 		resultColor = gColor;
		 	}else if (checkColor == "B"){
		 		resultColor = bColor
		 	}
		 	return resultColor;
		 	}
		},
		
}
// 日本+9:00
var getUTCtoLocalTime = function(localTime) {
	var d = new Date(localTime);
	// 年月日・曜日・時分秒の取得
	var month = d.getMonth() + 1;
	var day = d.getDate();
	var hour = d.getHours();
	var minute = d.getMinutes();
	var second = d.getSeconds();

	// 1桁を2桁に変換する
	if (month < 10) {
		month = "0" + month;
	}
	if (day < 10) {
		day = "0" + day;
	}
	if (hour < 10) {
		hour = "0" + hour;
	}
	if (minute < 10) {
		minute = "0" + minute;
	}
	if (second < 10) {
		second = "0" + second;
	}

	return {
		year:d.getFullYear(),
		day:day,
		month:month,
		hour:hour,
		minute:minute,
		second:second,
		date : d.getFullYear() + "/" + month + "/" + day,
		time : hour + ":" + minute + ":" + second
	}
}

var gTimeTest = function() {
alert("gTimeTest")
}


var getDateAndTime = function() {
	
	var d = new Date();
	// 年月日・曜日・時分秒の取得
	var month = d.getMonth() + 1;
	var day = d.getDate();
	var hour = d.getHours();
	var minute = d.getMinutes();
	var second = d.getSeconds();

	// 1桁を2桁に変換する
	if (month < 10) {
		month = "0" + month;
	}
	if (day < 10) {
		day = "0" + day;
	}
	if (hour < 10) {
		hour = "0" + hour;
	}
	if (minute < 10) {
		minute = "0" + minute;
	}
	if (second < 10) {
		second = "0" + second;
	}

	return {
		year:d.getFullYear(),
		day:day,
		month:month,
		hour:hour,
		minute:minute,
		second:second,
		date : d.getFullYear() + "/" + month + "/" + day,
		time : hour + ":" + minute + ":" + second
	}
}

var gBeaconPosition = {
    setPosition : function() {
        // frame 空データ取得
		return gExbViewerData;
	},
	getPosition : function() {
		return gExbViewerData;
	},

	testGetFunc : function() {
		alert("testGetFunc");
	},
	
	checkHankaku : function(targetName) {
		var vCheckCount = 0;
		for(var i = 0 ; i<gArhankaku.length;++i ){
			var vSearch = new RegExp(gArhankaku[i], "ig");
			var vCheckValue = targetName.match(vSearch)
			if(vCheckValue!= null){
				//bResult = true;
				//vCheckCount;
				vCheckCount = vCheckValue.length;
				break;
			}
			
		}
		return vCheckCount;
	},
	
	setTagNameAfterColor : function(beaconData) {
		var vColor;
		if(beaconData.depName == "組織長"){
			vColor = "2vmin solid rgb(90, 59, 59)";
		}else if(beaconData.depName == "企画総務部"){
			vColor = "2vmin solid rgb(154, 79, 0)";
		}else if(beaconData.depName == "営業部"){
			vColor = "2vmin solid rgb(0, 52, 105)";
		}else if(beaconData.depName == "法人営業部"){
			vColor = "2vmin solid rgb(0, 72, 72)";
		}else if(beaconData.depName == "その他（サテライト）"){
			vColor = "2vmin solid rgb(142, 0, 0)";
		}else if(beaconData.depName == "その他（物品）"){
			vColor = "2vmin solid rgb(162, 53, 108)";
		}
		return vColor;
	},
	
	getTextColor : function(depName) {
		var dbDepData = gObjData.getDepatureData()
		var vTextColor = "";
		for(var i = 0 ; i < dbDepData.length; ++i){
			  if(depName == dbDepData[i].name){
				  vTextColor = dbDepData[i].textColor;
				  break;
			  }
		}
		
		return vTextColor;
		
	},
	getColorUi : function(depName) {
		var color = "red";
		var dbDepData = gObjData.getDepatureData()
		for(var i = 0 ; i < dbDepData.length; ++i){
			  if(depName == dbDepData[i].name){
				  color =  dbDepData[i].color;
				  break;
			  }
		}
		return color
	},
	
	setTextColor : function(element,depName) {
		var dbDepData = gObjData.getDepatureData()
		for(var i = 0 ; i < dbDepData.length; ++i){
			  if(depName == dbDepData[i].name){
				  var vRgb = dbDepData[i].listColor;
				  var szTemp = vRgb.split("rgb(");
				 	var szRGBTemp = szTemp[1].split(",");
				 	var rColor = szRGBTemp[0].trim();
				 	var gColor = szRGBTemp[1].trim();
				 	var bColorTemp = szRGBTemp[2].split(")");
				 	var bColor = bColorTemp[0].trim();
				    	
				    // 最高値は 255 なので、約半分の数値 127 を堺目にして白/黒の判別する
				    var cY = 0.3*rColor + 0.6*gColor + 0.1*bColor;
				    
				    if(cY > 127){
				    	// 黒に設定
				    	element.style.color = "#000000"
			             
			        }else{
			        	element.style.color = "#FFFFFF"; 
			        }
			  }
		}
	},
	setColorUi : function(element,type,depName) {
		//gArDepName = ["組織長","企画総務部","営業部","法人営業部","その他（サテライト）","その他（物品）"];
		var dbDepData = gObjData.getDepatureData()
		if(type == "listStyle"){
			for(var i = 0 ; i < dbDepData.length; ++i){
				  if(depName == dbDepData[i].name){
					  element.style.background = dbDepData[i].listColor;
				  }
				}
		}else if(type == "powerLevelStyle"){
			  if(depName == gArPowerLevel[0]){
				   element.style.background = "rgb(0, 102, 204)";
				}else if(depName == gArPowerLevel[1]){
					element.style.background = "rgb(255, 235, 59)";
					element.style.color = "black";
				}else if(depName == gArPowerLevel[2]){
					element.style.background = "rgb(192, 0, 0)";
				}else {
					element.style.background = "gray";
				}
		}else if(type == "iconStyle"){
			for(var i = 0 ; i < dbDepData.length; ++i){
			  if(depName == dbDepData[i].name){
				  element.style.background = dbDepData[i].color;
				  element.style.color = dbDepData[i].textColor;
			  }
			}
		}else if(type == "textStyle"){
			for(var i = 0 ; i < dbDepData.length; ++i){
				  if(depName == dbDepData[i].name){
					  element.style.color = dbDepData[i].textColor;
				  }
			}
		}else{
		}
		return element;
	},
	
	getLastPosition : function(pos_id) {
		var objBeaconData = this.getPosition();
		var vLastPosition
		for(var i = 0; i<objBeaconData.length;++i){
			if(objBeaconData[i].id == pos_id ){
				vLastPosition = objBeaconData[i].posName;
				break;
			}
		}
		return vLastPosition;
	},
	
	getCurrentTime : function() {
		
		var dt = getDateAndTime();
		return dt;
		
	},
	
	//　不在判断処理
	getLastStatus : function(currentTime,targetTime) {
		var vLastStatus;
		
		//現在遅刻取得
    	var curYear = currentTime.year;
    	var curMonth= currentTime.month;
    	var curDay = currentTime.day;
    	var curHour= currentTime.hour;
    	var curMinute= currentTime.minute;
    	var curSecond= currentTime.second;
    	// utcをlocalタイムに変更
    	var vTargetTime = getUTCtoLocalTime(targetTime);
    	var targetYear = vTargetTime.year;
		var targetMonth=vTargetTime.month;
		var targetDay = vTargetTime.day;
		var targetHour=vTargetTime.hour;
		var targetMinute=vTargetTime.minute;
		var targetSecond=vTargetTime.second;
		
		var targetDate = new Date(targetYear,targetMonth,targetDay,targetHour,targetMinute,targetSecond);
		var curDate  = new Date(curYear,curMonth,curDay,curHour,curMinute,curSecond);
		var compareTime = (curDate.getTime() - targetDate.getTime())/60000
		// 分単位で比較
		if(compareTime >= COMP_INTERVAL){
			vLastStatus = "不在";
		}else{
			vLastStatus = "在席";
		}
		
		return vLastStatus;
	},

}


var gViewerManage = {
    resizeAllData : function() {
         var selectMapElement = [].slice.call(document.querySelectorAll(".level"));
            selectMapElement.forEach(function(map, pos) {
                 map.addEventListener('click', function(event) {
                    var x = (event.offsetX || event.clientX) - (gIconWidth/2);
                    var y = (event.offsetY || event.clientY) - (gIconHeight/2);
                    //console.dir("x :" + x + "\n" +  "y :" + y + "\n")
                    console.dir("y :" + y + "\n" +  "x :" + x + "\n")
             });
         });

    },
    onManageEvent : function() {
    $( ".change-selector" ).change(function() {
          if($( ".change-selector" )[0].checked){
            //alert($( this ).val());
             var result =  $( this ).val();
             gModal.confirm(gTitle.viewer_manage,gMessage.viewer_manage,result)
          }else{
            // 閉じる
            location.reload();
          }
        });
    },

    /*確認処理*/
    manageConfirm : function(jumpAdress) {
         window.open(jumpAdress, "マネージャー画面", "width=1366, height=768, toolbar=no, menubar=no, scrollbars=yes, resizable=yes" );
         gViewerManage.clearMap();
         gViewerManage.drawExbeacon();
         gViewerManage.showMapUploadBtn();
         //gViewerManage.eventMapUploadBtn();

    },

    showMapUploadBtn : function() {
        var el = document.getElementsByClassName("map__upload--frame hidden")[0]
        classie.remove(el, 'hidden');
    },

    hiddenMapUploadBtn : function(vFloor) {
            //alert(vFloor)
            var el = document.getElementsByClassName("inputFrame")
            for(var i = 0 ; i< el.length ; ++i){
                el[i].className = "inputFrame hidden"
            }
            if(el!=null){
                if(vFloor == 4){
                    classie.remove(el[0], 'hidden');
                }else if(vFloor == 3){
                    classie.remove(el[1], 'hidden');
                }
            }
    },

    drawClonePosNew : function(pos_id) {
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
    },

    reDrawExbeacon : function(vExbViewerData) {
        bInputCheck = true;
        var vCount = 0;
        var vExbViewerElement = document.getElementsByClassName("exbViewer");
        gExbViewerUiData = []
        for (var i = 0;i< vExbViewerData.length; ++i){
            var viewer_pos_count = vExbViewerData[i].posCount
            for (var j = 0; j< viewer_pos_count; ++j){
                gExbViewerUiData[vCount] = {};

                var id = vExbViewerData[i].id
                gExbViewerUiData[vCount].id = Number(id);

                var visible = vExbViewerData[i].visible
                gExbViewerUiData[vCount].visible = visible;

                var pos = [];
                pos = gViewerManage.drawClonePosNew(id)
                gExbViewerUiData[vCount].pos = pos;

                var viewType = vExbViewerData[i].viewType
                gExbViewerUiData[vCount].viewType = viewType;

                var viewer_pos_x = vExbViewerData[i].x
                gExbViewerUiData[vCount].x = Number(viewer_pos_x);

                var viewer_pos_y = vExbViewerData[i].y
                gExbViewerUiData[vCount].y = Number(viewer_pos_y);

                var viewer_pos_margin = vExbViewerData[i].margin
                gExbViewerUiData[vCount].margin = Number(viewer_pos_margin);

                var viewer_pos_num = vExbViewerData[i].posNum
                gExbViewerUiData[vCount].posNum = Number(viewer_pos_num);

                var viewer_pos_count = vExbViewerData[i].posCount
                gExbViewerUiData[vCount].posCount = Number(viewer_pos_count);

                var viewer_pos_floor = vExbViewerData[i].floor
                gExbViewerUiData[vCount].floor = Number(viewer_pos_floor);

                var viewer_pos_display_order = document.getElementById("viewer_pos_display_order-" + i).textContent;
                gExbViewerUiData[vCount].displayOrder = Number(viewer_pos_display_order);

                var viewer_pos_size = vExbViewerData[i].size
                gExbViewerUiData[vCount].size = Number(viewer_pos_size);

                var exb_pos_name = vExbViewerData[i].beaconId
                gExbViewerUiData[vCount].beaconId = exb_pos_name;

                // indexを増加
                vCount++;
            }
        }

        gAddPositionMargin(gExbViewerUiData);

        gExbViewerUiData.forEach(function(beaconData,i) {
                var pinFrame = document.createElement('div');
                pinFrame.style.top = beaconData.pos.y + "px";
                pinFrame.style.left = beaconData.pos.x + "px";
                pinFrame.style.width = (beaconData.size) * (MARGIN_BASE) + "px";
                pinFrame.style.height =(beaconData.size) * (MARGIN_BASE)+ "px";

                 // 文字サイズ再調整
                gResize.textResize(pinFrame,beaconData.size)

                pinFrame.id = "exb_id_" + beaconData.posNum;
                pinFrame.className = "exb__viewer--frame";
                pinFrame.textContent = beaconData.posNum;

                var tempElement = document.getElementById("beaconMap-" +beaconData.displayOrder)
                if(tempElement!= null)
                    tempElement.appendChild(pinFrame);
                // 非表示のopacityを設定
                if(beaconData.visible == "false"){
                    //pinFrame.style.opacity = 0.8;
                    pinFrame.style.background = "#a2a29e"
                }
        });

    },
    drawExbeacon : function() {
        //gExbViewerUiData = gExbViewerData;
        gExbViewerUiData=[];
        var vCount = 0;
        var vExbViewerElement = document.getElementsByClassName("exbViewer");
        for (var i = 0;i< vExbViewerElement.length; ++i){
            var viewer_pos_count = Number(document.getElementById("viewer_pos_count-" + i).textContent);
            for (var j = 0; j< viewer_pos_count; ++j){
                gExbViewerUiData[vCount] = {};

                var id = document.getElementById("viewer_id-" + i).textContent;
                gExbViewerUiData[vCount].id = Number(id);

                var visible = document.getElementById("viewer_visible-" + i).textContent;
                gExbViewerUiData[vCount].visible = visible;

                var pos = [];
                pos = gViewerManage.drawClonePosNew(id)
                gExbViewerUiData[vCount].pos = pos;

                var viewType = document.getElementById("viewer_pos_type-" + i).textContent;
                gExbViewerUiData[vCount].viewType = viewType;

                var viewer_pos_x = document.getElementById("viewer_pos_x-" + i).textContent;
                gExbViewerUiData[vCount].x = Number(viewer_pos_x);

                var viewer_pos_y = document.getElementById("viewer_pos_y-" + i).textContent;
                gExbViewerUiData[vCount].y = Number(viewer_pos_y);

                var viewer_pos_margin = document.getElementById("viewer_pos_margin-" + i).textContent;
                gExbViewerUiData[vCount].margin = Number(viewer_pos_margin);

                var viewer_pos_num = document.getElementById("viewer_id-" + i).textContent;
                gExbViewerUiData[vCount].posNum = Number(viewer_pos_num);

                var viewer_pos_count = document.getElementById("viewer_pos_count-" + i).textContent;
                gExbViewerUiData[vCount].posCount = Number(viewer_pos_count);

                var viewer_pos_display_order = document.getElementById("viewer_pos_display_order-" + i).textContent;
                gExbViewerUiData[vCount].displayOrder = Number(viewer_pos_display_order);

                var viewer_pos_floor = document.getElementById("viewer_pos_floor-" + i).textContent;
                gExbViewerUiData[vCount].floor = Number(viewer_pos_floor);

                var viewer_pos_size = document.getElementById("viewer_pos_size-" + i).textContent;
                gExbViewerUiData[vCount].size = Number(viewer_pos_size);

                var exb_pos_name = document.getElementById("exb_pos_name-" + i).textContent;
                gExbViewerUiData[vCount].beaconId = exb_pos_name;

                // indexを増加
                vCount++;
            }
        }

        gAddPositionMargin(gExbViewerUiData);


        gExbViewerUiData.forEach(function(beaconData,i) {
                var pinFrame = document.createElement('div');
                pinFrame.style.top = beaconData.pos.y + "px";
                pinFrame.style.left = beaconData.pos.x + "px";
                pinFrame.style.width = (beaconData.size) * (MARGIN_BASE) + "px";
                pinFrame.style.height =(beaconData.size) * (MARGIN_BASE)+ "px";

                // 文字サイズ再調整
                gResize.textResize(pinFrame,beaconData.size)

                pinFrame.id = "exb_id_" + beaconData.posNum;
                pinFrame.className = "exb__viewer--frame";
                pinFrame.textContent = beaconData.posNum;

                var tempElement = document.getElementById("beaconMap-" +beaconData.displayOrder)
                if(tempElement!= null)
                    tempElement.appendChild(pinFrame);
                // 非表示のopacityを設定
                if(beaconData.visible == "false"){
                    //pinFrame.style.opacity = 0.8;
                    pinFrame.style.background = "#a2a29e"
                }
        });

    },
    clearMap : function() {
        updating = true;
        var userTag = [].slice.call(document.querySelectorAll(".user__tag--name"))
        // 画面クリア
        $(userTag).remove()
    }
}
var gTopMenu = {
	// メニュー遷移用selectBox
	setMenuSelect : function() {
	 var kanriFrame = $('#kanri-category');
		if(kanriFrame!=null){
		// 管理者用selectbox value取得
		$('#kanri-category').change(function() {
			
			if(!gInit.bHiddenMenu){
				var result = $('#kanri-category option:selected').val();
				$(location).attr('href', result);
			}else{
				gModal.confirm(gTitle.clickMenuSelectBox, gMessage.clickMenuSelectBox);
			}
			
			
		});
		}
	},
}

var bInputCheck = false;
var arAddDeleteIndex = [];
var gInit = {
		
		inputNameAllClear : function() {
			var vInputElement = document.getElementsByClassName("form-control input-sm")
			
			for(var i = 0 ; i < vInputElement.length;++i ){
				vInputElement[i].name = "";
			}
		},
		
		backBtnStopEvent : function() {
			var vMenuElement = document.getElementsByClassName("btn btn-navbar btn-back")
			var btnMenu = vMenuElement[0]
			
			btnMenu.addEventListener('click', function(event) {
				if(bInputCheck || bCheckInsert){
					event.preventDefault();
					gModal.confirm(gTitle.backViewlist, gMessage.backViewlist);
				}
			});
			
			var vCount = 0;
			inputBtn = [].slice.call(document.querySelectorAll(".input-sm"));
			inputBtn.forEach(function(inputBtn, pos) {
				inputBtn.addEventListener('change', function() {
					bInputCheck = true;
					gInit.hiddenMenuFrame(bInputCheck)
				});
			 });
			
		},
		focusScrollEnd : function() {
			tBody.scrollTop = tBody.scrollHeight;
		},
		btnViewUpdateEvent : function() {
			 var btnUpdate = $("#btn-update");
			    var btnUpdateStatus = $("#btn-update-status");
				var updating = true;
				var startUpdate = function() {
					if (!updating) {
						btnUpdate.find("i").addClass("fa-spin");
						updating = true;
						location.reload();
					}
				}
				var finishUpdate = function() {
					btnUpdate.find("i").removeClass("fa-spin");
					updating = false;
				}
				btnUpdate.on("click", function() {
					if(bInputCheck || gInit.reNewCheckFrame()){
						gModal.confirm(gTitle.viewReload, gMessage.viewReload);
						 
					}else{
						startUpdate();
					}
					
				});
			    
				finishUpdate();
		},
		
		getExbSelectCloneIndex : function() {
			 var arSelectCloneIndex = [];
			 var checkItem = document.getElementsByClassName("checkClone")
			   for(var i = 0 ; i < checkItem.length;++i ){
				   if(checkItem[i].children[0].children[0].checked){
					   var vId = checkItem[i].children[2].children[0].id
					   var vTemp = vId.split("edit-id-")
					   var vPos = Number(vTemp[1])
					   arSelectCloneIndex.push(vPos)
				   }
			   }
			return arSelectCloneIndex;
		},
		
		getSelectCloneIndex : function() {
			 var arSelectCloneIndex = [];
			 var checkItem = document.getElementsByClassName("checkClone")
			   for(var i = 0 ; i < checkItem.length;++i ){
				   if(checkItem[i].children[0].children[0].checked){
					   var vId = checkItem[i].children[1].id
					   var vTemp = vId.split("edit-id-")
					   var vPos = Number(vTemp[1])
					   arSelectCloneIndex.push(vPos)
				   }
			   }
			return arSelectCloneIndex;
		},
		
		deleteExbCheckFrame : function(arSelectIndex) {
			for(var i = 0 ; i < arSelectIndex.length;++i ){
				var vChildElement = document.getElementById("edit-id-" + arSelectIndex[i]);
				$(vChildElement.parentElement.parentElement).remove()
			}
			//this.reNewCheckFrame();
		},
		
		deleteCheckFrame : function(arSelectIndex) {
			for(var i = 0 ; i < arSelectIndex.length;++i ){
				arAddDeleteIndex.push(arSelectIndex[i])
				var vChildElement = document.getElementById("edit-id-" + arSelectIndex[i]);
				$(vChildElement.parentElement).remove()
			}
			//this.reNewCheckFrame();
		},
		
		deleteHiddenFrame : function(arSelectIndex) {
			for(var i = 0 ; i < arSelectIndex.length;++i ){
				arAddDeleteIndex.push(arSelectIndex[i])
				var vChildElement = document.getElementById("edit-id-" + arSelectIndex[i]);
				//$(vChildElement.parentElement).remove()
				vChildElement.parentElement.style.backgroundColor = "red";
				vChildElement.parentElement.children[0].children[0].checked = false
				vChildElement.parentElement.children[1].children[0].value = -1
				vChildElement.parentElement.children[3].children[0].value = ""
				vChildElement.parentElement.children[4].children[0].value = ""
				vChildElement.parentElement.children[5].children[0].value = ""

				vChildElement.parentElement.className = "hidden pos-search common__clone--frame"
			}
			//this.reNewCheckFrame();
		},
		
		reNewCheckFrame : function() {
			var vResult = true;
			var checkItem = document.getElementsByClassName("checkClone")
			if(checkItem.length == 0){
				var vTotalCheck = document.getElementsByClassName("checkTotal")
				for(var i = 0 ; i < vTotalCheck.length;++i ){
					vTotalCheck[i].disabled = false;
				}
				vResult = false;
			}
			return vResult;
		},
		
		
		
		hiddenCheckFrame : function() {
			var vTotalCheck = document.getElementsByClassName("checkTotal")
			for(var i = 0 ; i < vTotalCheck.length;++i ){
				vTotalCheck[i].disabled = true;
			}
		},
		
		
		bHiddenMenu : false,
		hiddenMenuFrame : function(bCheck) {
			var cMenu = document.getElementById("kanri-category")
					if(bCheck){
						// off
						cMenu.style.background ="#9E9E9E"
						//cMenu.disabled = 1;
						gInit.bHiddenMenu = true;
						
					}else if (!bInputCheck){
						
						// on
						cMenu.style.background ="" 
						cMenu.disabled = 0;
						gInit.bHiddenMenu = false;
					}
			
			
//			cMenu.addEventListener('change', function(event) {
//					alert("gg")
//					event.preventDefault();
//			});
		},
		
		setCheckEvent : function() {
		var vTotalCheck = document.getElementsByName("chk_total");
		var checkItem = document.getElementsByName("chk_info")
		vTotalCheck[0].addEventListener('click', function() {
			 //alert("check")
			 if(vTotalCheck[0].checked){
				 for(var i = 0 ; i < checkItem.length;++i ){
				 		checkItem[i].checked = true;
					   	//arCheck.push(checkItem[i].value)
				   }
			 }else{
				 for(var i = 0 ; i < checkItem.length;++i ){
				 		checkItem[i].checked = false;
				   }
			 }
			 
		});
	},
	getPkey : function(type) {
		 var arPkey = [];
		 var vPos;
		 var checkItem = document.getElementsByName("chk_info")
		   for(var i = 0 ; i < checkItem.length;++i ){
			   if(checkItem[i].checked){
				   var vPosElement =  document.getElementById("edit-id-" + i)
				   if(type == "exb"){
					   vPos = Number(vPosElement.value.trim())
				   }else{
					   vPos = Number(vPosElement.value)
				   }
				   	arPkey.push(vPos)
			   }
		   }
		return arPkey;
	},
	
	getSelectIndex : function() {
		 var arSelectIndex = [];
		 var checkItem = document.getElementsByName("chk_info")
		   for(var i = 0 ; i < checkItem.length;++i ){
			   if(checkItem[i].checked){
				   var vPosElement =  document.getElementById("edit-id-" + i)
				   var vPos = Number(vPosElement.textContent.trim())
				   	arSelectIndex.push(i)
			   }
		   }
		return arSelectIndex;
	},
	
	
	getInsertArry : function(vNum) {
		var arInsert = [];
		var vBigNum = 0;
		
		var vLength = document.getElementsByClassName("pos-search").length
		
		var vPosSearchElement = document.getElementsByClassName("pos-search");
		for(var i = 0 ; i < vPosSearchElement.length; ++i ){
			var vCurNum = Number(vPosSearchElement[i].children[1].children[0].value)
			if( vCurNum > vBigNum){
				vBigNum = vCurNum;
			}
				
		}
		
//	    var vPosElement =  document.getElementById("edit-id-" + (vLength-1))
//	    var vEndPos = 0;
//	    if(vLength>0){
//    		vEndPos = Number(vLength)
//	    }else{
//		    vEndPos = 0;
//	    }
		   
	   for(var i = 0 ; i < vNum;++i ){
		   var vInsertNum = ++vBigNum
		   arInsert.push(vInsertNum)
	   }

		return arInsert;
	},
	
	duplicationCheck: function(vInputString,vInputIndex,vElementClassName) {
		var bResult = false;
		var vTargetElement = [].slice.call(document.querySelectorAll(vElementClassName));
		
		for(var i = 0 ; i < vTargetElement.length;i++ ){
			if(vInputIndex != i){
				vInputString += "";
				if(vInputString == vTargetElement[i].value){
					bResult = true;
					gModal.alert(gTitle.updateException,gMessage.replaceMessage(gMessage.updateException6,(vInputString)))
					break;
				}
			}else{
				
			}
		}
		return bResult;
	},
	
	
	setScrollSize : function(viewName) {
		var vViewHeight;
		var vControlWidth=0;
		if(viewName == "viewlist"){
			vViewHeight = 240;
			vControlWidth = 20;
		}else if (viewName == "telemetry"){
			var vCheckBrowser = this.checkBrowser();
			if(vCheckBrowser =="ie"){
				vViewHeight = 160;
			}else{
				vViewHeight = 130;
			}
			
			vControlWidth = 18;
		}else if (viewName == "maintenance"){
			vViewHeight = 190;
			vControlWidth = 20;
		}else if (viewName == "exb"){
			vViewHeight = 180;
			vControlWidth = 20;
		}else if (viewName == "department"){
			vViewHeight = 180;
			vControlWidth = 20;
		}else if (viewName == "exbViewer"){
            vViewHeight = 180;
            vControlWidth = 20;
        }
		
		// 画面サイズを再調整
		var vScrollBdoy = document.getElementsByClassName("scrollBody")
		var vScrollHead = document.getElementsByClassName("scrollHead")
		var resizeWidth = window.innerWidth - vControlWidth;
		var resizeHeadWidth = window.innerWidth-36;
		var resizeHeight = window.innerHeight - vViewHeight;
		var resizeCssHeight = resizeHeight + "" + "px"
		var resizeCssWidth = resizeWidth + "" + "px"
		var resizeCssHeadWidth = resizeHeadWidth + "" + "px"
		if(vScrollBdoy[0]!=null){
			vScrollBdoy[0].style.height = resizeCssHeight;
			vScrollBdoy[0].style.width = resizeCssWidth;
			vScrollHead[0].style.width = resizeCssHeadWidth;	
		}
		
		if(viewName == "telemetry"){
			var vTelemetryLoading = document.getElementById("loading__item")
			vTelemetryLoading.style.width = resizeCssWidth;
			
		}
		//document.body.style.overflowY = "hidden"
	},
	
	checkBrowser : function() {
		var vResultBrowser = ""
		var agent = navigator.userAgent.toLowerCase();
		if ( (navigator.appName == 'Netscape' && navigator.userAgent.search('Trident') != -1) || (agent.indexOf("msie") != -1) ) {
			vResultBrowser = "ie"
    	}else {
    		vResultBrowser = "other"
    	}
		
		return vResultBrowser;
	},

    spinAnimationStart : function() {
    $('#update-element').addClass('fa-spin');
    },
    spinAnimationEnd : function(vEndCheck) {
        if(vEndCheck){
            setTimeout(function() {
            // 更新ボタンアニメーション終了
            $('#update-element').removeClass('fa-spin');
            $('#update-element').css('transform', 'none');
            }, 500);
        }else{
        }
    },
	spinAnimation : function(secUpdate) {
	    var vSec = 1000;
	    if(typeof(secUpdate) == "undefined"){
	    }else {
	        vSec = secUpdate;
	    }
        // 更新ボタンアニメーション開始
        $('#update-element').addClass('fa-spin');
        setTimeout(function() {
        // 更新ボタンアニメーション終了
        $('#update-element').removeClass('fa-spin');
        $('#update-element').css('transform', 'none');
        }, vSec);
	},

    spinAnimationReDraw : function() {
        $('#update-element')[0].className  = "btn__icon--style fa fa-refresh fa-3x fa-spin"
        var elemError = document.getElementById("errorIcon");
        elemError.parentNode.removeChild(elemError);

        //$("#errorIcon")[0].remove();
    },

	spinAnimationError : function() {
	    // spin停止
	    $('#update-element').removeClass('fa-spin');
        $('#update-element').css('transform', 'none');

        // error iconを生成
        var getElement = document.getElementById("errorSpan");
        var afterFrame = document.createElement('i');
        afterFrame.id = "errorIcon"
        afterFrame.className = "fa fa-exclamation-triangle fa-stack-1x error__triangle--frame text-danger";
        getElement.appendChild(afterFrame);

    },
	reloadView : function() {
        // 更新ボタンアニメーション開始
        $('#update-element').addClass('fa-spin');
        location.reload();
    },


    // 入力された時間差で画面をクリアする。
    refreshFadeOut : function() {
        var userTag = [].slice.call(document.querySelectorAll(".user__tag--name"))
        for(var i =0;i<userTag.length;++i){
            $(userTag[i]).removeClass('animation__fade--in')
            $(userTag[i]).addClass('animation__fade--out')
        }
    },

    refreshFadeIn : function() {
        var userTag = [].slice.call(document.querySelectorAll(".user__tag--name"))
        setTimeout(function() {
            for(var i =0;i<userTag.length;++i){
                $(userTag[i]).removeClass('animation__fade--out')
                $(userTag[i]).addClass('animation__fade--in')
            }
            // ie対応の為アニメーション終わった時点で属性クラスを削除
            setTimeout(function() {
                for(var i =0;i<userTag.length;++i){
                    $(userTag[i]).removeClass('animation__fade--in')
                }
            }, 500);
        }, 500);
    },

    // 入力された時間差で画面をクリアする。
    refreshSecond : function(vSec) {
        var userTag = [].slice.call(document.querySelectorAll(".user__tag--name"))
        for(var i =0;i<userTag.length;++i){
            $(userTag[i]).removeClass('animation__fade--in')
            $(userTag[i]).addClass('animation__fade--out')
        }
        setTimeout(function() {
            for(var i =0;i<userTag.length;++i){
                $(userTag[i]).removeClass('animation__fade--out')
                $(userTag[i]).addClass('animation__fade--in')
            }
            // ie対応の為アニメーション終わった時点で属性クラスを削除
            setTimeout(function() {
                for(var i =0;i<userTag.length;++i){
                    $(userTag[i]).removeClass('animation__fade--in')
                }
            }, 500);
        }, vSec);
    },
}

var bImportBtnCheck = false;
var bExportBtnCheck = false;
var gCsv = {
		csvConfirmBtnEvent : function(event_) {
			var vOkBtn = document.getElementById("alertOk");
			vOkBtn.addEventListener('click', function() {
				if(bImportBtnCheck){
					 bImportBtnCheck = false;
					 vHref = document.getElementById('csvExport')
					 window.location.href = vHref;
				}else if(bExportBtnCheck){
					bExportBtnCheck = false;
					 $("#csvInputFileFrame").trigger( "click" );
				}
				
			});
			
		},
		
		csvExportBtnEvent : function() {
			var csvExportBtn = document.getElementById("csvExportBtn");
			csvExportBtn.addEventListener('click', function(event) {
				bImportBtnCheck = true;
				// 一応ストップ
				event.preventDefault();
				//gModal.alert("csv処理","csvエクスポートを行いますか。");
				gModal.confirm(gTitle.csvExport,gMessage.csvExport);
			});
		 },
		csvImportBtnEvent : function(type) {
			var csvImportBtn = document.getElementById("csvImportBtn");
			csvImportBtn.addEventListener('click', function() {
				bExportBtnCheck = true;
				//gModal.alert("csv処理","csvインポートを行いますか。");
				gModal.confirm(gTitle.csvImport,gMessage.csvImport);
			});
			// csvを選択してクリックした。
			var csvBtn = document.getElementById("csvInputFileFrame");
		　		csvBtn.addEventListener('change', function() {
		　			gCsv.csvImportJson(type);
		　			
			});
		},
		csvImportJson : function(type) {
			
			var vUrl = "none"
			if(type == "exbMaster"){
				vUrl = "../exb/importCSV"
			}else if (type == "departmentMaster"){
				vUrl = "../department/importCSV"
			}
			
			var fd = new FormData();  
			 // token
		    var vToken = document.getElementsByName("csrfToken")
		    fd.append( 'csrfToken', vToken[0].value);
		    // file
		    var vFileElement = document.getElementById("csvInputFileFrame")
		    fd.append( 'file', $(vFileElement)[0].files[0]);
		    
			 $.ajax({
		           url: vUrl,
		           data: fd,
		           enctype: "multipart/form-data", 
		           processData: false,
		           contentType: false,
		           type: 'POST',
		           success: function(result){
		           //  $('#data').empty();
		           //  $('#data').append(data);
		        	  // alert(json_data[0]);
		        	   if(result=="重複"){
		        		   alert('番号重複、もう一度csvを確認してください。');
		        		   location.reload();
		    		   }else if(result=="nullCheck"){
		        		   alert('番号にnullが入ってます。');
		        		   location.reload();
		    		   }else if(result=="deleteError"){
                           gModal.alert(gTitle.csvImport,gMessage.importException)
                           vBtnOk = document.getElementsByClassName("btn btn-primary")[0];
                           vBtnOk.addEventListener('click', function(event) {
                                 location.reload();
                            });
                       }else{
		    			   alert('result OK');
		    			   location.reload();
		    		   }
		             //alert('success');
		             // 登録されたボタンelementを削除

		           },error: function (e) {
		               console.dir(e);
		               alert("result NG");
		               location.reload();
		           }
		         });
			 
		}
		
}
var gModal = {
		gDialog : "",
		
		loadingDialogStart : function() {
			gDialog = bootbox.dialog({
	             title: gTitle.dbResult,
	             message: '<p><i class="fa fa-spin fa-spinner"></i> Loading...</p>'
            });
		},
		
		loadingDialogEnd : function(vTitle,vResult) {
		    gDialog.init(function(){
		             setTimeout(function(){
		            	 gDialog.find('.bootbox-body').html(vResult);
		             }, 3000);
		             setTimeout(function(){
		            	 location.reload();
		             }, 4000);
             });
		},
		prompt : function(vTitle,vFunctionName) {
			bootbox.prompt({
			    title: vTitle,
			    buttons: {
			        cancel: {
			            label: '<i class="fa fa-times"></i> ' + gModalBtn.cancel
			        },
			        confirm: {
			            label: '<i class="fa fa-check"></i> ' + gModalBtn.confirm
			        }
			    },
			    callback: function (result) {
			    	if(result){
			    		if(vTitle == gMessage.insert){
				    		var vNum = Number(result);
					        if(isNaN(vNum) == false) {
					        	if(vNum>0 && vNum <= INSERT_MAX){
					        	   arInsert = gInit.getInsertArry(vNum);
					        	   vFunctionName();
								   gInit.focusScrollEnd();
					        	}else if(vNum == 0) {
				        		   gModal.alert(gTitle.insertException,gMessage.insertException1);
					        	}else{
				        		  gModal.alert(gTitle.insertException,gMessage.replaceMessage(gMessage.insertException3,INSERT_MAX));
					        	}
						   }else{
							   gModal.alert(gTitle.insertException,gMessage.insertException2);
						   }
				    	}
			    	}
			    }
			});
			
		},
		
		alert : function(vTitle,vMessage,vFunctionName) {
			bootbox.alert({
				  title: vTitle,
			      message: vMessage,
			});
		},
		confirm : function(vTitle,vMessage,vFunctionName) {
			bootbox.confirm({
			    title: vTitle,
			    message: vMessage,
			    buttons: {
			        cancel: {
			            label: '<i class="fa fa-times"></i> ' + gModalBtn.cancel
			        },
			        confirm: {
			            label: '<i class="fa fa-check"></i> ' + gModalBtn.confirm
			        }
			    },
			    callback: function (result) {
			        if(result){
						//dbExecute("更新処理","../exb/updateExb")
			        	if(vTitle == gTitle.update　|| vTitle == gTitle.delete){
			        		gDatabase.dbExecute(vTitle,vFunctionName)	
			        	}else if (vTitle == gTitle.backViewlist){
			        		var vHref = "/"
							window.location.href = vHref;
			        	}else if (vTitle == gTitle.csvExport){
			        		 var vHref = document.getElementById('csvExport')
							 window.location.href = vHref;
			        	}else if (vTitle ==gTitle.csvImport){
			        		$("#csvInputFileFrame").trigger( "click" );
			        	}else if (vTitle == gTitle.viewReload){
			        		location.reload();
			        	}else if (vTitle == gTitle.addDeleteException){
			        		var arSelectCloneIndex;
			        		if (vFunctionName == "exb"){
			        			arSelectCloneIndex = gInit.getExbSelectCloneIndex();
			        			gInit.deleteExbCheckFrame(arSelectCloneIndex)
			        		}else{
			        			arSelectCloneIndex = gInit.getSelectCloneIndex();
			        			gInit.deleteHiddenFrame(arSelectCloneIndex)
			        		}
		 					bCheckInsert = gInit.reNewCheckFrame();
		 					gInit.hiddenMenuFrame(bCheckInsert);
			        	}else if (vTitle == gTitle.clickMenuSelectBox){
			        		var result = $('#kanri-category option:selected').val();
							$(location).attr('href', result);
							
			        	}else if(vTitle == gTitle.viewer_manage){
			        	    gViewerManage.manageConfirm(vFunctionName)
			        	}
			        }else{
			        	gInit.inputNameAllClear();
			        	if(vTitle == gTitle.viewer_manage){
			        	   $( ".change-selector" )[0].checked = false;
			        	}
			        }
			    }
			});
		},
}


var gDatabase = {
		resultCheck : function() {
			var vDbResultElement = document.getElementById("dbResult")
			if(vDbResultElement != null){
				var vAlertCloseBtn = document.getElementById("alertClose")
				var vModalElement = document.getElementById("resultModal")
				vAlertCloseBtn.addEventListener('click', function(event) {
					if(vModalElement != null){
						$(vModalElement).remove()
					}
				});

				var vAlertOkBtn = document.getElementById("alertOk")
                vAlertOkBtn.addEventListener('click', function(event) {
                    if(vModalElement != null){
                        $(vModalElement).remove()
                    }
                });

				var vMsgArea= document.getElementsByClassName("modal-body")
				var vResult = vDbResultElement.textContent.trim()
				var vDbMessageElement = document.getElementById("dbMessage")
				var vMsg = "";
				if(vResult == "resultOK"){
					vModalElement.children[0].children[0].className += " alert__result--ok"
					vMsg = gMessage.resultOK
				}else if(vResult == "resultNG"){
					vModalElement.children[0].children[0].className += " alert__result--ng"
					vMsg = gMessage.resultNG + "<br>" + vDbMessageElement.textContent.trim();
				}
				vMsgArea[0].innerHTML = vMsg
			}
		},
		
		dbExecute : function(vTitle,vUrl) {
			gModal.loadingDialogStart();
			var formElement = $("#viewForm")
			
			if(formElement[0] != null){
				if(vTitle == gTitle.delete){
					formElement[0].action = vUrl
				}else if(vTitle == gTitle.update){
					formElement[0].action = vUrl
				}

				 // 送信ボタン生成
                var vButton = document.createElement("button");
                vButton.id = "dbExecuteBtn"
                vButton.className = "btn hidden";
    　			formElement[0].appendChild(vButton);

	    		$("#dbExecuteBtn").trigger( "click" );
			}
		}
}
