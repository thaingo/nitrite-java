package org.dizitart.no2.rx;

import io.reactivex.internal.functions.ObjectHelper;
import org.dizitart.no2.Document;
import org.dizitart.no2.collection.DocumentCursor;
import org.dizitart.no2.collection.Lookup;
import org.dizitart.no2.collection.RecordIterable;

import java.util.concurrent.Callable;

/**
 * @author Anindya Chatterjee
 */
public final class FlowableDocumentCursor extends FlowableRecordIterable<Document> {

    private final Callable<DocumentCursor> supplier;

    FlowableDocumentCursor(Callable<DocumentCursor> supplier) {
        super(supplier);
        this.supplier = supplier;
    }

    public FlowableRecordIterable<Document> project(Document projection) {
        Callable<RecordIterable<Document>> projectionSupplier = () -> {
            DocumentCursor documentCursor = ObjectHelper.requireNonNull(supplier.call(),
                    "The supplier supplied is null");
            return documentCursor.project(projection);
        };
        return FlowableRecordIterable.create(projectionSupplier);
    }

    public FlowableRecordIterable<Document> join(FlowableDocumentCursor foreignCursor, Lookup lookup) {
        Callable<RecordIterable<Document>> joinSupplier = () -> {
            DocumentCursor documentCursor = ObjectHelper.requireNonNull(supplier.call(),
                    "The supplier supplied is null");

            DocumentCursor foreignDocumentCursor = ObjectHelper.requireNonNull(foreignCursor.supplier.call(),
                    "The supplier supplied is null");

            return documentCursor.join(foreignDocumentCursor, lookup);
        };
        return FlowableRecordIterable.create(joinSupplier);
    }
}
