package inc.flide.vim8.ime.layout.models.yaml

import android.view.KeyEvent
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.addDeserializer
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import inc.flide.vim8.ime.layout.models.yaml.versions.common.Flags
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll

class FlagsSpec : FunSpec({
    val mapper = ObjectMapper().registerKotlinModule().registerModule(
        SimpleModule(Flags.FlagsDeserializer::class.qualifiedName)
            .addDeserializer(
                Flags::class,
                Flags.FlagsDeserializer()
            )
    )

    context("Deserializing a non array flag when it's a number") {
        test("the number is positive") {
            checkAll(Arb.int(0, 3)) { number ->
                val flag = mapper.readValue<Flags>(number.toString())
                flag.value shouldBe number
            }
        }

        test("the number is negative") {
            val exception = shouldThrow<MismatchedInputException> {
                mapper.readValue<Flags>("-3")
            }
            exception.message shouldBe """flag value must be positive
 at [Source: (String)"-3"; line: 1, column: 1]"""
        }
    }

    context("Deserializing a non array flag when it's a string") {
        test("it's a valid string") {
            val flag = mapper.readValue<Flags>(""""META_SHIFT_ON"""")
            flag.value shouldBe KeyEvent.META_SHIFT_ON
        }

        test("it's an invalid string") {
            val exception =
                shouldThrow<MismatchedInputException> {
                    mapper.readValue<Flags>(""""NOT_A_FLAG"""")
                }
            exception.message shouldBe """unknown meta modifier
 at [Source: (String)""NOT_A_FLAG""; line: 1, column: 1]"""
        }
    }

    test("Deserializing an array flag") {
        val flag = mapper.readValue<Flags>("""["META_SHIFT_ON",1]""")
        val expected = KeyEvent.META_SHIFT_ON or 1
        flag.value shouldBe expected
    }
})
