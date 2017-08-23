package com.sebastian_daschner.scalable_coffee_shop.beans.boundary;

import com.sebastian_daschner.scalable_coffee_shop.beans.control.BeanStorage;
import com.sebastian_daschner.scalable_coffee_shop.events.control.EventProducer;
import com.sebastian_daschner.scalable_coffee_shop.events.entity.BeansFetched;
import com.sebastian_daschner.scalable_coffee_shop.events.entity.BeansStored;
import com.sebastian_daschner.scalable_coffee_shop.events.entity.OrderBeansValidated;
import com.sebastian_daschner.scalable_coffee_shop.events.entity.OrderFailedBeansNotAvailable;

import javax.inject.Inject;
import java.util.UUID;

public class BeanCommandService {

    @Inject
    EventProducer eventProducer;

    @Inject
    BeanStorage beanStorage;

    public void storeBeans(final String beanOrigin, final int amount) {
        eventProducer.publish(new BeansStored(beanOrigin, amount));
    }

    void validateBeans(final String beanOrigin, final UUID orderId) {
        if (beanStorage.getRemainingAmount(beanOrigin) > 0)
            eventProducer.publish(new OrderBeansValidated(orderId));
        else
            eventProducer.publish(new OrderFailedBeansNotAvailable(orderId));
    }

    void fetchBeans(final String beanOrigin) {
        eventProducer.publish(new BeansFetched(beanOrigin));
    }

}
