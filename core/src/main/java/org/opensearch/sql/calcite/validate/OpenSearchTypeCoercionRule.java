/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.sql.calcite.validate;

import com.google.common.collect.ImmutableSet;
import java.util.Map;
import org.apache.calcite.sql.type.SqlTypeCoercionRule;
import org.apache.calcite.sql.type.SqlTypeMappingRule;
import org.apache.calcite.sql.type.SqlTypeName;

/** Copied from {@link SqlTypeCoercionRule} as it does not allow extending. */
public class OpenSearchTypeCoercionRule implements SqlTypeMappingRule {
  OpenSearchTypeCoercionRule() {
    super();
  }

  @Override
  public Map<SqlTypeName, ImmutableSet<SqlTypeName>> getTypeMapping() {
    return Map.of();
  }
}
