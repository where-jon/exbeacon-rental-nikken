package controllers.system

import java.awt.image.BufferedImage
import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.nio.file.Files
import java.util.Base64

import com.mohiva.play.silhouette.api.Silhouette
import controllers.{BaseController, site}
import javax.imageio.ImageIO
import javax.inject.Inject
import models.system.MapViewerData
import play.api.Configuration
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import utils.silhouette.MyEnv

/**
  * フロアマップ登録クラス.
  */
class FloorMapController @Inject()(config: Configuration
  , val silhouette: Silhouette[MyEnv]
  , val messagesApi: MessagesApi
  , ws: WSClient
  , floorDAO: models.system.floorDAO
  ) extends BaseController with I18nSupport {

  val IMAGE_WIDTH = config.getInt("web.btxmaster.mapWidth").get
  val IMAGE_HEIGHT = config.getInt("web.btxmaster.mapHeight").get
  var gResult = "none"

  val mapViewerForm = Form(mapping(
    "map_id" -> list(number),
    "map_width" -> list(number),
    "map_height" -> list(number),
    "map_image" -> list(text),
    "map_position" -> list(text)

  )(MapViewerData.apply)(MapViewerData.unapply))


  def uploadFloorMap = SecuredAction(parse.multipartFormData) { implicit request =>
        System.out.println("mapUpload");
        val indexId = request.body.dataParts.get("index_id").map { t => t.head }
        System.out.println(indexId.get.toInt);
        val mapId = request.body.dataParts.get("map_id[" + indexId.get.toInt + "]").map { t =>
          t.head
        }
        request.body.file("map_image[" + indexId.get.toInt + "]").map { picture =>
          val filename = picture.filename
          System.out.println("filename:" + filename);
          val contentType = picture.contentType.get
          val byteArray = Files.readAllBytes(picture.ref.file.toPath)

          if(byteArray.length > 0){
            val scaledByteArray = originScale(byteArray, IMAGE_WIDTH, IMAGE_HEIGHT)

            val b64 = Base64.getEncoder.encodeToString(scaledByteArray)
            val b64img = s"data:image/false;base64,${b64}"
            val in = new ByteArrayInputStream(byteArray)
            val org = ImageIO.read(in)

            System.out.println("11width:" + org.getWidth);
            System.out.println("11height:" + org.getHeight);

            if (1 == floorDAO.updateFloorMap(mapId.get.toInt, b64img, org.getWidth, org.getHeight)) {
              Redirect("/system/floorMap").flashing("resultOK" -> Messages("db.update.ok"))
            } else {
              Redirect("/system/floorMap").flashing("resultNG" -> Messages("error"))
            }
          }else{
            Redirect("/system/floorMap").flashing("resultNG" -> Messages("error.system.mapManager.image.empty"))
          }

        }.getOrElse {
          Ok(Json.toJson(false))
        }

  }

  private def originScale(bytes: Array[Byte], width: Int, height: Int): Array[Byte] = {
    val in = new ByteArrayInputStream(bytes)
    val org = ImageIO.read(in)
    // 既存サイズにする
    var fixWidth = org.getWidth
    var fixHeight = org.getHeight

    if (fixWidth > 4000 || fixHeight > 4000) {
      val vImgWidth = BigDecimal(fixWidth)
      val vImgHeight = BigDecimal(fixHeight)
      var divideResult = vImgWidth / vImgHeight
      System.out.println("vGetAspect:" + divideResult)
      if (fixWidth > 4000) {
        fixWidth = 4000;
        var v4000 = 4000 / divideResult
        val vGetAspect2 = v4000.setScale(0, scala.math.BigDecimal.RoundingMode.FLOOR)
        System.out.println("v4000:" + vGetAspect2)
        fixHeight = vGetAspect2.intValue()
      } else {
        fixHeight = 4000;
        var v4000 = 4000 / divideResult
        val vGetAspect2 = v4000.setScale(0, scala.math.BigDecimal.RoundingMode.FLOOR)
        System.out.println("v4000:" + vGetAspect2)
        fixWidth = vGetAspect2.intValue()
      }

    }

    val resized = org.getScaledInstance(fixWidth, fixHeight, java.awt.Image.SCALE_DEFAULT)
    val dst = new BufferedImage(fixWidth, fixHeight, BufferedImage.TYPE_INT_ARGB)

    val g = dst.createGraphics()
    g.drawImage(resized, 0, 0, null)
    g.dispose

    val b = new ByteArrayOutputStream()
    ImageIO.write(dst, "png", b)
    b.toByteArray
  }

  /** 初期表示 */
  def index = SecuredAction { implicit request =>
    val reqIdentity = request.identity
    if(reqIdentity.level >= 3) {
      val placeId = super.getCurrentPlaceId
      val mapViewer = floorDAO.selectFloorAll(placeId)
      Ok(views.html.system.floorMap(mapViewerForm, mapViewer))
    }else{
      Redirect(site.routes.ItemCarMaster.index)
    }
  }

}
