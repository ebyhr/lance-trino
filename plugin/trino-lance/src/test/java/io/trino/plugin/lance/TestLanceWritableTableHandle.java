/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.trino.plugin.lance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Provider;
import io.airlift.json.JsonCodec;
import io.airlift.json.JsonCodecFactory;
import io.airlift.json.ObjectMapperProvider;
import io.trino.spi.connector.SchemaTableName;
import io.trino.spi.type.Type;
import io.trino.type.TypeDeserializer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.trino.spi.type.IntegerType.INTEGER;
import static io.trino.type.InternalTypeManager.TESTING_TYPE_MANAGER;
import static org.assertj.core.api.Assertions.assertThat;

final class TestLanceWritableTableHandle
{
    private final JsonCodec<LanceWritableTableHandle> codec;

    TestLanceWritableTableHandle()
    {
        Provider<ObjectMapper> jsonMapper = new ObjectMapperProvider()
                .withJsonDeserializers(ImmutableMap.of(Type.class, new TypeDeserializer(TESTING_TYPE_MANAGER)));
        codec = new JsonCodecFactory(jsonMapper).jsonCodec(LanceWritableTableHandle.class);
    }

    @Test
    void testRoundTrip()
    {
        LanceWritableTableHandle expected = new LanceWritableTableHandle(
                new SchemaTableName("schema", "test"),
                "test",
                "{}",
                List.of(LanceColumnHandle.required("col").setTrinoType(INTEGER).build()),
                List.of("test"),
                Map.of("key", "value"),
                true,
                false,
                true,
                UUID.randomUUID().toString(),
                "2.2");

        String json = codec.toJson(expected);
        LanceWritableTableHandle actual = codec.fromJson(json);

        assertThat(actual).isEqualTo(expected);
    }
}
