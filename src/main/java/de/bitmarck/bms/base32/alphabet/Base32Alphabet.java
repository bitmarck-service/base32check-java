package de.bitmarck.bms.base32.alphabet;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public interface Base32Alphabet {
    /**
     * Converts the specified index to a character.
     *
     * @throws IndexOutOfBoundsException if the specified byte is not supported by this alphabet
     */
    char toChar(int index);

    /**
     * Converts the specified char to an index.
     *
     * @throws IllegalArgumentException if the specified char is not supported by this alphabet
     */
    int toIndex(char c);

    /**
     * Indicates whether the specified character should be ignored.
     */
    boolean ignore(char c);

    /**
     * Padding character.
     */
    char pad();

    class CharIndicesLookup {
        private final int min;
        private final int[] indices;

        public CharIndicesLookup(Map<Character, Integer> indicesMap) {
            Set<Character> keySet = indicesMap.keySet();
            min = Collections.min(keySet);
            int max = Collections.max(keySet);

            indices = new int[max - min + 1];
            for (int i = 0; i < indices.length; i++) {
                indices[i] = indicesMap.getOrDefault((char) (i + min), -1);
            }
        }

        public int getIndex(char c) {
            int lookupIndex = c - min;
            if (lookupIndex >= 0 && lookupIndex < indices.length && indices[lookupIndex] >= 0) {
                return indices[lookupIndex];
            } else {
                throw new IllegalArgumentException();
            }
        }
    }
}
