package cn.berberman.demo

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.util.log.StdErrLog

fun Array<String>.main() {
	val server = Server(2333)
	server.handler = handler {
		get("/") {
			sendText("2333")
		}
		get("/233") {
			sendText("{3")
		}
		post("/eee") {
			logger.info(getBody())
			sendText("gg")
		}
	}
	server.start()
	server.join()
}

val logger: StdErrLog = StdErrLog.getLogger(DslHandlerBuilder::class.java)