package org.example;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

public class CustomListTest {
    static List<List<Integer>> listProvider() {
        List<List<Integer>> result = new ArrayList<>();
        result.add(new ArrayList<>());
        result.add(new CustomList());
        return result;
    }

    @ParameterizedTest
    @MethodSource("listProvider")
    void shouldAddElementToList(List<Integer> list) {
        list.add(1);

        assertEquals(1, list.get(0));
        assertFalse(list.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("listProvider")
    void shouldInsertElementAtIndex(List<Integer> list) {
        list.add(1);
        list.add(2);
        list.add(1, 3);

        assertEquals(3, list.get(1));
    }

    @ParameterizedTest
    @MethodSource("listProvider")
    void shouldRemoveElementByIndex(List<Integer> list) {
        list.add(1);
        list.remove(0);

        assertTrue(list.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("listProvider")
    void shouldReturnFalseWhenListIsNotEmpty(List<Integer> list) {
        list.add(1);
        assertFalse(list.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("listProvider")
    void shouldReturnTrueWhenListIsEmpty(List<Integer> list) {
        assertTrue(list.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("listProvider")
    void shouldReturnElementByIndex(List<Integer> list) {
        list.add(1);
        int element = list.get(0);

        assertEquals(1, element);
    }

    @ParameterizedTest
    @MethodSource("listProvider")
    void shouldUpdateElementAtIndex(List<Integer> list) {
        list.add(1);
        list.set(0, 2);

        assertEquals(2, list.get(0));
    }

    @ParameterizedTest
    @MethodSource("listProvider")
    void shouldReturnCorrectSize(List<Integer> list) {
        list.add(1);
        list.add(2);

        assertEquals(2, list.size());
    }

    @ParameterizedTest
    @MethodSource("listProvider")
    void shouldClearAllElements(List<Integer> list) {
        list.add(1);
        list.add(2);

        list.clear();
        assertEquals(0, list.size());
    }

    @ParameterizedTest
    @MethodSource("listProvider")
    void shouldThrowExceptionWhenIndexIsOutOfBounds(List<Integer> list) {
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> list.add(2, 1));
        assertThrows(IndexOutOfBoundsException.class, () -> list.remove(0));
        assertThrows(IndexOutOfBoundsException.class, () -> list.set(1, 2));
    }

    @ParameterizedTest
    @MethodSource("listProvider")
    void shouldReturnNullWhenElementIsNull(List<Integer> list) {
        list.add(null);
        assertNull(list.get(0));
    }

    @ParameterizedTest
    @MethodSource("listProvider")
    void shouldExtendArraySizeWhenCapacityExceeded(List<Integer> list) {
        for (int i = 0; i < 10; i++) {
            list.add(1);
        }
        assertEquals(10, list.size());

        for (int i = 0; i < 10; i++) {
            list.add(1);
        }
        assertEquals(20, list.size());

        list.clear();
        assertEquals(0, list.size());
    }
}
