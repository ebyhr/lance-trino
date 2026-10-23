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

import com.google.common.collect.ImmutableList;
import io.trino.spi.connector.ColumnMetadata;
import io.trino.spi.connector.ConnectorSession;
import io.trino.spi.connector.ConnectorTableMetadata;
import io.trino.spi.connector.ConnectorTransactionHandle;
import io.trino.spi.connector.InMemoryRecordSet;
import io.trino.spi.connector.RecordCursor;
import io.trino.spi.connector.SchemaTableName;
import io.trino.spi.connector.SystemTable;
import io.trino.spi.predicate.TupleDomain;
import org.lance.namespace.model.ListTableVersionsRequest;
import org.lance.namespace.model.ListTableVersionsResponse;
import org.lance.namespace.model.TableVersion;

import java.util.List;

import static io.trino.spi.type.BigintType.BIGINT;
import static io.trino.spi.type.DateTimeEncoding.packDateTimeWithZone;
import static io.trino.spi.type.TimeZoneKey.UTC_KEY;
import static io.trino.spi.type.TimestampWithTimeZoneType.TIMESTAMP_TZ_MILLIS;
import static io.trino.spi.type.VarcharType.VARCHAR;
import static java.util.Objects.requireNonNull;

class VersionsTable
        implements SystemTable
{
    private final LanceRuntime runtime;
    private final List<String> tableId;
    private final ConnectorTableMetadata tableMetadata;

    VersionsTable(LanceRuntime runtime, SchemaTableName tableName, List<String> tableId)
    {
        this.runtime = requireNonNull(runtime, "runtime is null");
        this.tableId = ImmutableList.copyOf(requireNonNull(tableId, "tableId is null"));
        tableMetadata = new ConnectorTableMetadata(requireNonNull(tableName, "tableName is null"), ImmutableList.<ColumnMetadata>builder()
                .add(new ColumnMetadata("version", BIGINT))
                .add(new ColumnMetadata("manifest_path", VARCHAR))
                .add(new ColumnMetadata("manifest_size", BIGINT))
                .add(new ColumnMetadata("e_tag", VARCHAR))
                .add(new ColumnMetadata("timestamp", TIMESTAMP_TZ_MILLIS))
                // TODO Add 'metadata' column once InMemoryRecordSet.Builder.addRow supports map type
                .build());
    }

    @Override
    public Distribution getDistribution()
    {
        return Distribution.SINGLE_COORDINATOR;
    }

    @Override
    public ConnectorTableMetadata getTableMetadata()
    {
        return tableMetadata;
    }

    @Override
    public RecordCursor cursor(ConnectorTransactionHandle transactionHandle, ConnectorSession session, TupleDomain<Integer> constraint)
    {
        InMemoryRecordSet.Builder table = InMemoryRecordSet.builder(tableMetadata.getColumns());

        String pageToken = null;
        do {
            ListTableVersionsRequest request = new ListTableVersionsRequest();
            request.setId(tableId);
            request.setPageToken(pageToken);

            ListTableVersionsResponse response = runtime.getNamespace().listTableVersions(request);
            for (TableVersion version : response.getVersions()) {
                Long timestampMillis = version.getTimestampMillis();
                table.addRow(
                        version.getVersion(),
                        version.getManifestPath(),
                        version.getManifestSize(),
                        version.geteTag(),
                        timestampMillis == null ? null : packDateTimeWithZone(timestampMillis, UTC_KEY));
            }
            pageToken = response.getPageToken();
        }
        while (pageToken != null);
        return table.build().cursor();
    }
}
