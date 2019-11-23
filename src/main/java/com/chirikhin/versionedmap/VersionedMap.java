package com.chirikhin.versionedmap;

public interface VersionedMap<K, V> {

    /**
     * Increments version of the map and associates the specified value with the specified key in the map of
     * the new version. If the map previously contained a mapping for the key, the old value is replaced by the
     * specified value in the new version of the map. All the other mappings that the map of the previous version had
     * are still available in the map of the new version
     * @return a new version of the map
     */
    int put(K key, V value);

    /**
     * @return a value corresponding to the key and stored in the map of the last version
     */
    V get(K key);

    /**
     * @return a value corresponding to the key and stored in the map of the specified version.
     * If the passed version is higher than the map's version, the map's version is used instead.
     */
    V getByVersion(K key, int version);

    /**
     * Increments version of the map and removes the value associated with the key from the map of a new version
     * @return a new version of the map
     */
    int delete(K key);

    /**
     * @return the current version of the map. The initial version is 0
     */
    int getCurrentVersion();

    /**
     * Removes all the data stored in the map and sets the map's version to 0
     */
    void clear();
}
