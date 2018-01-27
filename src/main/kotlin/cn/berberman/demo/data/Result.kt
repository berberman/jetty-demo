package cn.berberman.demo.data

data class Result<out T>(val code: Int,
                         val message: String,
                         val data: T) {
	enum class Code(val code: Int) { SUCCESS(0), ERROR(1) }
}