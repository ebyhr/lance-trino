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

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Locale.ENGLISH;

final class LanceTableNames
{
    private LanceTableNames() {}

    private static final Pattern TABLE_PATTERN;

    static {
        String tableTypes = Stream.of(TableType.values())
                .map(tableType -> tableType.name().toLowerCase(ENGLISH))
                .collect(Collectors.joining("|"));
        TABLE_PATTERN = Pattern.compile("(?<table>[^$]+)" +
                "(?:\\$(?<type>(?i:" + tableTypes + ")))?");
    }

    static boolean isValidLanceTable(String tableName)
    {
        return TABLE_PATTERN.matcher(tableName).matches();
    }

    static Optional<TableType> tableTypeFrom(String tableName)
    {
        Matcher match = TABLE_PATTERN.matcher(tableName);
        checkArgument(match.matches(), "Invalid Lance table name: %s", tableName);
        return Optional.ofNullable(match.group("type"))
                .map(type -> TableType.valueOf(type.toUpperCase(ENGLISH)));
    }

    static String tableNameFrom(String tableName)
    {
        Matcher match = TABLE_PATTERN.matcher(tableName);
        checkArgument(match.matches(), "Invalid Lance table name: %s", tableName);
        return match.group("table");
    }
}
