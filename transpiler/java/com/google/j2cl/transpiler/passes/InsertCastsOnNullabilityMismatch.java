/*
 * Copyright 2021 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.j2cl.transpiler.passes;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import com.google.j2cl.transpiler.ast.ArrayTypeDescriptor;
import com.google.j2cl.transpiler.ast.CastExpression;
import com.google.j2cl.transpiler.ast.CompilationUnit;
import com.google.j2cl.transpiler.ast.DeclaredTypeDescriptor;
import com.google.j2cl.transpiler.ast.Expression;
import com.google.j2cl.transpiler.ast.IntersectionTypeDescriptor;
import com.google.j2cl.transpiler.ast.PrimitiveTypeDescriptor;
import com.google.j2cl.transpiler.ast.TypeDescriptor;
import com.google.j2cl.transpiler.ast.TypeVariable;
import com.google.j2cl.transpiler.ast.UnionTypeDescriptor;
import com.google.j2cl.transpiler.passes.ConversionContextVisitor.ContextRewriter;

/** Inserts casts in places where necessary due to nullability differences in type arguments. */
public final class InsertCastsOnNullabilityMismatch extends NormalizationPass {
  @Override
  public void applyTo(CompilationUnit compilationUnit) {
    compilationUnit.accept(
        new ConversionContextVisitor(
            new ContextRewriter() {
              // Don't insert cast for member qualifiers, as their type is inferred.
              @Override
              public Expression rewriteMemberQualifierContext(
                  TypeDescriptor inferredTypeDescriptor,
                  TypeDescriptor actualTypeDescriptor,
                  Expression expression) {
                return expression;
              }

              @Override
              public Expression rewriteTypeConversionContext(
                  TypeDescriptor inferredTypeDescriptor,
                  TypeDescriptor actualTypeDescriptor,
                  Expression expression) {
                return needsCast(expression.getTypeDescriptor(), project(inferredTypeDescriptor))
                    ? CastExpression.newBuilder()
                        .setExpression(expression)
                        .setCastTypeDescriptor(project(inferredTypeDescriptor))
                        .build()
                    : expression;
              }
            }));
  }

  private static boolean needsCast(TypeDescriptor from, TypeDescriptor to) {
    if (!isDenotable(to, /* seenTypeVariables= */ ImmutableSet.of())) {
      return false;
    }

    if (from.isNullable() && !to.isNullable()) {
      return true;
    }

    return typeArgumentsNeedsCast(from, to);
  }

  private static boolean typeArgumentsNeedsCast(TypeDescriptor from, TypeDescriptor to) {
    return Streams.zip(
            getTypeArgumentDescriptors(from).stream(),
            getTypeArgumentDescriptors(to).stream(),
            InsertCastsOnNullabilityMismatch::typeArgumentNeedsCast)
        .anyMatch(Boolean::booleanValue);
  }

  private static boolean typeArgumentNeedsCast(TypeDescriptor from, TypeDescriptor to) {
    // Insert explicit cast from wildcard to non-wildcard, since in some cases wildcard type
    // arguments are inferred as non-wildcard in the AST.
    if (isWildcard(from) && !isWildcard(to)) {
      return true;
    }

    return from.isNullable() != to.isNullable() || typeArgumentsNeedsCast(from, to);
  }

  private static TypeDescriptor project(TypeDescriptor typeDescriptor) {
    if (typeDescriptor instanceof TypeVariable) {
      TypeVariable typeVariable = (TypeVariable) typeDescriptor;
      if (typeVariable.isWildcardOrCapture()) {
        TypeDescriptor lowerBound = typeVariable.getLowerBoundTypeDescriptor();
        if (lowerBound != null) {
          return project(lowerBound);
        }
        TypeDescriptor upperBound = typeVariable.getUpperBoundTypeDescriptor();
        return project(upperBound);
      }
    }

    return typeDescriptor;
  }

  /** Returns whether this type is denotable as a top-level type or type argument. */
  private static boolean isDenotable(
      TypeDescriptor to, ImmutableSet<TypeVariable> seenTypeVariables) {
    if (to instanceof PrimitiveTypeDescriptor) {
      return true;
    }

    if (to instanceof IntersectionTypeDescriptor || to instanceof UnionTypeDescriptor) {
      return false;
    }

    if (to instanceof DeclaredTypeDescriptor) {
      DeclaredTypeDescriptor declaredTypeDescriptor = (DeclaredTypeDescriptor) to;
      return !declaredTypeDescriptor.getTypeDeclaration().isAnonymous()
          && declaredTypeDescriptor.getTypeArgumentDescriptors().stream()
              .allMatch(it -> isDenotable(it, seenTypeVariables));
    }

    if (to instanceof ArrayTypeDescriptor) {
      ArrayTypeDescriptor arrayTypeDescriptor = (ArrayTypeDescriptor) to;
      return isDenotable(arrayTypeDescriptor.getComponentTypeDescriptor(), seenTypeVariables);
    }

    if (to instanceof TypeVariable) {
      TypeVariable typeVariable = (TypeVariable) to;
      if (!typeVariable.isWildcardOrCapture()) {
        return true;
      }
      if (seenTypeVariables.contains(typeVariable)) {
        return true;
      }
      TypeDescriptor lowerBound = typeVariable.getLowerBoundTypeDescriptor();
      if (lowerBound != null && !isDenotable(lowerBound, seenTypeVariables)) {
        return false;
      }
      TypeDescriptor upperBound = typeVariable.getUpperBoundTypeDescriptor();
      return isDenotable(
          upperBound,
          new ImmutableSet.Builder<TypeVariable>()
              .addAll(seenTypeVariables)
              .add(typeVariable)
              .build());
    }

    throw new IllegalArgumentException();
  }

  private static boolean isWildcard(TypeDescriptor typeDescriptor) {
    if (typeDescriptor instanceof TypeVariable) {
      TypeVariable typeVariable = (TypeVariable) typeDescriptor;
      return typeVariable.isWildcardOrCapture();
    }

    return false;
  }

  private static ImmutableList<TypeDescriptor> getTypeArgumentDescriptors(
      TypeDescriptor typeDescriptor) {
    if (typeDescriptor instanceof DeclaredTypeDescriptor) {
      DeclaredTypeDescriptor declaredTypeDescriptor = (DeclaredTypeDescriptor) typeDescriptor;
      return declaredTypeDescriptor.getTypeArgumentDescriptors();
    }

    if (typeDescriptor instanceof ArrayTypeDescriptor) {
      ArrayTypeDescriptor arrayTypeDescriptor = (ArrayTypeDescriptor) typeDescriptor;
      return ImmutableList.of(arrayTypeDescriptor.getComponentTypeDescriptor());
    }

    return ImmutableList.of();
  }
}
