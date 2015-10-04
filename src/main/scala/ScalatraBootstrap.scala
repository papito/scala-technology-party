import _root_.com.whyisitdoingthat.controllers.StaticAssetController
import com.whyisitdoingthat.controllers.IndexController
import org.scalatra._
import javax.servlet.ServletContext

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    context.mount(new IndexController, "/*")
    context.mount(new StaticAssetController, "/static/*")
  }
}
