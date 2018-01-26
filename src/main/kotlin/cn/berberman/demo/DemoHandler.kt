package cn.berberman.demo

import org.eclipse.jetty.http.HttpMethod
import org.eclipse.jetty.http.HttpStatus
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.AbstractHandler
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

data class RouteMeta(val target: String, val baseRequest: Request, val request: HttpServletRequest, val response: HttpServletResponse)

class RequestProcessor(val path: String, private val block: RequestProcessor.() -> Unit) {
	private lateinit var baseRequest: Request
	private lateinit var response: HttpServletResponse
	private lateinit var request: HttpServletRequest

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
		block()
		finish()
		logger.info("请求处理完成")
	}
}

class HandlerBuilder {

	fun get(path: String, block: RequestProcessor.() -> Unit) = runReturnUnit { RequestProcessor(path, block).let(ProcessorHolder.getList::add) }
	fun post(path: String, block: RequestProcessor.() -> Unit) = runReturnUnit { RequestProcessor(path, block).let(ProcessorHolder.postList::add) }
	fun delete(path: String, block: RequestProcessor.() -> Unit) = runReturnUnit { RequestProcessor(path, block).let(ProcessorHolder.deleteList::add) }
	fun put(path: String, block: RequestProcessor.() -> Unit) = runReturnUnit { RequestProcessor(path, block).let(ProcessorHolder.putList::add) }


}

object ProcessorHolder {
	val getList = mutableListOf<RequestProcessor>()
	val putList = mutableListOf<RequestProcessor>()
	val deleteList = mutableListOf<RequestProcessor>()
	val postList = mutableListOf<RequestProcessor>()
}

fun handler(block: HandlerBuilder.() -> Unit) = object : AbstractHandler() {

	init {
		HandlerBuilder().block()
	}

	override fun handle(target: String?, baseRequest: Request?, request: HttpServletRequest?, response: HttpServletResponse?) {
		fun isNull(any: Any?) = any?.equals(null) ?: true
		if (isNull(baseRequest) || isNull(request) || isNull(response) || isNull(target)) return
		val routeMeta = RouteMeta(target!!, baseRequest!!, request!!, response!!)
		val targetRequest = routeMeta.request
		val targetPath = routeMeta.target
		val targetResponse = routeMeta.response
		val method = HttpMethod.fromString(targetRequest.method) ?: null ?: throw ServletException("什么情况？？")
		logger.info("请求:\n目标地址:$targetPath\n请求方法:$method\n请求IP:${targetRequest.remoteAddr}")
		when (method) {
			HttpMethod.GET    -> ProcessorHolder.getList.firstOrNull { it.path == targetPath }?.invoke(routeMeta)
			HttpMethod.POST   -> ProcessorHolder.postList.firstOrNull { it.path == targetPath }?.invoke(routeMeta)
			HttpMethod.PUT    -> ProcessorHolder.putList.firstOrNull { it.path == targetPath }?.invoke(routeMeta)
			HttpMethod.DELETE -> ProcessorHolder.deleteList.firstOrNull { it.path == targetPath }?.invoke(routeMeta)


			else              -> {
				val msg = "未支持$method"
				logger.warn(msg)
				targetResponse.sendText(msg)
				targetResponse.status = HttpStatus.BAD_REQUEST_400
				routeMeta.baseRequest.isHandled = true
			}
		}
	}
}