package models.system

import java.sql.SQLException

import anorm.SqlParser._
import anorm._
import javax.inject.Inject
import models.Btx
import play.api.Logger
import play.api.db._

// フォーム定義

case class ExbInfo(
  floorId: Int,
  exbDeviceNo: Int,
  exbDeviceId: Int
)

case class ExbDeviceInfo(
  place_id: Int,
  exbDeviceNo: Int,
  exbDeviceId: Int
)

case class ExbDeleteForm(deleteExbId: String, floorId: String)
case class ExbUpdateForm(
  inputExbId: String
  ,inputDeviceId:String
  ,inputPreDeviceId:String
  ,inputDeviceNo:String
  ,inputDeviceName: String
  ,inputPosName: String
  ,setupFloorId: String
)

case class Exb(
  exb_id: Int,
  exb_device_id: String,
  exb_name: String,
  exb_pos_name: String

)

case class ExbMasterData(
  viewerId: List[Int],
  viewerVisible: List[Boolean],
  viewerPosType: List[Int],
  viewerPosX: List[String],
  viewerPosY: List[String],
  viewerPosMargin: List[Int],
  viewerPosCount: List[Int],
  viewerPosFloor: List[Int],
  viewerPosSize: List[Int],
  viewerPosNum: List[Int]
)

case class ExbApi(
  exb_id: Int,
  exb_device_name: String,
  exb_pos_name: String,
  floor_id: Int,
  cur_floor_name: String
)

case class ExbTotal(
  exb_id: Int,
  exb_device_id: String,
  exb_name: String,
  exb_pos_name: String,
  viewer_visible: Boolean,
  viewer_pos_type: String,
  viewer_pos_count: Int,
  viewer_pos_x: String,
  viewer_pos_y: String,
  viewer_pos_margin: Int,
  viewer_pos_floor: String,
  viewer_pos_size: Int
)

case class ExbData(
  exbId: List[Int],
  exbEditId: List[Int],
  exbDeviceId: List[String],
  exbName: List[String],
  exbPosName: List[String]

)

case class ExbAll(
   exb_id: Int,
   exb_device_id: Int,
   exb_device_no: Int,
   exb_device_name: String,
   exb_pos_name: String,
   exb_pos_x: String,
   exb_pos_y: String,
   exb_view_flag: Boolean,
   view_type_id: Int,
   view_type_name: String,
   view_tx_size: Int,
   view_tx_margin: Int,
   view_tx_count: Int,
   place_id: Int,
   floor_id: Int,
   floor_name: String,
   display_order: Int
)

@javax.inject.Singleton
class ExbDAO @Inject() (dbapi: DBApi) {

  private val db = dbapi.database("default")

  /**
    * EXBマスタ情報の取得
    * @return
    */
  def select(placeId:Int, btxIdList: Seq[Int] = Seq[Int]()): Seq[Btx] = {

    val simple = {
      get[Int]("exb_id") ~
        get[Int]("place_id")  map {
        case exb_id ~ place_id  =>
          Btx(exb_id, place_id)
      }
    }

    db.withConnection { implicit connection =>
      val selectPh =
        """
          select
              b.exb_id
            , b.place_id
          from
            place_master p
            inner join exb_master b
              on p.place_id = b.place_id
          where
            p.active_flg = true
        """

      var wherePh = """ and p.place_id = {placeId} """
      if(btxIdList.isEmpty == false){
        wherePh += s""" and b.exb_id in (${btxIdList.mkString(",")}) """
      }
      val orderPh =
        """
          order by
            b.exb_id, b.place_id
        """
      SQL(selectPh + wherePh + orderPh).on('placeId -> placeId).as(simple.*)
    }
  }

  // Parser
  val simple = {
    get[Int]("exb_id") ~
      get[String]("exb_device_id") ~
      get[String]("exb_name") ~
      get[String]("exb_pos_name") map {
        case exb_id ~ exb_device_id ~ exb_name ~ exb_pos_name =>
          Exb(exb_id, exb_device_id, exb_name, exb_pos_name)
      }
  }

  // Parser
  val simpleTotal = {
    get[Int]("exb_id") ~
      get[String]("exb_device_id") ~
      get[String]("exb_name") ~
      get[String]("exb_pos_name") ~
      get[Boolean]("viewer_visible") ~
      get[String]("viewer_pos_type") ~
      get[Int]("viewer_pos_count") ~
      get[String]("viewer_pos_x") ~
      get[String]("viewer_pos_y") ~
      get[Int]("viewer_pos_margin") ~
      get[String]("viewer_pos_floor") ~
      get[Int]("viewer_pos_size") map {
        case exb_id ~ exb_device_id ~ exb_name ~ exb_pos_name ~ viewer_visible ~ viewer_pos_type ~ viewer_pos_count ~ viewer_pos_x ~ viewer_pos_y ~ viewer_pos_margin ~ viewer_pos_floor ~ viewer_pos_size =>
          ExbTotal(exb_id, exb_device_id, exb_name, exb_pos_name, viewer_visible, viewer_pos_type, viewer_pos_count, viewer_pos_x, viewer_pos_y, viewer_pos_margin, viewer_pos_floor, viewer_pos_size)
      }
  }

  // Parser
  val simpleApi = {
    get[Int]("exb_id") ~
      get[String]("exb_device_name") ~
      get[String]("exb_pos_name") ~
      get[Int]("floor_id") ~
      get[String]("cur_floor_name") map {
      case exb_id ~ exb_device_name ~ exb_pos_name~ floor_id ~ cur_floor_name =>
        ExbApi(exb_id, exb_device_name, exb_pos_name,floor_id, cur_floor_name)
    }
  }
  def selectExbApiInfo(placeId: Int, posId: Int): Seq[ExbApi] = {
    db.withConnection { implicit connection =>
      val sql = SQL(
        """
        select
           e.exb_id
          , e.exb_device_name
          , e.exb_pos_name
          , coalesce(floor.floor_id,-1) as floor_id
          , coalesce(floor.floor_name,'無し') as cur_floor_name
        from
          exb_master e
          left join floor_master floor
          on e.floor_id = floor.floor_id
          and floor.active_flg = true
          where e.place_id = {placeId} and
          e.exb_device_id = {posId}
          order by exb_id;
		          """).on(
        "placeId" -> placeId,
         "posId" -> posId
      )
      sql.as(simpleApi.*)
    }
  }

  /**
    * EXB設置場所番号重複チェック
    * @return
    */
  def exbSetupNumCheck(exbDeviceId:Int, placeId: Int) = {

    db.withConnection { implicit connection =>
      val sql = SQL("""
                      select
                      exb_id,
                      exb_device_id,
                      exb_device_no,
                      exb_device_name,
                      exb_pos_name,
                      exb_pos_x,
                      exb_pos_y,
                      exb_view_flag,
                      exb.view_type_id,
                      v.view_type_name,
                      view_tx_size,
                      view_tx_margin,
                      view_tx_count,
                      exb.place_id,
                      coalesce(exb.floor_id, -1) as floor_id,
                      coalesce(floor.floor_name, '無') as floor_name,
                      coalesce(floor.display_order, -1) as display_order
                    from exb_master as exb
                      left JOIN view_type as v on v.view_type_id = exb.view_type_id and v.active_flg = true
                      left JOIN floor_master as floor on floor.floor_id = exb.floor_id
                    where exb.place_id = {placeId}
                    and exb.exb_device_id ={exbDeviceId}
                    order by exb_id;
       """).on(
        "exbDeviceId" -> exbDeviceId,
        "placeId" -> placeId
      )

      sql.as(simpleExbAll.*)
    }
  }

  /**
    * EXBの更新
    * @return
    */
  def update(inputDeviceId:Int,inputDeviceNo:Int,inputDeviceName:String,inputPosName:String,setupFloorId: Int,exbId: Int,placeId: Int) = {

    db.withTransaction { implicit connection =>
      SQL(
        """
          update exb_master set
              exb_device_id = {inputDeviceId}
            , exb_device_no = {inputDeviceNo}
            , exb_device_name = {inputDeviceName}
            , exb_pos_name = {inputPosName}
            , floor_id = {setupFloorId}
            , updatetime = now()
          where exb_id = {exbId}
          and place_id = {placeId};
        """).on(
        'inputDeviceId -> inputDeviceId,
        'inputDeviceNo -> inputDeviceNo,
        'inputDeviceName -> inputDeviceName,
        'inputPosName -> inputPosName,
        'setupFloorId -> setupFloorId,
        'exbId -> exbId,
        'placeId -> placeId
      ).executeUpdate()
      // コミット
      connection.commit()
      Logger.debug(s"""EXBを更新、ID：" + ${exbId.toString}""")
    }
  }

  /**
    * EXBの新規登録
    * @return
    */
  def insertData(inputDeviceId:Int,inputDeviceNo:Int,inputDeviceName:String,inputPosName:String,setupFloorId: Int,placeId: Int) = {
    db.withTransaction { implicit connection =>
      // フロアマスタへの登録
      val params: Seq[NamedParameter] = Seq(
        "inputDeviceId" -> inputDeviceId
        ,"inputDeviceNo" -> inputDeviceNo
        ,"inputDeviceName" -> inputDeviceName
        ,"inputPosName" -> inputPosName
        ,"setupFloorId" -> setupFloorId
        ,"placeId" -> placeId

      )
      val insertSql = SQL(
        """
           insert into exb_master (exb_device_id, exb_device_no, exb_device_name, exb_pos_name, exb_pos_x, exb_pos_y, exb_view_flag, view_type_id, view_tx_size, view_tx_margin, view_tx_count, place_id, floor_id, updatetime)
           values ({inputDeviceId}, {inputDeviceNo}, {inputDeviceName},{inputPosName},0,0,true,1,25,-1,1,{placeId},{setupFloorId},now())

        """
      ).on(params:_*)

      // SQL実行
      val floorId: Option[Long] = insertSql.executeInsert()
      Logger.debug(s"""フロアを登録、ID：" + ${floorId.get.toInt}""")
    }
  }


  /**
    * EXB削除
    * @return
    */
  def deleteById(exbId:Int): Unit = {
    db.withTransaction { implicit connection =>
      SQL("""delete from exb_master where exb_id = {exbId} ;""").on('exbId -> exbId).executeUpdate()
      // コミット
      connection.commit()

      Logger.debug(s"""EXBを削除、ID：" + ${exbId.toString}""")
    }
  }


  def deleteExb(exb: ExbData): String = {
    var vCheck = false;
    var vResult = "exception"
    db.withTransaction { implicit connection =>
      val statement = connection.createStatement()
      var num = 0
      System.out.println("exb.exbId.length" + exb.exbId.length)
      var vEndPoint = exb.exbId.length - 1;
      for (num <- 0 to vEndPoint) {
        var exb_id = exb.exbId(num);
        val sql = SQL("""
              delete from exb_master
                  where exb_id = {exb_id};
              """).on(
          'exb_id -> exb_id
        )
        try {
          val result = sql.executeUpdate()
          vResult = "success"
        } catch {
          case e: SQLException => {
            println("Database error" + e)
            if (!vCheck) {
              vCheck = true;
              vResult = e + ""
            }
          }
        }
      }
    }
    vResult
  }

  def updateExb(exb: ExbData): String = {
    var vCheck = false;
    var vResult = "exception"
    db.withTransaction { implicit connection =>

      val statement = connection.createStatement()
      var num = 0
      System.out.println("exb.exbId.length" + exb.exbId.length)
      var vEndPoint = exb.exbId.length - 1;

      for (num <- 0 to vEndPoint) {
        var exb_id = exb.exbId(num);
        var exb_edit_id = exb.exbEditId(num);
        var exb_device_id = exb.exbDeviceId(num);
        var exb_name = exb.exbName(num);
        var exb_pos_name = exb.exbPosName(num);

        System.out.println("exb_id" + exb_id)
        System.out.println("exb_edit_id" + exb_edit_id)
        System.out.println("exb_device_id" + exb_device_id)
        System.out.println("exb_name" + exb_name)
        System.out.println("exb_pos_name" + exb_pos_name)
        //var vNum = num + 1;
        val sql = SQL("""
              update exb_master
                  set exb_pos_name = {exb_pos_name},
                  	  exb_device_id = {exb_device_id},
        			  exb_id = {exb_edit_id},
                  	  exb_name = {exb_name}
                  where exb_id = {exb_id};

               INSERT INTO exb_master (exb_id,exb_device_id,exb_name,exb_pos_name)
               select {exb_edit_id}, {exb_device_id}, {exb_name}, {exb_pos_name}

               where not exists (select 1 from exb_master where exb_id = {exb_edit_id});

              """).on(
          'exb_id -> exb_id,
          'exb_edit_id -> exb_edit_id,
          'exb_device_id -> exb_device_id,
          'exb_name -> exb_name,
          'exb_pos_name -> exb_pos_name
        )
        try {
          val result = sql.executeUpdate()
          vResult = "success"
        } catch {
          case e: SQLException => {
            println("Database error " + e)
            if (!vCheck) {
              vCheck = true;
              vResult = e + ""
            }
            // breakPoint.break
          }
        }
      }

    }
    vResult
  }


  /*exbeacn情報を取得 Parser 20180724*/
  val simpleExbAll = {
    get[Int]("exb_id") ~
      get[Int]("exb_device_id") ~
      get[Int]("exb_device_no") ~
      get[String]("exb_device_name") ~
      get[String]("exb_pos_name") ~
      get[String]("exb_pos_x") ~
      get[String]("exb_pos_y") ~
      get[Boolean]("exb_view_flag") ~
      get[Int]("view_type_id") ~
      get[String]("view_type_name") ~
      get[Int]("view_tx_size") ~
      get[Int]("view_tx_margin") ~
      get[Int]("view_tx_count") ~
      get[Int]("place_id") ~
      get[Int]("floor_id")~
      get[String]("floor_name")~
      get[Int]("display_order")map {
        case exb_id ~ exb_device_id ~ exb_device_no ~ exb_device_name ~ exb_pos_name ~ exb_pos_x ~ exb_pos_y ~ exb_view_flag ~ view_type_id ~ view_type_name~ view_tx_size ~ view_tx_margin ~ view_tx_count ~ place_id ~ floor_id ~ floor_name~ display_order =>
          ExbAll(exb_id, exb_device_id, exb_device_no, exb_device_name, exb_pos_name,exb_pos_x, exb_pos_y, exb_view_flag, view_type_id, view_type_name, view_tx_size, view_tx_margin, view_tx_count, place_id,floor_id,floor_name,display_order)
      }
  }


  def updateExbMaster(exbViewer: ExbMasterData): String = {
    var vCheck = false;
    var vResult = "exception"
    db.withTransaction { implicit connection =>

      val statement = connection.createStatement()
      var num = 0
      System.out.println("length" + exbViewer.viewerId.length)
      var vEndPoint = exbViewer.viewerId.length - 1;

      for (num <- 0 to vEndPoint) {
        var viewer_id = exbViewer.viewerId(num);
        var viewer_visible = exbViewer.viewerVisible(num);
        var viewer_pos_type = exbViewer.viewerPosType(num);
        var viewer_pos_x = exbViewer.viewerPosX(num);
        var viewer_pos_y = exbViewer.viewerPosY(num);
        var viewer_pos_margin = exbViewer.viewerPosMargin(num);
        var viewer_pos_count = exbViewer.viewerPosCount(num);
        var viewer_pos_floor = exbViewer.viewerPosFloor(num);
        var viewer_pos_size = exbViewer.viewerPosSize(num);
        var viewer_pos_num = exbViewer.viewerPosNum(num);

        //var vNum = num + 1;
        val sql = SQL("""
              update exb_master
                  set exb_view_flag = {viewer_visible},
                  	  view_type_id = {viewer_pos_type},
        			        exb_pos_x = {viewer_pos_x},
                      exb_pos_y = {viewer_pos_y},
                      view_tx_margin = {viewer_pos_margin},
                      view_tx_count = {viewer_pos_count},
                      floor_id = {viewer_pos_floor},
                      view_tx_size = {viewer_pos_size}
                  where exb_id = {viewer_id};

              """).on(
          'viewer_id -> viewer_id,
          'viewer_visible -> viewer_visible,
          'viewer_pos_type -> viewer_pos_type,
          'viewer_pos_x -> viewer_pos_x,
          'viewer_pos_y -> viewer_pos_y,
          'viewer_pos_margin -> viewer_pos_margin,
          'viewer_pos_count -> viewer_pos_count,
          'viewer_pos_floor -> viewer_pos_floor,
          'viewer_pos_size -> viewer_pos_size
        )
        try {
          val result = sql.executeUpdate()
          vResult = "success"
        } catch {
          case e: SQLException => {
            println("Database error " + e)
            if (!vCheck) {
              vCheck = true;
              vResult = e + ""
            }
            // breakPoint.break
          }
        }
      }

    }
    vResult
  }

  /*exbeacn情報を取得　20180724*/
  def selectExbAll(placeId: Int): Seq[ExbAll] = {
    db.withConnection { implicit connection =>
      val sql = SQL("""
        select
          exb_id,
          exb_device_id,
          exb_device_no,
          exb_device_name,
          exb_pos_name,
          exb_pos_x,
          exb_pos_y,
          exb_view_flag,
          exb.view_type_id,
          v.view_type_name,
          view_tx_size,
          view_tx_margin,
          view_tx_count,
          exb.place_id,
          coalesce(exb.floor_id, -1) as floor_id,
          coalesce(floor.floor_name, '無') as floor_name,
          coalesce(floor.display_order, -1) as display_order
        from exb_master as exb
          left JOIN view_type as v on v.view_type_id = exb.view_type_id and v.active_flg = true
          left JOIN floor_master as floor on floor.floor_id = exb.floor_id
        where exb.place_id = {placeId}
        order by exb_id;
       """).on(
      "placeId" -> placeId
      )

      sql.as(simpleExbAll.*)
    }
  }

}
