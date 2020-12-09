package com.lowes.auditor.client.entities.util

import java.util.Optional
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.primaryConstructor

fun <T> mergeData(property: KProperty1<out T, Any?>, left: T, right: T): Any? {
    val leftValue = property.getter.call(left)
    val rightValue = property.getter.call(right)
    return rightValue?.let {
        if ((property.returnType.classifier as KClass<*>).isSubclassOf(Map::class)) (leftValue as? Map<*, *>)?.plus(it as Map<*, *>)
        else leftValue?.merge(it)
    } ?: rightValue ?: leftValue
}

fun <T> lastNonNull(property: KProperty1<out T, Any?>, left: T, right: T) =
    property.getter.call(right) ?: property.getter.call(left)

inline infix fun <reified T : Any> T.merge(other: T): T? {
    val nameToProperty = this::class.declaredMemberProperties.associateBy { it.name }
    val primaryConstructor = this::class.primaryConstructor
    val args: Map<KParameter, Any?>? = primaryConstructor?.parameters?.associateWith { parameter ->
        val property = nameToProperty[parameter.name]
        val type = property?.returnType?.classifier as KClass<*>
        when {
            type.isData || type.isSubclassOf(Map::class) -> mergeData(property, this, other)
            else -> lastNonNull(property, this, other)
        }
    }
    return args?.let { primaryConstructor.callBy(it) }
}

fun <T> T?.orDefault(defaultValue: T): T {
    return this ?: defaultValue
}

fun <T> Optional<T>.getOrNull(): T? {
    return if (this.isPresent) {
        this.get()
    } else {
        null
    }
}
