package com.example.service.pattern.observer.subject;

import com.example.common.enums.MessageTypeEnum;
import com.example.service.model.OtherRequest;
import com.example.service.model.ServiceRedisObject;
import com.example.service.pattern.observer.ISender;
import com.example.service.pattern.observer.MailSender;
import com.example.service.pattern.observer.NotiSender;
import com.example.service.pattern.observer.SMSSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SenderWorker implements ISenderWorker{
    List<ISender> senders = new ArrayList<>();

    @Override
    public void add(ISender sender) {
        senders.add(sender);
    }

    @Override
    public void process() {
        senders.forEach(sender -> sender.process());
    }

    public void pushMessage(OtherRequest request, List<ServiceRedisObject> services) {
        services.forEach(service -> {
            ISender sender = getSender(request, service);
            if (Objects.nonNull(sender)) add(sender);
        });
        process();
    }

    public ISender getSender(OtherRequest request, ServiceRedisObject service) {
        if (service.getTempType().equals(MessageTypeEnum.NOTIFICATION.key())) {
            return new NotiSender();
        }
        if (service.getTempType().equals(MessageTypeEnum.SMS.key())) {
            return new SMSSender();
        }
        if (service.getTempType().equals(MessageTypeEnum.MAIL.key())) {
            return new MailSender(request, service);
        }
        if (service.getTempType().equals(MessageTypeEnum.ZALO.key())) {
            return null;
        }
        return null;
    }
}
