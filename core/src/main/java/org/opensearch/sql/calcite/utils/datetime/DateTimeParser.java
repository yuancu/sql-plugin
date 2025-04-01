/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.sql.calcite.utils.datetime;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.opensearch.sql.data.model.ExprTimeValue;
import org.opensearch.sql.data.model.ExprValueUtils;
import org.opensearch.sql.data.type.ExprCoreType;
import org.opensearch.sql.exception.SemanticCheckException;
import org.opensearch.sql.expression.function.FunctionProperties;

public interface DateTimeParser {

  static LocalDateTime parseTimeOrTimestamp(String input) {
    if (input == null || input.trim().isEmpty()) {
      throw new SemanticCheckException("Cannot parse a null/empty date-time string.");
    }

    try {
      return parseTime(input);
    } catch (Exception ignored) {
    }

    try {
      return parseTimestamp(input);
    } catch (Exception ignored) {
    }

    throw new SemanticCheckException(
        String.format("time:%s in unsupported format, please use 'HH:mm:ss[.SSSSSSSSS]'", input));
  }

  static LocalDateTime parseDateOrTimestamp(String input) {
    if (input == null || input.trim().isEmpty()) {
      throw new SemanticCheckException("Cannot parse a null/empty date-time string.");
    }

    try {
      return parseDate(input);
    } catch (Exception ignored) {
    }

    try {
      return parseTimestamp(input);
    } catch (Exception ignored) {
    }

    throw new SemanticCheckException(
        String.format("date:%s in unsupported format, please use 'yyyy-MM-dd'", input));
  }

  static LocalDateTime parseTimestamp(String input) {
    // The timestampValue is time-zone unaware. Therefore, the LocalDateTime is created in UTC
    return LocalDateTime.ofInstant(
        ExprValueUtils.fromObjectValue(input, ExprCoreType.TIMESTAMP).timestampValue(),
        ZoneOffset.UTC);
  }

  static LocalDateTime parseTime(String input) {
    return LocalDateTime.ofInstant(
        (new ExprTimeValue(input)).timestampValue(new FunctionProperties()), ZoneOffset.UTC);
  }

  static LocalDateTime parseDate(String input) {
    return LocalDateTime.ofInstant(
        ExprValueUtils.fromObjectValue(input, ExprCoreType.DATE).timestampValue(), ZoneOffset.UTC);
  }
}
