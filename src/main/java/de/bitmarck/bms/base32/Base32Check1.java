package de.bitmarck.bms.base32;

import de.bitmarck.bms.base32.alphabet.Base32Alphabet;
import de.bitmarck.bms.base32.alphabet.Base32RFC4648;

public class Base32Check1 {
    private static final int cardinal = 1 << 5;
    private final int[][] primitivePowers;

    public Base32Check1(int[] primitive) {
        primitivePowers = getPrimitivePowers(primitive);
    }

    private static final Base32Check1 instance = new Base32Check1(new int[]{
            0x01, //bin"00001",
            0x11, //bin"10001",
            0x08, //bin"01000",
            0x05, //bin"00101",
            0x03, //bin"00011"
    });

    public static Base32Check1 getInstance() {
        return instance;
    }

    private static int[][] getPrimitivePowers(int[] primitive) {
        int[][] primitivePowers = new int[cardinal - 1][];

        primitivePowers[1] = primitive;

        for (int i = 2; i <= primitivePowers.length; i++) {
            int[] values = matMul(primitivePowers[i - 1], primitive);

            if (i < primitivePowers.length) {
                primitivePowers[i] = values;
            } else {
                primitivePowers[0] = values;
            }
        }

        return primitivePowers;
    }

    private static int[] matMul(int[] a, int[] b) {
        int[] mat = new int[a.length];
        for (int i = 0; i < a.length; i++) {
            mat[i] = 0;
            for (int j = 0; j < b.length; j++) {
                if ((a[i] & (1 << (b.length - 1 - j))) != 0) {
                    mat[i] ^= b[j];
                }
            }
        }

        return mat;
    }

    public char compute(String payload) {
        return compute(payload, Base32RFC4648.getInstance());
    }

    public char compute(String payload, Base32Alphabet alphabet) {
        final int len = payload.length();

        int sum = 0;
        for (int i = 0; i < len; i++) {
            int value = alphabet.toIndex(payload.charAt(i));
            sum ^= matMul(new int[]{value}, primitivePowers[(i + 1) % (cardinal - 1)])[0];
        }

        int exp = (cardinal - len - 2) % (cardinal - 1);
        if (exp < 0) {
            exp += cardinal - 1;
        }

        return alphabet.toChar(matMul(new int[]{sum}, primitivePowers[exp])[0]);
    }

    public boolean validate(String payload) {
        return validate(payload, Base32RFC4648.getInstance());
    }

    public boolean validate(String payload, Base32Alphabet alphabet) {
        return compute(payload, alphabet) == 'A';
    }
}
