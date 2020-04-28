package de.bitmarck.bms.base32

import org.scalacheck.Gen
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks._

class Base32Check1Spec extends AnyWordSpec {
  private val base32check1 = Base32Check1.getInstance()

  private val base32Char = Gen.oneOf(Base32Alphabet)
  private val base32String = Gen.listOfN(16, base32Char).map(_.mkString)

  "Base32Check1" should {
    val tests = Table(
      "payload" -> "check1",
      "" -> 'A',
      "A" -> 'A',
      "AB" -> 'Q',
      "ABC" -> 'J',
      "ABCD" -> 'V',
      "ABCDE" -> 'I',
      "ABCDEF" -> 'G',
      "ABCDEFG" -> 'A',
      "ABCDEFGH" -> 'T',
      "ABCDEFGHI" -> '5',
      "ABCDEFGHIJ" -> 'K',
      "ABCDEFGHIJK" -> 'A',
      "ABCDEFGHIJKL" -> 'F',
      "ABCDEFGHIJKLM" -> 'U',
      "ABCDEFGHIJKLMN" -> 'M',
      "ABCDEFGHIJKLMNO" -> 'R',
      "ABCDEFGHIJKLMNOP" -> '7',
      "ABCDEFGHIJKLMNOPQ" -> 'X',
      "ABCDEFGHIJKLMNOPQR" -> 'D',
      "ABCDEFGHIJKLMNOPQRS" -> 'I',
      "ABCDEFGHIJKLMNOPQRST" -> '5',
      "ABCDEFGHIJKLMNOPQRSTU" -> 'U',
      "ABCDEFGHIJKLMNOPQRSTUV" -> 'Q',
      "ABCDEFGHIJKLMNOPQRSTUVW" -> 'D',
      "ABCDEFGHIJKLMNOPQRSTUVWX" -> 'K',
      "ABCDEFGHIJKLMNOPQRSTUVWXY" -> 'J',
      "ABCDEFGHIJKLMNOPQRSTUVWXYZ" -> 'Y',
      "ABCDEFGHIJKLMNOPQRSTUVWXYZ2" -> 'R',
      "ABCDEFGHIJKLMNOPQRSTUVWXYZ23" -> 'V',
      "ABCDEFGHIJKLMNOPQRSTUVWXYZ234" -> 'U',
      "ABCDEFGHIJKLMNOPQRSTUVWXYZ2345" -> 'U',
      // 31 chars % 31 == 0 chars - see https://github.com/espadrine/base32check/pull/2 :
      "ABCDEFGHIJKLMNOPQRSTUVWXYZ23456" -> 'V',
      "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567" -> 'V',
      // 62 chars % 31 == 0 chars - see https://github.com/espadrine/base32check/pull/2 :
      "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567ABCDEFGHIJKLMNOPQRSTUVWXYZ2345" -> '6',
      // 93 chars % 31 == 0 chars - see https://github.com/espadrine/base32check/pull/2 :
      "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567ABCDEFGHIJKLMNOPQRSTUVWXYZ234567ABCDEFGHIJKLMNOPQRSTUVWXYZ234" -> 'K', // 93 chars
      "CONSECRATIO" -> 'X',
      "CAFEBABE" -> 'N',
      "CAFEDEAD" -> 'A',
      "DEADBEEF" -> 'L',
      "234567" -> 'Z'
    ).ensuring(
      _.forall { case (payload, check1) => Base32Regex.matches(payload + check1) },
      "String is not a valid Base32 string!"
    )

    "compute a checksum" in {
      forAll(tests)((payload, check1) => base32check1.compute(payload) shouldBe check1)
    }

    "accept a valid checksum" in {
      forAll(tests)((payload, check1) => base32check1.validate(payload + check1) shouldBe true)
    }

    "reject an invalid checksum" in {
      forAll(tests)((payload, check1) => whenever(check1 != 'A')(base32check1.validate(payload) shouldBe false))
    }

    "throw up on input characters not in the Base32 alphabet" in {
      val invalid = Table("payload") ++ (Char.MinValue to Char.MaxValue).map("" + _).filterNot(Base32Alphabet.contains)
      forAll(invalid)(payload => intercept[IllegalArgumentException](base32check1.compute(payload)))
    }

    // https://espadrine.github.io/blog/posts/a-base32-checksum.html
    "detect all single character substitutions (1sub)" in {
      forAllBase32Strings { string =>
        forAll(base32Char) { char =>
          assertAll(string.indices.map(string -> string.updated(_, char)))
        }
      }
    }

    // https://espadrine.github.io/blog/posts/a-base32-checksum.html
    "detect all character transpositions with zero characters in between them (0-trans)" in {
      forAllBase32Strings { string =>
        assertAll(string.indices.drop(1).map { i =>
          string -> string.updated(i - 1, string(i)).updated(i, string(i - 1))
        })
      }
    }

    // https://espadrine.github.io/blog/posts/a-base32-checksum.html
    "detect all character transpositions with one character in between them (1-trans)" in {
      forAllBase32Strings { string =>
        assertAll(string.indices.drop(2).map { i =>
          string -> string.updated(i - 2, string(i)).updated(i, string(i - 2))
        })
      }
    }

    // https://espadrine.github.io/blog/posts/a-base32-checksum.html
    "detect all identical substitutions of two identical characters with zero characters in between them (0-twin)" in {
      forAllBase32Strings { string =>
        forAll(base32Char, minSuccessful(1)) { origChar =>
          forAll(base32Char) { substChar =>
            assertAll(string.indices.drop(1).map { i =>
              string.updated(i - 1, origChar).updated(i, origChar) -> string.updated(i - 1, substChar).updated(i, substChar)
            })
          }
        }
      }
    }

    // https://espadrine.github.io/blog/posts/a-base32-checksum.html
    "detect all identical substitutions of two identical characters with one character in between them (1-twin)" in {
      forAllBase32Strings { string =>
        forAll(base32Char, minSuccessful(1)) { origChar =>
          forAll(base32Char) { substChar =>
            assertAll(string.indices.drop(2).map { i =>
              string.updated(i - 2, origChar).updated(i, origChar) -> string.updated(i - 2, substChar).updated(i, substChar)
            })
          }
        }
      }
    }
  }

  private def forAllBase32Strings(f: String => Any): Unit = {
    forAll(base32String) { string: String =>
      whenever(Base32Regex.matches(string) && string.length % 8 == 0) {
        f(string)
      }
    }
  }

  private def assertAll(tests: Seq[(String, String)]): Unit = {
    forAll(Table("original" -> "modified") ++ tests) { (original, modified) =>
      whenever(original != modified) {
        base32check1.compute(original) should not be base32check1.compute(modified)
      }
    }
  }
}
