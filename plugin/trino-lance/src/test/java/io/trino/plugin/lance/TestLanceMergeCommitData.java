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

import io.airlift.json.JsonCodec;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.airlift.json.JsonCodec.jsonCodec;
import static org.assertj.core.api.Assertions.assertThat;

final class TestLanceMergeCommitData
{
    private final JsonCodec<LanceMergeCommitData> codec = jsonCodec(LanceMergeCommitData.class);

    @Test
    void testRoundTrip()
    {
        LanceMergeCommitData expected = new LanceMergeCommitData(
                List.of(new FragmentDeletion(1, List.of(123))),
                List.of("{}"),
                100L,
                10L);

        String json = codec.toJson(expected);
        LanceMergeCommitData actual = codec.fromJson(json);

        assertThat(actual).isEqualTo(expected);
    }
}
