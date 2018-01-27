package cn.berberman.demo.dsl

class ProcessorHolder {
	val getList = mutableListOf<RequestProcessor>()
	val putList = mutableListOf<RequestProcessor>()
	val deleteList = mutableListOf<RequestProcessor>()
	val postList = mutableListOf<RequestProcessor>()

	private fun processPathVariable(list: List<RequestProcessor>) {
		list.filter { it.path.endsWith("/{}") }.forEach {
			it.path = it.path.removeSuffix("{}")
			it.isPathVariable = true
		}
	}

	fun processPathVariable() {
		processPathVariable(getList)
		processPathVariable(putList)
		processPathVariable(deleteList)
		processPathVariable(postList)
	}
}