/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.sql.calcite;

import org.apache.calcite.rel.rel2sql.RelToSqlConverter;
import org.apache.calcite.sql.SqlDialect;

/**
 * An extension of {@link RelToSqlConverter} to convert a relation algebra tree, translated from of
 * PPL query, into a SQL statement.
 *
 * <p>Currently, we haven't implemented any specific change to it, just leaving it for future
 * extension.
 */
public class PplRelToSqlConverter extends RelToSqlConverter {
  /**
   * Creates a RelToSqlConverter.
   *
   * @param dialect the SQL dialect to use
   */
  public PplRelToSqlConverter(SqlDialect dialect) {
    super(dialect);
  }
}
