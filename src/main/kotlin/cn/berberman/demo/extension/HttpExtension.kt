package cn.berberman.demo.extension

import org.eclipse.jetty.server.Request
import java.util.stream.Collectors
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

fun HttpServletResponse.redirect(url: String) =
		sendRedirect(url)

fun HttpServletRequest.getBody(): String = reader.lines().collect(Collectors.joining(System.lineSeparator()))

inline fun <reified T> HttpServletRequest.getBodyAsObject(onError: (ClassCastException) -> T): T =
		try {
			gson.fromJson(getBody(), T::class.java)
		} catch (e: ClassCastException) {
			onError(e)
		}

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
