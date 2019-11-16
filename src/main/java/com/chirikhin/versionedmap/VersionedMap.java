package com.chirikhin.versionedmap;

public interface VersionedMap<K, V> {
    int put(K key, V value);

    V get(K key);

    V getByVersion(K key, int version);

    int getCurrentVersion();

    void clear();
}
