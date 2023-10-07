package inc.flide.vim8.ime.layout.parsers

import arrow.core.Option
import arrow.integrations.jackson.module.registerArrowModule
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import inc.flide.vim8.ime.layout.models.KeyboardData
import java.io.File

interface CacheParser {
    fun load(file: File): Option<KeyboardData>
    fun save(file: File, keyboardData: KeyboardData): Boolean
}

class CborParser : CacheParser {
    private val mapper = CBORMapper()
        .registerKotlinModule()
        .registerArrowModule()

    override fun load(file: File): Option<KeyboardData> = Option.catch {
        mapper.readValue(file)
    }

    override fun save(file: File, keyboardData: KeyboardData): Boolean =
        Option.catch { mapper.writeValue(file, keyboardData) }.isSome()
}
