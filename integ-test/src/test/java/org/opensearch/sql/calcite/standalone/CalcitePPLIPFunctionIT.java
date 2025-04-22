/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.sql.calcite.standalone;

import static org.opensearch.sql.legacy.TestsConstants.TEST_INDEX_GEOIP;
import static org.opensearch.sql.legacy.TestsConstants.TEST_INDEX_WEBLOGS;
import static org.opensearch.sql.util.MatcherUtils.columnName;
import static org.opensearch.sql.util.MatcherUtils.rows;
import static org.opensearch.sql.util.MatcherUtils.schema;
import static org.opensearch.sql.util.MatcherUtils.verifyColumn;
import static org.opensearch.sql.util.MatcherUtils.verifyDataRows;
import static org.opensearch.sql.util.MatcherUtils.verifySchema;

import java.io.IOException;
import java.util.Map;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;

public class CalcitePPLIPFunctionIT extends CalcitePPLIntegTestCase {
  @Override
  public void init() throws IOException {
    super.init();
    loadIndex(Index.GEOIP);
    loadIndex(Index.WEBLOG);
  }

  @Ignore
  @Test
  public void testGeoIpEnrichment() {

    JSONObject resultGeoIp =
        executeQuery(
            String.format(
                "search source=%s | eval enrichmentResult = geoip(\\\"%s\\\",%s)",
                TEST_INDEX_GEOIP, "dummycityindex", "ip"));

    verifyColumn(resultGeoIp, columnName("name"), columnName("ip"), columnName("enrichmentResult"));
    verifyDataRows(
        resultGeoIp,
        rows("Test user - USA", "10.1.1.1", Map.of("country", "USA", "city", "Seattle")),
        rows("Test user - Canada", "127.1.1.1", Map.of("country", "Canada", "city", "Vancouver")));
  }

  @Ignore
  @Test
  public void testGeoIpEnrichmentWithSingleOption() {

    JSONObject resultGeoIp =
        executeQuery(
            String.format(
                "search source=%s | eval enrichmentResult = geoip(\\\"%s\\\",%s,\\\"%s\\\")",
                TEST_INDEX_GEOIP, "dummycityindex", "ip", "city"));

    verifyColumn(resultGeoIp, columnName("name"), columnName("ip"), columnName("enrichmentResult"));
    verifyDataRows(
        resultGeoIp,
        rows("Test user - USA", "10.1.1.1", Map.of("city", "Seattle")),
        rows("Test user - Canada", "127.1.1.1", Map.of("city", "Vancouver")));
  }

  @Ignore
  @Test
  public void testGeoIpEnrichmentWithSpaceSeparatedMultipleOptions() {

    JSONObject resultGeoIp =
        executeQuery(
            String.format(
                "search source=%s | eval enrichmentResult = geoip(\\\"%s\\\",%s,\\\"%s\\\")",
                TEST_INDEX_GEOIP, "dummycityindex", "ip", "city , country"));

    verifyColumn(resultGeoIp, columnName("name"), columnName("ip"), columnName("enrichmentResult"));
    verifyDataRows(
        resultGeoIp,
        rows("Test user - USA", "10.1.1.1", Map.of("country", "USA", "city", "Seattle")),
        rows("Test user - Canada", "127.1.1.1", Map.of("country", "Canada", "city", "Vancouver")));
  }

  @Test
  public void testCidrMatch() {

    JSONObject result;

    // No matches
    result =
        executeQuery(
            String.format(
                "source=%s | where cidrmatch(host, '250.0.0.0/24') | fields host",
                TEST_INDEX_WEBLOGS));
    verifySchema(result, schema("host", null, "ip"));
    verifyDataRows(result);

    // One match
    result =
        executeQuery(
            String.format(
                "source=%s | where cidrmatch(host, '0.0.0.0/24') | fields host",
                TEST_INDEX_WEBLOGS));
    verifySchema(result, schema("host", null, "ip"));
    verifyDataRows(result, rows("0.0.0.2"));

    // Multiple matches
    result =
        executeQuery(
            String.format(
                "source=%s | where cidrmatch(host, '1.2.3.0/24') | fields host",
                TEST_INDEX_WEBLOGS));
    verifySchema(result, schema("host", null, "ip"));
    verifyDataRows(result, rows("1.2.3.4"), rows("1.2.3.5"));

    result =
        executeQuery(
            String.format(
                "source=%s | head 1 | eval m4 = CIDRMATCH('192.169.1.5', '192.169.1.0/24'), m6 ="
                    + " CIDRMATCH('2003:0db8:0000:0000:0000:0000:0000:0000', '2003:db8::/32') |"
                    + " fields m4, m6",
                TEST_INDEX_WEBLOGS));
    verifySchema(result, schema("m4", "boolean"), schema("m6", "boolean"));
    verifyDataRows(result, rows(true, true));
  }
}
