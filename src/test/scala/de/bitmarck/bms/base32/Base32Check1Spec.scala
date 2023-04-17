package de.bitmarck.bms.base32

import munit.FunSuite

import scala.util.Random

class Base32Check1Spec extends FunSuite {
  private val base32Check1 = Base32Check1.getInstance()

  private val tests = Seq[(String, Char)](
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

  private def randomString: String =
    Array.fill(Random.nextInt(16) + 1)(Base32Alphabet(Random.nextInt(Base32Alphabet.length))).mkString

  private lazy val randomBase32Strings: Seq[String] =
    Iterator.continually(randomString)
      .filter(string => Base32Regex.matches(string) && string.length % 8 == 0)
      .take(20)
      .toSeq

  private def assertDifferentChecksum(original: String, modified: String): Unit =
    if (original != modified)
      assertNotEquals(base32Check1.compute(original), base32Check1.compute(modified))

  test("Base32Check1 should compute a checksum") {
    tests.foreach { case (payload, check1) =>
      assertEquals(base32Check1.compute(payload), check1)
    }
  }

  test("Base32Check1 should accept a valid checksum") {
    tests.foreach { case (payload, check1) =>
      assert(base32Check1.validate(payload + check1))
    }
  }

  test("Base32Check1 should reject an invalid checksum") {
    tests.foreach { case (payload, check1) =>
      assertEquals(base32Check1.validate(payload), check1 == 'A')
    }
  }

  test("Base32Check1 should throw up on input characters not in the Base32 alphabet") {
    val invalid: Seq[String] = (Char.MinValue to Char.MaxValue).map("" + _).filterNot(Base32Alphabet.contains)
    invalid.foreach { payload =>
      intercept[IllegalArgumentException](base32Check1.compute(payload))
    }
  }

  // https://espadrine.github.io/blog/posts/a-base32-checksum.html
  test("Base32Check1 should detect all single character substitutions (1sub)") {
    randomBase32Strings.foreach { string =>
      Base32Alphabet.foreach { char =>
        string.indices.map {
          string -> string.updated(_, char)
        }.foreach { case (original, modified) =>
          assertDifferentChecksum(original, modified)
        }
      }
    }
  }

  // https://espadrine.github.io/blog/posts/a-base32-checksum.html
  test("Base32Check1 should detect all character transpositions with zero characters in between them (0-trans)") {
    randomBase32Strings.foreach { string =>
      string.indices.drop(1)
        .map { i =>
          string -> string.updated(i - 1, string(i)).updated(i, string(i - 1))
        }.foreach { case (original, modified) =>
        assertDifferentChecksum(original, modified)
      }
    }
  }

  // https://espadrine.github.io/blog/posts/a-base32-checksum.html
  test("Base32Check1 should detect all character transpositions with one character in between them (1-trans)") {
    randomBase32Strings.foreach { string =>
      string.indices.drop(2).map { i =>
        string -> string.updated(i - 2, string(i)).updated(i, string(i - 2))
      }.foreach { case (original, modified) =>
        assertDifferentChecksum(original, modified)
      }
    }
  }

  // https://espadrine.github.io/blog/posts/a-base32-checksum.html
  test("Base32Check1 should detect all identical substitutions of two identical characters with zero characters in between them (0-twin)") {
    randomBase32Strings.foreach { string =>
      Base32Alphabet.foreach { origChar => // minSuccessful(1)
        Base32Alphabet.foreach { substChar =>
          string.indices.drop(1).map { i =>
            string.updated(i - 1, origChar).updated(i, origChar) -> string.updated(i - 1, substChar).updated(i, substChar)
          }.foreach { case (original, modified) =>
            assertDifferentChecksum(original, modified)
          }
        }
      }
    }
  }

  // https://espadrine.github.io/blog/posts/a-base32-checksum.html
  test("Base32Check1 should detect all identical substitutions of two identical characters with one character in between them (1-twin)") {
    randomBase32Strings.foreach { string =>
      Base32Alphabet.foreach { origChar => // minSuccessful(1)
        Base32Alphabet.foreach { substChar =>
          string.indices.drop(2).map { i =>
            string.updated(i - 2, origChar).updated(i, origChar) -> string.updated(i - 2, substChar).updated(i, substChar)
          }.foreach { case (original, modified) =>
            assertDifferentChecksum(original, modified)
          }
        }
      }
    }
  }
}
