package inc.flide.vim8.ime.layout

import android.content.ContentResolver
import android.content.Context
import android.content.res.Resources
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import arrow.core.None
import arrow.core.right
import inc.flide.vim8.arbitraries.Arbitraries
import inc.flide.vim8.cache
import inc.flide.vim8.ime.layout.models.KeyboardData
import inc.flide.vim8.ime.layout.models.error.ExceptionWrapperError
import inc.flide.vim8.ime.layout.models.info
import inc.flide.vim8.ime.layout.models.yaml.name
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FunSpec
import io.kotest.property.arbitrary.next
import io.mockk.clearMocks
import io.mockk.clearStaticMockk
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import java.io.InputStream
import org.apache.commons.codec.digest.DigestUtils

class LayoutSpec : FunSpec({
    lateinit var context: Context
    lateinit var androidResources: Resources
    lateinit var androidContentResolver: ContentResolver
    lateinit var cache: Cache
    lateinit var inputStream: InputStream
    val layoutLoader = mockk<LayoutLoader>()

    beforeSpec {
        mockkStatic(DigestUtils::class)
        mockkStatic(Context::cache)
        context = mockk(relaxed = true) {
            every { cache() } answers { lazy { cache } }
            every { resources } answers { androidResources }
            every { contentResolver } answers { androidContentResolver }
            every { packageName } returns ""
        }
        every { DigestUtils.md5Hex(any<InputStream>()) } returns ""
    }

    beforeTest {
        androidResources = mockk()
        androidContentResolver = mockk()
        cache = mockk(relaxed = true) {
            every { load(any()) } returns None
        }
        inputStream = mockk(relaxed = true)
    }

    context("Embedded layout") {

        beforeTest {
            every { androidResources.getIdentifier(any(), any(), any()) } returns 0
        }

        context("loading InputStream") {
            test("the resource is found") {
                every { androidResources.openRawResource(any()) } returns inputStream
                EmbeddedLayout("test").inputStream(context) shouldBeRight inputStream
            }

            test("the resource is not found") {
                val exception = Exception("resource not found")
                every { androidResources.openRawResource(any()) } throws exception
                EmbeddedLayout("test").inputStream(context) shouldBeLeft ExceptionWrapperError(
                    exception
                )
            }
        }

        test("loadKeyboardData") {
            val layout = spyk(EmbeddedLayout("en"))
            every { layout.inputStream(any()) } returns inputStream.right()
            val keyboardData = Arbitraries.arbKeyboardData.next()
            every {
                layoutLoader.loadKeyboardData(any())
            } returns keyboardData.right()
            layout.loadKeyboardData(layoutLoader, context) shouldBeRight KeyboardData.info.name.set(
                keyboardData,
                "English"
            )
        }
    }

    context("Custom layout") {
        val uri = mockk<Uri>()
        afterTest { clearMocks(uri) }
        context("loading InputStream") {
            test("the resource is found") {
                every { androidContentResolver.openInputStream(any()) } returns inputStream
                CustomLayout(uri).inputStream(context) shouldBeRight inputStream
            }

            test("the resource is not found") {
                val exception = Exception("resource not found")
                every { androidContentResolver.openInputStream(any()) } throws exception
                CustomLayout(uri).inputStream(context) shouldBeLeft ExceptionWrapperError(
                    exception
                )
            }
        }

        context("loadKeyboardData") {
            val layout = spyk(CustomLayout(uri))
            every { layout.inputStream(any()) } returns inputStream.right()
            val keyboardData = Arbitraries.arbKeyboardData.next()
            every { layoutLoader.loadKeyboardData(any()) } returns keyboardData.right()

            test("scheme is a file") {
                every { uri.scheme } returns "file"
                every { uri.lastPathSegment } returns "file.yaml"
                layout.loadKeyboardData(
                    layoutLoader,
                    context
                ) shouldBeRight KeyboardData.info.name.set(
                    keyboardData,
                    "file.yaml"
                )
            }

            test("scheme is a content") {
                every { uri.scheme } returns "content"
                val cursor = mockk<Cursor>(relaxed = true)
                every {
                    androidContentResolver.query(
                        any(),
                        any(),
                        any(),
                        any(),
                        any()
                    )
                } returns cursor
                every { cursor.count } returns 1
                every { cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME) } returns 0
                every { cursor.getString(any()) } returns "content.yaml"
                every { layoutLoader.loadKeyboardData(any()) } returns keyboardData.right()
                layout.loadKeyboardData(
                    layoutLoader,
                    context
                ) shouldBeRight KeyboardData.info.name.set(
                    keyboardData,
                    "content.yaml"
                )
            }
        }
    }

    afterSpec {
        clearStaticMockk(DigestUtils::class, Context::class)
    }
})
