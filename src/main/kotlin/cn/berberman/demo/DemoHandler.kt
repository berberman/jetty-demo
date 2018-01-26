package cn.berberman.demo

import org.eclipse.jetty.http.HttpMethod
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
			target?.let(::println)
//			it.writer.flush()
			baseRequest?.isHandled = true
		}
	}
}

data class RouteMeta(val target: String, val baseRequest: Request, val request: HttpServletRequest, val response: HttpServletResponse)

class RequestProcessor(val path: String, routeMeta: RouteMeta, private val block: RequestProcessor.() -> Unit) {
	private val baseRequest = routeMeta.baseRequest
	private val response = routeMeta.response
	private val request = routeMeta.request

	var status: Int = 200
		set(value) {
			field = value
			response.status = field
		}

	fun sendText(message: String) {
		response.sendText(message)
	}

	fun getBody() = request.getBody()

	fun finish() = baseRequest.finish()

	operator fun invoke() = block()
}

class DslHandlerBuilder(val routeMeta: RouteMeta) {

	fun get(path: String, block: RequestProcessor.() -> Unit) = runReturnUnit { RequestProcessor(path, routeMeta, block).let(HandlerHolder.getList::add) }
	fun post(path: String, block: RequestProcessor.() -> Unit) = runReturnUnit { RequestProcessor(path, routeMeta, block).let(HandlerHolder.postList::add) }
	fun delete(path: String, block: RequestProcessor.() -> Unit) = runReturnUnit { RequestProcessor(path, routeMeta, block).let(HandlerHolder.deleteList::add) }
	fun put(path: String, block: RequestProcessor.() -> Unit) = runReturnUnit { RequestProcessor(path, routeMeta, block).let(HandlerHolder.putList::add) }


}

object HandlerHolder {
	val getList = mutableListOf<RequestProcessor>()
	val putList = mutableListOf<RequestProcessor>()
	val deleteList = mutableListOf<RequestProcessor>()
	val postList = mutableListOf<RequestProcessor>()

}

fun handler(block: DslHandlerBuilder.() -> Unit) = object : AbstractHandler() {
	override fun handle(target: String?, baseRequest: Request?, request: HttpServletRequest?, response: HttpServletResponse?) {
		fun isNull(any: Any?) = any?.equals(null) ?: true
		if (isNull(baseRequest) || isNull(request) || isNull(response) || isNull(target)) return
		logger.info(target)
		fun DslHandlerBuilder.process() {
			val targetRequest = routeMeta.request
			val targetPath = routeMeta.target
			when (HttpMethod.fromString(targetRequest.method)) {
				HttpMethod.GET    -> HandlerHolder.getList.filter { it.path == targetPath }.firstOrNull()?.invoke()
				HttpMethod.POST   -> HandlerHolder.postList.filter { it.path == targetPath }.firstOrNull()?.invoke()
				HttpMethod.PUT    -> HandlerHolder.putList.filter { it.path == targetPath }.firstOrNull()?.invoke()
				HttpMethod.DELETE -> HandlerHolder.deleteList.filter { it.path == targetPath }.firstOrNull()?.invoke()
				else              -> {
					println("未支持")
				}
			}
		}
		DslHandlerBuilder(RouteMeta(target!!, baseRequest!!, request!!, response!!)).apply(block).process()
		logger.info(HandlerHolder.getList.joinToString())
	}
//TODO 严重问题：会重复创建Processor
}

//class ContextHandlerBuilder {
//	private var rootContext = "/"
//	fun context(url: String, block: DslHandlerBuilder.() -> Unit) {
//
//	}
//}