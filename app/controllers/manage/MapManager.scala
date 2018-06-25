package controllers.manage

import javax.inject.Inject

import com.mohiva.play.silhouette.api.Silhouette
import controllers.BaseController
import models.MapViewerData
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.ws.WSClient
import utils.silhouette.MyEnv
import play.api.data.Form
import play.api.data.Forms._
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.util.Base64
import javax.imageio.ImageIO
import play.api.libs.json.Json
import java.awt.image.BufferedImage

/**
  * マップ登録クラス.
  */
class MapManager @Inject()(config: Configuration
                           , val silhouette: Silhouette[MyEnv]
                           , val messagesApi: MessagesApi
                           , carDAO: models.carDAO
                           , ws: WSClient
                           , btxDAO: models.btxDAO
                           , mapViewerDAO: models.MapViewerDAO
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


  def uploadMap = SecuredAction(parse.multipartFormData) { implicit request =>
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
      //System.out.println("IMAGE_WIDTH:" + IMAGE_WIDTH);
      //System.out.println("IMAGE_HEIGHT:" + IMAGE_HEIGHT);

      //val scaledByteArray = scaleImage2(byteArray, IMAGE_WIDTH, IMAGE_HEIGHT)
      val scaledByteArray = scaleMoto(byteArray, IMAGE_WIDTH, IMAGE_HEIGHT)

      val b64 = Base64.getEncoder.encodeToString(scaledByteArray)
      val b64img = s"data:image/false;base64,${b64}"
      val in = new ByteArrayInputStream(byteArray)
      val org = ImageIO.read(in)

      System.out.println("11width:" + org.getWidth);
      System.out.println("11height:" + org.getHeight);

      if (1 == mapViewerDAO.updateMapData(mapId.get.toInt, b64img, org.getWidth, org.getHeight)) {
        Redirect("/manage/mapManager").flashing("resultOK" -> Messages("db.update.ok"))
      } else {
        Redirect("/manage/mapManager").flashing("resultNG" -> Messages("error"))
      }
    }.getOrElse {
      Ok(Json.toJson(false))
    }
  }

  private def scaleMoto(bytes: Array[Byte], width: Int, height: Int): Array[Byte] = {
    val in = new ByteArrayInputStream(bytes)
    val org = ImageIO.read(in)
    // 既存サイズにする
    var fixWidth = org.getWidth
    var fixHeight = org.getHeight

    if (fixWidth > 4000 || fixHeight > 4000) {
      val vImgWidth = BigDecimal(fixWidth)
      val vImgHeight = BigDecimal(fixHeight)
      var divideResult = vImgWidth / vImgHeight
      //val vGetAspect = divideResult.setScale(2, scala.math.BigDecimal.RoundingMode.HALF_UP)
      System.out.println("vGetAspect:" + divideResult)
      if (fixWidth > 4000) {
        fixWidth = 4000;
        var v4000 = 4000 / divideResult
        //var divideResult2 = vImgWidth / vImgHeight
        val vGetAspect2 = v4000.setScale(0, scala.math.BigDecimal.RoundingMode.FLOOR)
        System.out.println("v4000:" + vGetAspect2)
        fixHeight = vGetAspect2.intValue()
      } else {
        fixHeight = 4000;
        var v4000 = 4000 / divideResult
        //var divideResult2 = vImgWidth / vImgHeight
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

    if (super.isCmsLogged) {
      // 選択された現場の現場ID
      val placeId = super.getCurrentPlaceId
      // 業者情報
      val carList = carDAO.selectCarInfo(placeId = placeId)


      val mapViewer = mapViewerDAO.selectAll()
      Ok(views.html.manage.mapManager(mapViewerForm, mapViewer))
      //k(views.html.manage.mapManager(carList, placeId))
    } else {
      Redirect(CMS_NOT_LOGGED_RETURN_PATH).flashing(ERROR_MSG_KEY -> Messages("error.cmsLogged.invalid"))
    }
  }

}
