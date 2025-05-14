package com.example.service.pattern.observer.subject;

import com.example.service.pattern.observer.ISender;

public interface ISenderWorker {
    void add(ISender sender);
    void process();
}
