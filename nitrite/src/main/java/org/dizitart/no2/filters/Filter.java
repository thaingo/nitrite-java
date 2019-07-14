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

package org.dizitart.no2.filters;

import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.FindOptions;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.index.IndexedQueryTemplate;
import org.dizitart.no2.index.TextIndexer;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.spatial.EqualityType;
import org.dizitart.no2.store.NitriteMap;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

import java.util.Set;

import static org.dizitart.no2.exceptions.ErrorCodes.FE_POINT_NULL;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;

/**
 * An interface to specify filtering criteria during find operation. When
 * a filter is applied to a collection, based on the criteria it returns
 * a set of {@link NitriteId}s of matching records.
 * 
 * Each filtering criteria is based on a value of a document. If the value
 * is indexed, the find operation takes the advantage of it and only scans
 * the index map for that value. But if the value is not indexed, it scans
 * the whole collection.
 * 
 * 
 * The supported filters are:
 * 
 * .Comparison Filters
 * [width="80%",cols="2,4,10"]
 * |===
 * |Filter  |Method   |Description
 * 
 * |Equals
 * |{@link Filter#eq(String, Object)}
 * |Matches values that are equal to a specified value.
 * 
 * |Greater
 * |{@link Filter#gt(String, Comparable)}
 * |Matches values that are greater than a specified value.
 * 
 * |GreaterEquals
 * |{@link Filter#gte(String, Comparable)}
 * |Matches values that are greater than or equal to a specified value.
 * 
 * |Lesser
 * |{@link Filter#lt(String, Comparable)}
 * |Matches values that are less than a specified value.
 * 
 * |LesserEquals
 * |{@link Filter#lte(String, Comparable)}
 * |Matches values that are less than or equal to a specified value.
 * 
 * |In
 * |{@link Filter#in(String, Comparable[])}
 * |Matches any of the values specified in an array.
 * |===
 * 
 * 
 * .Logical Filters
 * [width="80%",cols="2,4,10"]
 * |===
 * |Filter  |Method   |Description
 * 
 * |Not
 * |{@link Filter#not(Filter)}
 * |Inverts the effect of a filter and returns results that do not match the filter.
 * 
 * |Or
 * |{@link Filter#or(Filter[])}
 * |Joins filters with a logical OR returns all ids of the documents that match the conditions
 * of either filter.
 * 
 * |And
 * |{@link Filter#and(Filter[])}
 * |Joins filters with a logical AND returns all ids of the documents that match the conditions
 * of both filters.
 * |===
 * 
 * 
 * .Array Filter
 * [width="80%",cols="2,4,10"]
 * |===
 * |Filter  |Method   |Description
 * 
 * |Element Match
 * |{@link Filter#elemMatch(String, Filter)}
 * |Matches documents that contain an array field with at least one element that matches
 * the specified filter.
 * |===
 * 
 * 
 * .Text Filters
 * [width="80%",cols="2,4,10"]
 * |===
 * |Filter  |Method   |Description
 * 
 * |Text
 * |{@link Filter#text(String, String)}
 * |Performs full-text search.
 * 
 * |Regex
 * |{@link Filter#regex(String, String)}
 * |Selects documents where values match a specified regular expression.
 * |===
 * 
 * 
 * [[app-listing]]
 * [source,java]
 * .Example of various filters
 * --
 * 
 * // returns the ids of the documents whose age field value is 30
 * collection.find(eq("age", 30));
 * 
 * // age field value is greater than 30
 * collection.find(gt("age", 30));
 * 
 * // age field value is not 30
 * collection.find(not(eq("age", 30)));
 * 
 * // age field value is 30 and salary greater than 10K
 * collection.find(and(eq("age", 30), gt("salary", 10000)));
 * 
 * // note field contains the string 'hello'
 * collection.find(regex("note", "hello"));
 * 
 * // prices field contains price value between 10 to 20
 * collection.find(elemMatch("prices", and(gt("$", 10), lt("$", 20))));
 * 
 * --
 * 
 * A nitrite document can contain another document. To specify a field
 * of a nested document a '.' operator is used. If a field is an array
 * or list, array/list index can be used as a field to access a specific
 * element in them.
 * 
 * [[app-listing]]
 * [source,java]
 * .Example of nested document
 * --
 * NitriteMapper nitriteMapper = new JacksonMapper();
 * 
 * // parse a json into a document
 * doc = nitriteMapper.parse("{" +
 * "  score: 1034," +
 * "  location: {  " +
 * "       state: 'NY', " +
 * "       city: 'New York', " +
 * "       address: {" +
 * "            line1: '40', " +
 * "            line2: 'ABC Street', " +
 * "            house: ['1', '2', '3'] " +
 * "       }" +
 * "  }," +
 * "  category: ['food', 'produce', 'grocery'], " +
 * "  objArray: [{ field: 1}, {field: 2}]" +
 * "}");
 * 
 * // insert the doc into collection
 * collection.insert(doc);
 * 
 * // filter on nested document
 * collection.find(eq("location.address.line1", "40"));
 * 
 * // filter on array using array index
 * collection.find(eq("location.address.house.2", "3"));
 * 
 * // filter on object array
 * collection.find(eq("objArray.0.field", 1));
 * 
 * 
 * --
 *
 * @author Anindya Chatterjee
 * @see NitriteCollection#find(Filter, FindOptions) NitriteCollection#find(Filter, FindOptions)
 * @see NitriteCollection#find(Filter) NitriteCollection#find(Filter)
 * @since 1.0
 */
public interface Filter {
    /**
     * A filter to select all elements.
     */
    Filter ALL = null;

    /**
     * Creates an equality filter which matches documents where the value
     * of a field equals the specified value.
     *
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all documents where 'age' field has value as 30
     * collection.find(eq("age", 30));
     * --
     *
     * @param field the value
     * @param value the value
     * @return the equality filter.
     */
    static Filter eq(String field, Object value) {
        return new EqualsFilter(field, value);
    }

    /**
     * Creates an and filter which performs a logical AND operation on two filters and selects
     * the documents that satisfy both filters.
     *
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all documents where 'age' field has value as 30 and
     * // 'name' field has value as John Doe
     * collection.find(and(eq("age", 30), eq("name", "John Doe")));
     * --
     *
     * @param filters operand filters
     * @return the and filter
     */
    static Filter and(Filter... filters) {
        return new AndFilter(filters);
    }

    /**
     * Creates an or filter which performs a logical OR operation on two filters and selects
     * the documents that satisfy at least one of the filter.
     *
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all documents where 'age' field has value as 30 or
     * // 'name' field has value as John Doe
     * collection.find(or(eq("age", 30), eq("name", "John Doe")));
     * --
     *
     * @param filters operand filters
     * @return the or filter
     */
    static Filter or(Filter... filters) {
        return new OrFilter(filters);
    }

    /**
     * Creates a not filter which performs a logical NOT operation on a `filter` and selects
     * the documents that *_do not_* satisfy the `filter`. This also includes documents
     * that do not contain the value.
     *
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all documents where 'age' field has value not equals to 30
     * collection.find(not(eq("age", 30)));
     * --
     *
     * @param filter the filter
     * @return the not filter
     */
    static Filter not(Filter filter) {
        return new NotFilter(filter);
    }

    /**
     * Creates a greater than filter which matches those documents where the value
     * of the value is greater than (i.e. >) the specified value.
     *
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all documents where 'age' field has value greater than 30
     * collection.find(gt("age", 30));
     * --
     *
     * @param field the value
     * @param value the value
     * @return the greater than filter
     */
    static Filter gt(String field, Comparable value) {
        return new GreaterThanFilter(field, value);
    }

    /**
     * Creates a greater equal filter which matches those documents where the value
     * of the value is greater than or equals to (i.e. >=) the specified value.
     *
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all documents where 'age' field has value greater than or equal to 30
     * collection.find(gte("age", 30));
     * --
     *
     * @param field the value
     * @param value the value
     * @return the greater or equal filter
     */
    static Filter gte(String field, Comparable value) {
        return new GreaterEqualFilter(field, value);
    }

    /**
     * Creates a lesser than filter which matches those documents where the value
     * of the value is less than (i.e. <) the specified value.
     *
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all documents where 'age' field has value less than 30
     * collection.find(lt("age", 30));
     * --
     *
     * @param field the value
     * @param value the value
     * @return the lesser than filter
     */
    static Filter lt(String field, Comparable value) {
        return new LesserThanFilter(field, value);
    }

    /**
     * Creates a lesser equal filter which matches those documents where the value
     * of the value is lesser than or equals to (i.e. <=) the specified value.
     *
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all documents where 'age' field has value lesser than or equal to 30
     * collection.find(lte("age", 30));
     * --
     *
     * @param field the value
     * @param value the value
     * @return the lesser equal filter
     */
    static Filter lte(String field, Comparable value) {
        return new LesserEqualFilter(field, value);
    }

    /**
     * Creates a text filter which performs a text search on the content of the fields
     * indexed with a full-text index.
     *
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all documents where 'address' field has value 'roads'.
     * collection.find(text("address", "roads"));
     * --
     *
     * @param field the value
     * @param value the text value
     * @return the text filter
     * @see TextIndexer
     * @see org.dizitart.no2.index.fulltext.TextTokenizer
     */
    static Filter text(String field, String value) {
        return new TextFilter(field, value);
    }

    /**
     * Creates a string filter which provides regular expression capabilities
     * for pattern matching strings in documents.
     *
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all documents where 'name' value starts with 'jim' or 'joe'.
     * collection.find(regex("name", "^(jim|joe).*"));
     * --
     *
     * @param field the value
     * @param value the regular expression
     * @return the regex filter
     */
    static Filter regex(String field, String value) {
        return new RegexFilter(field, value);
    }

    /**
     * Creates an in filter which matches the documents where
     * the value of a field equals any value in the specified array.
     *
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all documents where 'age' field has value in [20, 30, 40]
     * collection.find(in("age", 20, 30, 40));
     * --
     *
     * @param field  the value
     * @param values the range values
     * @return the in filter
     */
    static Filter in(String field, Comparable... values) {
        return new InFilter(field, values);
    }

    /**
     * Creates a notIn filter which matches the documents where
     * the value of a field not equals any value in the specified array.
     *
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all documents where 'age' field has value not in [20, 30, 40]
     * collection.find(notIn("age", 20, 30, 40));
     * --
     *
     * @param field  the value
     * @param values the range values
     * @return the notIn filter
     */
    static Filter notIn(String field, Comparable... values) {
        return new NotInFilter(field, values);
    }

    /**
     * Creates an element match filter that matches documents that contain an array
     * value with at least one element that matches the specified `filter`.
     *
     * [[app-listing]]
     * [source,java]
     * .Example
     * --
     * // matches all documents which has an array field - 'color' and the array
     * // contains a value - 'red'.
     * collection.find(elemMatch("color", eq("$", "red"));
     * --
     *
     * @param field  the value
     * @param filter the filter to satisfy
     * @return the element match filter
     */
    static Filter elemMatch(String field, Filter filter) {
        return new ElementMatchFilter(field, filter);
    }

    static Filter within(String field, Geometry geometry) {
        return new WithinFilter(field, geometry);
    }

    static Filter intersects(String field, Geometry geometry) {
        return new IntersectsFilter(field, geometry);
    }

    static Filter near(String field, Coordinate point, Double distance) {
        return new NearFilter(field, point, distance);
    }

    static Filter near(String field, Point point, Double distance) {
        if (point == null) {
            throw new FilterException(errorMessage("point cannot be null", FE_POINT_NULL));
        }
        return new NearFilter(field, point, distance);
    }

    static Filter geoEq(String field, Geometry geometry, EqualityType equalityType) {
        return new GeoEqualsFilter(field, geometry, equalityType);
    }

    /**
     * Filters a document map and returns the set of {@link NitriteId}s of
     * matching {@link Document}s.
     *
     * @param documentMap the document map
     * @return a set of {@link NitriteId}s of matching documents.
     */
    Set<NitriteId> apply(NitriteMap<NitriteId, Document> documentMap);

    /**
     * Sets {@link IndexedQueryTemplate} in the filter object.
     *
     * @param indexedQueryTemplate the indexed query template
     */
    void setIndexedQueryTemplate(IndexedQueryTemplate indexedQueryTemplate);

    /**
     * Sets {@link NitriteMapper} to the filter.
     *
     * @param nitriteMapper the {@link NitriteMapper}.
     */
    void setNitriteMapper(NitriteMapper nitriteMapper);


}
