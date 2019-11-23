package com.chirikhin.versionedmap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

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
    void get_throwsException_ifRequestedKeyIsNull() {
        assertThrows(NullPointerException.class, () -> concurrentVersionedMap.get(null));
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
    void getByVersion_throwsException_ifRequestedKeyIsNull() {
        assertThrows(NullPointerException.class, () -> concurrentVersionedMap.getByVersion(null, 1));
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

    @Test
    void put_throwException_ifInsertingValueIsNull() {
        Assertions.assertThrows(NullPointerException.class, () -> concurrentVersionedMap.put(DEFAULT_KEY, null));
        Assertions.assertEquals(0, concurrentVersionedMap.getCurrentVersion());
    }

    @Test
    void put_throwException_ifInsertingKeyIsNull() {
        Assertions.assertThrows(NullPointerException.class, () -> concurrentVersionedMap.put(DEFAULT_KEY, null));
        Assertions.assertEquals(0, concurrentVersionedMap.getCurrentVersion());
    }

    @Test
    void delete_nothingHappened_ifTheMapIsEmpty() {
        concurrentVersionedMap.delete(DEFAULT_KEY);
    }

    @Test
    void delete_valueIsDeletedInMapOfNewVersion() {
        concurrentVersionedMap.put(DEFAULT_KEY, DEFAULT_VALUE);

        int versionAfterDelete = concurrentVersionedMap.delete(DEFAULT_KEY);

        Assertions.assertNull(concurrentVersionedMap.get(DEFAULT_KEY));
        Assertions.assertNull(concurrentVersionedMap.getByVersion(DEFAULT_KEY, versionAfterDelete));
    }

    @Test
    void delete_valueIsStillAvailableInTheMapOfPreviousVersion() {
        int versionAfterPut = concurrentVersionedMap.put(DEFAULT_KEY, DEFAULT_VALUE);

        concurrentVersionedMap.delete(DEFAULT_KEY);

        Assertions.assertEquals(DEFAULT_VALUE, concurrentVersionedMap.getByVersion(DEFAULT_KEY, versionAfterPut));
    }

    @Test
    void put_allElementsArePut_ifMultipleThreadsPut() throws InterruptedException, ExecutionException {
        int amountOfThreads = 50;

        ArrayList<Callable<Integer>> putTasks = new ArrayList<>(amountOfThreads);

        for (int i = 0; i < amountOfThreads; ++i) {
            int key = i;
            String value = String.valueOf(i);
            putTasks.add(() -> concurrentVersionedMap.put(key, value));
        }

        List<Integer> listVersions = doConcurrently(putTasks);

        for (int i = 1; i <= amountOfThreads; ++i) {
            Assertions.assertTrue(listVersions.contains(i));
        }

        for (int i = 0; i < amountOfThreads; ++i) {
            Assertions.assertEquals(String.valueOf(i), concurrentVersionedMap.get(i));
        }

        Assertions.assertEquals(amountOfThreads, concurrentVersionedMap.getCurrentVersion());
    }

    @Test
    void delete_onlySpecifiedElementsAreDeleted_ifMultipleThreadsDelete() throws ExecutionException, InterruptedException {
        int amountOfPutCalls = 400;
        int amountOfDeletes = 50;

        for (int i = 0; i < amountOfPutCalls; ++i) {
            concurrentVersionedMap.put(i, String.valueOf(i));
        }

        List<Callable<Integer>> deleteTasks = new ArrayList<>(amountOfDeletes);

        for (int i = 0; i < amountOfDeletes; ++i) {
            int key = i;
            deleteTasks.add(() -> concurrentVersionedMap.delete(key));
        }

        List<Integer> versions = doConcurrently(deleteTasks);

        for (int i = amountOfPutCalls + 1; i <= amountOfPutCalls + amountOfDeletes; ++i) {
            Assertions.assertTrue(versions.contains(i));
        }

        Assertions.assertEquals(amountOfPutCalls + amountOfDeletes,
                concurrentVersionedMap.getCurrentVersion());

        for (int i = 0; i < amountOfDeletes; ++i) {
            Assertions.assertNull(concurrentVersionedMap.get(i));
        }

        for (int i = amountOfDeletes; i < amountOfPutCalls; ++i) {
            Assertions.assertNotNull(concurrentVersionedMap.get(i));
        }
    }

    private <V> List<V> doConcurrently(List<Callable<V>> tasks) throws InterruptedException, ExecutionException {
        int amountOfThreads = tasks.size();

        CountDownLatch readyCountDownLatch = new CountDownLatch(amountOfThreads);
        CountDownLatch startCountDownLatch = new CountDownLatch(1);

        List<FutureTask<V>> futureTasks = new ArrayList<>(amountOfThreads);

        for (int i = 0; i < amountOfThreads; ++i) {
            int counter  = i;
            FutureTask<V> futureTask = new FutureTask<>(() -> {
                readyCountDownLatch.countDown();
                startCountDownLatch.await();
                return tasks.get(counter).call();
            });

            Thread thread = new Thread(futureTask);
            thread.start();

            futureTasks.add(futureTask);
        }

        readyCountDownLatch.await();
        startCountDownLatch.countDown();

        List<V> result = new ArrayList<>(amountOfThreads);

        for (Future<V> mapVersion : futureTasks) {
            result.add(mapVersion.get());
        }

        return result;
    }

}