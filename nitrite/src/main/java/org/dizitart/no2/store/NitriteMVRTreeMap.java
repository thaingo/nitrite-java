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

package org.dizitart.no2.store;

import org.h2.mvstore.MVMap;
import org.h2.mvstore.rtree.MVRTreeMap;
import org.h2.mvstore.rtree.SpatialKey;
import org.locationtech.jts.geom.Geometry;

import java.util.Iterator;

/**
 *
 * @since 4.0.0
 * @author Anindya Chatterjee
 */
public class NitriteMVRTreeMap extends NitriteMVMap<SpatialKey, Geometry> implements NitriteRTreeMap {
    private final MVRTreeMap<Geometry> mvMap;

    NitriteMVRTreeMap(MVRTreeMap<Geometry> mvMap, NitriteStore nitriteStore) {
        super(mvMap, nitriteStore);
        this.mvMap = mvMap;
    }

    @Override
    public void add(SpatialKey key, Geometry value) {
        mvMap.add(key, value);
    }

    @Override
    public Iterator<SpatialKey> findIntersectingKeys(SpatialKey spatialKey) {
        return mvMap.findIntersectingKeys(spatialKey);
    }

    @Override
    public Iterator<SpatialKey> findContainedKeys(SpatialKey spatialKey) {
        return mvMap.findContainedKeys(spatialKey);
    }

    @Override
    MVMap<SpatialKey, Geometry> getBackingMVMap() {
        return mvMap;
    }
}
