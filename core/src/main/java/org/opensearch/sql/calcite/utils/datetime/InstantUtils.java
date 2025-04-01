/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.sql.calcite.utils.datetime;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public interface InstantUtils {

  /**
   * Convert epoch milliseconds to Instant.
   *
   * @param epochMillis epoch milliseconds
   * @return Instant that represents the given epoch milliseconds
   */
  public static Instant fromEpochMills(long epochMillis) {
    return Instant.ofEpochMilli(epochMillis);
  }

  /**
   * Convert internal date to Instant.
   *
   * @param date internal date in days since epoch
   * @return Instant that represents the given date at timezone UTC at 00:00:00
   */
  static Instant fromInternalDate(int date) {
    LocalDate localDate = LocalDate.ofEpochDay(date);
    return localDate.atStartOfDay(ZoneId.of("UTC")).toInstant();
  }
}
