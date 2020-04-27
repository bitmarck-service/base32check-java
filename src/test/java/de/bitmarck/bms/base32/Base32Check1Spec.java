package de.bitmarck.bms.base32;

import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class Base32Check1Spec {
    static class Base32Check1Entry {
        public final String base32;
        public final char checksum;

        public Base32Check1Entry(String base32, char checksum) {
            this.base32 = base32;
            this.checksum = checksum;
        }
    }

    private final Base32Check1Entry[] table = new Base32Check1Entry[]{
            new Base32Check1Entry("", 'A'),
            new Base32Check1Entry("A", 'A'),
            new Base32Check1Entry("AB", 'Q'),
            new Base32Check1Entry("ABC", 'J'),
            new Base32Check1Entry("ABCD", 'V'),
            new Base32Check1Entry("ABCDE", 'I'),
            new Base32Check1Entry("ABCDEF", 'G'),
            new Base32Check1Entry("ABCDEFG", 'A'),
            new Base32Check1Entry("ABCDEFGH", 'T'),
            new Base32Check1Entry("ABCDEFGHI", '5'),
            new Base32Check1Entry("ABCDEFGHIJ", 'K'),
            new Base32Check1Entry("ABCDEFGHIJK", 'A'),
            new Base32Check1Entry("ABCDEFGHIJKL", 'F'),
            new Base32Check1Entry("ABCDEFGHIJKLM", 'U'),
            new Base32Check1Entry("ABCDEFGHIJKLMN", 'M'),
            new Base32Check1Entry("ABCDEFGHIJKLMNO", 'R'),
            new Base32Check1Entry("ABCDEFGHIJKLMNOP", '7'),
            new Base32Check1Entry("ABCDEFGHIJKLMNOPQ", 'X'),
            new Base32Check1Entry("ABCDEFGHIJKLMNOPQR", 'D'),
            new Base32Check1Entry("ABCDEFGHIJKLMNOPQRS", 'I'),
            new Base32Check1Entry("ABCDEFGHIJKLMNOPQRST", '5'),
            new Base32Check1Entry("ABCDEFGHIJKLMNOPQRSTU", 'U'),
            new Base32Check1Entry("ABCDEFGHIJKLMNOPQRSTUV", 'Q'),
            new Base32Check1Entry("ABCDEFGHIJKLMNOPQRSTUVW", 'D'),
            new Base32Check1Entry("ABCDEFGHIJKLMNOPQRSTUVWX", 'K'),
            new Base32Check1Entry("ABCDEFGHIJKLMNOPQRSTUVWXY", 'J'),
            new Base32Check1Entry("ABCDEFGHIJKLMNOPQRSTUVWXYZ", 'Y'),
            new Base32Check1Entry("ABCDEFGHIJKLMNOPQRSTUVWXYZ2", 'R'),
            new Base32Check1Entry("ABCDEFGHIJKLMNOPQRSTUVWXYZ23", 'V'),
            new Base32Check1Entry("ABCDEFGHIJKLMNOPQRSTUVWXYZ234", 'U'),
            new Base32Check1Entry("ABCDEFGHIJKLMNOPQRSTUVWXYZ2345", 'U'),
            // 31 chars % 31 == 0 chars - see https://github.com/espadrine/base32check/pull/2 :
            new Base32Check1Entry("ABCDEFGHIJKLMNOPQRSTUVWXYZ23456", 'V'),
            new Base32Check1Entry("ABCDEFGHIJKLMNOPQRSTUVWXYZ234567", 'V'),
            // 62 chars % 31 == 0 chars - see https://github.com/espadrine/base32check/pull/2 :
            new Base32Check1Entry("ABCDEFGHIJKLMNOPQRSTUVWXYZ234567ABCDEFGHIJKLMNOPQRSTUVWXYZ2345", '6'),
            // 93 chars % 31 == 0 chars - see https://github.com/espadrine/base32check/pull/2 :
            new Base32Check1Entry("ABCDEFGHIJKLMNOPQRSTUVWXYZ234567ABCDEFGHIJKLMNOPQRSTUVWXYZ234567ABCDEFGHIJKLMNOPQRSTUVWXYZ234", 'K'), // 93 chars
            new Base32Check1Entry("CONSECRATIO", 'X'),
            new Base32Check1Entry("CAFEBABE", 'N'),
            new Base32Check1Entry("CAFEDEAD", 'A'),
            new Base32Check1Entry("DEADBEEF", 'L'),
            new Base32Check1Entry("234567", 'Z')
    };

    @Test
    void computeChecksum() {
        Base32Check1 base32Check1 = Base32Check1.getInstance();

        assertAll(
                Stream.of(table).map(entry -> () -> assertEquals(base32Check1.compute(entry.base32), entry.checksum))
        );
    }

    @Test
    void acceptValidChecksum() {
        Base32Check1 base32Check1 = Base32Check1.getInstance();

        assertAll(
                Stream.of(table).map(entry -> () -> assertTrue(base32Check1.validate(entry.base32 + entry.checksum)))
        );
    }

    @Test
    void rejectInvalidChecksum() {
        Base32Check1 base32Check1 = Base32Check1.getInstance();

        assertAll(
                Stream.of(table).filter(entry -> entry.checksum != 'A').map(entry -> () -> assertFalse(base32Check1.validate(entry.base32)))
        );
    }

    // TODO: complete spec
}
