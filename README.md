# exbeacon-daidan-web
ダイダン株式会社：建設現場の高所作業車及び仮設材管理システム

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
| BATCH_INTERVAL | 0 0,10,20,30,40,50 8-23 ? * * | 10分おき, 8:00-24:00に実行|
| BATCH_LOG_LEVEL | INFO=出力する、DEBUG=出力しない | 状況によって判断 |


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

