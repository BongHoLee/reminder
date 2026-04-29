package com.bong.reminder.support

import java.lang.reflect.Field

fun injectId(entity: Any, id: Long) {
    val field: Field = generateSequence<Class<*>>(entity::class.java) { it.superclass }
        .mapNotNull { runCatching { it.getDeclaredField("id") }.getOrNull() }
        .first()
    field.isAccessible = true
    field.set(entity, id)
}
