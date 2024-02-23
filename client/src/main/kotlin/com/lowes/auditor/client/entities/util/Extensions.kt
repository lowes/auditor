package com.lowes.auditor.client.entities.util

import java.util.Optional
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.primaryConstructor

/**
 * Merges the property of left(first) object and right(second) object. In case the property exists on both objects, right's one takes precedence.
 * @param T type of the object
 * @param property instance of [KProperty1]
 * @param left instance of first object
 * @param right instance of second object
 * @return object containing merged property
 */
fun <T> mergeData(
    property: KProperty1<out T, Any?>,
    left: T,
    right: T,
): Any? {
    val leftValue = property.getter.call(left)
    val rightValue = property.getter.call(right)
    return rightValue?.let {
        if ((property.returnType.classifier as KClass<*>).isSubclassOf(Map::class)) {
            (leftValue as? Map<*, *>)?.plus(it as Map<*, *>)
        } else {
            leftValue?.merge(it)
        }
    } ?: rightValue ?: leftValue
}

/**
 * Returns the not null value of the property from right(second) object.
 * If its null then the left(first) object property is returned.
 * If left(first) object property is also null, then return null.
 *
 * @param property instance of [KProperty1]
 * @param left instance of first object
 * @param left instance of second object
 * @return non null value of eithe right object or left object. If both are null, then return null.
 */
fun <T> lastNonNull(
    property: KProperty1<out T, Any?>,
    left: T,
    right: T,
) = property.getter.call(right) ?: property.getter.call(left)

/**
 * Merges all properties of the passed object with caller object.
 * [other] object property takes precedence over caller object.
 * If [other] object property is null, the caller object property is retained.
 * If both [other] and caller object properties are null, the merged property is also null.
 * @param T type of the object which needs to be merged.
 * @param other the passed object which will override the properties
 */
inline infix fun <reified T : Any> T.merge(other: T): T? {
    val nameToProperty = this::class.declaredMemberProperties.associateBy { it.name }
    val primaryConstructor = this::class.primaryConstructor
    val args: Map<KParameter, Any?>? =
        primaryConstructor?.parameters?.associateWith { parameter ->
            val property = nameToProperty[parameter.name]
            val type = property?.returnType?.classifier as KClass<*>
            when {
                type.isData || type.isSubclassOf(Map::class) -> mergeData(property, this, other)
                else -> lastNonNull(property, this, other)
            }
        }
    return args?.let { primaryConstructor.callBy(it) }
}

/**
 * Returns the default value in case caller object is null.
 * @param T type of the object
 * @param defaultValue default value of the object
 * @return returns the caller object if it is not null, [defaultValue] otherwise
 */
fun <T> T?.orDefault(defaultValue: T): T {
    return this ?: defaultValue
}

/**
 * Get the value of the [Optional] object if not null
 * @param T type of the object inside [Optional]
 * @return returns the value if the optional value exists, null otherwise.
 */
fun <T> Optional<T>.getOrNull(): T? {
    return if (this.isPresent) {
        this.get()
    } else {
        null
    }
}
