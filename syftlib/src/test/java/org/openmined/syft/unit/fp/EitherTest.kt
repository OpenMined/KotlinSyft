package org.openmined.syft.unit.fp

import org.openmined.syft.fp.Either
import org.openmined.syft.fp.getOrElse
import org.junit.Test


class EitherTest {

    @Test
    fun `Either Right should return correct type`() {
        val result = Either.Right("ironman")

        assert(result.isRight)
        assert(!result.isLeft)
        result.fold({},
            { right ->
                assert("ironman" == right)
            })
    }

    @Test fun `Either Left should return correct type`() {
        val result = Either.Left("ironman")

        assert(result.isLeft)
        assert(!result.isRight)
        result.fold(
            { left ->
                assert("ironman" == left)
            }, {})
    }

    @Test fun `Either fold should ignore passed argument if it is Right type`() {
        val result = Either.Right("Right").getOrElse("Other")

        assert("Right" == result)
    }

    @Test fun `Either fold should return argument if it is Left type`() {
        val result = Either.Left("Left").getOrElse("Other")

        assert("Other" == result)
    }
}