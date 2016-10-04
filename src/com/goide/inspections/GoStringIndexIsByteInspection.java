/*
 * Copyright 2013-2016 Sergey Ignatov, Alexander Zolotov, Florin Patan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.goide.inspections;

import com.goide.GoTypes;
import com.goide.psi.*;
import com.goide.psi.impl.GoTypeUtil;
import com.goide.quickfix.GoStringIndexIsByteQuickFix;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.Trinity;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

public class GoStringIndexIsByteInspection extends GoInspectionBase {

  private static final String TEXT_HINT = "Mismatched types: byte and string";
  private static final GoStringIndexIsByteQuickFix STRING_INDEX_IS_BYTE_QUICK_FIX = new GoStringIndexIsByteQuickFix();

  @NotNull
  @Override
  protected GoVisitor buildGoVisitor(@NotNull ProblemsHolder holder, @NotNull LocalInspectionToolSession session) {
    return new GoVisitor() {

      @Override
      public void visitConditionalExpr(@NotNull GoConditionalExpr o) {
        GoExpression left = o.getLeft();
        GoExpression right = o.getRight();

        GoIndexOrSliceExpr indexExpr;
        GoStringLiteral stringLiteral;
        if (left instanceof GoIndexOrSliceExpr && right instanceof GoStringLiteral) {
          indexExpr = (GoIndexOrSliceExpr)left;
          stringLiteral = (GoStringLiteral)right;
        }
        else if (right instanceof GoIndexOrSliceExpr && left instanceof GoStringLiteral) {
          indexExpr = (GoIndexOrSliceExpr)right;
          stringLiteral = (GoStringLiteral)left;
        }
        else {
          return;
        }

        if (isStringIndexExpression(indexExpr)) {
          LocalQuickFix[] fixes =
            isSingleCharLiteral(stringLiteral) ? new LocalQuickFix[]{STRING_INDEX_IS_BYTE_QUICK_FIX} : LocalQuickFix.EMPTY_ARRAY;
          holder.registerProblem(o, TEXT_HINT, ProblemHighlightType.GENERIC_ERROR, fixes);
        }
      }
    };
  }

  private static boolean isStringIndexExpression(@NotNull GoIndexOrSliceExpr expr) {
    GoExpression expression = expr.getExpression();
    GoType type = expression != null ? expression.getGoType(null) : null;

    if (!GoTypeUtil.isString(type)) {
      return false;
    }

    Trinity<GoExpression, GoExpression, GoExpression> indices = expr.getIndices();
    return indices.getSecond() == null
           && indices.getThird() == null
           && expr.getNode().getChildren(TokenSet.create(GoTypes.COLON)).length == 0;
  }

  public static boolean isSingleCharLiteral(@NotNull GoStringLiteral literal) {
    return literal.getDecodedText().length() == 1;
  }
}
