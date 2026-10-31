import java.util.Arrays;
import java.util.Random;

public class SortingTest {

    private static final int[] SIZES = {10, 100, 1000, 10000};
    private static final int REPEATS = 5;
    private static final int WARMUP = 3;
    private static final long SEED = 42L;

    public static void main(String[] args) {
        System.out.printf("%-8s %-15s %-15s %-15s %-15s%n",
                "n", "Пузырьковая, ns", "Выборочная, ns", "Сортировка вставками, ns", "Быстрая, ns");
        System.out.println("---------------------------------------------------------------");

        Random rand = new Random(SEED);

        for (int size : SIZES) {
            int[] base = new int[size];
            for (int i = 0; i < size; i++) {
                base[i] = rand.nextInt(10_000); 
            }

            int[] sortedControl = base.clone();
            Arrays.sort(sortedControl);

            for (int w = 0; w < WARMUP; w++) {
                SortingAlgorithms.bubbleSort(base.clone());
                SortingAlgorithms.selectionSort(base.clone());
                SortingAlgorithms.insertionSort(base.clone());
                SortingAlgorithms.quickSort(base.clone(), 0, size - 1);
            }

            long[] bubbleTimes = new long[REPEATS];
            long[] selectionTimes = new long[REPEATS];
            long[] insertionTimes = new long[REPEATS];
            long[] quickTimes = new long[REPEATS];

            for (int r = 0; r < REPEATS; r++) {
                bubbleTimes[r] = measureAndCheck(base, sortedControl,
                        arr -> SortingAlgorithms.bubbleSort(arr), "bubble");
                selectionTimes[r] = measureAndCheck(base, sortedControl,
                        arr -> SortingAlgorithms.selectionSort(arr), "selection");
                insertionTimes[r] = measureAndCheck(base, sortedControl,
                        arr -> SortingAlgorithms.insertionSort(arr), "insertion");
                quickTimes[r] = measureAndCheck(base, sortedControl,
                        arr -> SortingAlgorithms.quickSort(arr, 0, arr.length - 1), "quick");
            }

            System.out.printf("%-8d %-15d %-15d %-15d %-15d%n",
                    size,
                    median(bubbleTimes),
                    median(selectionTimes),
                    median(insertionTimes),
                    median(quickTimes));
        }

        System.out.println("\n--- Проверка крайних случаев ---");
        checkEdgeCases();

        System.out.println("\n--- Быстрая сортировка на отсортированном/обратном массиве ---");
        selfWork();
    }


    @FunctionalInterface
    interface SortAction {
        void sort(int[] arr);
    }

    private static long measureAndCheck(int[] base, int[] sortedControl,
                                        SortAction action, String name) {
        int[] copy = base.clone();
        long start = System.nanoTime();
        action.sort(copy);
        long end = System.nanoTime();

        if (!Arrays.equals(copy, sortedControl)) {
            throw new IllegalStateException("Ошибка корректности в " + name
                    + ": массив не отсортирован или потеряны значения");
        }
        return end - start;
    }

    private static long median(long[] arr) {
        long[] copy = arr.clone();
        Arrays.sort(copy);
        return copy[copy.length / 2];
    }


    private static void checkEdgeCases() {
        int[][] cases = {
                {},                      
                {7},
                {5, 5, 5, 5, 5},
                {9, 7, 5, 3, 1},
                {3, 1, 2, 3, 1, 2},
        };
        String[] names = {"пустой", "один элемент", "все одинаковые",
                          "обратный порядок", "повторяющиеся"};

        for (int i = 0; i < cases.length; i++) {
            int[] original = cases[i].clone();

            int[] a1 = original.clone(); SortingAlgorithms.bubbleSort(a1);
            int[] a2 = original.clone(); SortingAlgorithms.selectionSort(a2);
            int[] a3 = original.clone(); SortingAlgorithms.insertionSort(a3);
            int[] a4 = original.clone(); SortingAlgorithms.quickSort(a4, 0, a4.length - 1);

            int[] expected = original.clone();
            Arrays.sort(expected);

            boolean ok = Arrays.equals(a1, expected)
                    && Arrays.equals(a2, expected)
                    && Arrays.equals(a3, expected)
                    && Arrays.equals(a4, expected);

            System.out.printf("  %-20s -> %s%n", names[i], ok ? "OK" : "FAIL");
        }
    }


    private static void selfWork() {
        int n = 1000;
        Random rand = new Random(SEED);

        int[] randomArr = new int[n];
        for (int i = 0; i < n; i++) randomArr[i] = rand.nextInt(10_000);

        int[] sortedArr = randomArr.clone();
        Arrays.sort(sortedArr);

        int[] reverseArr = new int[n];
        for (int i = 0; i < n; i++) reverseArr[i] = sortedArr[n - 1 - i];

        System.out.printf("%-18s %s%n", "Массив",
                "Quick Sort (3 повтора, ns)");
        System.out.println("Случайный:        " + quickThreeRuns(randomArr));
        System.out.println("Отсортированный:  " + quickThreeRuns(sortedArr));
        System.out.println("Обратный:         " + quickThreeRuns(reverseArr));
    }

    private static String quickThreeRuns(int[] base) {
        long[] times = new long[3];
        for (int r = 0; r < 3; r++) {
            int[] copy = base.clone();
            long s = System.nanoTime();
            SortingAlgorithms.quickSort(copy, 0, copy.length - 1);
            times[r] = System.nanoTime() - s;
        }
        return String.format("%d / %d / %d", times[0], times[1], times[2]);
    }
}