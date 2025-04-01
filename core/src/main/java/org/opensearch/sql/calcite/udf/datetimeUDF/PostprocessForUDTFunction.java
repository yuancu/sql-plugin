/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.sql.calcite.udf.datetimeUDF;

import java.util.Objects;
import org.apache.calcite.runtime.SqlFunctions;
import org.apache.calcite.sql.type.SqlTypeName;
import org.opensearch.sql.calcite.udf.UserDefinedFunction;
import org.opensearch.sql.data.model.ExprValue;
import org.opensearch.sql.data.model.ExprValueUtils;

public class PostprocessForUDTFunction implements UserDefinedFunction {
  @Override
  public Object eval(Object... args) {
    Object candidate = args[0];
    if (Objects.isNull(candidate)) {
      return null;
    }
    SqlTypeName sqlTypeName = (SqlTypeName) args[1];

    Object o =
        switch (sqlTypeName) {
          case DATE -> SqlFunctions.internalToDate((int) candidate).toLocalDate();
          case TIME -> SqlFunctions.internalToTime((int) candidate).toLocalTime();
          case TIMESTAMP -> SqlFunctions.internalToTimestamp((long) candidate).toLocalDateTime();
          default -> throw new IllegalArgumentException("Unsupported sql type: " + sqlTypeName);
        };
    ExprValue datetime = ExprValueUtils.fromObjectValue(o);
    return datetime.valueForCalcite();
  }
}
