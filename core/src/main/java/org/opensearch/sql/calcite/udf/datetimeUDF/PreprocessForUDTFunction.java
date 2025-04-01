/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.sql.calcite.udf.datetimeUDF;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.runtime.SqlFunctions;
import org.apache.calcite.sql.type.SqlReturnTypeInference;
import org.apache.calcite.sql.type.SqlTypeName;
import org.opensearch.sql.calcite.udf.UserDefinedFunction;
import org.opensearch.sql.calcite.utils.UserDefinedFunctionUtils;
import org.opensearch.sql.data.model.ExprValue;
import org.opensearch.sql.data.model.ExprValueUtils;
import org.opensearch.sql.data.type.ExprCoreType;
import org.opensearch.sql.data.type.ExprType;

/** This function is to transfer UDT to calcite datetime types */
public class PreprocessForUDTFunction implements UserDefinedFunction {
  @Override
  public Object eval(Object... args) {
    Object candidate = args[0];
    if (Objects.isNull(candidate)) {
      return null;
    }
    SqlTypeName sqlTypeName = (SqlTypeName) args[1];
    ExprType exprType = UserDefinedFunctionUtils.convertSqlTypeToExprType(sqlTypeName);
    ExprValue datetime = ExprValueUtils.fromObjectValue(candidate, exprType);

    return switch (sqlTypeName) {
      case DATE -> SqlFunctions.toInt(java.sql.Date.valueOf(datetime.dateValue()));
      case TIMESTAMP ->
      // datetime does not carry timezone information, use UTC to avoid timezone offset issues.
      SqlFunctions.toLong(
          java.sql.Timestamp.valueOf(
              LocalDateTime.ofInstant(datetime.timestampValue(), ZoneOffset.UTC)));
      case TIME -> SqlFunctions.toInt(java.sql.Time.valueOf(datetime.timeValue()));
      default -> throw new IllegalArgumentException("Unsupported sql type: " + sqlTypeName);
    };
  }

  public static SqlReturnTypeInference getSqlReturnTypeInference(ExprType type) {
    return opBinding -> {
      RelDataType relDataType;
      switch (type) {
        case ExprCoreType.DATE:
          relDataType = opBinding.getTypeFactory().createSqlType(SqlTypeName.DATE);
          break;
        case ExprCoreType.TIME:
          relDataType = opBinding.getTypeFactory().createSqlType(SqlTypeName.TIME);
          break;
        case ExprCoreType.TIMESTAMP:
          relDataType = opBinding.getTypeFactory().createSqlType(SqlTypeName.TIMESTAMP);
          break;
        default:
          throw new IllegalArgumentException("Unsupported sql type: " + type);
      }
      return opBinding.getTypeFactory().createTypeWithNullability(relDataType, true);
    };
  }

  public static SqlTypeName getInputType(ExprType type) {
    switch (type) {
      case ExprCoreType.DATE:
        return SqlTypeName.DATE;
      case ExprCoreType.TIME:
        return SqlTypeName.TIME;
      case ExprCoreType.TIMESTAMP:
        return SqlTypeName.TIMESTAMP;
      default:
        throw new IllegalArgumentException("Unsupported sql type: " + type);
    }
  }
}
