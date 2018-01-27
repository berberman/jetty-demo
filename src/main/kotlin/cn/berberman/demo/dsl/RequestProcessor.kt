package cn.berberman.demo.dsl

import cn.berberman.demo.data.RouteMeta
import cn.berberman.demo.extension.*
import org.eclipse.jetty.server.Request
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Suppress("unused")
class RequestProcessor(var path: String, private val block: RequestProcessor.() -> Unit) {
	private lateinit var baseRequest: Request
	private lateinit var response: HttpServletResponse
	private lateinit var request: HttpServletRequest
	private var pathVariable: String? = null
	var isPathVariable = false
	var status: Int = 200
		set(value) {
			field = value
			response.status = field
		}

	fun sendText(message: String) =
			response.sendText(message)


	fun sendJson(json: String) =
			response.sendJson(json)

	fun sendHtml(html: String) =
			response.sendHtml(html)

	fun redirect(url: String) =
			response.redirect(url)

	fun sendObjectAsJson(any: Any) =
			sendJson(any.toJson())

	fun getBody(): String = request.getBody()

	fun getPathVariable() = pathVariable

	fun getParameter(name: String, onError: () -> String = { "" }) =
			request.getParameter(name) ?: onError()

	inline fun <reified T> getBodyAsObject(onError: (ClassCastException) -> T) =
			accessRequest.getBodyAsObject(onError)

	@PublishedApi
	internal val accessRequest: HttpServletRequest
		get() = request

	private fun finish() = baseRequest.finish()

	operator fun invoke(meta: RouteMeta) {
		baseRequest = meta.baseRequest
		response = meta.response
		request = meta.request
		pathVariable = meta.pathVariable
		block()
//		logger.info(request.getAttribute("javax.servlet.error.exception")?.toString()?:"null")
		finish()
		logger.info("请求处理完成")
	}
}
