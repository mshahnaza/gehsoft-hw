package org.example;

public class ArrayOperations {
    public static void shiftLeftSystemCopy(int[] array, int positions) {
        if (!(array == null) && !(array.length == 0) && !(positions >= array.length) && !(positions <= 0)) {
            int[] newArray = new int[positions];

            System.arraycopy(array, 0, newArray, 0, positions);
            System.arraycopy(array, positions, array, 0, array.length - positions);
            System.arraycopy(newArray, 0, array, array.length - positions, positions);
        }
    }


    public static void shiftLeftManualLoop(int[] array, int positions) {
        if (!(array == null) && !(array.length == 0) && !(positions >= array.length) && !(positions <= 0)) {
            int[] newArray = new int[positions];

            for (int i = 0; i < positions; i++) {
                newArray[i] = array[i];
            }

            for (int i = positions; i < array.length; i++) {
                array[i-positions] = array[i];
            }

            for (int i = 0; i < positions; i++) {
                array[array.length-positions+i] = newArray[i];
            }
        }
    }
}