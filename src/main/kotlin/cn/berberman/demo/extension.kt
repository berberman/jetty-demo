package cn.berberman.demo

import com.google.gson.Gson
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.util.log.StdErrLog
import java.util.stream.Collectors
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

fun Request.finish() {
	isHandled = true
}

fun HttpServletResponse.sendText(message: String) {
	contentType = "text/plain"
	writer.print(message)
	writer.flush()
}

fun HttpServletResponse.sendHtml(html: String) {
	contentType = "text/html"
	writer.print(html)
	writer.flush()

}

fun HttpServletResponse.sendJson(json: String) {
	contentType = "application/json"
	writer.print(json)
	writer.flush()
}

inline fun <reified T> HttpServletRequest.getBodyAsObject(onError: (ClassCastException) -> T): T = try {
	gson.fromJson(getBody(), T::class.java)
} catch (e: ClassCastException) {
	onError(e)
}


fun HttpServletRequest.getBody(): String = reader.lines().collect(Collectors.joining(System.lineSeparator()))

inline fun <T> runReturnUnit(block: () -> T) = run { block();Unit }

fun HttpServletResponse.redirect(url: String) {
	sendRedirect(url)
}

fun Any.toJson(): String = gson.toJson(this)

val gson = Gson()
val logger: StdErrLog = StdErrLog.getLogger(HandlerBuilder::class.java)