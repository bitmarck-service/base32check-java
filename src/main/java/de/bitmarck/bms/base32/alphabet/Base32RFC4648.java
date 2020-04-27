package de.bitmarck.bms.base32.alphabet;

import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Base 32 alphabet as defined by [[https://tools.ietf.org/html/rfc4648#section-6 RF4648 section 4]]. Whitespace is ignored.
 */
public final class Base32RFC4648 implements Base32Alphabet {
    private final char[] chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".toCharArray();
    private final CharIndicesLookup indicesLookup = new CharIndicesLookup(
            IntStream.range(0, chars.length)
                    .boxed()
                    .collect(Collectors.toMap(i -> chars[i], Function.identity()))
    );

    private Base32RFC4648() {
    }

    private static final Base32RFC4648 instance = new Base32RFC4648();

    public static Base32RFC4648 getInstance() {
        return instance;
    }

    @Override
    public char toChar(int index) {
        return chars[index];
    }

    @Override
    public int toIndex(char c) {
        return indicesLookup.getIndex(c);
    }

    @Override
    public boolean ignore(char c) {
        return Character.isWhitespace(c);
    }

    @Override
    public char pad() {
        return '=';
    }
}
