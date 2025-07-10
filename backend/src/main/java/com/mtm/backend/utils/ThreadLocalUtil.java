package com.mtm.backend.utils;

public class ThreadLocalUtil {
    private static final ThreadLocal<Integer> threadLocal = new ThreadLocal<>();

    public static void set(Integer value) {
        threadLocal.set(value);
    }

    public static Integer get() {
        return threadLocal.get();
    }

    public static void remove() {
        threadLocal.remove();
    }
}
