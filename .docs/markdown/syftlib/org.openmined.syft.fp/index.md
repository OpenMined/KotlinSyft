[syftlib](../index.md) / [org.openmined.syft.fp](./index.md)

## Package org.openmined.syft.fp

### Types

| [Either](-either/index.md) | Represents a value of one of two possible types (a disjoint union). Instances of [Either](-either/index.md) are either an instance of [Left](-either/-left/index.md) or [Right](-either/-right/index.md). FP Convention dictates that [Left](-either/-left/index.md) is used for "failure" and [Right](-either/-right/index.md) is used for "success".`sealed class Either<out L, out R>` |

### Functions

| [getOrElse](get-or-else.md) | Returns the value from this `Right` or the given argument if this is a `Left`. Right(12).org.openmined.syft.fp.getOrElse(17) RETURNS 12 and Left(12).org.openmined.syft.fp.getOrElse(17) RETURNS 17`fun <L, R> `[`Either`](-either/index.md)`<L, R>.getOrElse(value: R): R` |

