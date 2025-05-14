package com.example.service.pattern.observer;

import com.example.service.model.OtherRequest;

public class NotiSender implements ISender<OtherRequest, Object> {
    @Override
    public Object process() {
        return null;
    }

    private void init() {

    }

    private boolean validate(OtherRequest request) {
        return true;
    }

    private Object send(OtherRequest request) {
        return null;
    }
}
