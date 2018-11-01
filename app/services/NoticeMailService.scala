package services

import akka.actor.{ActorRef, ActorSystem}
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import javax.inject.{Inject, Named, Singleton}
import play.api.inject.ApplicationLifecycle

@Singleton
class NoticeMailService @Inject()(
                            system: ActorSystem,
                            lifeCycle: ApplicationLifecycle,
                            @Named("noticeMailActor") noticeMailActor: ActorRef
                          ) {
  QuartzSchedulerExtension(system).schedule("noticeMailActor", noticeMailActor, "")
}
