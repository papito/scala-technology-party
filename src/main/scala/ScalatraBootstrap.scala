import com.whyisitdoingthat.controllers.{FancyAsyncController, StaticAssetController}
import org.scalatra._
import javax.servlet.ServletContext

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    context.mount(new StaticAssetController, "/")
    context.mount(new FancyAsyncController, "/ws")
    context.mount(new StaticAssetController, "/static/*")
  }
}
