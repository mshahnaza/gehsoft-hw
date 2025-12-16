package org.example;

import org.example.hw04.MyLinkedHashMap;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class MyLinkedHashMapTest {

    @Test
    void should_putAndGetValueCorrectly() {
        MyLinkedHashMap<String, Integer> map = new MyLinkedHashMap<>();
        map.put("A", 1);
        map.put("B", 2);

        assertEquals(Integer.valueOf(1), map.get("A"));
        assertEquals(Integer.valueOf(2), map.get("B"));
        assertNull(map.get("C"));
    }

    @Test
    void should_updateValueWithoutChangingOrder_when_accessOrderFalse() {
        MyLinkedHashMap<String, Integer> map = new MyLinkedHashMap<>(false);
        map.put("A", 1);
        map.put("B", 2);

        map.put("A", 10);

        List<String> expectedKeys = new ArrayList<>();
        expectedKeys.add("A");
        expectedKeys.add("B");

        List<String> keys = new ArrayList<>(map.keySet());
        assertEquals(expectedKeys, keys);
        assertEquals(Integer.valueOf(10), map.get("A"));
    }

    @Test
    void should_moveToEnd_whenAccessOrderTrue() {
        MyLinkedHashMap<String, Integer> map = new MyLinkedHashMap<>(true);
        map.put("A", 1);
        map.put("B", 2);
        map.put("C", 3);

        map.get("A");

        List<String> expectedKeys = new ArrayList<>();
        expectedKeys.add("B");
        expectedKeys.add("C");
        expectedKeys.add("A");

        List<String> keys = new ArrayList<>(map.keySet());
        assertEquals(expectedKeys, keys);
    }

    @Test
    void should_removeElement_and_preserveOrder() {
        MyLinkedHashMap<String, Integer> map = new MyLinkedHashMap<>();
        map.put("A", 1);
        map.put("B", 2);
        map.put("C", 3);

        map.remove("B");

        List<String> expectedKeys = new ArrayList<>();
        expectedKeys.add("A");
        expectedKeys.add("C");

        List<String> keys = new ArrayList<>(map.keySet());
        assertEquals(expectedKeys, keys);
        assertNull(map.get("B"));
    }

    @Test
    void should_clearAllEntries() {
        MyLinkedHashMap<String, Integer> map = new MyLinkedHashMap<>();
        map.put("A", 1);
        map.put("B", 2);
        map.clear();

        assertTrue(map.keySet().isEmpty());
        assertTrue(map.values().isEmpty());
        assertTrue(map.entrySet().isEmpty());
    }

    @Test
    void should_returnCorrectKeySetOrder() {
        MyLinkedHashMap<String, Integer> map = new MyLinkedHashMap<>();
        map.put("A", 1);
        map.put("B", 2);
        map.put("C", 3);

        List<String> expectedKeys = new ArrayList<>();
        expectedKeys.add("A");
        expectedKeys.add("B");
        expectedKeys.add("C");

        List<String> keys = new ArrayList<>(map.keySet());
        assertEquals(expectedKeys, keys);
    }

    @Test
    void should_returnCorrectValuesOrder() {
        MyLinkedHashMap<String, Integer> map = new MyLinkedHashMap<>();
        map.put("A", 1);
        map.put("B", 2);
        map.put("C", 3);

        List<Integer> expectedValues = new ArrayList<>();
        expectedValues.add(1);
        expectedValues.add(2);
        expectedValues.add(3);

        List<Integer> values = new ArrayList<>(map.values());
        assertEquals(expectedValues, values);
    }

    @Test
    void should_returnCorrectEntrySetOrder() {
        MyLinkedHashMap<String, Integer> map = new MyLinkedHashMap<>();
        map.put("A", 1);
        map.put("B", 2);
        map.put("C", 3);

        List<String> expectedKeys = new ArrayList<>();
        expectedKeys.add("A");
        expectedKeys.add("B");
        expectedKeys.add("C");

        List<String> keysFromEntries = new ArrayList<>();
        for (Map.Entry<String, Integer> e : map.entrySet()) {
            keysFromEntries.add(e.getKey());
        }

        assertEquals(expectedKeys, keysFromEntries);
    }

    @Test
    void should_returnTrueForContainsKeyAndValue() {
        MyLinkedHashMap<String, Integer> map = new MyLinkedHashMap<>();
        map.put("A", 1);
        map.put("B", 2);

        assertTrue(map.containsKey("A"));
        assertTrue(map.containsValue(2));
        assertFalse(map.containsKey("C"));
        assertFalse(map.containsValue(3));
    }

    @Test
    void should_copyAllEntries_when_putAllCalled() {
        MyLinkedHashMap<String, Integer> map1 = new MyLinkedHashMap<>();
        map1.put("A", 1);
        map1.put("B", 2);

        MyLinkedHashMap<String, Integer> map2 = new MyLinkedHashMap<>();
        map2.putAll(map1);

        List<String> expectedKeys = new ArrayList<>();
        expectedKeys.add("A");
        expectedKeys.add("B");

        List<Integer> expectedValues = new ArrayList<>();
        expectedValues.add(1);
        expectedValues.add(2);

        assertEquals(2, map2.size());
        assertEquals(expectedKeys, new ArrayList<>(map2.keySet()));
        assertEquals(expectedValues, new ArrayList<>(map2.values()));
    }

    @Test
    void should_returnTrueOrFalseForIsEmpty() {
        MyLinkedHashMap<String, Integer> map = new MyLinkedHashMap<>();
        assertTrue(map.isEmpty());
        map.put("A", 1);
    }
}