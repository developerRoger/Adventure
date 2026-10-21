package com.firstTry.Adventure;

import java.util.Arrays;

public class JavaIncouldTest {
    public static void bubbleSort(int[] array) {
        int n = array.length;

        for (int i = 0; i < n-1 ; i++) {
            boolean swapped = false;
            for (int j = 0; j < n - i - 1; j++) {
                // 比较相邻元素，交换顺序错误的元素
                if (array[j] > array[j + 1]) {
                    int temp = array[j];
                    array[j] = array[j + 1];
                    array[j + 1] = temp;
                    System.out.println("i="+i+",j="+j + Arrays.toString(array));
                    swapped=true;
                }
            }
            if(!swapped) break;
        }
    }

    public static void main(String[] args) {
        int[] arr = {64, 34, 25, 12, 22, 11, 5};
        bubbleSort(arr);
        System.out.println("Sorted array: " + Arrays.toString(arr));
    }
}
