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

import io.trino.spi.connector.ConnectorInsertTableHandle;
import io.trino.spi.connector.ConnectorOutputTableHandle;
import io.trino.spi.connector.SchemaTableName;

import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Handle for write operations (CREATE TABLE, INSERT).
 * Implements both ConnectorInsertTableHandle and ConnectorOutputTableHandle
 * so it can be used for both INSERT and CREATE TABLE AS SELECT operations.
 *
 * @param tableId Lance table identifier for namespace operations.
 * @param storageOptions Storage options for accessing the table (S3 credentials, endpoint, etc.).
 * @param tableExisted True if the table existed before this operation. Used to determine whether to use overwrite mode (existing table) or create mode (new table).
 * @param transactionId Transaction ID used to look up the cached Dataset.
 * @param fileFormatVersion File format version to use when writing. Returns null to use existing table's version (for INSERT) or default (for new tables).
 */
public record LanceWritableTableHandle(
        SchemaTableName tableName,
        String tablePath,
        String schemaJson,
        List<LanceColumnHandle> inputColumns,
        List<String> tableId,
        Map<String, String> storageOptions,
        boolean forCreateTable,
        boolean replace,
        boolean tableExisted,
        String transactionId,
        String fileFormatVersion)
        implements ConnectorInsertTableHandle, ConnectorOutputTableHandle
{
    public LanceWritableTableHandle
    {
        requireNonNull(tableName, "tableName is null");
        requireNonNull(tablePath, "tablePath is null");
        requireNonNull(schemaJson, "schemaJson is null");
        requireNonNull(inputColumns, "inputColumns is null");
        requireNonNull(tableId, "tableId is null");
        requireNonNull(storageOptions, "storageOptions is null");
    }
}
