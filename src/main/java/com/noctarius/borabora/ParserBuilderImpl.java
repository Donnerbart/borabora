/*
 * Copyright (c) 2016, Christoph Engelbert (aka noctarius) and
 * contributors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.noctarius.borabora;

import com.noctarius.borabora.builder.ParserBuilder;
import com.noctarius.borabora.spi.CommonTagCodec;
import com.noctarius.borabora.spi.SelectStatementStrategy;
import com.noctarius.borabora.spi.TagDecoder;
import com.noctarius.borabora.spi.pipeline.QueryOptimizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class ParserBuilderImpl
        implements ParserBuilder {

    private final List<TagDecoder> tagDecoders = new ArrayList<>();
    private final List<QueryOptimizer> queryOptimizers = new ArrayList<>();
    private SelectStatementStrategy selectStatementStrategy = BinarySelectStatementStrategy.INSTANCE;

    public ParserBuilderImpl() {
        addTagDecoder(CommonTagCodec.INSTANCE);
    }

    @Override
    public <V> ParserBuilder addTagDecoder(TagDecoder<V> tagDecoder) {
        tagDecoders.add(tagDecoder);
        return this;
    }

    @Override
    public <V> ParserBuilder addTagDecoder(TagDecoder<V> tagDecoder1, TagDecoder<V> tagDecoder2) {
        tagDecoders.add(tagDecoder1);
        tagDecoders.add(tagDecoder2);
        return this;
    }

    @Override
    public <V> ParserBuilder addTagDecoder(TagDecoder<V> tagDecoder1, TagDecoder<V> tagDecoder2, TagDecoder<V>... tagDecoders) {
        this.tagDecoders.add(tagDecoder1);
        this.tagDecoders.add(tagDecoder2);
        this.tagDecoders.addAll(Arrays.asList(tagDecoders));
        return this;
    }

    @Override
    public ParserBuilder withSelectStatementStrategy(SelectStatementStrategy selectStatementStrategy) {
        this.selectStatementStrategy = selectStatementStrategy;
        return this;
    }

    @Override
    public ParserBuilder addQueryOptimizer(QueryOptimizer queryOptimizer) {
        if (!this.queryOptimizers.contains(queryOptimizer)) {
            this.queryOptimizers.add(queryOptimizer);
        }
        return this;
    }

    @Override
    public ParserBuilder addQueryOptimizer(QueryOptimizer queryOptimizer1, QueryOptimizer queryOptimizer2) {
        addQueryOptimizer(queryOptimizer1);
        addQueryOptimizer(queryOptimizer2);
        return this;
    }

    @Override
    public ParserBuilder addQueryOptimizer(QueryOptimizer queryOptimizer1, QueryOptimizer queryOptimizer2,
                                           QueryOptimizer... queryOptimizers) {

        addQueryOptimizer(queryOptimizer1);
        addQueryOptimizer(queryOptimizer2);
        for (QueryOptimizer queryOptimizer : queryOptimizers) {
            addQueryOptimizer(queryOptimizer);
        }
        return this;
    }

    @Override
    public ParserBuilder asBinarySelectStatementStrategy() {
        selectStatementStrategy = BinarySelectStatementStrategy.INSTANCE;
        return this;
    }

    @Override
    public ParserBuilder asObjectSelectStatementStrategy() {
        selectStatementStrategy = ObjectSelectStatementStrategy.INSTANCE;
        return this;
    }

    @Override
    public Parser build() {
        return new ParserImpl(tagDecoders, selectStatementStrategy);
    }

}
