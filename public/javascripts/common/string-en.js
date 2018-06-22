var gTitle = {

    // ボタン
    update : "update",
    upsert : "update",
    insert : "insert",
    delete : "delete",
    viewer_manage : "viewer_manage",


    // 例外
    updateException : "update(error)",
    deleteException : "delete(error)",
    insertException : "insert(error)",
    addDeleteException : "delete(error)",

    // db
    dbResult : "db Result",

    // csv
    csvImport : "CSV Import",
    csvExport : "CSV Export",

    // 一覧画面へ遷移
    backViewlist : "View List",

    // メニュー遷移
    clickMenuSelectBox : "Menu > Select Box",

    // 画面再表示
    viewReload : "Reload",
}

var gMessage = {

    // ボタン
     update : "Do you want to update the data?",
     upsert : "Empty rows will be deleted . Would you like to update it?",
    insert : "Please specify the number of rows to be added (1 to 10)",
    delete : "The selected rows will be deleted. Are you sure?",
    addDelete : "Added rows have been selected. Are you sure you want to delete them?",

    // 更新例外
    updateException1: "There is no input in line [%s]",
    updateException2: "Please check the EXB number in line[%s]",
    updateException3: "EXB number [%s] is duplicated.",
    updateException4: "Input item for No. [%s] is compulsory.",
    updateException5: "Device ID input is mandatory for line [%s]",
    updateException6: "EXB number「[%s]」is duplicated EXB番号",
    updateException8: "EXB number is a required for added row [%s]",
    updateExceptionNumber: "Please input a number for this line [%s]",



    // 削除例外
    deleteException : "the row to be deleted is not selected",
    deleteException1 : "There are people in the selected department, so it can not be deleted.  <br> Please assign them to other departments, and try again.",
    importException : "Import is not allowed because there are some people in that department.  <br> Please assign them to other departments,  and try agai",

    // 追加例外
    insertException1 : "Please enter a number greater than 1",
    insertException2 : "Please ente a numbers",
    insertException3 : "一度に追加できるのは、最大[%s]行です。",

    // DB結果
    resultOK : "result OK",
    resultNG : "result NG",

    // csv
    csvImport : "CSV will be imported, are you sure?",
    csvExport : "CSV will be exported, are you sure?",

    // 一覧画面へ遷移
    backViewlist : "The data might have been edited.  <br> Do you want to discard changes and leave this screen?",

    // メニュー遷移
    clickMenuSelectBox : "The data might have been edited. <br> Do you want to discard changes and leave this screen?",

    // 画面再表示
    viewReload : "The data might have been edited.  <br>  Do you want to discard your changes and reload the screen?",

    // viewer manage
    viewer_manage : "manage OK？",


    replaceMessage : function(vMessage,vInputValue) {
      var vMessage = vMessage.replace("[%s]", vInputValue)

      return vMessage
    }

}
var gModalBtn = {
    // ボタン
    confirm : "CONFIRM",
    cancel : "CANCEL",

}
var gCsvResult = {

}