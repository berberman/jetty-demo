package cn.berberman.demo.extension

import cn.berberman.demo.dsl.HandlerBuilder
import com.google.gson.Gson
import org.eclipse.jetty.util.log.StdErrLog


inline fun <T> runReturnUnit(block: () -> T) = run { block();Unit }


fun Any.toJson(): String = gson.toJson(this)

val gson = Gson()

val logger: StdErrLog = StdErrLog.getLogger(HandlerBuilder::class.java)

