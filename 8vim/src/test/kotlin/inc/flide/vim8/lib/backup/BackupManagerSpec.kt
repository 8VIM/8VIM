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
import inc.flide.vim8.AppPrefs
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
import io.mockk.clearMocks
import io.mockk.clearStaticMockk
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.lang.ref.WeakReference
import java.nio.charset.Charset
import java.nio.file.Files
import java.util.UUID

class BackupManagerSpec : FunSpec({
    val context = mockk<Context>()
    val prefs = mockk<AppPrefs>(relaxed = true)
    val layoutPrefs = mockk<AppPrefs.Layout>()
    val customLayout = mockk<CustomLayout>(relaxed = true)
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

        val history = mockk<PreferenceData<Set<String>>>(relaxed = true)
        val customPrefs = mockk<AppPrefs.Layout.Custom>()
        val current = mockk<PreferenceData<Layout<*>>>(relaxed = true)
        val defaultLayout = mockk<EmbeddedLayout>()
        val contentResolver = mockk<ContentResolver>(relaxed = true)
        val uri = mockk<Uri>()

        every { layoutPrefs.current } returns current
        every { layoutPrefs.custom } returns customPrefs
        every { appPreferenceModel() } returns CachedPreferenceModel(prefs)
        every { customPrefs.history } returns history
        every { context.cacheDir } answers { tmpCacheDir }
        every { context.filesDir } answers { tmpFilesDir }
        every { context.contentResolver } returns contentResolver
        every { contentResolver.readToFile(any(), any()) } just Runs
        every { history.get() } returns setOf("custom")
        every { history.key } returns "prefs_layout_custom_history"
        every { current.key } returns "prefs_layout_current"
        every { current.default } returns defaultLayout
        every { defaultLayout.path } returns "en"
        every { "custom".toCustomLayout() } returns customLayout
        every { ZipUtils.zip(any(), any()) } just Runs
        every { ZipUtils.unzip(any(), any()) } just Runs
        every { availableLayouts } returns WeakReference(mockk<AvailableLayouts>(relaxed = true))
        every { context.fileUri(any()) } returns uri
        every { uri.toString() } answers {
            File(
                File(tmpFilesDir, uuid.toString()),
                "custom"
            ).absolutePath
        }
    }

    beforeTest {
        tmpCacheDir = Files.createTempDirectory("8vim_cache").toFile()
        tmpFilesDir = Files.createTempDirectory("8vim_files").toFile()
        every { prefs.layout } returns layoutPrefs
        every { customLayout.defaultName(any()) } returns "test"
        every { customLayout.md5(any()) } returns "uuid".some()
    }

    context("Export") {
        withData(nameFn = { "Current Layout $it" }, "custom", "en") { layout ->
            withData(
                nameFn = { "Content existing: ${it != null}" },
                contentStream(),
                null
            ) { content ->
                every { prefs.exportedKeys } returns mapOf(
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
                    val keys = slot<Map<String, Any?>>()
                    every { prefs.version } returns appVersion
                    every {
                        prefs.exportedKeys = capture(keys)
                    } propertyType Map::class answers { value }
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
                        keys.captured shouldContainAll mapOf(
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
        clearMocks(customLayout, prefs)
    }

    afterSpec {
        clearStaticMockk(UUID::class, ZipUtils::class, ContentResolver::class)
    }
})
