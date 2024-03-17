package inc.flide.vim8.lib

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.Charset
import java.nio.file.Files

class ZipUtilsSpec : FunSpec({
    var inputDir: File? = null
    var inputSubDir: File? = null
    var outputDir: File? = null
    var outputSubDir: File? = null
    var zipFile: File? = null

    beforeTest {
        inputDir = Files.createTempDirectory("8vim_zip").toFile()
        inputSubDir = File(inputDir, "sub")
        inputSubDir?.mkdir()
        outputDir = Files.createTempDirectory("8vim_zip").toFile()
        outputSubDir = File(outputDir, "sub")
        zipFile = Files.createTempFile("8vim_zip", ".zip").toFile()
    }

    test("Zip and unzip") {
        checkAll<String, String> { first, second ->
            FileOutputStream(File(inputDir, "file")).use {
                it.write(
                    first.toByteArray(
                        Charset.defaultCharset()
                    )
                )
            }
            FileOutputStream(File(inputSubDir, "file")).use {
                it.write(
                    second.toByteArray(
                        Charset.defaultCharset()
                    )
                )
            }
            ZipUtils.zip(inputDir!!, zipFile!!)
            zipFile!!.exists().shouldBeTrue()
            zipFile!!.isFile.shouldBeTrue()
            ZipUtils.unzip(zipFile!!, outputDir!!)
            File(outputDir, "file").readText(Charset.defaultCharset()) shouldBe first
            File(outputSubDir, "file").readText(Charset.defaultCharset()) shouldBe second
        }
    }

    afterTest {
        inputDir?.deleteRecursively()
        outputDir?.deleteRecursively()
    }
})
