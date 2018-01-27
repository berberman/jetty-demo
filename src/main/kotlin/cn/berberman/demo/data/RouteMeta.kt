package cn.berberman.demo.data

import org.eclipse.jetty.server.Request
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

data class RouteMeta(val target: String,
                     val baseRequest: Request,
                     val request: HttpServletRequest,
                     val response: HttpServletResponse,
                     var pathVariable: String?)
