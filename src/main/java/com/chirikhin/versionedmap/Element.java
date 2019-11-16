package com.chirikhin.versionedmap;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
class Element<V> {
    private final Integer version;
    private final V value;
}
