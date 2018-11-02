package services

import akka.actor.{ActorRef, ActorSystem}
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import javax.inject.{Inject, Named, Singleton}
import play.api.inject.ApplicationLifecycle

@Singleton
class ItemLogDataDeleteService @Inject() (
                            system: ActorSystem,
                            lifeCycle: ApplicationLifecycle,
                            @Named("itemLogDataDeleteActor") itemLogDataDeleteActor: ActorRef
                          ) {
  QuartzSchedulerExtension(system).schedule("itemLogDataDeleteActor", itemLogDataDeleteActor, "")
}
