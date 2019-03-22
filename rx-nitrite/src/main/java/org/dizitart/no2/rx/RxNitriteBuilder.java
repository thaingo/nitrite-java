package org.dizitart.no2.rx;

import com.fasterxml.jackson.databind.Module;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteBuilder;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.index.TextIndexer;
import org.dizitart.no2.index.fulltext.TextTokenizer;

import java.io.File;

/**
 * @author Anindya Chatterjee
 */
public class RxNitriteBuilder {

    private final NitriteBuilder nitriteBuilder;
    private Scheduler scheduler;

    RxNitriteBuilder(NitriteBuilder builder) {
        this.nitriteBuilder = builder;
        this.scheduler = Schedulers.io();
    }

    public RxNitriteBuilder filePath(String path) {
        nitriteBuilder.filePath(path);
        return this;
    }

    public RxNitriteBuilder filePath(File file) {
        nitriteBuilder.filePath(file);
        return this;
    }

    public RxNitriteBuilder autoCommitBufferSize(int size) {
        nitriteBuilder.autoCommitBufferSize(size);
        return this;
    }

    public RxNitriteBuilder readOnly() {
        nitriteBuilder.readOnly();
        return this;
    }

    public RxNitriteBuilder compressed() {
        nitriteBuilder.readOnly();
        return this;
    }

    public RxNitriteBuilder disableAutoCommit() {
        nitriteBuilder.disableAutoCommit();
        return this;
    }

    public RxNitriteBuilder disableAutoCompact() {
        nitriteBuilder.disableAutoCompact();
        return this;
    }

    public RxNitriteBuilder textIndexer(TextIndexer textIndexer) {
        nitriteBuilder.textIndexer(textIndexer);
        return this;
    }

    public RxNitriteBuilder textTokenizer(TextTokenizer textTokenizer) {
        nitriteBuilder.textTokenizer(textTokenizer);
        return this;
    }

    public RxNitriteBuilder nitriteMapper(NitriteMapper nitriteMapper) {
        nitriteBuilder.nitriteMapper(nitriteMapper);
        return this;
    }

    public RxNitriteBuilder disableShutdownHook() {
        nitriteBuilder.disableShutdownHook();
        return this;
    }

    public RxNitriteBuilder registerJacksonModule(Module module) {
        nitriteBuilder.registerJacksonModule(module);
        return this;
    }

    public RxNitriteBuilder scheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
        return this;
    }

    public RxNitrite openOrCreate() {
        Nitrite nitrite = nitriteBuilder.openOrCreate();
        return new RxNitrite(nitrite, scheduler);
    }

    public RxNitrite openOrCreate(String userId, String password) {
        Nitrite nitrite = nitriteBuilder.openOrCreate(userId, password);
        return new RxNitrite(nitrite, scheduler);
    }
}
