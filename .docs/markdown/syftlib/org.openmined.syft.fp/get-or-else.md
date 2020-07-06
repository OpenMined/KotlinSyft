[syftlib](../index.md) / [org.openmined.syft.fp](index.md) / [getOrElse](./get-or-else.md)

# getOrElse

`fun <L, R> `[`Either`](-either/index.md)`<L, R>.getOrElse(value: R): R`

Returns the value from this `Right` or the given argument if this is a `Left`.
Right(12).org.openmined.syft.fp.getOrElse(17) RETURNS 12 and Left(12).org.openmined.syft.fp.getOrElse(17) RETURNS 17

