package de.bitmarck.bms.base32

import java.nio.charset.StandardCharsets.US_ASCII

import org.scalacheck.Arbitrary._
import org.scalacheck.Gen._
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks._

class Base32Spec extends AnyWordSpec {
  "Base32Codec" should {
    // See [RFC 4648, Section 10](https://tools.ietf.org/html/rfc4648#section-10):
    val rfc4648Tests = Table("bytes" -> "uppercaseString") ++ Seq(
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
    val customTests = Table("bytes" -> "uppercaseString") ++ Seq(
      "cafebabe" -> "ZL7LVPQ=",
      "cafedead" -> "ZL7N5LI=",
      "deadbeef" -> "32W353Y=",
      "cafebabe deadbeef cafe" -> "ZL7LVPW6VW7O7SX6"
    ).map { case (input, output) => (BigInt(input.filter(_ != ' '), 16).toByteArray.drop(1), output) }

    val tests = (rfc4648Tests ++ customTests).ensuring(
      _.forall { case (_, uppercaseString) => Base32Regex.matches(uppercaseString) },
      "String is not a valid Base32 production!"
    )

    "decode a sequence of bytes from a string" in {
      forAll(tests) { (bytes, uppercaseString) =>
        Base32.decode(uppercaseString) shouldBe bytes
      }
    }

    "encode a sequence of bytes to a string" in {
      forAll(tests) { (bytes, uppercaseString) =>
        Base32.encode(bytes) shouldBe uppercaseString
      }
    }


    "throw up when decoding a string which is not a valid Base32 encoding" in {
      forAll(listOf(arbitrary[Char]).map(s => s.mkString), minSuccessful(100)) { string =>
        whenever(!Base32Regex.matches(string) || string.length % 8 != 0) {
          intercept[IllegalArgumentException](Base32.decode(string))
        }
      }
    }
  }
}
