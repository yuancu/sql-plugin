/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.sql.expression.function.udf;

import java.util.List;
import org.apache.calcite.adapter.enumerable.NotNullImplementor;
import org.apache.calcite.adapter.enumerable.NullPolicy;
import org.apache.calcite.adapter.enumerable.RexToLixTranslator;
import org.apache.calcite.linq4j.tree.Expression;
import org.apache.calcite.linq4j.tree.Expressions;
import org.apache.calcite.rex.RexCall;
import org.apache.calcite.sql.type.OperandTypes;
import org.apache.calcite.sql.type.ReturnTypes;
import org.apache.calcite.sql.type.SqlReturnTypeInference;
import org.apache.calcite.sql.type.SqlTypeTransforms;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.opensearch.sql.expression.function.ImplementorUDF;
import org.opensearch.sql.expression.function.UDFOperandMetadata;

public class Sha2Function extends ImplementorUDF {
  private Sha2Function(NotNullImplementor implementor, NullPolicy nullPolicy) {
    super(implementor, nullPolicy);
  }

  public static Sha2Function sha2() {
    return new Sha2Function(new Sha2Implementor(), NullPolicy.ANY);
  }

  @Override
  public SqlReturnTypeInference getReturnTypeInference() {
    return ReturnTypes.VARCHAR.andThen(SqlTypeTransforms.FORCE_NULLABLE);
  }

  @Override
  public UDFOperandMetadata getOperandMetadata() {
    return UDFOperandMetadata.wrap(OperandTypes.STRING_INTEGER);
  }

  public static class Sha2Implementor implements NotNullImplementor {
    @Override
    public Expression implement(
        RexToLixTranslator translator, RexCall call, List<Expression> translatedOperands) {
      return Expressions.call(Sha2Implementor.class, "getDigest", translatedOperands);
    }

    public static String getDigest(String input, int algorithm) {
      return switch (algorithm) {
        case 224 -> Hex.encodeHexString(
            DigestUtils.getDigest(MessageDigestAlgorithms.SHA_224).digest(input.getBytes()));
        case 256 -> DigestUtils.sha256Hex(input);
        case 384 -> DigestUtils.sha384Hex(input);
        case 512 -> DigestUtils.sha512Hex(input);
        default -> throw new IllegalArgumentException(
            String.format(
                "Unsupported SHA2 algorithm: %d. Only 224, 256, 384, and 512 are supported.",
                algorithm));
      };
    }
  }
}
