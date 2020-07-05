package org.openmined.syft.fp

/**
 * Copyright (C) 2019 Fernando Cejas Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Represents a value of one of two possible types (a disjoint union).
 * Instances of [Either] are either an instance of [Left] or [Right].
 * FP Convention dictates that [Left] is used for "failure"
 * and [Right] is used for "success".
 *
 * @see Left
 * @see Right
 */
sealed class Either<out L, out R> {
    /** * Represents the left side of [Either] class which by convention is a "Failure". */
    data class Left<out L>(val a: L) : Either<L, Nothing>()

    /** * Represents the right side of [Either] class which by convention is a "Success". */
    data class Right<out R>(val b: R) : Either<Nothing, R>()

    /**
     * Returns true if this is a Right, false otherwise.
     * @see Right
     */
    val isRight get() = this is Right<R>

    /**
     * Returns true if this is a Left, false otherwise.
     * @see Left
     */
    val isLeft get() = this is Left<L>

    /**
     * Creates a Left type.
     * @see Left
     */
    fun <L> left(a: L) = Left(a)


    /**
     * Creates a Left type.
     * @see Right
     */
    fun <R> right(b: R) = Right(b)

    /**
     * Applies fnL if this is a Left or fnR if this is a Right.
     * @see Left
     * @see Right
     */
    fun fold(fnL: (L) -> Any, fnR: (R) -> Any): Any =
            when (this) {
                is Left -> fnL(a)
                is Right -> fnR(b)
            }
}

///**
// * Composes 2 functions
// * See <a href="https://proandroiddev.com/kotlins-nothing-type-946de7d464fb">Credits to Alex Hart.</a>
// */
//fun <A, B, C> ((A) -> B).c(f: (B) -> C): (A) -> C = {
//    f(this(it))
//}
//
///**
// * Right-biased flatMap() FP convention which means that Right is assumed to be the default case
// * to operate on. If it is Left, operations like map, flatMap, ... return the Left value unchanged.
// */
//fun <T, L, R> org.openmined.syft.fp.Either<L, R>.flatMap(fn: (R) -> org.openmined.syft.fp.Either<L, T>): org.openmined.syft.fp.Either<L, T> =
//        when (this) {
//            is org.openmined.syft.fp.Either.Left -> org.openmined.syft.fp.Either.Left(a)
//            is org.openmined.syft.fp.Either.Right -> fn(b)
//        }
//
///**
// * Right-biased map() FP convention which means that Right is assumed to be the default case
// * to operate on. If it is Left, operations like map, flatMap, ... return the Left value unchanged.
// */
//fun <T, L, R> org.openmined.syft.fp.Either<L, R>.map(fn: (R) -> (T)): org.openmined.syft.fp.Either<L, T> = this.flatMap(fn.c(::right))
//
/** Returns the value from this `Right` or the given argument if this is a `Left`.
 *  Right(12).org.openmined.syft.fp.getOrElse(17) RETURNS 12 and Left(12).org.openmined.syft.fp.getOrElse(17) RETURNS 17
 */
fun <L, R> Either<L, R>.getOrElse(value: R): R =
        when (this) {
            is Either.Left -> value
            is Either.Right -> b
        }