package cn.berberman.demo

import org.eclipse.jetty.server.Server

fun Array<String>.main() {
	val server = Server(2333)
	//TODO 实现PathVariable
	server.handler = handler {
		get("/") {
			sendText("2333")
		}
		post("/eee") {
			logger.info(getBody())
			sendText("gg")
		}
		post("/json") {
			sendObjectAsJson(arrayOf(Apple(8, "banana"),Apple(-1,"peach")))
			logger.info(getBodyAsObject<Array<Apple>> { throw IllegalArgumentException() }[0].name)
		}
	}
	server.start()
	server.join()
}
data class Apple(val size: Int, val name: String)
