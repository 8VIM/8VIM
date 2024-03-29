package inc.flide.vim8.lib.backup

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import arrow.core.left
import arrow.core.right
import arrow.core.some
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import inc.flide.vim8.app.availableLayouts
import inc.flide.vim8.appPreferenceModel
import inc.flide.vim8.datastore.CachedPreferenceModel
import inc.flide.vim8.datastore.model.PreferenceData
import inc.flide.vim8.datastore.model.PreferenceModel
import inc.flide.vim8.ime.layout.AvailableLayouts
import inc.flide.vim8.ime.layout.CustomLayout
import inc.flide.vim8.ime.layout.EmbeddedLayout
import inc.flide.vim8.ime.layout.Layout
import inc.flide.vim8.ime.layout.models.error.ExceptionWrapperError
import inc.flide.vim8.ime.layout.toCustomLayout
import inc.flide.vim8.lib.ZipUtils
import inc.flide.vim8.lib.android.fileUri
import inc.flide.vim8.lib.android.readToFile
import io.kotest.assertions.arrow.core.shouldBeNone
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.assertions.arrow.core.shouldBeSome
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.maps.shouldContainAll
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.clearStaticMockk
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.lang.ref.WeakReference
import java.nio.charset.Charset
import java.nio.file.Files
import java.util.UUID

class BackupManagerSpec : FunSpec({
    lateinit var context: Context
    lateinit var customLayout: CustomLayout
    lateinit var prefExportedKeys: Map<String, Any?>
    var currentVersion = 0
    var tmpCacheDir: File? = null
    var tmpFilesDir: File? = null
    val uuid = UUID(0, 0)
    val objectMapper = JsonMapper.builder().build().registerKotlinModule()

    fun contentStream(): InputStream =
        ByteArrayInputStream("content".toByteArray(Charset.defaultCharset()))

    beforeSpec {
        mockkStatic(ContentResolver::readToFile)
        mockkStatic(Context::fileUri)
        mockkObject(ZipUtils)
        mockkStatic(::appPreferenceModel)
        mockkStatic(String::toCustomLayout)
        mockkStatic(UUID::randomUUID)
        mockkStatic(::availableLayouts)

        every { UUID.randomUUID() } returns uuid
        val uri = mockk<Uri>()
        every { uri.toString() } answers {
            File(
                File(tmpFilesDir, uuid.toString()),
                "custom"
            ).absolutePath
        }
        context = mockk {
            every { cacheDir } answers { tmpCacheDir }
            every { filesDir } answers { tmpFilesDir }
            every { contentResolver } returns mockk {
                every { readToFile(any(), any()) } just Runs
            }
            every { fileUri(any()) } returns uri
        }

        every { appPreferenceModel() } returns CachedPreferenceModel(
            mockk {
                every { version } answers { currentVersion }
                every { exportedKeys } answers { prefExportedKeys }
                every { exportedKeys = any() } propertyType Map::class answers {
                    @Suppress("unchecked_cast")
                    prefExportedKeys = value as Map<String, Any?>
                }
                every { layout } returns mockk {
                    every { current } returns mockk<PreferenceData<Layout<*>>>(relaxed = true) {
                        every { key } returns "prefs_layout_current"
                        every { default } returns mockk<EmbeddedLayout> {
                            every { path } returns "en"
                        }
                    }
                    every { custom } returns mockk {
                        every { history } returns mockk(relaxed = true) {
                            every { get() } returns setOf("custom")
                            every { key } returns "prefs_layout_custom_history"
                        }
                    }
                }
            }
        )
        every { "custom".toCustomLayout() } answers { customLayout }
        every { ZipUtils.zip(any(), any()) } just Runs
        every { ZipUtils.unzip(any(), any()) } just Runs
        every { availableLayouts } returns WeakReference(mockk<AvailableLayouts>(relaxed = true))
    }

    beforeTest {
        customLayout = mockk(relaxed = true) {
            every { defaultName(any()) } returns "test"
            every { md5(any()) } returns "uuid".some()
        }
        tmpCacheDir = Files.createTempDirectory("8vim_cache").toFile()
        tmpFilesDir = Files.createTempDirectory("8vim_files").toFile()
        currentVersion = 0
        prefExportedKeys = emptyMap()
    }

    context("Export") {
        withData(nameFn = { "Current Layout $it" }, "custom", "en") { layout ->
            withData(
                nameFn = { "Content existing: ${it != null}" },
                contentStream(),
                null
            ) { content ->
                prefExportedKeys = mapOf(
                    "prefs_layout_custom_history" to setOf("custom"),
                    "prefs_layout_current" to "${layout.first()}$layout"
                )
                every { customLayout.inputStream(any()) } answers {
                    content?.right() ?: ExceptionWrapperError(Exception("error")).left()
                }
                val manager = BackupManager(context)
                val exportResult = manager.export()
                exportResult.shouldBeRight()

                val dir = File(tmpCacheDir, uuid.toString())

                val settingsFile = File(dir, "settings.json")
                settingsFile.exists().shouldBeTrue()

                val expectedSettings = mapOf(
                    "prefs_layout_custom_history" to (
                        if (content != null) {
                            listOf("custom/uuid_test")
                        } else {
                            emptyList()
                        }
                        ),
                    "prefs_layout_current" to (
                        if (layout == "custom" && content != null) {
                            "ccustom/uuid_test"
                        } else {
                            "een"
                        }
                        )
                )
                val result = objectMapper.readValue<Map<String, Any?>>(settingsFile)
                result shouldContainExactly expectedSettings

                if (content != null) {
                    val file = File(File(dir, "custom"), "uuid_test")
                    file.exists().shouldBeTrue()
                    file.readText(Charset.defaultCharset()) shouldBe "content"
                }
            }
        }
    }

    context("Import") {
        withData(nameFn = { "App version $it" }, 0, 1, 2) { appVersion ->
            withData(nameFn = { "Backup version $it" }, 0, 1) { backupVersion ->
                withData(nameFn = { "Current Layout $it" }, "custom", "en") { layout ->
                    val dstDir = File(tmpFilesDir, uuid.toString())
                    val custom = File(dstDir, "custom").absolutePath
                    dstDir.mkdirs()
                    currentVersion = appVersion
                    val data = mapOf<String, Any?>(
                        PreferenceModel.DATASTORE_VERSION to backupVersion,
                        "prefs_layout_current" to "${layout.first()}$layout",
                        "prefs_layout_custom_history" to listOf("custom")
                    )
                    objectMapper.writeValue(File(dstDir, "settings.json"), data)
                    val manager = BackupManager(context)
                    val res = manager.import(mockk<Uri>(relaxed = true))
                    if (appVersion < backupVersion) {
                        res.shouldBeRight().shouldBeSome()
                    } else {
                        res.shouldBeRight().shouldBeNone()
                        val current = if (layout == "custom") {
                            "c$custom"
                        } else {
                            "een"
                        }
                        prefExportedKeys shouldContainAll mapOf(
                            PreferenceModel.DATASTORE_VERSION to backupVersion,
                            "prefs_layout_current" to current,
                            "prefs_layout_custom_history" to setOf(custom)
                        )
                    }
                }
            }
        }
    }

    afterTest {
        tmpCacheDir?.deleteRecursively()
        tmpFilesDir?.deleteRecursively()
    }

    afterSpec {
        clearStaticMockk(UUID::class, ZipUtils::class, ContentResolver::class)
    }
})
