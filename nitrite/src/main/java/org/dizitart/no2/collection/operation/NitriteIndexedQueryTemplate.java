package org.dizitart.no2.collection.operation;

import org.dizitart.no2.index.ComparableIndexer;
import org.dizitart.no2.index.IndexedQueryTemplate;
import org.dizitart.no2.index.SpatialIndexer;
import org.dizitart.no2.index.TextIndexer;

/**
 * @author Anindya Chatterjee.
 */
class NitriteIndexedQueryTemplate implements IndexedQueryTemplate {

    private ComparableIndexer comparableIndexer;
    private TextIndexer textIndexer;
    private SpatialIndexer spatialIndexer;
    private IndexTemplate indexTemplate;

    NitriteIndexedQueryTemplate(IndexTemplate indexTemplate,
                                ComparableIndexer comparableIndexer,
                                TextIndexer textIndexer,
                                SpatialIndexer spatialIndexer) {
        this.comparableIndexer = comparableIndexer;
        this.textIndexer = textIndexer;
        this.indexTemplate = indexTemplate;
        this.spatialIndexer = spatialIndexer;
    }

    @Override
    public ComparableIndexer getComparableIndexer() {
        return comparableIndexer;
    }

    @Override
    public TextIndexer getTextIndexer() {
        return textIndexer;
    }

    @Override
    public SpatialIndexer getSpatialIndexer() {
        return spatialIndexer;
    }

    @Override
    public boolean isIndexing(String field) {
        return indexTemplate.isIndexing(field);
    }

    @Override
    public boolean hasIndex(String field) {
        return indexTemplate.hasIndex(field);
    }
}
