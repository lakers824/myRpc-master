package com.xlfc.common.extension;

public class Holder<T> {

    //volatile使得value对所有线程可见
    private volatile T value;

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }
}
