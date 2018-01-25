package cn.berberman.demo

import org.eclipse.jetty.server.Server

fun Array<String>.main() {
	val server = Server(2333)
	val handler = DemoHandler()
	server.handler = handler
	server.start()
	server.join()
}