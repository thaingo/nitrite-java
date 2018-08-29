/*
 *
 * Copyright 2017-2018 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.kno2.filters

import org.dizitart.no2.collection.Filter
import org.dizitart.no2.filters.Filters
import org.dizitart.no2.spatial.EqualityType
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.Point
import kotlin.reflect.KProperty

/**
 * @since 2.1.0
 * @author Anindya Chatterjee
 */

/**
 * Creates an equality filter which matches documents where the value
 * of a field equals the specified [value].
 */
inline infix fun <reified T> String.eq(value: T?): Filter = Filters.eq(this, value)

/**
 * Creates a greater than filter which matches those documents where the value
 * of the value is greater than (i.e. >) the specified [value].
 */
inline infix fun <reified T: Comparable<T>> String.gt(value: T?): Filter = Filters.gt(this, value)

/**
 * Creates a greater equal filter which matches those documents where the value
 * of the value is greater than or equals to (i.e. >=) the specified [value].
 */
inline infix fun <reified T: Comparable<T>> String.gte(value: T?): Filter = Filters.gte(this, value)

/**
 * Creates a lesser than filter which matches those documents where the value
 * of the value is less than (i.e. <) the specified [value].
 */
inline infix fun <reified T: Comparable<T>> String.lt(value: T?): Filter = Filters.lt(this, value)

/**
 * Creates a lesser equal filter which matches those documents where the value
 * of the value is lesser than or equals to (i.e. <=) the specified [value].
 */
inline infix fun <reified T: Comparable<T>> String.lte(value: T?): Filter = Filters.lte(this, value)

/**
 * Creates an in filter which matches the documents where
 * the value of a field equals any value in the specified array of [values].
 */
inline infix fun <reified T: Comparable<T>> String.within(values: Array<T>): Filter = Filters.`in`(this, *values)

/**
 * Creates an in filter which matches the documents where
 * the value of a field equals any value in the specified array of [values].
 */
inline infix fun <reified T: Comparable<T>> String.within(values: Iterable<T>): Filter
        = Filters.`in`(this, *(values.toList().toTypedArray()))

/**
 * Creates an element match filter that matches documents that contain an array
 * value with at least one element that matches the specified [filter].
 */
infix fun String.elemMatch(filter: Filter): Filter = Filters.elemMatch(this, filter)

/**
 * Creates a text filter which performs a text search on the content of the fields
 * indexed with a full-text index.
 */
infix fun String.text(value: String?): Filter = Filters.text(this, value)

/**
 * Creates a string filter which provides regular expression capabilities
 * for pattern matching strings in documents.
 */
infix fun String.regex(value: String?): Filter = Filters.regex(this, value)


inline infix fun <reified T: Geometry> String.within(value: T?): Filter = Filters.within(this, value)

inline infix fun <reified T: Geometry> String.intersects(value: T?): Filter = Filters.intersects(this, value)

inline fun <reified T: Coordinate> String.near(value: T?, distance: Double): Filter = Filters.near(this, value, distance)

inline fun <reified T: Point> String.near(value: T?, distance: Double): Filter = Filters.near(this, value, distance)

inline fun <reified T: Geometry> String.geoEq(value: T?, equalityType: EqualityType): Filter = Filters.geoEq(this, value, equalityType)


/**
 * Creates an and filter which performs a logical AND operation on two filters and selects
 * the documents that satisfy both filters.
 */
inline infix fun <reified T : Filter> Filter.and(filter: T): Filter = Filters.and(this, filter)

/**
 * Creates an or filter which performs a logical OR operation on two filters and selects
 * the documents that satisfy at least one of the filter.
 */
inline infix fun <reified T : Filter> Filter.or(filter: T): Filter = Filters.or(this, filter)

/**
 * Creates a not filter which performs a logical NOT operation on a [filter] and selects
 * the documents that do not satisfy the [filter]. This also includes documents
 * that do not contain the value.
 */
operator fun Filter.not() : Filter = Filters.not(this)

/**
 * Creates an equality filter which matches objects where the value
 * of a property equals the specified [value].
 */
inline infix fun <reified T> KProperty<T?>.eq(value: T?): Filter = Filters.eq(this.name, value)

/**
 * Creates a greater than filter which matches those objects where the value
 * of the property is greater than (i.e. >) the specified [value].
 */
inline infix fun <reified T: Comparable<T>> KProperty<T?>.gt(value: T?): Filter = Filters.gt(this.name, value)

/**
 * Creates a greater equal filter which matches those objects where the value
 * of the property is greater than or equals to (i.e. >=) the specified [value].
 */
inline infix fun <reified T: Comparable<T>> KProperty<T?>.gte(value: T?): Filter = Filters.gte(this.name, value)

/**
 * Creates a lesser than filter which matches those objects where the value
 * of the property is less than (i.e. <) the specified [value].
 */
inline infix fun <reified T: Comparable<T>> KProperty<T?>.lt(value: T?): Filter = Filters.lt(this.name, value)

/**
 * Creates a lesser equal filter which matches those objects where the value
 * of the property is lesser than or equals to (i.e. <=) the specified [value].
 */
inline infix fun <reified T: Comparable<T>> KProperty<T?>.lte(value: T?): Filter = Filters.lte(this.name, value)

/**
 * Creates an in filter which matches the objects where
 * the value of a property equals any value in the specified array of [values].
 */
inline infix fun <reified T: Comparable<T>> KProperty<T?>.within(values: Array<T>): Filter
        = Filters.`in`(this.name, *values)

/**
 * Creates an in filter which matches the objects where
 * the value of a property equals any value in the specified list of [values].
 */
inline infix fun <reified T: Comparable<T>> KProperty<T?>.within(values: Iterable<T>): Filter
        = Filters.`in`(this.name, *(values.toList().toTypedArray()))

/**
 * Creates an element match filter that matches objects that contain an array
 * value with at least one element that matches the specified [filter].
 */
inline infix fun <reified T> KProperty<Iterable<T>?>.elemMatch(filter: Filter): Filter
        = Filters.elemMatch(this.name, filter)

/**
 * Creates a text filter which performs a text search on the content of the property
 * indexed with a full-text index.
 */
infix fun KProperty<String?>.text(value: String?): Filter = Filters.text(this.name, value)

/**
 * Creates a string filter which provides regular expression capabilities
 * for pattern matching strings in objects.
 */
infix fun KProperty<String?>.regex(value: String?): Filter = Filters.regex(this.name, value)

inline infix fun <reified T: Geometry> KProperty<T?>.within(value: T?): Filter = Filters.within(this.name, value)

inline infix fun <reified T: Geometry> KProperty<T?>.intersects(value: T?): Filter = Filters.intersects(this.name, value)

inline fun <reified T: Geometry> KProperty<T?>.near(value: Point, distance: Double): Filter = Filters.near(this.name, value, distance)

inline fun <reified T: Geometry> KProperty<T?>.near(value: Coordinate, distance: Double): Filter = Filters.near(this.name, value, distance)

inline fun <reified T: Geometry> KProperty<T?>.geoEq(value: T?, equalityType: EqualityType = EqualityType.Exact): Filter
    = Filters.geoEq(this.name, value, equalityType)