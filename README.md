# exbeacon-daidan-web
ダイダン株式会社：建設現場の高所作業車及び仮設材予約システム

PCブラウザ（Chrome）、iPad対応（1024x768）

技術要素
--------

* Play Framework (Scala) 2.5
* PostgreSQL 9.6
* Heroku

主要ブランチ
------------

| ブランチ名 | 目的 | 備考 |
|------------|------|------|
| master | PoC1-本番用最新ブランチ | - |
| develop | 開発・動作確認向けブランチ | - |

Heroku環境
----------

対応するHerokuアプリは以下のとおり。

| アプリ名 | 目的 | 対応ブランチ | Herokuグループ（アカウント） |
|----------|------|--------------|------|
| exbeacon-daidan-web | 本番環境 | master | Whereオーガナイゼーション(予定) |
| exbeacon-daidan-web-stage | 動作確認・検証環境 | develop | developer@where123.jp |

環境変数
--------

本システムを動作させるために必要な環境変数は以下の通り。

| 環境変数 | 意味 | 備考 |
|----------|------|------|
| DATABASE_URL | データベース接続先URL |  |
| BATCH_INTERVAL | 測位APIデータ保存バッチのスケジュール | バッチによって最新のデータを保存する。（履歴は貯めない） |
| BATCH_LOG_LEVEL | 測位APIデータ保存バッチのログ設定(INFO/DEBUG) | INFO=出力する、DEBUG=出力しない |


＜本番用の環境変数の値＞

| 環境変数名 | 値 | 備考 |
|----------|------|------|
| DATABASE_URL | herokuダッシュボード参照 |  |
| ITEMLOG_BATCH_INTERVAL | 0 */30 * ? * * | 施行中現場の位置情報を30分間隔でバッチ実行|
| ITEMLOG_BATCH_START | TRUE=出力する、=出力しない | 状況によって判断 |
| POSITIONIG_COUNTMINUTE | 30 | 作業車稼働率状況画面でバッチ間隔30分と合わせてカウントする |
| POSITIONIG_ABSENCEMINUTE | 30 | 設定「分」単位で不在を判断する |
| POSITIONIG_UPDATESEC | 600 | 現場状況画面自動更新「秒」 |
| VIEW_COUNT | 999 | 現場状況画面最大表示数 |
| PAGE_LINE_COUNT | 20 | ページング１画面毎、表示数 |
| RESERVE_MAX_COUNT | 100 | 作業車・立馬予約取消、その他仮設材予約と取消画面複数選択最大設定値 |
| ITEMLOG_DELETE_BATCH_START_TIME | 0 0 0 1 * ? | 毎月1日0時にバッチ実行 |
| ITEMLOG_DELETE_INTERVAL | 3 | 3ヶ月経過した仮設材ログと予約情報を削除 |
| ITEMLOG_DELETE_BATCH_START | true | ログ削除バッチ実行許可 |
| DELETION_EXCLUSION_SITE | "" | ログ削除しない現場IDをカンマ区切りで設定 |
| NOTICE_MAIL_BATCH_START_TIME | 0 0 9 * * ? | 毎日9時にバッチ実行 |
| NOTICE_MAIL_INTERVAL | 7 | 仮設材ログ削除通知　月末の何日前かを設定 ※4日～20日以下で設定可能 |
| NOTICE_MAIL_BATCH_START | true | メール送信バッチ実行許可 |
| DEVELOP_MAIL_ADDRESS | xxxxxxx@where123.jp | developのメールアドレス |
| NOTICE_MAIL_TEST_DATE | "" | テスト確認用メール送信日算出後の日付を設定　※本番では値無 |


環境構築
--------

### データベース管理
* `conf/evolutions/default/` フォルダ内にスクリプトあり。
    * 1.sql：全テーブルのCREATE および DROP
    * 2.sql：初期データ（マスタデータ）の INSERT および DELETE

### APIインターフェース
* [exbeacon-docomo-chiba-web](https://github.com/whereinc/exbeacon-docomo-chiba-web) と同じ形式

### ローカル用環境変数の設定
conf/application.local.conf を参照
起動方法：activator -Dconfig.resource=application.local.conf run

