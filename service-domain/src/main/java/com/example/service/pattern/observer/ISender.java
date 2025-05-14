package com.example.service.pattern.observer;

public interface ISender<I, O> {
    O process();
}
