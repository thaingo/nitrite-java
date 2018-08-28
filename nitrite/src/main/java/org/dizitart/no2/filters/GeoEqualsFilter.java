package org.dizitart.no2.filters;

import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.index.SpatialIndexer;
import org.dizitart.no2.spatial.EqualityType;
import org.dizitart.no2.store.NitriteMap;
import org.locationtech.jts.geom.Geometry;

import java.util.Set;

import static org.dizitart.no2.exceptions.ErrorCodes.FE_GEO_EQ_FILTER_FIELD_NOT_INDEXED;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;

/**
 * @author Anindya Chatterjee.
 */
class GeoEqualsFilter extends SpatialFilter {
    private EqualityType equality;

    protected GeoEqualsFilter(String field, Geometry geometry, EqualityType equality) {
        super(field, geometry);
        this.equality = equality;
    }

    @Override
    public Set<NitriteId> apply(NitriteMap<NitriteId, Document> documentMap) {
        Geometry geometry = getGeometry();

        if (getIndexedQueryTemplate().hasIndex(getField())
            && !getIndexedQueryTemplate().isIndexing(getField())) {
            SpatialIndexer spatialIndexer = getIndexedQueryTemplate().getSpatialIndexer();
            return spatialIndexer.findEqual(getField(), geometry, equality);
        } else {
            throw new FilterException(errorMessage(getField() + " is not indexed",
                FE_GEO_EQ_FILTER_FIELD_NOT_INDEXED));
        }
    }
}
