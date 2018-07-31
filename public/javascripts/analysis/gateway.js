var vData;
var results;
var arOverTime = []
var secUpdateUnit = 300000 // 5分更新

$(function() {

    function getJson() {
    	var addr = "../analysis/gateway/getdata"
    	$.ajax({
    		cache:false,
            type: "GET",
            url : addr,
            dataType: "json",
            success: function (d) {

            	results = d;
            	for(index = 0 ; index < results.length;++index)
            	{
            		beacon=results[index];
            		 if (beacon.updated != 0 && !isNaN(beacon.updated)) {
                         item = $('#beaconRowTemplate').clone();
                         var item2 = $(item);
                         $(item2).find('.num').text(beacon.num);
                         $(item2).find('.deviceid').text(beacon.deviceid);
                         var vTime = new Date(beacon.updated).toLocaleString("ja");
                         $(item2).find('.updated').text(new Date(beacon.updated).toLocaleString("ja"));
                         if (beacon.updated < (Date.now() - 60*60*24*1000)) {
                             // 24時間以上前
                        	 $(item2).addClass("txWarning");
                        	 var vFirmElement = $(item2).find('.updated')
                        	 vFirmElement[0].title=("最終取得24時間以上前\n"+new Date(beacon.updated).toLocaleString("ja"))
                         } else {
                        	 $(item2).addClass("success");
                         }

                     } else {
                    	 var  rowTemple = $('#beaconRowTemplateNotDetected');
                         var item = rowTemple.clone();
                         var item2 = $(item);

                         $(item2).addClass("danger");
                         $(item2).find('.num').text(beacon.num);
                         $(item2).find('.deviceid').text(beacon.deviceid);
                         $(item2).find('.updated').text("-");
                     }

            		 var vBeaconTableBody = document.getElementById("beaconTableBody");
                     $(vBeaconTableBody).append($(item2));
                     $(item2).removeClass('template hidden');
        		}
            	 $('.beacon-loading').addClass('hidden');
            	 finishUpdate();

            	 hover= [].slice.call(document.querySelectorAll(".txWarning--frame"));
            	 hover.forEach(function(hoverBtn, pos) {
            	 		hoverBtn.addEventListener('mouseover', function() {
            	 			 //$(this).css("color", "red");
            	 		});
            	 		hoverBtn.addEventListener('mouseout', function() {
            	 			  //$(this).css("color", "black");
            	 		});
            	 	});
            },
            error: function (e) {
                console.dir(e);
                gInit.spinAnimationError();
            }
        });
    }
    //vData =Excloud.telemetry();
    btnUpdate = $("#btn-update");
    btnUpdate.on("click", function() {
        gInit.reloadView();
	});
    var updating = false;
    // 5分単位
    setInterval(function() {
	 gInit.reloadView();
	}, secUpdateUnit);

    // メニュー遷移用selectBox
    gTopMenu.setMenuSelect();

	var startUpdate = function() {
		if (!updating) {
			// 仮データでテスト
			// kariGetAllDataJsonMocky();
			gInit.spinAnimationStart();
			getJson();
			updating = true;
		}
	}

	var finishUpdate = function() {
	    gInit.spinAnimationEnd(updating);
		updating = false;
	}

    startUpdate();

});
