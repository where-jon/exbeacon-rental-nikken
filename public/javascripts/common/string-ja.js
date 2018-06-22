var gTitle = {

    // ボタン
    update : "更新処理",
    upsert : "更新処理",
    insert : "追加処理",
    delete : "削除処理",
    viewer_manage : "viewer_manage",


    // 例外
    updateException : "更新処理（エラー）",
    deleteException : "削除処理（エラー）",
    insertException : "追加処理（エラー）",
    addDeleteException : "削除処理（エラー）",

    // db
    dbResult : "処理結果",

    // csv
    csvImport : "インポート処理",
    csvExport : "エクスポート処理",

    // 一覧画面へ遷移
    backViewlist : "一覧画面へ遷移",

    // メニュー遷移
    clickMenuSelectBox : "メニューから画面遷移",

    // 画面再表示
    viewReload : "再読込み",
}

var gMessage = {

    // ボタン
    update : "データを更新しますか？",
    upsert : "何も入力されていない行は削除します。更新してもよろしいですか？",
    insert : "追加する行数を指定してください。（1～10）",
    delete : "選択している行を削除します。よろしいですか？",
    addDelete : "追加した行を選択しています。削除しても、よろしいですか？",

    // 更新例外
    updateException1: "[%s]行目に未入力の欄があります。",
    updateException2: "[%s]行目EXB番号を確認してください。",
    updateException3: "EXB番号[%s]が重複しています。",
    updateException4: "No.[%s]に必須入力項目があります。",
    updateException5: "[%s]行目、機器IDは必須入力項目です。",
    updateException6: "EXB番号「[%s]」が重複しています。",
    updateException8: "追加行の[%s]行目、EXB番号は必須入力項目です。",
    updateExceptionNumber: "[%s]は数字で入力してください。",

    // 削除例外
    deleteException : "削除対象となる行が選択されていません。",
    deleteException1 : "選択した本部に所属している人がいるため、削除できません。<br>該当者全員を他の本部に変更したのち、再度実施してください。",
    importException : "本部に所属している人がいるため、インポートできません。<br>該当者全員を他の本部に変更したのち、再度実施してください。",

    // 追加例外
    insertException1 : "1以上の数字を入力してください。",
    insertException2 : "数字で入力してください。",
    insertException3 : "一度に追加できるのは、最大[%s]行です。",

    // DB結果
    resultOK : "成功しました。",
    resultNG : "エラーが発生しました。",

    // csv
    csvImport : "CSVインポートを行います。よろしいですか？",
    csvExport : "CSVエクスポートを行います。よろしいですか？",


    // 一覧画面へ遷移
    backViewlist : "データが編集された可能性があります。<br>変更を破棄して、画面を移ってもよろしいですか？",

    // メニュー遷移
    clickMenuSelectBox : "データが編集された可能性があります。<br>変更を破棄して、画面を移ってもよろしいですか？",

    // 画面再表示
    viewReload : "データが編集された可能性があります。<br>変更を破棄して、画面を再読込みしてもよろしいですか？",

    // viewer manage
    viewer_manage : "manageします？",


    replaceMessage : function(vMessage,vInputValue) {
      var vMessage = vMessage.replace("[%s]", vInputValue)

      return vMessage
    }

}
var gModalBtn = {
    // ボタン
    confirm : "確認",
    cancel : "取消",

}
var gCsvResult = {

}
