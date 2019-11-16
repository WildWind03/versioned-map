package com.chirikhin.versionedmap;

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A thread-safe implementation of {@link com.chirikhin.versionedmap.VersionedMap}
 */
public class ConcurrentVersionedMap<K, V> implements VersionedMap<K, V> {

    private final Map<K, List<Element<V>>> internalMap = new ConcurrentHashMap<>();

    private int versionCounter = 0;

    /**
     * Increments version of the map and associates the specified value with the specified key in the map of
     * the new version. If the map previously contained a mapping for the key, the old value is replaced by the
     * specified value in the new version of the map. All the other mappings that the map of the previous version had
     * are still available in the map of the new version
     * @return a new version of the map
     */
    @Override
    public synchronized int put(K key, V value) {
        List<Element<V>> elementList = internalMap.get(key);

        if (null == elementList) {
            elementList = new CopyOnWriteArrayList<>();
            elementList.add(new Element<>(versionCounter + 1, value));
            internalMap.put(key, elementList);
        } else {
            elementList.add(new Element<>(versionCounter + 1, value));
        }

        return ++versionCounter;
    }

    /**
     * @return a value corresponding to the key and stored in the map of the last version
     */
    @Override
    public V get(K key) {
        List<Element<V>> elementList = internalMap.get(key);

        if (null == elementList) {
            return null;
        }

        return elementList.get(elementList.size() - 1).getValue();
    }

    /**
     * @return a value corresponding to the key and stored in the map of the specified version.
     * If the passed version is higher than the map's version, the map's version is used instead
     */
    @Override
    public V getByVersion(K key, int version) {
        List<Element<V>> elementList = internalMap.get(key);

        if (null == elementList) {
            return null;
        }

        /*
        This transformation is made lazily and therefore applies only on the elements used in the binary search
         */
        List<Integer> versionList = Lists.transform(elementList, Element::getVersion);

        int foundElementIndex = Collections.binarySearch(versionList, version);

        if (foundElementIndex < 0) {
            /*
            The binary search method returns (-(insertion point) - 1). To calculate the insertion point from that,
            it is needed to add 1 to the returned value and then multiply by (-1)
             */
            int insertionPoint = (-1) * (foundElementIndex + 1);

            /*
            "insertionPoint - 1 < 0" means that there is no element with the lower version than the requested one
            and as a result the map of the requested version doesn't contain a value corresponding to the requested
            version
             */
            if (insertionPoint - 1 < 0) {
                return null;
            } else {
                return elementList.get(insertionPoint - 1).getValue();
            }
        } else {
            return elementList.get(foundElementIndex).getValue();
        }
    }

    /**
     * @return the current version of the map. The initial version is 0
     */
    @Override
    public synchronized int getCurrentVersion() {
        return versionCounter;
    }

    /**
     * Removes all the data stored in the map and sets the map's version to 0
     */
    @Override
    public synchronized void clear() {
        versionCounter = 0;
        internalMap.clear();
    }
}
