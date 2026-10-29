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

import org.apache.arrow.vector.types.pojo.Field;
import org.lance.schema.LanceField;

import java.util.Map;

public final class BlobUtils
{
    public static final String LANCE_ENCODING_BLOB_KEY = "lance-encoding:blob";
    public static final String LANCE_ENCODING_BLOB_VALUE = "true";
    public static final String ARROW_EXTENSION_NAME = "ARROW:extension:name";
    public static final String BLOB_V2_EXTENSION = "lance.blob.v2";

    public static final String BLOB_POSITION_SUFFIX = "__blob_pos";
    public static final String BLOB_SIZE_SUFFIX = "__blob_size";

    public enum BlobVirtualColumnType
    {
        NONE,
        POSITION,
        SIZE
    }

    private BlobUtils() {}

    public static boolean isBlobArrowField(Field field)
    {
        if (field == null) {
            return false;
        }

        Map<String, String> metadata = field.getMetadata();
        if (metadata == null || metadata.isEmpty()) {
            return false;
        }

        return isBlobMetadata(metadata);
    }

    public static boolean isBlobField(LanceField field)
    {
        if (field == null) {
            return false;
        }
        return isBlobMetadata(field.getMetadata());
    }

    // Formats before 2.2 keep lance-encoding:blob. Unset uses Lance 14's stable default, 2.2.
    public static boolean useBlobV2(String fileFormatVersion)
    {
        if (fileFormatVersion == null || fileFormatVersion.isEmpty()) {
            return true;
        }
        return switch (fileFormatVersion) {
            case "legacy", "0.1", "2.0", "2.1" -> false;
            default -> true;
        };
    }

    private static boolean isBlobMetadata(Map<String, String> metadata)
    {
        if (metadata == null || metadata.isEmpty()) {
            return false;
        }
        if (LANCE_ENCODING_BLOB_VALUE.equalsIgnoreCase(metadata.get(LANCE_ENCODING_BLOB_KEY))) {
            return true;
        }
        return BLOB_V2_EXTENSION.equals(metadata.get(ARROW_EXTENSION_NAME));
    }

    public static String getBlobPositionColumnName(String columnName)
    {
        return columnName + BLOB_POSITION_SUFFIX;
    }

    public static String getBlobSizeColumnName(String columnName)
    {
        return columnName + BLOB_SIZE_SUFFIX;
    }
}
