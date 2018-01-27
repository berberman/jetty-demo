package cn.berberman.demo

import cn.berberman.demo.data.Result
import cn.berberman.demo.server.SimpleServer

fun Array<String>.main() {
	//TODO 异常处理机制
	SimpleServer(2333) {
		handler {
			get("/apple/{}") {
				getPathVariable()?.let {
					sendObjectAsJson(Result(Result.Code.SUCCESS.code, "success", find(it)))
				}
			}
			post("/apple/create") {
				var apple = getBodyAsObject<Apple?> { null }
				if (apple == null) {
					val size = getParameter("size").toInt()
					val name = getParameter("name")
					apple = Apple(size, name)
				}
				save(apple)
				sendObjectAsJson(Result(Result.Code.SUCCESS.code, "success", apple))
			}
			delete("/apple/delete/{}") {
				getPathVariable()?.let {
					delete(it)
					sendObjectAsJson(Result(Result.Code.SUCCESS.code, "success", Unit))
				}
			}
		}
		startSync()
	}
}

data class Apple(val size: Int, val name: String)

//Simulate DB Operation
fun find(name: String) = Unit

fun save(apple: Apple) = Unit

fun delete(name: String) = Unit