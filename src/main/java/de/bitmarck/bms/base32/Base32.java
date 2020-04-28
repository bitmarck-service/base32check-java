package de.bitmarck.bms.base32;

import de.bitmarck.bms.base32.alphabet.Base32Alphabet;
import de.bitmarck.bms.base32.alphabet.Base32RFC4648;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

public class Base32 {
    /**
     * Selects at most 8 bits from a byte array as a right aligned byte
     */
    private static int bitsAtOffset(byte[] bytes, long bitIndex, int length) {
        int i = (int) (bitIndex / 8);
        if (i >= bytes.length) return 0;

        int off = (int) (bitIndex - (i * 8));
        int mask = ((1 << length) - 1) << (8 - length);
        int half = (bytes[i] << off) & mask;
        int full;
        if (off + length <= 8 || i + 1 >= bytes.length) {
            full = half;
        } else {
            full = half | ((bytes[i + 1] & ((mask << (8 - off)) & 0xFF)) >>> (8 - off));
        }

        return full >>> (8 - length);
    }

    public static String encode(byte[] bytes) {
        return encode(bytes, Base32RFC4648.getInstance());
    }

    /**
     * Converts the contents of this vector to a base 32 string using the specified alphabet.
     *
     * @group conversions
     */
    public static String encode(byte[] bytes, Base32Alphabet alphabet) {
        int bitsPerChar = 5;
        int bytesPerGroup = 5;
        int charsPerGroup = bytesPerGroup * 8 / bitsPerChar;

        CharBuffer bldr = CharBuffer.allocate((bytes.length + bytesPerGroup - 1) / bytesPerGroup * charsPerGroup);

        {
            long bidx = 0;
            while ((bidx / 8) < bytes.length) {
                char c = alphabet.toChar(bitsAtOffset(bytes, bidx, bitsPerChar));
                bldr.append(c);
                bidx += bitsPerChar;
            }
        }

        if (alphabet.pad() != (char) 0) {
            int padLen = (((bytes.length + bitsPerChar - 1) / bitsPerChar * bitsPerChar) - bytes.length) * 8 / bitsPerChar;
            int i = 0;
            while (i < padLen) {
                bldr.append(alphabet.pad());
                i += 1;
            }
        }

        return bldr.flip().toString();
    }

    public static byte[] decode(String str) {
        return decode(str, Base32RFC4648.getInstance());
    }

    /**
     * Constructs a `ByteVector` from a base 32 string or returns an error message if the string is not valid base 32.
     * An empty input string results in an empty ByteVector.
     * The string may contain whitespace characters and hyphens which are ignored.
     *
     * @group base
     */
    public static byte[] decode(String str, Base32Alphabet alphabet) {
        int bitsPerChar = 5;
        int bytesPerGroup = 5;
        int charsPerGroup = bytesPerGroup * 8 / bitsPerChar;

        char Pad = alphabet.pad();
        int idx = 0, bidx = 0, buffer = 0, padding = 0;
        ByteBuffer acc = ByteBuffer.allocate((str.length() + charsPerGroup - 1) / charsPerGroup * bytesPerGroup);
        while (idx < str.length()) {
            char c = str.charAt(idx);

            if (Pad != (char) 0 && c == Pad) {
                padding += 1;
            } else if (!alphabet.ignore(c)) {
                if (padding > 0) {
                    throw new IllegalArgumentException(
                            "Unexpected character '" + c + "' at index " + idx + " after padding character; only '=' and whitespace characters allowed after first padding character"
                    );
                }

                int index;
                try {
                    index = alphabet.toIndex(c);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Invalid base 32 character '" + c + "' at index " + idx);
                }

                buffer |= (index << (8 - bitsPerChar) >>> bidx) & 0xFF;
                bidx += bitsPerChar;

                if (bidx >= 8) {
                    bidx -= 8;
                    acc.put((byte) buffer);
                    buffer = (index << (8 - bidx)) & 0xFF;
                }
            }

            idx++;
        }

        if (bidx >= bitsPerChar) {
            acc.put((byte) buffer);
        }

        acc.flip();
        byte[] bytes = new byte[acc.remaining()];
        acc.get(bytes);

        int expectedPadding = (((bytes.length + bitsPerChar - 1) / bitsPerChar * bitsPerChar) - bytes.length) * 8 / bitsPerChar;
        if (padding != 0 && padding != expectedPadding) {
            throw new IllegalArgumentException(
                    "Malformed padding - optionally expected " + expectedPadding + " padding characters such that the quantum is completed"
            );
        }

        return bytes;
    }
}
