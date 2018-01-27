package cn.berberman.demo

import org.eclipse.jetty.http.HttpMethod
import org.eclipse.jetty.http.HttpStatus
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.AbstractHandler
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

data class RouteMeta(val target: String, val baseRequest: Request, val request: HttpServletRequest, val response: HttpServletResponse, var pathVariable: String?)

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
		finish()
		logger.info("请求处理完成")
	}
}

class HandlerBuilder {
	val holder = ProcessorHolder()
	fun get(path: String, block: RequestProcessor.() -> Unit) = runReturnUnit { RequestProcessor(path, block).let(holder.getList::add) }
	fun post(path: String, block: RequestProcessor.() -> Unit) = runReturnUnit { RequestProcessor(path, block).let(holder.postList::add) }
	fun delete(path: String, block: RequestProcessor.() -> Unit) = runReturnUnit { RequestProcessor(path, block).let(holder.deleteList::add) }
	fun put(path: String, block: RequestProcessor.() -> Unit) = runReturnUnit { RequestProcessor(path, block).let(holder.putList::add) }


}

class ProcessorHolder {
	val getList = mutableListOf<RequestProcessor>()
	val putList = mutableListOf<RequestProcessor>()
	val deleteList = mutableListOf<RequestProcessor>()
	val postList = mutableListOf<RequestProcessor>()

	private fun processPathVariable(list: List<RequestProcessor>) {
		list.filter { it.path.endsWith("/{}") }.forEach {
			it.path = it.path.removeSuffix("{}")
			it.isPathVariable = true
			println(it.path)
		}
	}

	fun processPathVariable() {
		processPathVariable(getList)
		processPathVariable(putList)
		processPathVariable(deleteList)
		processPathVariable(postList)
	}
}

fun handler(block: HandlerBuilder.() -> Unit) = object : AbstractHandler() {
	val builder: HandlerBuilder = HandlerBuilder().apply(block)
	val holder: ProcessorHolder

	init {
		builder.holder.processPathVariable()
		holder = builder.holder
	}

	override fun handle(target: String?, baseRequest: Request?, request: HttpServletRequest?, response: HttpServletResponse?) {
		fun isNull(any: Any?) = any?.equals(null) ?: true
		if (isNull(baseRequest) || isNull(request) || isNull(response) || isNull(target)) return
		val routeMeta = RouteMeta(target!!, baseRequest!!, request!!, response!!, null)
		val targetRequest = routeMeta.request
		var targetPath = routeMeta.target
		val method = HttpMethod.fromString(targetRequest.method) ?: null ?: throw ServletException("什么情况？？")
		logger.info("请求:\n目标地址:$targetPath\n请求方法:$method\n请求IP:${targetRequest.remoteAddr}")

		fun RequestProcessor.convertToNormalPath() =
				if (isPathVariable) {
					val path = targetPath.substring(0, path.length)
					val variable = targetPath.substring(path.length, targetPath.length)
					routeMeta.pathVariable = variable
					targetPath = path
				} else Unit

		when (method) {
			HttpMethod.GET    -> holder.getList.firstOrNull { it.apply { convertToNormalPath() }.path == targetPath }?.invoke(routeMeta)
			HttpMethod.POST   -> holder.postList.firstOrNull { it.apply { convertToNormalPath() }.path == targetPath }?.invoke(routeMeta)
			HttpMethod.PUT    -> holder.putList.firstOrNull { it.apply { convertToNormalPath() }.path == targetPath }?.invoke(routeMeta)
			HttpMethod.DELETE -> holder.deleteList.firstOrNull { it.apply { convertToNormalPath() }.path == targetPath }?.invoke(routeMeta)

			else              -> {
				val msg = "未支持$method"
				logger.warn(msg)
				routeMeta.response.apply {
					sendText(msg)
					status = HttpStatus.BAD_REQUEST_400
				}
				routeMeta.baseRequest.isHandled = true
			}
		}
	}
}