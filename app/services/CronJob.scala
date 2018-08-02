package services

import javax.inject.{Inject, Named, Singleton}

import akka.actor.{ActorRef, ActorSystem}
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import play.api.inject.ApplicationLifecycle

trait Cron

@Singleton
class CronJob @Inject() (system: ActorSystem
                         , lifeCycle: ApplicationLifecycle
                         , @Named("ItemLogDataActor") itemLogDataActor: ActorRef
                        ) extends Cron {

  // itemLogテーブルデータ保存バッチ起動
  QuartzSchedulerExtension(system).schedule("ItemLogDataActor", itemLogDataActor, "ItemLogDataActor")
}