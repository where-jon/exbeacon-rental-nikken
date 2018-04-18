package services

import javax.inject.{Inject, Named, Singleton}


import akka.actor.{ActorRef, ActorSystem, Props}
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import play.api.inject.ApplicationLifecycle
import play.api.libs.concurrent.Akka

trait Cron

@Singleton
class CronJob @Inject() (system: ActorSystem
                         , lifeCycle: ApplicationLifecycle
                         , @Named("SaveBtxDataActor") saveBtxDataActor: ActorRef
                        ) extends Cron {

  // BTXデータ保存バッチ起動
  QuartzSchedulerExtension(system).schedule("SaveBtxDataActor", saveBtxDataActor, "SaveBtxDataActor")
}