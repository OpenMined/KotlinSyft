import java.io.File

val file = File("/tmp/whatisthis/thisismyfile.txt")
file.parentFile?.let {
    if (!it.exists()) it.mkdirs()
} ?:
file.useLines{"Content"})

println(file.absolutePath)