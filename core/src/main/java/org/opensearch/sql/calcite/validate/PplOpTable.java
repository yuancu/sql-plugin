/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.sql.calcite.validate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.calcite.sql.SqlFunction;
import org.apache.calcite.sql.SqlFunctionCategory;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlOperator;
import org.apache.calcite.sql.SqlOperatorTable;
import org.apache.calcite.sql.SqlSyntax;
import org.apache.calcite.sql.validate.SqlNameMatcher;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.opensearch.sql.expression.function.BuiltinFunctionName;

/**
 * PPLOpTable is a custom implementation of {@link SqlOperatorTable} that provides a way to register
 * and look up PPL operators.
 */
public class PplOpTable implements SqlOperatorTable {
  // Implementation notes:
  //  - Did not extend ListSqlOperatorTable because it does not support registering multiple
  // SqlOperator to one name.
  //  - Did not extend ReflectiveSqlOperatorTable because it relies on reflectively looking for
  // member fields of
  //    SqlOperator type, which is not suitable for our use case.
  //  - Did not add SqlOperatorTable to PPLFuncImpTable to reduce chaos with existing implementation

  protected Map<BuiltinFunctionName, ArrayList<SqlOperator>> operators;

  private static final PplOpTable INSTANCE = new PplOpTable();

  public static PplOpTable getInstance() {
    return INSTANCE;
  }

  private PplOpTable() {
    this.operators = new HashMap<>();
  }

  @Override
  public void lookupOperatorOverloads(
      SqlIdentifier opName,
      @Nullable SqlFunctionCategory category,
      SqlSyntax syntax,
      List<SqlOperator> operatorList,
      SqlNameMatcher nameMatcher) {
    if (!opName.isSimple()) {
      return;
    }
    final String simpleName = opName.getSimple();
    lookUpOperators(
        simpleName,
        op -> {
          if (op.getSyntax() != syntax && op.getSyntax().family != syntax.family) {
            // Allow retrieval on exact syntax or family; for example,
            // CURRENT_DATETIME has FUNCTION_ID syntax but can also be called with
            // both FUNCTION_ID and FUNCTION syntax (e.g. SELECT CURRENT_DATETIME,
            // CURRENT_DATETIME('UTC')).
            return;
          }
          if (category != null
              && category != category(op)
              && !category.isUserDefinedNotSpecificFunction()) {
            return;
          }
          operatorList.add(op);
        });
  }

  protected void lookUpOperators(String name, Consumer<SqlOperator> consumer) {
    final BuiltinFunctionName funcNameOpt = sqlFunctionNameToPplFunctionName(name);
    if (funcNameOpt == null) {
      return; // No function with this name
    }
    if (!operators.containsKey(funcNameOpt)) {
      return; // The function is not registered
    }
    operators.get(funcNameOpt).forEach(consumer);
  }

  /**
   * At this stage, the function name of a Calcite's builtin operator is acquired via
   * `sqlFunction.getSqlIdentifier()`
   *
   * <p>This will return the name in Calcite, instead of that registered in PPL. We use this method
   * to convert the Calcite function name to the PPL function name.
   */
  private BuiltinFunctionName sqlFunctionNameToPplFunctionName(String name) {
    return switch (name.toUpperCase(Locale.ROOT)) {
      case "CONVERT" -> BuiltinFunctionName.CONV;
      case "ILIKE" -> BuiltinFunctionName.LIKE;
      case "CHAR_LENGTH" -> BuiltinFunctionName.LENGTH;
      case "NOT_EQUALS", "<>" -> BuiltinFunctionName.XOR;
      default -> BuiltinFunctionName.of(name).orElse(null);
    };
  }

  protected static SqlFunctionCategory category(SqlOperator operator) {
    if (operator instanceof SqlFunction) {
      return ((SqlFunction) operator).getFunctionType();
    } else {
      return SqlFunctionCategory.SYSTEM;
    }
  }

  @Override
  public List<SqlOperator> getOperatorList() {
    return operators.values().stream()
        .flatMap(iterable -> StreamSupport.stream(iterable.spliterator(), false))
        .collect(Collectors.toList());
  }

  public void add(BuiltinFunctionName name, SqlOperator operator) {
    ArrayList<SqlOperator> list = operators.getOrDefault(name, new ArrayList<>());
    list.add(operator);
    operators.put(name, list);
  }
}
