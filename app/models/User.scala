package models

import utils.silhouette.IdentitySilhouette
import com.mohiva.play.silhouette.password.BCryptPasswordHasher
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import anorm._
import anorm.SqlParser._
import concurrent.Await
import javax.inject.Inject
import play.api.db._
import play.api.Logger

case class User(
    id: Option[Long],
    email: String,
    emailConfirmed: Boolean,
    password: String,
    name: String,
    placeId: Option[Int],
    currentPlaceId: Option[Int],
    isSysMng: Boolean,
    /*
	 * A user can register some accounts from third-party services, then it will have access to different parts of the webpage. The 'master' privilege has full access.
	 * Ex: ("master") -> full access to every point of the webpage.
	 * Ex: ("serviceA") -> have access only to general and serviceA areas.
	 * Ex: ("serviceA", "serviceB") -> have access only to general, serviceA and serviceB areas.
	 */
    services: List[String]
) extends IdentitySilhouette {
  def key = email
}

object User {

  val services = Seq("serviceA", "serviceB", "serviceC")

  var users = scala.collection.mutable.Seq[User]()

  def find(email: String): Option[User] = users.find(_.email == email)

  def update(o: Option[User]): Option[User] = {
    o match {
      case Some(u) => {
        remove(u.email)
        users :+= u
      }
      case None =>
    }
    o
  }

  def remove(email: String): Unit = {
    users = users.filter { u => (u.email != email) }
  }
}

@javax.inject.Singleton
class UserDAO @Inject() (dbapi: DBApi) {

  private val db = dbapi.database("default")

  // Parser
  val simple = {
    get[Int]("user_id") ~
    get[String]("email") ~
      get[String]("name") ~
      get[String]("password") ~
      get[Option[Int]]("place_id") ~ get[Option[Int]]("current_place_id") map {
        case id ~ email ~ name ~ password ~ place_id ~ current_place_id =>
          User(
            Some(id.toString.toLong)
            , email
            , true
            , password
            , name
            , place_id
            , if(current_place_id == None){
                if(place_id == None){
                  Option(1)
                }else{
                  place_id
                }
              }else{
                current_place_id
              }
            , (place_id == None)
            , List("master")
          )
      }
  }

  def findByEmail(email: String): Future[Option[User]] = {
    Future.successful(
      User.find(email) match {
        case Some(u) => Some(u)
        case None => db.withConnection { implicit connection =>
          val sql = SQL("""
            select
                user_id
              , email
              , name
              , password
              , place_id
              , current_place_id
            from
              user_master
            where
              email = {email}
              and active_flg = true
            """).on('email -> email)

          User.update(sql.as(simple.singleOpt))
        }
      }
    )
  }

  def save(user: User): Future[User] = {
    Future.successful(
      db.withConnection { implicit connection =>
//        val sql = SQL("""
//            update user_master set password = {password} where email = {email};
//            insert into user_master (email, name, password)
//                   select {email}, {name}, {password}
//                   where not exists (select 1 from user_master where email = {email});
//          """).on(
//          'email -> user.email,
//          'name -> user.name,
//          'password -> user.password
//        )
//
//        sql.executeUpdate()

        val newUser = User(user.id
          , user.email
          , true
          , user.password
          , user.name
          , user.placeId
          , user.currentPlaceId
          , (user.placeId == None)
          , user.services)
        User.update(Some(newUser)).get
      }
    )
  }

  def remove(email: String): Future[Unit] = {
    Future.successful(
      db.withConnection { implicit connection =>
//        val sql = SQL("""
//            delete from user_master where email = {email};
//          """).on(
//          'email -> email
//        )
//
//        sql.executeUpdate()
//
//        User.remove(email)
      }
    )
  }

}