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

import com.noctarius.borabora.spi.TypeSpecs;
import org.junit.Ignore;
import org.junit.Test;

import static com.noctarius.borabora.Predicates.any;
import static com.noctarius.borabora.Predicates.matchFloat;
import static com.noctarius.borabora.Predicates.matchInt;
import static com.noctarius.borabora.Predicates.matchString;

public class QueryLanguageAcceptanceTestCase
        extends AbstractTestCase {

    private final Parser parser = Parser.newBuilder().build();

    @Test(expected = QueryParserException.class)
    public void fail_empty_query()
            throws Exception {

        parser.prepareQuery("");
    }

    @Test(expected = QueryParserException.class)
    public void fail_stream_nint() {
        parser.prepareQuery("#-1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void fail_query_stream_nint() {
        Query.newBuilder().stream(-1).build();
    }

    @Test(expected = QueryParserException.class)
    public void fail_stream_ufloat() {
        parser.prepareQuery("#1.0");
    }

    @Test(expected = QueryParserException.class)
    public void fail_stream_nfloat() {
        parser.prepareQuery("#-1.0");
    }

    @Test(expected = QueryParserException.class)
    public void fail_stream_ufloat_comma() {
        parser.prepareQuery("#1,0");
    }

    @Test(expected = QueryParserException.class)
    public void fail_stream_nfloat_comma() {
        parser.prepareQuery("#-1,0");
    }

    @Test
    public void test_stream_uint() {
        Query query = Query.newBuilder().stream(0).build();
        evaluate(query, "#0");

        query = Query.newBuilder().build();
        evaluate(query, "#0");

        query = Query.newBuilder().stream(123).build();
        evaluate(query, "#123");
    }

    @Test
    public void test_stream_base() {
        Query query = Query.newBuilder().build();
        evaluate(query, "#");
    }

    @Test
    public void test_stream_sequence_int() {
        Query query = Query.newBuilder().stream(1).sequence(1).build();
        evaluate(query, "#1(1)");
    }

    @Test(expected = QueryParserException.class)
    public void fail_stream_sequence_nint() {
        parser.prepareQuery("#1(-1)");
    }

    @Test(expected = IllegalArgumentException.class)
    public void fail_query_stream_sequence_nint() {
        Query.newBuilder().stream(1).sequence(-1).build();
    }

    @Test
    public void test_stream_dictionary_int() {
        Query query = Query.newBuilder().stream(1).dictionary(1).build();
        evaluate(query, "#1{1}");
    }

    @Test
    public void test_stream_dictionary_nint() {
        Query query = Query.newBuilder().stream(1).dictionary(-1).build();
        evaluate(query, "#1{-1}");
    }

    @Test(expected = QueryParserException.class)
    public void fail_type_match_missing_type_sped() {
        parser.prepareQuery("#->");
    }

    @Test(expected = QueryParserException.class)
    public void fail_type_match_unknown_type() {
        parser.prepareQuery("#->gg");
    }

    @Test(expected = NullPointerException.class)
    public void fail_type_match_null() {
        Query.newBuilder().requireType(null).build();
    }

    @Test(expected = QueryParserException.class)
    public void fail_type_match_wrong_optional_sign() {
        parser.prepareQuery("#->!number");
    }

    @Test
    public void test_type_match() {
        Query query = Query.newBuilder().requireType(TypeSpecs.Number).build();
        evaluate(query, "#->number");

        query = Query.newBuilder().nullOrType(TypeSpecs.Number).build();
        evaluate(query, "#->?number");
    }

    @Test
    public void test_dictionary_access_string() {
        Query query = Query.newBuilder().dictionary(matchString("test")).build();
        evaluate(query, "#{'test'}");
    }

    @Test
    public void test_stream_dictionary_access_string() {
        Query query = Query.newBuilder().stream(1).dictionary(matchString("test")).build();
        evaluate(query, "#1{'test'}");
    }

    @Test
    public void test_dictionary_access_uint() {
        Query query = Query.newBuilder().dictionary(matchInt(123)).build();
        evaluate(query, "#{123}");
    }

    @Test
    public void test_dictionary_access_nint() {
        Query query = Query.newBuilder().dictionary(matchInt(-123)).build();
        evaluate(query, "#{-123}");
    }

    @Test
    public void test_dictionary_access_ufloat() {
        Query query = Query.newBuilder().dictionary(matchFloat(123.0)).build();
        evaluate(query, "#{123.0}");
    }

    @Test
    public void test_stream_dictionary_access_ufloat() {
        Query query = Query.newBuilder().stream(1).dictionary(matchFloat(123.0)).build();
        evaluate(query, "#1{123.0}");
    }

    @Test
    public void test_dictionary_access_nfloar() {
        Query query = Query.newBuilder().dictionary(matchFloat(-123.0)).build();
        evaluate(query, "#{-123.0}");
    }

    @Test
    public void test_stream_dictionary_access_nfloar() {
        Query query = Query.newBuilder().stream(1).dictionary(matchFloat(-123.0)).build();
        evaluate(query, "#1{-123.0}");
    }

    @Test(expected = QueryParserException.class)
    public void fail_dictionary_access_illegal_argument() {
        parser.prepareQuery("#{test}");
    }

    @Test
    public void test_sequence_access_uint() {
        Query query = Query.newBuilder().sequence(123).build();
        evaluate(query, "#(123)");
    }

    @Test(expected = QueryParserException.class)
    public void fail_sequence_access_nint() {
        parser.prepareQuery("#(-123)");
    }

    @Test
    public void test_sequence_select() {
        Query query = Query.newBuilder().asSequence() //
                           .putEntry().stream(0).endEntry() //
                           .putEntry().stream(0).endEntry().endSequence().build();
        evaluate(query, "(#, #)");
    }

    @Test
    public void test_sequence_select_subsequence() {
        Query query = Query.newBuilder().asSequence() //
                           .putEntry().stream(0).endEntry() //
                           .putEntry().asSequence().putEntry().stream(0).endEntry() //
                           .putEntry().stream(0).endEntry().endSequence().endEntry() //
                           .endSequence().build();
        evaluate(query, "(#, (#, #))");
    }

    @Test
    public void test_sequence_select_subdictionary_string() {
        Query query = Query.newBuilder().asSequence() //
                           .putEntry().stream(0).endEntry() //
                           .putEntry().asDictionary() //
                           .putEntry("a").stream(0).endEntry() //
                           .putEntry("b").stream(0).endEntry() //
                           .endDictionary().endEntry() //
                           .endSequence().build();
        evaluate(query, "(#, (a: #, b: #))");
    }

    @Test
    public void test_sequence_select_subdictionary_float() {
        Query query = Query.newBuilder().asSequence() //
                           .putEntry().stream(0).endEntry() //
                           .putEntry().asDictionary() //
                           .putEntry(1.0).stream(0).endEntry() //
                           .putEntry(2.0).stream(0).endEntry() //
                           .endDictionary().endEntry() //
                           .endSequence().build();
        evaluate(query, "(#, (1.0: #, 2.0: #))");
    }

    @Test
    public void test_sequence_select_subdictionary_int() {
        Query query = Query.newBuilder().asSequence() //
                           .putEntry().stream(0).endEntry() //
                           .putEntry().asDictionary() //
                           .putEntry(1).stream(0).endEntry() //
                           .putEntry(2).stream(0).endEntry() //
                           .endDictionary().endEntry() //
                           .endSequence().build();
        evaluate(query, "(#, (1: #, 2: #))");
    }

    @Test(expected = QueryParserException.class)
    public void fail_sequence_dictionary_select_string_mixed() {
        parser.prepareQuery("(#, c: (a: #, b: #))");
    }

    @Test(expected = QueryParserException.class)
    public void fail_sequence_dictionary_select_float_mixed() {
        parser.prepareQuery("(#, 3.0: (1.0: #, 2.0: #))");
    }

    @Test(expected = QueryParserException.class)
    public void fail_sequence_dictionary_select_int_mixed() {
        parser.prepareQuery("(#, 3: (1: #, 2: #))");
    }

    @Test
    public void test_type_check_uint() {
        Query query = Query.newBuilder().requireType(TypeSpecs.UInt).build();
        assertQueryEquals(query, parser.prepareQuery("#->uint"));
    }

    @Test
    public void test_type_check_nint() {
        Query query = Query.newBuilder().requireType(TypeSpecs.NInt).build();
        assertQueryEquals(query, parser.prepareQuery("#->nint"));
    }

    @Test
    public void test_type_check_int() {
        Query query = Query.newBuilder().requireType(TypeSpecs.Int).build();
        assertQueryEquals(query, parser.prepareQuery("#->int"));
    }

    @Test
    public void test_type_check_float() {
        Query query = Query.newBuilder().requireType(TypeSpecs.Float).build();
        assertQueryEquals(query, parser.prepareQuery("#->float"));
    }

    @Test
    public void test_type_check_string() {
        Query query = Query.newBuilder().requireType(TypeSpecs.String).build();
        assertQueryEquals(query, parser.prepareQuery("#->string"));
    }

    @Test
    public void test_type_check_dictionary() {
        Query query = Query.newBuilder().requireType(TypeSpecs.Dictionary).build();
        assertQueryEquals(query, parser.prepareQuery("#->dictionary"));
    }

    @Test
    public void test_type_check_sequence() {
        Query query = Query.newBuilder().requireType(TypeSpecs.Sequence).build();
        assertQueryEquals(query, parser.prepareQuery("#->sequence"));
    }

    @Test
    public void test_type_check_tag() {
        Query query = Query.newBuilder().requireType(TypeSpecs.SemanticTag).build();
        assertQueryEquals(query, parser.prepareQuery("#->tag"));
    }

    @Test(expected = QueryParserException.class)
    public void fail_type_check_unknown_tag() {
        parser.prepareQuery("#->tag$111");
    }

    @Test
    public void test_type_check_date_time() {
        Query query = Query.newBuilder().requireType(TypeSpecs.DateTime).build();
        assertQueryEquals(query, parser.prepareQuery("#->tag$0"));
        assertQueryEquals(query, parser.prepareQuery("#->datetime"));
    }

    @Test
    public void test_type_check_timestamp() {
        Query query = Query.newBuilder().requireType(TypeSpecs.Timstamp).build();
        assertQueryEquals(query, parser.prepareQuery("#->tag$1"));
        assertQueryEquals(query, parser.prepareQuery("#->timestamp"));
    }

    @Test
    public void test_type_check_enccbor() {
        Query query = Query.newBuilder().requireType(TypeSpecs.EncCBOR).build();
        assertQueryEquals(query, parser.prepareQuery("#->tag$24"));
        assertQueryEquals(query, parser.prepareQuery("#->enccbor"));
    }

    @Test
    public void test_type_check_optional_enccbor() {
        Query query = Query.newBuilder().nullOrType(TypeSpecs.EncCBOR).build();
        assertQueryEquals(query, parser.prepareQuery("#->?tag$24"));
        assertQueryEquals(query, parser.prepareQuery("#->?enccbor"));
    }

    @Test
    public void test_type_check_uri() {
        Query query = Query.newBuilder().requireType(TypeSpecs.URI).build();
        assertQueryEquals(query, parser.prepareQuery("#->tag$32"));
        assertQueryEquals(query, parser.prepareQuery("#->uri"));
    }

    @Test
    public void test_type_check_bool() {
        Query query = Query.newBuilder().requireType(TypeSpecs.Bool).build();
        assertQueryEquals(query, parser.prepareQuery("#->bool"));
    }

    @Test
    public void test_dictionary_select_string() {
        Query query = Query.newBuilder().asDictionary() //
                           .putEntry("a").stream(0).endEntry() //
                           .putEntry("b").stream(0).endEntry() //
                           .endDictionary().build();
        evaluate(query, "(a: #, b: #)");
    }

    @Test
    public void test_dictionary_select_float() {
        Query query = Query.newBuilder().asDictionary() //
                           .putEntry(1.0).stream(0).endEntry() //
                           .putEntry(2.0).stream(0).endEntry() //
                           .endDictionary().build();
        evaluate(query, "(1.0: #, 2.0: #)");
    }

    @Test
    public void test_dictionary_select_int() {
        Query query = Query.newBuilder().asDictionary() //
                           .putEntry(1).stream(0).endEntry() //
                           .putEntry(2).stream(0).endEntry() //
                           .endDictionary().build();
        evaluate(query, "(1: #, 2: #)");
    }

    @Test
    public void test_dictionary_select_string_subsequence() {
        Query query = Query.newBuilder().asDictionary() //
                           .putEntry("a").stream(0).endEntry() //
                           .putEntry("b").asSequence() //
                           .putEntry().stream(0).endEntry() //
                           .putEntry().stream(0).endEntry() //
                           .endSequence().endEntry() //
                           .endDictionary().build();
        evaluate(query, "(a: #, b: (#, #))");
    }

    @Test
    public void test_dictionary_select_float_subsequence() {
        Query query = Query.newBuilder().asDictionary() //
                           .putEntry(1.0).stream(0).endEntry() //
                           .putEntry(2.0).asSequence() //
                           .putEntry().stream(0).endEntry() //
                           .putEntry().stream(0).endEntry() //
                           .endSequence().endEntry() //
                           .endDictionary().build();
        evaluate(query, "(1.0: #, 2.0: (#, #))");
    }

    @Test
    public void test_dictionary_select_int_subsequence() {
        Query query = Query.newBuilder().asDictionary() //
                           .putEntry(1).stream(0).endEntry() //
                           .putEntry(2).asSequence() //
                           .putEntry().stream(0).endEntry() //
                           .putEntry().stream(0).endEntry() //
                           .endSequence().endEntry() //
                           .endDictionary().build();
        evaluate(query, "(1: #, 2: (#, #))");
    }

    @Test
    public void test_dictionary_select_string_subdictionary() {
        Query query = Query.newBuilder().asDictionary() //
                           .putEntry("c").stream(0).endEntry() //
                           .putEntry("d").asDictionary() //
                           .putEntry("a").stream(0).endEntry() //
                           .putEntry("b").stream(0).endEntry() //
                           .endDictionary().endEntry() //
                           .endDictionary().build();
        evaluate(query, "(c: #, d: (a: #, b: #))");
    }

    @Test
    public void test_dictionary_select_float_subdictionary() {
        Query query = Query.newBuilder().asDictionary() //
                           .putEntry(3.0).stream(0).endEntry() //
                           .putEntry(4.0).asDictionary() //
                           .putEntry(1.0).stream(0).endEntry() //
                           .putEntry(2.0).stream(0).endEntry() //
                           .endDictionary().endEntry() //
                           .endDictionary().build();
        evaluate(query, "(3.0: #, 4.0: (1.0: #, 2.0: #))");
    }

    @Test
    public void test_dictionary_select_int_subdictionary() {
        Query query = Query.newBuilder().asDictionary() //
                           .putEntry(3).stream(0).endEntry() //
                           .putEntry(4).asDictionary() //
                           .putEntry(1).stream(0).endEntry() //
                           .putEntry(2).stream(0).endEntry() //
                           .endDictionary().endEntry() //
                           .endDictionary().build();
        System.out.println("");
        evaluate(query, "(3: #, 4: (1: #, 2: #))");
    }

    @Test(expected = QueryParserException.class)
    public void fail_dictionary_sequence_string_select_mixed() {
        parser.prepareQuery("(c: #, (a: #, b: #))");
    }

    @Test(expected = QueryParserException.class)
    public void fail_dictionary_sequence_float_select_mixed() {
        parser.prepareQuery("(3.0: #, (1.0: #, 2.0: #))");
    }

    @Test(expected = QueryParserException.class)
    public void fail_dictionary_sequence_int_select_mixed() {
        parser.prepareQuery("(3: #, (1: #, 2: #))");
    }

    @Test(expected = QueryParserException.class)
    public void fail_broken_dictionary_query() {
        parser.prepareQuery("#{}");
    }

    @Test(expected = QueryParserException.class)
    public void fail_broken_sequence_query() {
        parser.prepareQuery("#()");
    }

    @Test(expected = QueryParserException.class)
    public void fail_broken_dictionary_float_query() {
        parser.prepareQuery("#{1.1.}");
    }

    @Test(expected = QueryParserException.class)
    public void fail_broken_sequence_float_query() {
        parser.prepareQuery("#(1.1.)");
    }

    @Test
    @Ignore
    public void test_sequence_dictionary_if_string() {
        Query query = null; // TODO
        evaluate(query, "(?(0){'foo'=='bar'}>>1)");
    }

    @Test
    @Ignore
    public void test_sequence_dictionary_if_uint() {
        Query query = null; // TODO
        evaluate(query, "(?(0){'foo'==1}>>1)");
        evaluate(query, "(?(0){'foo'>=1}>>1)");
        evaluate(query, "(?(0){'foo'>1}>>1)");
        evaluate(query, "(?(0){'foo'<=1}>>1)");
        evaluate(query, "(?(0){'foo'<1}>>1)");
    }

    @Test
    @Ignore
    public void test_sequence_dictionary_if_nint() {
        Query query = null; // TODO
        evaluate(query, "(?(0){'foo'==-1}>>1)");
        evaluate(query, "(?(0){'foo'>=-1}>>1)");
        evaluate(query, "(?(0){'foo'>-1}>>1)");
        evaluate(query, "(?(0){'foo'<=-1}>>1)");
        evaluate(query, "(?(0){'foo'<-1}>>1)");
    }

    @Test
    @Ignore
    public void test_sequence_dictionary_if_ufloat() {
        Query query = null; // TODO
        evaluate(query, "(?(0){'foo'==1.0}>>1)");
        evaluate(query, "(?(0){'foo'>=1.0}>>1)");
        evaluate(query, "(?(0){'foo'>1.0}>>1)");
        evaluate(query, "(?(0){'foo'<=1.0}>>1)");
        evaluate(query, "(?(0){'foo'<1.0}>>1)");
    }

    @Test
    @Ignore
    public void test_sequence_dictionary_if_nfloat() {
        Query query = null; // TODO
        evaluate(query, "(?(0){'foo'==-1.0}>>1)");
        evaluate(query, "(?(0){'foo'>=-1.0}>>1)");
        evaluate(query, "(?(0){'foo'>-1.0}>>1)");
        evaluate(query, "(?(0){'foo'<=-1.0}>>1)");
        evaluate(query, "(?(0){'foo'<-1.0}>>1)");
    }

    @Test
    @Ignore
    public void test_sequence_sequence_if_string() {
        Query query = null; // TODO
        evaluate(query, "(?(1=='bar')>>1)");
    }

    @Test
    @Ignore
    public void test_sequence_sequence_if_uint() {
        Query query = null; // TODO
        evaluate(query, "(?(1==1)>>1)");
        evaluate(query, "(?(1>=1)>>1)");
        evaluate(query, "(?(1>1)>>1)");
        evaluate(query, "(?(1<=1)>>1)");
        evaluate(query, "(?(1<1)>>1)");
    }

    @Test
    @Ignore
    public void test_sequence_sequence_if_nint() {
        Query query = null; // TODO
        evaluate(query, "(?(1==-1)>>1)");
        evaluate(query, "(?(1>=-1)>>1)");
        evaluate(query, "(?(1>-1)>>1)");
        evaluate(query, "(?(1<=-1)>>1)");
        evaluate(query, "(?(1<-1)>>1)");
    }

    @Test
    @Ignore
    public void test_sequence_sequence_if_ufloat() {
        Query query = null; // TODO
        evaluate(query, "(?(1==1.0)>>1)");
        evaluate(query, "(?(1>=1.0)>>1)");
        evaluate(query, "(?(1>1.0)>>1)");
        evaluate(query, "(?(1<=1.0)>>1)");
        evaluate(query, "(?(1<1.0)>>1)");
    }

    @Test
    @Ignore
    public void test_sequence_sequence_if_nfloat() {
        Query query = null; // TODO
        evaluate(query, "(?(1==-1.0)>>1)");
        evaluate(query, "(?(1>=-1.0)>>1)");
        evaluate(query, "(?(1>-1.0)>>1)");
        evaluate(query, "(?(1<=-1.0)>>1)");
        evaluate(query, "(?(1<-1.0)>>1)");
    }

    @Test
    @Ignore
    public void test_dictionary_dictionary_if_string() {
        Query query = null; // TODO
        evaluate(query, "#{?{'foo'=='bar'}>>'foo'}");
    }

    @Test
    @Ignore
    public void test_dictionary_dictionary_if_uint() {
        Query query = null; // TODO
        evaluate(query, "{?{'foo'==1}>>'foo'}");
        evaluate(query, "{?{'foo'>=1}>>'foo'}");
        evaluate(query, "{?{'foo'>1}>>'foo'}");
        evaluate(query, "{?{'foo'<=1}>>'foo'}");
        evaluate(query, "{?{'foo'<1}>>'foo'}");
    }

    @Test
    @Ignore
    public void test_dictionary_dictionary_if_nint() {
        Query query = null; // TODO
        evaluate(query, "{?{'foo'==-1}>>'foo'}");
        evaluate(query, "{?{'foo'>=-1}>>'foo'}");
        evaluate(query, "{?{'foo'>-1}>>'foo'}");
        evaluate(query, "{?{'foo'<=-1}>>'foo'}");
        evaluate(query, "{?{'foo'<-1}>>'foo'}");
    }

    @Test
    @Ignore
    public void test_dictionary_dictionary_if_ufloat() {
        Query query = null; // TODO
        evaluate(query, "{?{'foo'==1.0}>>'foo'}");
        evaluate(query, "{?{'foo'>=1.0}>>'foo'}");
        evaluate(query, "{?{'foo'>1.0}>>'foo'}");
        evaluate(query, "{?{'foo'<=1.0}>>'foo'}");
        evaluate(query, "{?{'foo'<1.0}>>'foo'}");
    }

    @Test
    @Ignore
    public void test_dictionary_dictionary_if_nfloat() {
        Query query = null; // TODO
        evaluate(query, "{?{'foo'==-1.0}>>'foo'}");
        evaluate(query, "{?{'foo'>=-1.0}>>'foo'}");
        evaluate(query, "{?{'foo'>-1.0}>>'foo'}");
        evaluate(query, "{?{'foo'<=-1.0}>>'foo'}");
        evaluate(query, "{?{'foo'<-1.0}>>'foo'}");
    }

    @Test
    public void test_any_sequence_index() {
        Query query = Query.newBuilder().sequenceMatch(any()).build();
        evaluate(query, "#(?)");

        query = Query.newBuilder().multiStream().sequenceMatch(any()).build();
        evaluate(query, "$(?)");
    }

    @Test
    public void test_match_any_stream_element() {
        Query query = Query.newBuilder().multiStream().build();
        evaluate(query, "$");
    }

    private void evaluate(Query query, String queryString) {
        Query parsedQuery = parser.prepareQuery(queryString);
        assertQueryEquals(query, parsedQuery);
    }

}
