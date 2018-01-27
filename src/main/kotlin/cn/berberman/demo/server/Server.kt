package cn.berberman.demo.server

import cn.berberman.demo.data.Result
import cn.berberman.demo.data.RouteMeta
import cn.berberman.demo.dsl.HandlerBuilder
import cn.berberman.demo.dsl.ProcessorHolder
import cn.berberman.demo.dsl.RequestProcessor
import cn.berberman.demo.extension.logger
import cn.berberman.demo.extension.runReturnUnit
import cn.berberman.demo.extension.sendJson
import cn.berberman.demo.extension.toJson
import org.eclipse.jetty.http.HttpMethod
import org.eclipse.jetty.http.HttpStatus
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.AbstractHandler
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class SimpleServer(port: Int, block: SimpleServer.() -> Unit) : Server(port) {
	init {
		block()
	}

	fun startSync() {
		start()
		join()
	}

	fun handler(block: HandlerBuilder.() -> Unit) = runReturnUnit {
		handler = object : AbstractHandler() {
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
							sendJson(Result(Result.Code.ERROR.code, msg, Unit).toJson())
							status = HttpStatus.BAD_REQUEST_400
						}
						routeMeta.baseRequest.isHandled = true
					}
				}
			}
		}
	}
}