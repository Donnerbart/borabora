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
package com.noctarius.borabora.spi;

import com.noctarius.borabora.impl.query.stages.QueryStage;
import com.noctarius.borabora.spi.pipeline.PipelineStage;

public interface SelectStatementStrategy {

    void beginSelect(QueryContext queryContext);

    void finalizeSelect(QueryContext queryContext);

    void beginDictionary(QueryContext queryContext);

    void endDictionary(QueryContext queryContext);

    void putDictionaryKey(String key, QueryContext queryContext);

    void putDictionaryKey(long key, QueryContext queryContext);

    void putDictionaryKey(double key, QueryContext queryContext);

    void putDictionaryValue(PipelineStage<QueryContext, QueryStage> previousPipelineStage, QueryContext queryContext);

    void beginSequence(QueryContext queryContext);

    void endSequence(QueryContext queryContext);

    void putSequenceValue(PipelineStage<QueryContext, QueryStage> previousPipelineStage, QueryContext queryContext);

}
