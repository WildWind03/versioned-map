package com.chirikhin.versionedmap;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ConcurrentVersionedMapTest {

    private static final int DEFAULT_KEY = 0;
    private static final int DEFAULT_KEY_2 = 1;
    private static final String DEFAULT_VALUE = "VALUE";
    private static final String DEFAULT_VALUE_2 = "VALUE2";

    private final ConcurrentVersionedMap<Integer, String> concurrentVersionedMap = new ConcurrentVersionedMap<>();

    @Test
    void get_returnNull_ifThereIsNoValueCorrespondingToKey() {
        assertNull(concurrentVersionedMap.get(DEFAULT_KEY));
    }

    @Test
    void get_returnElement_ifThereIsValueCorrespondingToKey() {
        concurrentVersionedMap.put(DEFAULT_KEY, DEFAULT_VALUE);

        assertEquals(DEFAULT_VALUE, concurrentVersionedMap.get(DEFAULT_KEY));
    }

    @Test
    void get_returnValueStoredInMapOfLastVersion_ifMapOfPreviousVersionHadItsOwnValueCorrespondingToKey() {
        concurrentVersionedMap.put(DEFAULT_KEY, DEFAULT_VALUE);
        concurrentVersionedMap.put(DEFAULT_KEY, DEFAULT_VALUE_2);

        assertEquals(DEFAULT_VALUE_2, concurrentVersionedMap.get(DEFAULT_KEY));
    }

    @Test
    void getByVersion_returnNull_ifThereIsNoValueCorrespondingToKey() {
        assertNull(concurrentVersionedMap.getByVersion(DEFAULT_KEY, 1));
    }

    @Test
    void getByVersion_returnNull_ifThereIsNoValueCorrespondingToKeyInMapOfSpecifiedVersion() {
        concurrentVersionedMap.put(DEFAULT_KEY, DEFAULT_VALUE);
        concurrentVersionedMap.put(DEFAULT_KEY_2, DEFAULT_VALUE_2);

        assertNull(concurrentVersionedMap.getByVersion(DEFAULT_KEY_2, 1));
    }

    @Test
    void getByVersion_returnValueStoredInMapOfCorrectVersion_ifMultipleElementsWerePutByTheSameKey() {
        concurrentVersionedMap.put(DEFAULT_KEY, DEFAULT_VALUE);
        concurrentVersionedMap.put(DEFAULT_KEY, DEFAULT_VALUE_2);

        assertEquals(DEFAULT_VALUE, concurrentVersionedMap.getByVersion(DEFAULT_KEY, 1));
        assertEquals(DEFAULT_VALUE_2, concurrentVersionedMap.getByVersion(DEFAULT_KEY, 2));
    }

    @Test
    void getByVersion_returnValueStoredInMapOfLastVersion_ifRequestedVersionIsTooHigh() {
        concurrentVersionedMap.put(DEFAULT_KEY, DEFAULT_VALUE);

        assertEquals(DEFAULT_VALUE, concurrentVersionedMap.getByVersion(DEFAULT_KEY, 2));
    }

    @Test
    void getCurrentVersion_returnZero_ifNoElementsWerePut() {
        assertEquals(0, concurrentVersionedMap.getCurrentVersion());
    }

    @Test
    void getCurrentVersion_returnOne_ifOneElementWasPut() {
        concurrentVersionedMap.put(DEFAULT_KEY, DEFAULT_VALUE);

        assertEquals(1, concurrentVersionedMap.getCurrentVersion());
    }

    @Test
    void getCurrentVersion_returnTwo_ifTwoElementsWerePut() {
        concurrentVersionedMap.put(DEFAULT_KEY, DEFAULT_VALUE);
        concurrentVersionedMap.put(DEFAULT_KEY_2, DEFAULT_VALUE_2);

        assertEquals(2, concurrentVersionedMap.getCurrentVersion());
    }

    @Test
    void clear_versionIsSetToZero_ifMapIsEmpty() {
        concurrentVersionedMap.clear();

        assertEquals(0, concurrentVersionedMap.getCurrentVersion());
    }

    @Test
    void clear_versionIsSetToZero_ifMapIsNotEmpty() {
        concurrentVersionedMap.put(DEFAULT_KEY, DEFAULT_VALUE);

        concurrentVersionedMap.clear();

        assertEquals(0, concurrentVersionedMap.getCurrentVersion());
    }

    @Test
    void clear_elementIsRemoved() {
        concurrentVersionedMap.put(DEFAULT_KEY, DEFAULT_VALUE);

        concurrentVersionedMap.clear();

        assertNull(concurrentVersionedMap.get(DEFAULT_KEY));
    }

    @Test
    void put_returnCorrectVersion_ifPutWasMadeOnce() {
        int newVersion = concurrentVersionedMap.put(DEFAULT_KEY, DEFAULT_VALUE);

        assertEquals(1, newVersion);
    }

    @Test
    void put_returnCorrectVersion_ifPutWasMadeTwice() {
        concurrentVersionedMap.put(DEFAULT_KEY, DEFAULT_VALUE);
        int versionAfterSecondPut = concurrentVersionedMap.put(DEFAULT_KEY, DEFAULT_VALUE_2);

        assertEquals(2, versionAfterSecondPut);
    }
}