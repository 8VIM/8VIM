package inc.flide.vim8.lib.kotlin

private const val CURLY_ARG_OPEN = '{'
private const val CURLY_ARG_CLOSE = '}'

typealias CurlyArg = Pair<String, Any?>

fun String.curlyFormat(vararg args: CurlyArg): String {
    return this.curlyFormat(args.asList())
}

fun String.curlyFormat(args: List<CurlyArg>): String {
    if (args.isEmpty()) return this
    val sb = StringBuilder(this)
    for ((n, arg) in args.withIndex()) {
        val (argName, argValue) = arg
        sb.formatCurlyArg(n.toString(), argValue)
        sb.formatCurlyArg(argName, argValue)
    }
    return sb.toString()
}

private fun StringBuilder.formatCurlyArg(name: String, value: Any?) {
    if (name.isBlank()) return
    val spec = "$CURLY_ARG_OPEN$name$CURLY_ARG_CLOSE"
    var index = this.lastIndexOf(spec)
    while (index >= 0) {
        val start = index
        val end = index + spec.length
        this.replace(start, end, value.toString())
        index = this.lastIndexOf(spec)
    }
}
