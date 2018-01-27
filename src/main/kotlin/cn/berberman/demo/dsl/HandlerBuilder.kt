package cn.berberman.demo.dsl

import cn.berberman.demo.extension.runReturnUnit

@Suppress("unused")
class HandlerBuilder {
	val holder = ProcessorHolder()

	fun get(path: String, block: RequestProcessor.() -> Unit) = runReturnUnit { RequestProcessor(path, block).let(holder.getList::add) }
	fun post(path: String, block: RequestProcessor.() -> Unit) = runReturnUnit { RequestProcessor(path, block).let(holder.postList::add) }
	fun delete(path: String, block: RequestProcessor.() -> Unit) = runReturnUnit { RequestProcessor(path, block).let(holder.deleteList::add) }
	fun put(path: String, block: RequestProcessor.() -> Unit) = runReturnUnit { RequestProcessor(path, block).let(holder.putList::add) }

}

