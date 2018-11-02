
import actors.ItemLogDataDeleteActor
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport
import services.ItemLogDataDeleteService

/**
 * 仮設材ログ削除バッチモジュール
 * playから本モジュールが呼び出されることで、仮設材ログ削除バッチアクターを起動します。
 * application.confのplay.modulesに本モジュールを定義することで、playから呼び出されます。
 */
class ItemLogDataDeleteModule extends AbstractModule with AkkaGuiceSupport{

  override def configure() = {
    // cron設定
    bind(classOf[ItemLogDataDeleteService]).asEagerSingleton()

    // cronで動かすバッチクラスの設定
    bindActor[ItemLogDataDeleteActor]("itemLogDataDeleteActor")
  }
}
