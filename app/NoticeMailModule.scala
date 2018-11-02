
import actors.NoticeMailActor
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport
import services.NoticeMailService

/**
 * メール送信バッチモジュール
 * playから本モジュールが呼び出されることで、メール送信バッチアクターを起動します。
 * application.confのplay.modulesに本モジュールを定義することで、playから呼び出されます。
 */
class NoticeMailModule extends AbstractModule with AkkaGuiceSupport{

  override def configure() = {
    // cron設定
    bind(classOf[NoticeMailService]).asEagerSingleton()

    // cronで動かすバッチクラスの設定
    bindActor[NoticeMailActor]("noticeMailActor")
  }
}
