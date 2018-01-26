package cn.berberman.demo

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.util.log.StdErrLog

fun Array<String>.main() {
	val server = Server(2333)
	server.handler = handler {
		get("/") {
			sendText("2333")
			finish()
		}
		get("/233") {
			sendText("{3")
			finish()
		}
	}
//	server.handler = object : AbstractHandler() {
//		override fun handle(target: String?, baseRequest: Request?, request: HttpServletRequest?, response: HttpServletResponse?) {
//			val logger=StdErrLog.getLogger(this::class.java)
//			logger.info(request!!.pathInfo)
//			logger.info(request!!.pathTranslated)
//			logger.info(request!!.requestURI)
//			logger.info(baseRequest!!.originalURI)
//			logger.info("233")
//			response!!.sendText("233")
//			baseRequest!!.finish()
//		}
//	}
	server.start()
	server.join()
}

val logger: StdErrLog = StdErrLog.getLogger(DslHandlerBuilder::class.java)