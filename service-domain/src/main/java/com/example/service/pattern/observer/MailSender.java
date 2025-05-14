package com.example.service.pattern.observer;

import com.example.common.utils.TemplateUtils;
import com.example.service.model.OtherRequest;
import com.example.service.model.Params;
import com.example.service.model.ServiceRedisObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class MailSender implements ISender<OtherRequest, Object> {
    private Map<String, String> paramsMap = new HashMap<>();
    List<String> emails = new ArrayList<>();
    String title, content = Strings.EMPTY;

    private OtherRequest request;
    private ServiceRedisObject service;

    public MailSender(OtherRequest request, ServiceRedisObject service) {
        this.request = request;
        this.service = service;
    }

    @Override
    public Object process() {
        log.info("[{}] MailSender | Start process", request.getMsgId());
        init(request);
        if(validate(request)) return null;
//        build(request, service);
        send();
        log.info("[{}] MailSender | End process", request.getMsgId());
        return null;
    }

    private void init(OtherRequest request) {
        paramsMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(request.getParams())) {
            for (Params params : request.getParams()) {
                paramsMap.put(params.getKey(), params.getValue());
            }
        }
    }

    private boolean validate(OtherRequest request) {
        return true;
    }

    private Object send() {
        return null;
    }

    private void build(OtherRequest request, ServiceRedisObject service) {
        if (service.getNeedTemp() != 0) {
            this.content = TemplateUtils.buildMessage(service.getTempContent(), paramsMap);
            this.title = service.getTempTitle();
        } else {
            this.content = request.getContent();
            this.title = request.getTitle();
        }

        this.emails = request.getEmails();
    }

}
