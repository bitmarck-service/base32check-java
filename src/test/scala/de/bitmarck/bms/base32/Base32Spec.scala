package de.bitmarck.bms.base32

import munit.FunSuite

import java.nio.charset.StandardCharsets.US_ASCII
import scala.util.Random

class Base32Spec extends FunSuite {
  // See [RFC 4648, Section 10](https://tools.ietf.org/html/rfc4648#section-10):
  val rfc4648Tests: Seq[(Array[Byte], String)] = Seq[(String, String)](
    "" -> "",
    "f" -> "MY======",
    "fo" -> "MZXQ====",
    "foo" -> "MZXW6===",
    "foob" -> "MZXW6YQ=",
    "fooba" -> "MZXW6YTB",
    "foobar" -> "MZXW6YTBOI======"
  ).map { case (input, output) => (input.getBytes(US_ASCII), output) }

  // Note that `BigInt("...", 16).toByteArray` is converted using big-endian byte order including a sign bit, which
  // results in a leading sign byte, which needs to be dropped to produce the expected results:
  val customTests: Seq[(Array[Byte], String)] = Seq[(String, String)](
    "cafebabe" -> "ZL7LVPQ=",
    "cafedead" -> "ZL7N5LI=",
    "deadbeef" -> "32W353Y=",
    "cafebabe deadbeef cafe" -> "ZL7LVPW6VW7O7SX6"
  ).map { case (input, output) => (BigInt(input.filter(_ != ' '), 16).toByteArray.drop(1), output) }

  val tests: Seq[(Array[Byte], String)] = (rfc4648Tests ++ customTests).ensuring(
    _.forall { case (_, uppercaseString) => Base32Regex.matches(uppercaseString) },
    "String is not a valid Base32 production!"
  )

  test("decode a sequence of bytes from a string") {
    tests.foreach { case (bytes, uppercaseString) =>
      assertEquals(Base32.decode(uppercaseString).toSeq, bytes.toSeq)
    }
  }

  test("encode a sequence of bytes to a string") {
    tests.foreach { case (bytes, uppercaseString) =>
      assertEquals(Base32.encode(bytes), uppercaseString)
    }
  }

  private def randomString: String = Random.nextString(Random.nextInt(32) + 1)

  lazy val randomInvalidBase32Strings: Seq[String] =
    Iterator.continually(randomString)
      .filter(string => !Base32Regex.matches(string) || string.length % 8 != 0)
      .take(20)
      .toSeq

  test("throw up when decoding a string which is not a valid Base32 encoding") {
    randomInvalidBase32Strings.foreach { string =>
      intercept[IllegalArgumentException](Base32.decode(string))
    }
  }
}
