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

import com.noctarius.borabora.impl.query.stages.AsDictionarySelectorQueryStage;
import com.noctarius.borabora.impl.query.stages.AsSequenceSelectorQueryStage;
import com.noctarius.borabora.impl.query.stages.QueryStage;
import com.noctarius.borabora.spi.Decoder;
import com.noctarius.borabora.spi.Encoder;
import com.noctarius.borabora.spi.QueryContext;
import com.noctarius.borabora.spi.SelectStatementStrategy;
import com.noctarius.borabora.spi.StreamValue;
import com.noctarius.borabora.spi.pipeline.PipelineStage;

import java.io.ByteArrayOutputStream;

import static com.noctarius.borabora.spi.Constants.EMPTY_QUERY_CONSUMER;
import static com.noctarius.borabora.spi.Constants.OFFSET_CODE_NULL;
import static com.noctarius.borabora.spi.Constants.OPCODE_BREAK_MASK;
import static com.noctarius.borabora.spi.Constants.SIMPLE_VALUE_NULL_BYTE;

public class BinarySelectStatementStrategy
        implements SelectStatementStrategy {

    public static final SelectStatementStrategy INSTANCE = new BinarySelectStatementStrategy();

    private BinarySelectStatementStrategy() {
    }

    @Override
    public void beginSelect(QueryContext queryContext) {
        queryContext.queryStackPush(new BinaryQueryContext());
    }

    @Override
    public void finalizeSelect(QueryContext queryContext) {
        BinaryQueryContext bqc = queryContext.queryStackPop();

        byte[] data = bqc.baos.toByteArray();
        Input input = Input.fromByteArray(data);

        short head = input.read(0);
        MajorType majorType = MajorType.findMajorType(head);
        ValueType valueType = ValueTypes.valueType(input, 0);

        QueryContext newQueryContext = new QueryContextImpl(input, EMPTY_QUERY_CONSUMER, (QueryContextImpl) queryContext);

        Value value = new StreamValue(majorType, valueType, 0, newQueryContext);
        queryContext.consume(value);
    }

    @Override
    public void beginDictionary(QueryContext queryContext) {
        putStructureHead(MajorType.Dictionary, queryContext);
    }

    @Override
    public void endDictionary(QueryContext queryContext) {
        putBreakMask(queryContext);
    }

    @Override
    public void putDictionaryKey(String key, QueryContext queryContext) {
        BinaryQueryContext bqc = queryContext.queryStackPeek();
        bqc.offset = Encoder.putString(key, bqc.offset, bqc.output);
    }

    @Override
    public void putDictionaryKey(long key, QueryContext queryContext) {
        BinaryQueryContext bqc = queryContext.queryStackPeek();
        bqc.offset = Encoder.putNumber(key, bqc.offset, bqc.output);
    }

    @Override
    public void putDictionaryKey(double key, QueryContext queryContext) {
        BinaryQueryContext bqc = queryContext.queryStackPeek();
        bqc.offset = Encoder.putDouble(key, bqc.offset, bqc.output);
    }

    @Override
    public void putDictionaryValue(PipelineStage<QueryContext, QueryStage> previousPipelineStage, QueryContext queryContext) {
        if (!(previousPipelineStage.stage() instanceof AsDictionarySelectorQueryStage) //
                && !(previousPipelineStage.stage() instanceof AsSequenceSelectorQueryStage)) {

            queryContext.offset(putValue(queryContext));
        }
    }

    @Override
    public void beginSequence(QueryContext queryContext) {
        putStructureHead(MajorType.Sequence, queryContext);
    }

    @Override
    public void endSequence(QueryContext queryContext) {
        putBreakMask(queryContext);
    }

    @Override
    public void putSequenceValue(PipelineStage<QueryContext, QueryStage> previousPipelineStage, QueryContext queryContext) {
        if (!(previousPipelineStage.stage() instanceof AsDictionarySelectorQueryStage) //
                && !(previousPipelineStage.stage() instanceof AsSequenceSelectorQueryStage)) {

            queryContext.offset(putValue(queryContext));
        }
    }

    private void putStructureHead(MajorType majorType, QueryContext queryContext) {
        BinaryQueryContext bqc = queryContext.queryStackPeek();
        bqc.offset = Encoder.encodeLengthAndValue(majorType, -1, bqc.offset, bqc.output);
    }

    private void putBreakMask(QueryContext queryContext) {
        BinaryQueryContext bqc = queryContext.queryStackPeek();
        bqc.output.write(bqc.offset++, (byte) OPCODE_BREAK_MASK);
    }

    private long putValue(QueryContext queryContext) {
        long offset = queryContext.offset();
        BinaryQueryContext bqc = queryContext.queryStackPeek();
        if (offset >= 0) {
            Input input = queryContext.input();
            short head = Decoder.readUInt8(input, offset);

            MajorType majorType = MajorType.findMajorType(head);
            byte[] data = Decoder.readRaw(input, majorType, offset);
            bqc.offset = bqc.output.write(data, bqc.offset, data.length);

            return offset + data.length;
        } else if (offset == OFFSET_CODE_NULL) {
            bqc.output.write(bqc.offset, SIMPLE_VALUE_NULL_BYTE);
            bqc.offset++;
        }
        return offset;
    }

    private static class BinaryQueryContext {
        private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        private final Output output = Output.toOutputStream(baos);

        private long offset = 0;
    }

}
