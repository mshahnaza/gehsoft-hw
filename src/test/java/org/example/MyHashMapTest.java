package org.example;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class MyHashMapTest {

    static Stream<Map<String, Integer>> mapsProvider() {
        return Stream.of(
                new MyHashMap<>(),
                new MyHashMapDoubleHash<>() // Можно тестировать несколько реализаций
        );
    }

    @ParameterizedTest
    @MethodSource("mapsProvider")
    void should_putOrUpdateValue_when_added(Map<String, Integer> map) {
        map.put("one", 1);
        assertEquals(1, map.put("one", 2));
    }

    @ParameterizedTest
    @MethodSource("mapsProvider")
    void should_returnValue_when_keyExists(Map<String, Integer> map) {
        map.put("one", 1);
        assertEquals(1, map.get("one"));
        assertNull(map.get("two"));
    }

    @ParameterizedTest
    @MethodSource("mapsProvider")
    void should_removeEntry_when_keyExists(Map<String, Integer> map) {
        map.put("one", 1);
        assertEquals(1, map.remove("one"));
        assertNull(map.get("one"));
    }

    @ParameterizedTest
    @MethodSource("mapsProvider")
    void should_clearAllEntries_when_clearCalled(Map<String, Integer> map) {
        map.put("one", 1);
        map.put("two", 2);
        map.clear();
        assertEquals(0, map.size());
        assertNull(map.get("one"));
    }

    @ParameterizedTest
    @MethodSource("mapsProvider")
    void should_returnTrue_when_keyExists(Map<String, Integer> map) {
        map.put("two", 2);
        assertTrue(map.containsKey("two"));
        assertFalse(map.containsKey("three"));
    }

    @ParameterizedTest
    @MethodSource("mapsProvider")
    void should_returnTrue_when_valueExists(Map<String, Integer> map) {
        map.put("one", 1);
        assertTrue(map.containsValue(1));
        assertFalse(map.containsValue(3));
    }

    @ParameterizedTest
    @MethodSource("mapsProvider")
    void should_returnAllEntries_when_entrySetCalled(Map<String, Integer> map) {
        map.put("one", 1);
        map.put("two", 2);
        map.put("three", 3);
        Set<Map.Entry<String, Integer>> entries = map.entrySet();
        assertEquals(3, entries.size());
    }

    @ParameterizedTest
    @MethodSource("mapsProvider")
    void should_returnAllKeys_when_keySetCalled(Map<String, Integer> map) {
        map.put("one", 1);
        map.put("two", 2);
        map.put("three", 3);
        Set<String> keys = map.keySet();
        assertEquals(3, keys.size());
        assertTrue(keys.contains("two"));
    }

    @ParameterizedTest
    @MethodSource("mapsProvider")
    void should_returnAllValues_when_valuesCalled(Map<String, Integer> map) {
        map.put("one", 1);
        map.put("two", 2);
        Collection<Integer> values = map.values();
        assertEquals(2, values.size());
        assertTrue(values.contains(1));
    }

    @ParameterizedTest
    @MethodSource("mapsProvider")
    void should_copyAllEntries_when_putAllCalled(Map<String, Integer> map) {
        map.put("one", 1);
        map.put("two", 2);
        MyHashMap<String, Integer> map2 = new MyHashMap<>();
        map2.putAll(map);

        assertEquals(2, map2.size());
        assertTrue(map2.containsKey("two"));
    }

    @ParameterizedTest
    @MethodSource("mapsProvider")
    void should_returnFalse_when_mapIsNotEmpty(Map<String, Integer> map) {
        map.put("one", 1);
        assertFalse(map.isEmpty());
    }
}
