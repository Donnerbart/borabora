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
package com.noctarius.borabora.impl.query.stages;

import com.noctarius.borabora.Input;
import com.noctarius.borabora.MajorType;
import com.noctarius.borabora.ValueType;
import com.noctarius.borabora.ValueTypes;
import com.noctarius.borabora.WrongTypeException;
import com.noctarius.borabora.spi.Decoder;
import com.noctarius.borabora.spi.QueryContext;
import com.noctarius.borabora.spi.TypeSpec;
import com.noctarius.borabora.spi.pipeline.PipelineStage;
import com.noctarius.borabora.spi.pipeline.VisitResult;

import java.util.Objects;

public class TypeMatcherQueryStage
        implements QueryStage {

    private final TypeSpec typeSpec;
    private final boolean required;

    public TypeMatcherQueryStage(TypeSpec typeSpec, boolean required) {
        Objects.requireNonNull(typeSpec, "typeSpec cannot be null");
        this.typeSpec = typeSpec;
        this.required = required;
    }

    @Override
    public VisitResult evaluate(PipelineStage<QueryContext, QueryStage> previousPipelineStage, //
                                PipelineStage<QueryContext, QueryStage> pipelineStage, //
                                QueryContext pipelineContext) {

        Input input = pipelineContext.input();
        long offset = pipelineContext.offset();

        short head = Decoder.readUInt8(input, offset);
        MajorType majorType = MajorType.findMajorType(head);
        ValueType valueType = ValueTypes.valueType(input, offset);
        if (!typeSpec.valid(majorType, input, offset) && required) {
            String msg = String.format("Element at offset %s is not of type %s but %s", offset, this.typeSpec, valueType);
            throw new WrongTypeException(msg);
        }

        return pipelineStage.visitChildren(pipelineContext);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TypeMatcherQueryStage)) {
            return false;
        }

        TypeMatcherQueryStage that = (TypeMatcherQueryStage) o;

        if (required != that.required) {
            return false;
        }
        return typeSpec != null ? typeSpec.equals(that.typeSpec) : that.typeSpec == null;
    }

    @Override
    public int hashCode() {
        int result = typeSpec != null ? typeSpec.hashCode() : 0;
        result = 31 * result + (required ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TYPE_MATCH[ " + "type=" + typeSpec + ", optional=" + !required + " ]";
    }

}
