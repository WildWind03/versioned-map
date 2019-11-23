package com.chirikhin.versionedmap;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A thread-safe implementation of {@link com.chirikhin.versionedmap.VersionedMap}
 */
public class ConcurrentVersionedMap<K, V> implements VersionedMap<K, V> {

    private final Map<K, List<Element<V>>> allVersionsMap = new ConcurrentHashMap<>();

    private final Map<K, V> lastVersionMap = new ConcurrentHashMap<>();

    private volatile int versionCounter = 0;

    /**
     * @throws NullPointerException if the specified key or value is null
     */
    @Override
    public int put(K key, V value) {
        if (null == value || null == key) {
            throw new NullPointerException("Key and value cannot be null");
        }

        return addNewElement(key, value);
    }

    /**
     * @throws NullPointerException if the specified key is null
     */
    @Override
    public V get(K key) {
        return lastVersionMap.get(key);
    }

    /**
     * @throws NullPointerException if the specified key is null
     */
    @Override
    public V getByVersion(K key, int version) {
        if (null == key) {
            throw new NullPointerException("Key cannot be null");
        }

        /*
        The allVersionsMap map should not be used if the lastVersionMap is already cleared. This piece of code
        prevents the situation when this method is called during the map is being cleared by another thread
        and the lastVersionMap is already cleared but the allVersionsMap has not yet
         */
        if (0 == versionCounter && lastVersionMap.isEmpty()) {
            return null;
        }

        List<Element<V>> elementList = allVersionsMap.get(key);

        /*
        Lack of the element in the allVersionsMap does not mean that the element has not already bean inserted into the
        lastVersionMap (see the order of insertion in the addNewElement method). This piece of code prevents
        the situation when result of getting element of the last version can be different by using this method
        and regular get
        */
        if (null == elementList || isRequestedVersionLast(version, elementList)) {
            return get(key);
        }

        int foundElementIndex = Collections.binarySearch(elementList, (VersionedObject) () -> version,
                Comparator.comparing(VersionedObject::getVersion));

        if (foundElementIndex >= 0) {
            return elementList.get(foundElementIndex).getValue();
        } else if (-1 == foundElementIndex) {
            return null;
        } else {
            int insertionPoint = (-1) * (foundElementIndex + 1);
            return elementList.get(insertionPoint - 1).getValue();
        }
    }

    @Override
    public int delete(K key) {
        return addNewElement(key, null);
    }

    @Override
    public synchronized int getCurrentVersion() {
        return versionCounter;
    }

    @Override
    public synchronized void clear() {
        versionCounter = 0;
        lastVersionMap.clear();
        allVersionsMap.clear();
    }

    private synchronized int addNewElement(K key, V value) {
        List<Element<V>> elementList = allVersionsMap.get(key);
        List<Element<V>> newElementList;

        if (null == elementList) {
            newElementList = new ArrayList<>(1);
        } else {
            newElementList = new ArrayList<>(elementList.size() + 1);
            newElementList.addAll(elementList);
        }

        ++versionCounter;

        newElementList.add(new Element<>(versionCounter, value));

        if (null == value) {
            lastVersionMap.remove(key);
        } else {
            lastVersionMap.put(key, value);
        }

        allVersionsMap.put(key, newElementList);

        return versionCounter;
    }

    private boolean isRequestedVersionLast(int version, List<Element<V>> elementList) {
        return version >= elementList.get(elementList.size() - 1).getVersion();
    }

    @Getter
    @AllArgsConstructor
    private static class Element<V> implements VersionedObject {
        private final int version;
        private final V value;
    }

    private interface VersionedObject {
        int getVersion();
    }
}
