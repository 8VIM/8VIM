/*
 * Copyright (C) 2021 Patrick Goldinger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("NOTHING_TO_INLINE")

package inc.flide.vim8.lib.android

import android.content.Context
import androidx.annotation.StringRes
import inc.flide.vim8.lib.kotlin.CurlyArg
import inc.flide.vim8.lib.kotlin.curlyFormat
import kotlin.reflect.KClass

@Throws(NullPointerException::class, ClassCastException::class)
fun <T : Any> Context.systemService(kClass: KClass<T>): T {
    val serviceName = this.getSystemServiceName(kClass.java)!!
    @Suppress("UNCHECKED_CAST")
    return this.getSystemService(serviceName) as T
}


fun <T : Any> Context.systemServiceOrNull(kClass: KClass<T>): T? {
    return try {
        this.systemService(kClass)
    } catch (e: Exception) {
        null
    }
}


@Throws(android.content.res.Resources.NotFoundException::class)
inline fun Context.stringRes(@StringRes id: Int, vararg args: CurlyArg): String {
    return this.resources.getString(id).curlyFormat(*args)
}
