/*
 *
 * Copyright 2017-2019 Nitrite author or authors.
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

package org.dizitart.no2.jmh;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.val;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dizitart.no2.Document;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.IndexOptions;
import org.dizitart.no2.collection.IndexType;
import org.dizitart.no2.collection.objects.ObjectRepository;
import org.dizitart.no2.common.mapper.Mappable;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.index.annotations.Id;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
public class Issue133 {
    private static final Logger LOGGER = LogManager.getLogger(Issue133.class);

    private final static int FORKS = 2;
    private final static int WARMUPS = 2;
    private final static int ITERATIONS = 6;
    private final static int MILLISECONDS = 1000;

    private final static int RANDOM_STRING_MIN_LENGTH = 100;
    private final static int RANDOM_STRING_MAX_LENGTH = 200;

    private final static long INDEXED_INTEGER_FIELD_LOWER_BOUND = 0L;

    @Getter
    @Setter
    @Accessors(fluent = true, chain = true)
    private static class NMappable implements Mappable {

        @Getter
        @Setter
        @Accessors(fluent = true, chain = true)
        private static class IMappable implements Mappable {
            private BigDecimal innerNumber1;
            private BigDecimal innerNumber2;
            private BigDecimal innerNumber3;

            @Override
            public Document write(NitriteMapper mapper) {
                val d = new Document();
                d.put("innerNumber1", innerNumber1);
                d.put("innerNumber2", innerNumber2);
                d.put("innerNumber3", innerNumber3);
                return d;
            }

            @Override
            public void read(NitriteMapper mapper, Document document) {
                innerNumber1 = document.get("innerNumber1", BigDecimal.class);
                innerNumber2 = document.get("innerNumber2", BigDecimal.class);
                innerNumber3 = document.get("innerNumber3", BigDecimal.class);
            }
        }

        @Id
        private String text1;
        private String text2;
        private String text3;
        private BigDecimal decimal1;
        private Long integer1;
        private Boolean flag1;
        private IMappable inner;

        @Override
        public Document write(NitriteMapper mapper) {
            val d = new Document();
            d.put("text1", text1);
            d.put("text2", text2);
            d.put("text3", text3);
            d.put("decimal1", decimal1);
            d.put("integer1", integer1);
            d.put("flag1", flag1);
            d.put("inner", mapper.asDocument(inner));
            return d;
        }

        @Override
        public void read(NitriteMapper mapper, Document document) {
            text1 = document.get("text1", String.class);
            text2 = document.get("text2", String.class);
            text3 = document.get("text3", String.class);
            decimal1 = document.get("decimal1", BigDecimal.class);
            integer1 = document.get("integer1", Long.class);
            flag1 = document.get("flag1", Boolean.class);
            inner = mapper.asObject(document.get("inner", Document.class), IMappable.class);
        }
    }

    private Nitrite nitrite;
    private ObjectRepository<NMappable> repository;

    private List<NMappable> entities;

    @Param({"100000", "200000"})
    private int entityCount;

    @Param({"5000", "10000", "20000"})
    private int indexFieldDispersion;

    @Param({"true", "false"})
    private boolean fetchData;

    @Setup
    public void setup() {
        nitrite = Nitrite.builder().openOrCreate();
        repository = nitrite.getRepository(NMappable.class);
        entities = IntStream.range(0, entityCount)
                .mapToObj(index -> randomEntity())
                .collect(Collectors.toList());
        repository.createIndex("integer1", IndexOptions.indexOptions(IndexType.NonUnique));
        repository.insert(entities.toArray(new NMappable[]{}));
    }

    @TearDown
    public void teardown() {
        repository.close();
    }

    @Benchmark
    @Fork(value = FORKS, jvmArgsAppend = {
            "-Xmx4096m",
            "-Xms2048m"})
    @Warmup(iterations = WARMUPS, timeUnit = TimeUnit.MILLISECONDS, time = MILLISECONDS)
    @Measurement(iterations = ITERATIONS, timeUnit = TimeUnit.MILLISECONDS, time = MILLISECONDS)
    public void benchmarkQueries() {
        val entity = entities.get(RandomUtils.nextInt(0, entityCount));
        val result = repository.find(Filter.eq("integer1", entity.integer1));
        if (fetchData) {
            result.forEach(m -> {
            });
        }
    }

    private NMappable randomEntity() {
        return new NMappable()
                .text1(RandomStringUtils.randomAlphanumeric(RANDOM_STRING_MIN_LENGTH, RANDOM_STRING_MAX_LENGTH))
                .text2(RandomStringUtils.randomAlphanumeric(RANDOM_STRING_MIN_LENGTH, RANDOM_STRING_MAX_LENGTH))
                .text3(RandomStringUtils.randomAlphanumeric(RANDOM_STRING_MIN_LENGTH, RANDOM_STRING_MAX_LENGTH))
                .decimal1(BigDecimal.valueOf(RandomUtils.nextDouble()))
                .integer1(RandomUtils.nextLong(INDEXED_INTEGER_FIELD_LOWER_BOUND, indexFieldDispersion))
                .flag1(RandomUtils.nextBoolean())
                .inner(new NMappable.IMappable()
                        .innerNumber1(BigDecimal.valueOf(RandomUtils.nextLong()))
                        .innerNumber2(BigDecimal.valueOf(RandomUtils.nextDouble()))
                        .innerNumber3(BigDecimal.valueOf(RandomUtils.nextDouble())));
    }

    public static void main(String[] args) throws RunnerException {
        LOGGER.info("Starting benchmark suite");
        Options opt = new OptionsBuilder()
                .include(Issue133.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}
