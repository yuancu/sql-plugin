/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.sql.data.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.opensearch.sql.data.model.ExprValueUtils.integerValue;
import static org.opensearch.sql.data.type.ExprCoreType.TIME;
import static org.opensearch.sql.data.type.ExprCoreType.TIMESTAMP;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;
import org.opensearch.sql.exception.ExpressionEvaluationException;
import org.opensearch.sql.expression.function.FunctionProperties;

public class DateTimeValueTest {

  private static final int NANOS_PRECISION_MAX = 9;

  @Test
  public void timeValueInterfaceTest() {
    ExprTimeValue timeValue = new ExprTimeValue("01:01:01");

    assertEquals(TIME, timeValue.type());

    assertEquals(LocalTime.parse("01:01:01"), timeValue.timeValue());
    // It is prohibited to acquire values which include date part from `ExprTimeValue`
    // without a FunctionProperties object
    var exception = assertThrows(ExpressionEvaluationException.class, timeValue::dateValue);
    assertEquals("invalid to get dateValue from value of type TIME", exception.getMessage());
    exception = assertThrows(ExpressionEvaluationException.class, timeValue::timestampValue);
    assertEquals("invalid to get timestampValue from value of type TIME", exception.getMessage());

    var functionProperties = new FunctionProperties();
    var today = LocalDate.now(functionProperties.getQueryStartClock());

    assertEquals(today, timeValue.dateValue(functionProperties));
    assertEquals(
        today.atTime(1, 1, 1),
        LocalDateTime.ofInstant(timeValue.timestampValue(functionProperties), ZoneOffset.UTC));
    assertEquals(
        ZonedDateTime.of(LocalTime.parse("01:01:01").atDate(today), ZoneOffset.UTC).toInstant(),
        timeValue.timestampValue(functionProperties));

    assertEquals("01:01:01", timeValue.value());
    assertEquals("TIME '01:01:01'", timeValue.toString());
    exception =
        assertThrows(ExpressionEvaluationException.class, () -> integerValue(1).timeValue());
    assertEquals("invalid to get timeValue from value of type INTEGER", exception.getMessage());
  }

  @Test
  public void timestampValueInterfaceTest() {
    ExprValue timestampValue = new ExprTimestampValue("2020-07-07 01:01:01");

    assertEquals(TIMESTAMP, timestampValue.type());
    assertEquals(
        ZonedDateTime.of(LocalDateTime.parse("2020-07-07T01:01:01"), ZoneOffset.UTC).toInstant(),
        timestampValue.timestampValue());
    assertEquals("2020-07-07 01:01:01", timestampValue.value());
    assertEquals("TIMESTAMP '2020-07-07 01:01:01'", timestampValue.toString());
    assertEquals(LocalDate.parse("2020-07-07"), timestampValue.dateValue());
    assertEquals(LocalTime.parse("01:01:01"), timestampValue.timeValue());
    assertEquals(
        LocalDateTime.parse("2020-07-07T01:01:01"),
        LocalDateTime.ofInstant(timestampValue.timestampValue(), ZoneOffset.UTC));
    assertThrows(
        ExpressionEvaluationException.class,
        () -> integerValue(1).timestampValue(),
        "invalid to get timestampValue from value of type INTEGER");
  }

  @Test
  public void dateValueInterfaceTest() {
    ExprValue dateValue = new ExprDateValue("2012-07-07");

    assertEquals(LocalDate.parse("2012-07-07"), dateValue.dateValue());
    assertEquals(LocalTime.parse("00:00:00"), dateValue.timeValue());
    assertEquals(
        LocalDateTime.parse("2012-07-07T00:00:00"),
        LocalDateTime.ofInstant(dateValue.timestampValue(), ZoneOffset.UTC));
    assertEquals(
        ZonedDateTime.of(LocalDateTime.parse("2012-07-07T00:00:00"), ZoneOffset.UTC).toInstant(),
        dateValue.timestampValue());
    Throwable exception =
        assertThrows(ExpressionEvaluationException.class, () -> integerValue(1).dateValue());
    assertEquals("invalid to get dateValue from value of type INTEGER", exception.getMessage());
  }

  @Test
  public void dateInUnsupportedFormat() {
    Throwable exception =
        assertThrows(ExpressionEvaluationException.class, () -> new ExprDateValue("2020-07-07Z"));
    assertEquals(
        "date:2020-07-07Z in unsupported format, please use 'yyyy-MM-dd'", exception.getMessage());
  }

  @Test
  public void timeInUnsupportedFormat() {
    Throwable exception =
        assertThrows(ExpressionEvaluationException.class, () -> new ExprTimeValue("01:01:0"));
    assertEquals(
        "time:01:01:0 in unsupported format, please use 'HH:mm:ss[.SSSSSSSSS]'",
        exception.getMessage());
  }

  @Test
  public void timestampInUnsupportedFormat() {
    Throwable exception =
        assertThrows(
            ExpressionEvaluationException.class,
            () -> new ExprTimestampValue("2020-07-07T01:01:01Z"));
    assertEquals(
        "timestamp:2020-07-07T01:01:01Z in unsupported format, "
            + "please use 'yyyy-MM-dd HH:mm:ss[.SSSSSSSSS]'",
        exception.getMessage());
  }

  @Test
  public void stringTimestampValue() {
    ExprValue stringValue = new ExprStringValue("2020-08-17 19:44:00");

    assertEquals(
        LocalDateTime.parse("2020-08-17T19:44:00").atZone(ZoneOffset.UTC).toInstant(),
        stringValue.timestampValue());
    assertEquals(LocalDate.parse("2020-08-17"), stringValue.dateValue());
    assertEquals(LocalTime.parse("19:44:00"), stringValue.timeValue());
    assertEquals("\"2020-08-17 19:44:00\"", stringValue.toString());

    Throwable exception =
        assertThrows(
            ExpressionEvaluationException.class,
            () -> new ExprStringValue("2020-07-07T01:01:01Z").timestampValue());
    assertEquals(
        "date:2020-07-07T01:01:01Z in unsupported format, " + "please use 'yyyy-MM-dd'",
        exception.getMessage());
  }

  @Test
  public void stringDateValue() {
    ExprValue stringValue = new ExprStringValue("2020-08-17");

    assertEquals(
        ZonedDateTime.parse("2020-08-17T00:00:00Z").toInstant(), stringValue.timestampValue());
    assertEquals(LocalDate.parse("2020-08-17"), stringValue.dateValue());
    assertEquals("\"2020-08-17\"", stringValue.toString());

    Throwable exception =
        assertThrows(
            ExpressionEvaluationException.class,
            () -> new ExprStringValue("2020-07-07Z").dateValue());
    assertEquals(
        "date:2020-07-07Z in unsupported format, please use 'yyyy-MM-dd'", exception.getMessage());
  }

  @Test
  public void stringTimeValue() {
    ExprValue stringValue = new ExprStringValue("19:44:00");

    assertEquals(LocalTime.parse("19:44:00"), stringValue.timeValue());
    assertEquals("\"19:44:00\"", stringValue.toString());

    Throwable exception =
        assertThrows(
            ExpressionEvaluationException.class, () -> new ExprStringValue("01:01:0").timeValue());
    assertEquals(
        "time:01:01:0 in unsupported format, please use 'HH:mm:ss[.SSSSSSSSS]'",
        exception.getMessage());
  }

  @Test
  public void timeWithVariableNanoPrecision() {
    String timeWithNanosFormat = "10:11:12.%s";

    // Check all lengths of nanosecond precision, up to max precision accepted
    StringBuilder nanos = new StringBuilder();
    for (int nanosPrecision = 1; nanosPrecision <= NANOS_PRECISION_MAX; nanosPrecision++) {
      nanos.append(nanosPrecision);
      String timeWithNanos = String.format(timeWithNanosFormat, nanos);

      ExprValue timeValue = new ExprTimeValue(timeWithNanos);
      assertEquals(LocalTime.parse(timeWithNanos), timeValue.timeValue());
    }
  }

  @Test
  public void timestampWithVariableNanoPrecision() {
    String dateValue = "2020-08-17";
    String timeWithNanosFormat = "10:11:12.%s";

    // Check all lengths of nanosecond precision, up to max precision accepted
    StringBuilder nanos = new StringBuilder();
    for (int nanoPrecision = 1; nanoPrecision <= NANOS_PRECISION_MAX; nanoPrecision++) {
      nanos.append(nanoPrecision);
      String timeWithNanos = String.format(timeWithNanosFormat, nanos);

      String timestampString = String.format("%s %s", dateValue, timeWithNanos);
      ExprValue timestampValue = new ExprTimestampValue(timestampString);

      assertEquals(LocalDate.parse(dateValue), timestampValue.dateValue());
      assertEquals(LocalTime.parse(timeWithNanos), timestampValue.timeValue());
      String localDateTime = String.format("%sT%s", dateValue, timeWithNanos);
      assertEquals(
          LocalDateTime.parse(localDateTime),
          LocalDateTime.ofInstant(timestampValue.timestampValue(), ZoneOffset.UTC));
    }
  }

  @Test
  public void timestampOverMaxNanoPrecision() {
    Throwable exception =
        assertThrows(
            ExpressionEvaluationException.class,
            () -> new ExprTimestampValue("2020-07-07 01:01:01.1234567890"));
    assertEquals(
        "timestamp:2020-07-07 01:01:01.1234567890 in unsupported format, please use "
            + "'yyyy-MM-dd HH:mm:ss[.SSSSSSSSS]'",
        exception.getMessage());
  }

  @Test
  public void timeOverMaxNanoPrecision() {
    Throwable exception =
        assertThrows(
            ExpressionEvaluationException.class, () -> new ExprTimeValue("01:01:01.1234567890"));
    assertEquals(
        "time:01:01:01.1234567890 in unsupported format, please use 'HH:mm:ss[.SSSSSSSSS]'",
        exception.getMessage());
  }
}
