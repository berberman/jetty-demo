package cn.berberman.demo

import org.eclipse.jetty.http.HttpStatus
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.AbstractHandler
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class DemoHandler : AbstractHandler() {
	override fun handle(target: String?, baseRequest: Request?, request: HttpServletRequest?, response: HttpServletResponse?) {
		response?.let {
			it.status = HttpStatus.OK_200
			it.contentType = "text/plain"
			it.writer.print("Hello World!")
			it.writer.flush()
			baseRequest?.isHandled = true
		}
	}
}