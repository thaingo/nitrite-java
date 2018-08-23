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

package org.dizitart.no2.index;

import org.dizitart.no2.NitriteId;
import org.dizitart.no2.spatial.EqualityType;
import org.locationtech.jts.geom.Geometry;

import java.util.Set;

/**
 *
 * @since 4.0.0
 * @author Anindya Chatterjee
 */
public interface SpatialIndexer extends Indexer<Geometry> {

    Set<NitriteId> findEqual(String field, Geometry geometry, EqualityType equalityType);

    Set<NitriteId> findWithin(String field, Geometry geometry);

    Set<NitriteId> findIntersects(String field, Geometry geometry);
}
