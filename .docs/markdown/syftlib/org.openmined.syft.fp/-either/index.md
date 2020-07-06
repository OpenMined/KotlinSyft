[syftlib](../../index.md) / [org.openmined.syft.fp](../index.md) / [Either](./index.md)

# Either

`sealed class Either<out L, out R>`

Represents a value of one of two possible types (a disjoint union).
Instances of [Either](./index.md) are either an instance of [Left](-left/index.md) or [Right](-right/index.md).
FP Convention dictates that [Left](-left/index.md) is used for "failure"
and [Right](-right/index.md) is used for "success".

**See Also**

[Left](-left/index.md)

[Right](-right/index.md)

### Types

| [Left](-left/index.md) |
* Represents the left side of [Either](./index.md) class which by convention is a "Failure".
<br>`data class Left<out L> : `[`Either`](./index.md)`<L, `[`Nothing`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-nothing/index.html)`>` |
| [Right](-right/index.md) |
* Represents the right side of [Either](./index.md) class which by convention is a "Success".
<br>`data class Right<out R> : `[`Either`](./index.md)`<`[`Nothing`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-nothing/index.html)`, R>` |

### Properties

| [isLeft](is-left.md) | Returns true if this is a Left, false otherwise.`val isLeft: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [isRight](is-right.md) | Returns true if this is a Right, false otherwise.`val isRight: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |

### Functions

| [fold](fold.md) | Applies fnL if this is a Left or fnR if this is a Right.`fun fold(fnL: (L) -> `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`, fnR: (R) -> `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`): `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html) |
| [left](left.md) | Creates a Left type.`fun <L> left(a: L): Left<L>` |
| [right](right.md) | Creates a Left type.`fun <R> right(b: R): Right<R>` |

### Extension Functions

| [getOrElse](../get-or-else.md) | Returns the value from this `Right` or the given argument if this is a `Left`. Right(12).org.openmined.syft.fp.getOrElse(17) RETURNS 12 and Left(12).org.openmined.syft.fp.getOrElse(17) RETURNS 17`fun <L, R> `[`Either`](./index.md)`<L, R>.getOrElse(value: R): R` |

