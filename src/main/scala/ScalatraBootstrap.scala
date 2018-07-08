import com.whyisitdoingthat.controllers.{StaticAssetController, WebsocketController}
import javax.servlet.ServletContext
import org.scalatra._

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {

    context.mount(new StaticAssetController, "/")
    context.mount(new WebsocketController, "/ws")
    context.mount(new StaticAssetController, "/static/*")
  }
}
