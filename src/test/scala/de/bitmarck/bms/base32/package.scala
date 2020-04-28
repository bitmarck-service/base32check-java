package de.bitmarck.bms

import scala.util.matching.Regex

package object base32 {
  // See [RFC 4648, Section 6](https://tools.ietf.org/html/rfc4648#section-6):
  val Base32Alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"

  // See [RFC 4648, Section 6](https://tools.ietf.org/html/rfc4648#section-6) and also
  // [java.util.regex.Pattern](https://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html):
  val Base32Regex: Regex = s"""[$Base32Alphabet]*={0,6}""".r
}
