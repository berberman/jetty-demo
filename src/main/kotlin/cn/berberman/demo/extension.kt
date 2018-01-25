package cn.berberman.demo

import org.eclipse.jetty.server.Request

fun Request.finish() {
	isHandled = true
}