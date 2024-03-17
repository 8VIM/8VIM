package inc.flide.vim8.lib

import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

object ZipUtils {
    fun zip(srcDir: File, dstFile: File) {
        check(srcDir.exists() && srcDir.isDirectory) { "Cannot zip standalone file." }
        dstFile.parentFile?.mkdirs()
        dstFile.delete()
        FileOutputStream(dstFile).use { outStream ->
            ZipOutputStream(outStream).use { zipOut ->
                zip(srcDir, zipOut, "")
            }
        }
    }

    private fun zip(srcDir: File, zipOut: ZipOutputStream, base: String) {
        val dir = File(srcDir, base)
        for (file in dir.listFiles() ?: arrayOf()) {
            val path = if (base.isBlank()) file.name else "$base/${file.name}"
            if (file.isDirectory) {
                zipOut.putNextEntry(ZipEntry("$path/"))
                zipOut.closeEntry()
                zip(srcDir, zipOut, path)
            } else {
                zipOut.putNextEntry(ZipEntry(path))
                file.inputStream().use { it.copyTo(zipOut) }
                zipOut.closeEntry()
            }
        }
    }

    fun unzip(srcFile: File, dstDir: File) {
        require(srcFile.exists() && srcFile.isFile) {
            "Given src file `$srcFile` is not valid or a directory."
        }
        dstDir.mkdirs()
        ZipFile(srcFile).use { flexFile ->
            val flexEntries = flexFile.entries()
            while (flexEntries.hasMoreElements()) {
                val flexEntry = flexEntries.nextElement()
                val flexEntryFile = File(dstDir, flexEntry.name).normalize()
                if (flexEntry.isDirectory) {
                    flexEntryFile.mkdir()
                } else {
                    flexFile.copy(flexEntry, flexEntryFile)
                }
            }
        }
    }

    private fun ZipFile.copy(srcEntry: ZipEntry, dstFile: File) {
        dstFile.outputStream().use { outStream ->
            this.getInputStream(srcEntry).use { inStream ->
                inStream.copyTo(outStream)
            }
        }
    }
}
