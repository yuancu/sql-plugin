/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.sql.calcite.validate;

import org.apache.calcite.jdbc.CalcitePrepare;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.prepare.CalciteCatalogReader;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.server.CalciteServerStatement;
import org.apache.calcite.sql.type.SqlTypeCoercionRule;
import org.apache.calcite.sql.validate.SqlValidator;
import org.apache.calcite.sql.validate.implicit.TypeCoercion;
import org.apache.calcite.tools.FrameworkConfig;
import org.opensearch.sql.calcite.utils.OpenSearchTypeFactory;

public class TypeChecker {
  public static SqlValidator getValidator(
      CalciteServerStatement statement, FrameworkConfig config) {
    SchemaPlus defaultSchema = config.getDefaultSchema();

    final CalcitePrepare.Context prepareContext = statement.createPrepareContext();
    final CalciteSchema schema =
        defaultSchema != null ? CalciteSchema.from(defaultSchema) : prepareContext.getRootSchema();
    CalciteCatalogReader catalogReader =
        new CalciteCatalogReader(
            schema.root(),
            schema.path(null),
            OpenSearchTypeFactory.TYPE_FACTORY,
            prepareContext.config());
    SqlValidator.Config validatorConfig =
        SqlValidator.Config.DEFAULT
            .withTypeCoercionRules(getTypeCoercionRule())
            .withTypeCoercionFactory(TypeChecker::createTypeCoercion);
    return new PplValidator(
        PplOpTable.getInstance(),
        catalogReader,
        OpenSearchTypeFactory.TYPE_FACTORY,
        validatorConfig);
  }

  public static SqlTypeCoercionRule getTypeCoercionRule() {
    var defaultMapping = SqlTypeCoercionRule.instance().getTypeMapping();
    // try deleting all coercion rules
    return SqlTypeCoercionRule.instance(defaultMapping);
  }

  /** Creates a custom TypeCoercion instance for PPL. This can be used as a TypeCoercionFactory. */
  public static TypeCoercion createTypeCoercion(
      RelDataTypeFactory typeFactory, SqlValidator validator) {
    return new PplTypeCoercion(typeFactory, validator);
  }
}
