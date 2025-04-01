/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.sql.calcite.udf.datetimeUDF;

import java.util.Objects;
import org.opensearch.sql.calcite.udf.UserDefinedFunction;
import org.opensearch.sql.data.model.ExprValue;
import org.opensearch.sql.data.model.ExprValueUtils;

public class PostprocessDateToStringFunction implements UserDefinedFunction {
  @Override
  public Object eval(Object... args) {
    Object candidate = args[0];
    if (Objects.isNull(candidate)) {
      return null;
    }
    ExprValue value = ExprValueUtils.fromObjectValue(candidate);
    return value.valueForCalcite();
  }
}
