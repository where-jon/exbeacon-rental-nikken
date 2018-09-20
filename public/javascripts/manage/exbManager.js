
$( window ).resize(function() {
    //removeTable();
    //fixTable();
    gInit.setScrollSize("exb")
});

var arInsert = [];
var arCheck = [];
var arSelectPos = [];
var bCheckInsert = false;

var bCheckConfirm = false
var bCheckDelete = false
var bCheckUpdate = false
var bNodataCountCheck
// json処理
function setJson(type) {
	 var bUpdateIdCheck = true;
	 bNodataCountCheck = false;
	 // 共通処理
     var checkLength = document.getElementsByClassName("pos-search").length
     var vTotalElement = document.getElementsByClassName("pos-search")
     // if(checkLength>0){
    	 var vDeletePos;
     	　var vMessage = "default";
    	 var vUrl = "";
    	 //fd.append( 'csrfToken', vToken[0].value);
    	 //stam.append( 'csrfToken', vToken[0].value);
    	 if(type == "insert"){
    	   // 追加処理
    		vMessage = "「追加」"
			vUrl ="../exb/insertExb"
		    for(var i = 0 ; i < arInsert.length;++i ){
 			}

		　}else if (type == "delete" ){
			// 削除処理
			vMessage = "「削除」"
			vUrl ="../exb/deleteExb"
			for(var i = 0 ; i < arSelectPos.length;++i ){
				// editId
        		vTotalElement[arSelectPos[i]].children[2].children[0].name = "exbId[" + i + "]";
			}
		　}else if (type == "update"){
			// 更新処理
			vMessage = "「更新」"
			vUrl ="../exb/updateExb"
				var iCount = 0
				var iEditIndex = 0
				var iEndCount = checkLength;
				var bRoofCheck = true;
				var vCheckId;

				var addLength = document.getElementsByClassName("pos-search common__clone--frame checkClone").length
				var vMotoLength = (checkLength - addLength);


				while(bRoofCheck ){
					// exbEditId
	        		//var vEditIdElement =  document.getElementById("edit-id-" + iCount)

	                var vEditId = Number(vTotalElement[iCount].children[2].children[0].value);

	        		if(vEditId == null || vEditId == ""  || isNaN(vEditId)){
	        			if(iCount < vMotoLength){
	        				bUpdateIdCheck = false;
		        			 //alert("EXB番号を確認してください")
		        			 bCheckConfirm　= false;
		        			 //gMessage.errorState(gTitle.update,"EXB番号を確認してください")
	        				if(isNaN(vEditId)){
	        					gModal.alert(gTitle.updateException,gMessage.replaceMessage(gMessage.updateExceptionNumber,"EXB番号"))
	        				}else{
	        					gModal.alert(gTitle.updateException,gMessage.replaceMessage(gMessage.updateException2,(iCount+1)))
	        				}


		        			break;
	        			}
	        		}else{
	        			if(iCount < vMotoLength){
	        			// editidが変更された時のイベント
	        			vCheckId = gInit.duplicationCheck(vEditId,iCount,".edit-check")
		        			if(vCheckId){
		        				bRoofCheck = false;
		        				bUpdateIdCheck = false;
		        				//alert("EXB番号"　+  vEditId + "が重複の為更新処理できません。")
		        				bCheckConfirm　= false;
		        				//gMessage.errorState(gTitle.update,"EXB番号"　+  vEditId + "が重複の為更新処理できません。")
		        				bNodataCountCheck = true;
		        			}else{
		        				bNodataCountCheck = false;
		        			}
	        			}


	        		}

	        		// 実id
	        		var vId;
	        		var vIdPosElement =  vTotalElement[iCount].children[1].children[0]
	        		if(vIdPosElement.textContent.trim() ==　"" && vEditId != null){
	        			vId = Number(vEditId)
	        		}else{
	        			vId = Number(vIdPosElement.textContent.trim())
	        		}

	        		//vIdPosElement.value = vId;
	        		// deviceId
	        		var vDeviceIdElement = vTotalElement[iCount].children[3].children[0]
	                var vDeviceId = vDeviceIdElement.value;

	        		// exbName
	        		var vExbNameElement =  vTotalElement[iCount].children[4].children[0]
	                var vExbName = vExbNameElement.value;

	        		// posName
	        		var vPosNameElement =  vTotalElement[iCount].children[5].children[0]
	                var vPosName = vPosNameElement.value;

	        		// nullがない場合だけfdに追加
	        		if( vDeviceId!=""){
	        			if(iCount >= vMotoLength){
	        				if(vEditId == "" && (vDeviceId!= "" || vExbName!= "" || vPosName!= "")  ){
        						bUpdateIdCheck = false;
	   		        			 //alert("EXB番号を確認してください")
  		        			 	bCheckConfirm　= false;
	   		        			 //gMessage.errorState(gTitle.update,"EXB番号を確認してください")
  		        			 	var checkPoint = iCount - vMotoLength + 1
  		        			 	gModal.alert(gTitle.updateException,gMessage.replaceMessage(gMessage.updateException8,checkPoint))
	   		        			break;
        					}else{
        						if(isNaN(vEditId)){
        							bUpdateIdCheck = false;
	       		        			 //alert("EXB番号を確認してください")
	       		        			 bCheckConfirm　= false;
	       		        			 //gMessage.errorState(gTitle.update,"EXB番号を確認してください")
	       		        			gModal.alert(gTitle.updateException,gMessage.replaceMessage(gMessage.updateExceptionNumber,"EXB番号"))
	       		        			break;
        						}
        						vCheckId = gInit.duplicationCheck(vEditId,iCount,".edit-check")
	    	        			if(vCheckId){
	    	        				bRoofCheck = false;
	    	        				bUpdateIdCheck = false;
	    	        				//alert("EXB番号"　+  vEditId + "が重複の為更新処理できません。")
	    	        				bCheckConfirm　= false;
	    	        				//gMessage.errorState(gTitle.update,"EXB番号"　+  vEditId + "が重複の為更新処理できません。")
	    	        				bNodataCountCheck = true;
	    	        			}
        					}
	        			}

		        		// form送信
		        		if(vIdPosElement.value ==　"" && vEditId != null){
		        			if(bCheckInsert){
		        				vIdPosElement.value = vId;
		        			}
		        			vIdPosElement.name = "exbId[" + iCount + "]";
		        			vTotalElement[iCount].children[2].children[0].name = "exbEditId[" + iCount + "]";
			        		vDeviceIdElement.name = "exbDeviceId[" + iCount + "]";
			        		vExbNameElement.name = "exbName[" + iCount + "]";
			        		vPosNameElement.name = "exbPosName[" + iCount + "]";
		        		}else{
		        			vIdPosElement.name = "exbId[" + iCount + "]";
		        			vTotalElement[iCount].children[2].children[0].name = "exbEditId[" + iCount + "]";
			        		vDeviceIdElement.name = "exbDeviceId[" + iCount + "]";
			        		vExbNameElement.name = "exbName[" + iCount + "]";
			        		vPosNameElement.name = "exbPosName[" + iCount + "]";
		        		}
		        		// test stam
		        		//testStam(iEditIndex)
		        		iEditIndex++;

	        		}else{
	        			if(iCount < vMotoLength){
        					//alert("リスト"+(iCount + 1) +"行目にnullがあるため更新拒否")
	        				bCheckConfirm　= false;
        					//gMessage.errorState(gTitle.update,"リスト"+(iCount + 1) +"行目にnullがあるため更新拒否")
        					gModal.alert(gTitle.updateException,gMessage.replaceMessage(gMessage.updateException5,(iCount + 1)))
        					bUpdateIdCheck = false;
        					bRoofCheck= false;
        					break;
        				}else{
        					var checkPoint = iCount - vMotoLength + 1
	        				if(vEditId != "" || vDeviceId!= "" || vExbName!= "" || vPosName!= "" ){
	        					if(vEditId == "" ){
	        						bUpdateIdCheck = false;
		   		        			 //alert("EXB番号を確認してください")
	   		        			 	bCheckConfirm　= false;
		   		        			 //gMessage.errorState(gTitle.update,"EXB番号を確認してください")
	   		        			 	gModal.alert(gTitle.updateException,gMessage.replaceMessage(gMessage.updateException2,("「追加行」" + checkPoint)))
		   		        			break;
	        					}
        						vCheckId = gInit.duplicationCheck(vEditId,iCount,".edit-check")
	    	        			if(vCheckId){
	    	        				bRoofCheck = false;
	    	        				bUpdateIdCheck = false;
	    	        				//alert("EXB番号"　+  vEditId + "が重複の為更新処理できません。")
	    	        				bCheckConfirm　= false;
	    	        				//gMessage.errorState(gTitle.update,"EXB番号"　+  vEditId + "が重複の為更新処理できません。")
	    	        				bNodataCountCheck = true;
	    	        			}else{
	    	        				bNodataCountCheck = false;
	    	        				gModal.alert(gTitle.updateException,gMessage.replaceMessage(gMessage.updateException5,"「追加行」" + checkPoint))
	        						bUpdateIdCheck = false;
		        					bRoofCheck= false;
	    	        			}


        					}else{
        						if(!bNodataCountCheck){
        	        				// deleteするものが存在
        		        			bNodataCountCheck = true;
        	        			}
        					}
        				}
	        		}


					//alert("iCount:"+ iCount)
					iCount++;

					if(iCount >= checkLength ) {
						bRoofCheck = false;
					}
				}
	　　　		}

    	 if(bUpdateIdCheck ){

    	 }else{
    		 bRoofCheck = true;
    	 }

	 return bUpdateIdCheck;
}

/* 更新処理 */
function updateBtnEvent() {
	var vUpdateBtn = document.getElementById("update-btn");
	vUpdateBtn.addEventListener('click', function() {
	   bCheckDelete = false;
	   bCheckUpdate = true;
	  arCheck = [];
	  arSelectPos = [];
	  var checkItem = document.getElementsByName("chk_info")
	  for(var i = 0 ; i < checkItem.length;++i ){
		   if(checkItem[i].checked){
			   var vPosElement =  document.getElementById("id-" + i)
			   var vPos = Number(vPosElement.textContent.trim())
			   	// alert(vPos)
			   	arCheck.push(vPos)
			   	arSelectPos.push(i)
		   }
	   }
	  var check = setJson("update");
	  if(check){
		  if(bNodataCountCheck){
			   //gModal.alert(gTitle.update,gMessage.upsert);
			  gModal.confirm(gTitle.update,gMessage.upsert,"../exb/updateExb")
		   }else{
			   //gModal.alert(gTitle.update,gMessage.update);
		      gModal.confirm(gTitle.update,gMessage.update,"../exb/updateExb")
		   }
	   }else{
		   bCheckUpdate = false;
	   }


	});
}

/* 追加処理 */
function addFrame() {
	// 追加の際既存checkBoxは選択できないようにする。
	gInit.hiddenCheckFrame();
	bCheckInsert = true;
	withBack = false;
	checkLength = document.getElementsByClassName("pos-search").length;
	var vIndex = checkLength;

    var vBodyElement = document.getElementById("tBody")

    for(var i = 0 ; i < arInsert.length;++i ){
    	var getElement = $('#cloneElement').clone();
    	var cloneElement = getElement[0]
    	cloneElement.className = "pos-search common__clone--frame checkClone"


    	// check name設置
    	cloneElement.children[0].children[0].name = "chk_info"
		// idNum 設定
        cloneElement.children[1].children[0].id = "id-" +  vIndex

    	// edit idNum 設定
    	cloneElement.children[2].children[0].id = "edit-id-" +  vIndex
    	cloneElement.children[2].children[0].className += " edit-check"
    	// cloneElement.children[2].children[0].value = "edit-id-" + vIndex
    	// deviceNum 設定
    	cloneElement.children[3].children[0].id = "device-id-" +  vIndex
    	// cloneElement.children[3].children[0].value = "device-id-" + vIndex
    	// 端末名 設定
    	cloneElement.children[4].children[0].id = "exb-name-" +  vIndex
    	// cloneElement.children[4].children[0].value = "exb-name-" + vIndex
    	// 設置場所名
    	cloneElement.children[5].children[0].id = "pos-name-" +  vIndex
    	// cloneElement.children[5].children[0].value = "pos-name-" + vIndex

    	vBodyElement.appendChild(cloneElement);
    	++vIndex;
    }
}

/* 追加処理 */
function insertBtnEvent() {
	arInsert = []
	var vInsertBtn = document.getElementById("insert-btn");
	vInsertBtn.addEventListener('click', function() {
		gModal.prompt(gMessage.insert,addFrame);
	});
}

/* 削除処理 */
function deleteBtnEvent() {

	// 削除ボタン
	var vDeleteBtn = document.getElementById("delete-btn");
	vDeleteBtn.addEventListener('click', function() {
		bCheckDelete = true;
		   if(!bCheckInsert){
			   arPkey = gInit.getPkey("exb");
			   arSelectPos = gInit.getSelectIndex();
			   if(arPkey.length>0){
				   var check = setJson("delete");
					if(check){
						//gModal.alert(gTitle.delete,gMessage.delete);
						gModal.confirm(gTitle.delete,gMessage.delete,"../exb/deleteExb");
					}else{
						bCheckDelete = false;
				    }
			   }else{
				   //alert("checkされた列がないです。")
				   //gModal.alert("削除処理","checkされた列がないです。","例外");
				   gModal.alert(gTitle.deleteException,gMessage.deleteException);
			   }
		   }else{
			   var arSelectCloneIndex = gInit.getExbSelectCloneIndex();
				if(arSelectCloneIndex.length>0){
					gModal.confirm(gTitle.addDeleteException,gMessage.addDelete,"exb");
				}else{
					gModal.alert(gTitle.addDeleteException,gMessage.deleteException);
				}
		　　}
	});
}

$(function() {
    // テーブルを固定
    //gInit.fixTable();
	// db結果がある場合
	gDatabase.resultCheck();
    // メニュー遷移用selectBox
    gTopMenu.setMenuSelect();
    gInit.setScrollSize("exb")　
     // checkボタンクリックイベント
    gInit.setCheckEvent();
    // 追加ボタンイベント
    insertBtnEvent()
    // 削除ボタンイベント
    deleteBtnEvent()
    // 更新ボタンイベント
    updateBtnEvent()
    // csv import (画面名)
    //gCsv.csvImportBtnEvent("exbMaster");

    // 画面更新
    gInit.btnViewUpdateEvent();
    // 一覧ボタンイベント
    //gInit.backBtnStopEvent();

});