package cn.berberman.demo

import org.eclipse.jetty.server.Request
import java.util.stream.Collectors
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

fun Request.finish() {
	isHandled = true
}

fun HttpServletResponse.sendText(message: String) {
	contentType = "text/plain"
	writer.print(message)
}

fun HttpServletRequest.getBody() = reader.lines().collect(Collectors.joining(System.lineSeparator()))

inline fun <T> runReturnUnit(block: () -> T) = run { block();Unit }
