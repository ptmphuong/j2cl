/*
 * Copyright 2022 Google Inc.
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
package com.google.j2cl.transpiler.backend.kotlin

import com.google.j2cl.transpiler.ast.ArrayTypeDescriptor
import com.google.j2cl.transpiler.ast.DeclaredTypeDescriptor
import com.google.j2cl.transpiler.ast.IntersectionTypeDescriptor
import com.google.j2cl.transpiler.ast.MethodDescriptor
import com.google.j2cl.transpiler.ast.PrimitiveTypeDescriptor
import com.google.j2cl.transpiler.ast.TypeDescriptor
import com.google.j2cl.transpiler.ast.TypeDescriptors
import com.google.j2cl.transpiler.ast.TypeVariable
import com.google.j2cl.transpiler.ast.UnionTypeDescriptor

internal val TypeDescriptor.isImplicitUpperBound
  get() = this == nullableAnyTypeDescriptor

// TODO(b/216796920): Remove when the bug is fixed.
private val DeclaredTypeDescriptor.directlyDeclaredTypeArgumentDescriptors: List<TypeDescriptor>
  get() = typeArgumentDescriptors.take(typeDeclaration.directlyDeclaredTypeParameterCount)

internal fun DeclaredTypeDescriptor.directlyDeclaredNonRawTypeArgumentDescriptors(
  projectToWildcards: Boolean
): List<TypeDescriptor> =
  if (!isRaw) directlyDeclaredTypeArgumentDescriptors
  else
    projectToWildcards.or(typeDeclaration.isRecursive).let { mapToWildcard ->
      typeDeclaration.directlyDeclaredTypeParameterDescriptors.map {
        if (mapToWildcard) TypeVariable.createWildcard() else it.upperBoundTypeDescriptor
      }
    }

/** Returns direct super type to use for super method call. */
internal fun DeclaredTypeDescriptor.directSuperTypeForMethodCall(
  methodDescriptor: MethodDescriptor
): DeclaredTypeDescriptor? =
  superTypesStream
    .map { superType ->
      // See if the method is in this supertype (in which case we are done) or if it is
      // overridden here (in which case this supertype is not the target).
      val declaredSuperMethodDescriptor =
        superType.declaredMethodDescriptors.find {
          it == methodDescriptor || it.isOverride(methodDescriptor)
        }
      when (declaredSuperMethodDescriptor) {
        // The method has not been found nor it is overridden in this supertype so continue looking
        // up the hierarchy; so if we find it up the hierarchy this is the supertype to return.
        null -> superType.takeIf { it.directSuperTypeForMethodCall(methodDescriptor) != null }
        // We found the implementation targeted, so return this supertype.
        methodDescriptor -> superType
        // We found an override of the method in the hierarchy, so this supertype is not providing
        // the implementation targeted.
        else -> null
      }
    }
    .filter { it != null }
    .findFirst()
    .orElse(null)

internal fun TypeDescriptor.contains(
  typeVariable: TypeVariable,
  seenTypeVariables: Set<TypeVariable> = setOf()
): Boolean =
  when (this) {
    is DeclaredTypeDescriptor -> typeArgumentDescriptors.any { it.contains(typeVariable) }
    is IntersectionTypeDescriptor -> intersectionTypeDescriptors.any { it.contains(typeVariable) }
    is ArrayTypeDescriptor -> componentTypeDescriptor?.contains(typeVariable) ?: false
    is TypeVariable ->
      if (seenTypeVariables.contains(this)) false
      else
        this == typeVariable ||
          seenTypeVariables.plus(this).let { seenTypeVariablesPlusThis ->
            upperBoundTypeDescriptor.contains(typeVariable, seenTypeVariablesPlusThis) ||
              (lowerBoundTypeDescriptor?.contains(typeVariable, seenTypeVariablesPlusThis) ?: false)
          }
    else -> false
  }

/** Returns whether this type is denotable as a top-level type of type argument. */
internal val TypeDescriptor.isDenotable
  get() =
    when (this) {
      is DeclaredTypeDescriptor -> !typeDeclaration.isAnonymous
      is TypeVariable -> !isWildcardOrCapture
      is IntersectionTypeDescriptor ->
        // The only intersection type currently supported in kotlin syntax is "T & Any".
        intersectionTypeDescriptors.let {
          it.size == 2 &&
            it[0].let { it is TypeVariable && !it.isWildcardOrCapture } &&
            it[1] == anyTypeDescriptor
        }
      is UnionTypeDescriptor -> false
      is PrimitiveTypeDescriptor -> true
      is ArrayTypeDescriptor -> true
      else -> error("Unhandled $this")
    }

internal val TypeVariable.hasNullableBounds: Boolean
  get() = upperBoundTypeDescriptor.isNullable && hasNullableRecursiveBounds

internal val TypeVariable.hasNullableRecursiveBounds: Boolean
  get() = upperBoundTypeDescriptors.all { it.canBeNullableAsBound }

/** Whether this type can be nullable when declared as an upper bound. */
internal val TypeDescriptor.canBeNullableAsBound: Boolean
  get() = this !is DeclaredTypeDescriptor || typeDeclaration.canBeNullableAsBound

/** Returns a version of this type descriptor where {@code canBeNull()} returns false. */
internal fun TypeDescriptor.makeNonNull(): TypeDescriptor =
  if (!canBeNull()) this
  else
    when (this) {
      is DeclaredTypeDescriptor -> toNonNullable()
      is TypeVariable ->
        if (!isWildcardOrCapture)
          if (hasNullableBounds)
          // Convert to {@code T & Any}
          IntersectionTypeDescriptor.newBuilder()
              .setIntersectionTypeDescriptors(listOf(this, anyTypeDescriptor))
              .build()
          else this
        else if (upperBoundTypeDescriptor.isImplicitUpperBound)
        // Ignore type variables which will be rendered as star (unbounded wildcard).
        this
        else
          TypeVariable.Builder.from(this)
            .setUpperBoundTypeDescriptorSupplier { upperBoundTypeDescriptor.makeNonNull() }
            // Set some unique ID to avoid conflict with other type variables.
            // TODO(b/246332093): Remove when the bug is fixed, and uniqueId reflects bounds
            // properly.
            .setUniqueKey(
              "<??>" +
                "+${upperBoundTypeDescriptor.makeNonNull().uniqueId}" +
                "-${lowerBoundTypeDescriptor?.uniqueId}"
            )
            .build()
      is IntersectionTypeDescriptor ->
        IntersectionTypeDescriptor.newBuilder()
          .setIntersectionTypeDescriptors(intersectionTypeDescriptors + anyTypeDescriptor)
          .build()
      is UnionTypeDescriptor ->
        UnionTypeDescriptor.newBuilder()
          .setUnionTypeDescriptors(unionTypeDescriptors.map { it.makeNonNull() })
          .build()
      is PrimitiveTypeDescriptor -> toNonNullable()
      is ArrayTypeDescriptor -> toNonNullable()
      else -> error("Unhandled $this")
    }

private val nullableAnyTypeDescriptor: TypeDescriptor
  get() = TypeDescriptors.get().javaLangObject

private val anyTypeDescriptor: TypeDescriptor
  get() = nullableAnyTypeDescriptor.toNonNullable()

internal val TypeVariable.isRecursive: Boolean
  get() = upperBoundTypeDescriptor.contains(this)
