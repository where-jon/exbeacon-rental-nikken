
import actors.ItemLogDataDeleteActor
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport
import services.{ItemLogDataDeleteCron, ItemLogDataDeleteService}

/**
 * This class is a Guice module that tells Guice how to bind several
 * different types. This Guice module is created when the Play
 * application starts.

 * Play will automatically use any class called `Module` that is in
 * the root package. You can create modules in other locations by
 * adding `play.modules.enabled` settings to the `application.conf`
 * configuration file.
 */
class ItemLogDataDeleteModule extends AbstractModule with AkkaGuiceSupport{

  override def configure() = {
    // cron設定
    bind(classOf[ItemLogDataDeleteService]).asEagerSingleton()

    // cronで動かすバッチクラスの設定
    bindActor[ItemLogDataDeleteActor]("itemLogDataDeleteActor")
  }
}
