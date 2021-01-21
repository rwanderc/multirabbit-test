To initialize/reinitialize the 3 different instances of RabbitMQ, run:

```
docker rm -f rabbit1 rabbit2 rabbit3; \
docker run -d -p 5672:5672 -p 15672:15672 --name rabbit1 rabbitmq:3-management; \
docker run -d -p 5673:5672 -p 15673:15672 --name rabbit2 rabbitmq:3-management; \
docker run -d -p 5674:5672 -p 15674:15672 --name rabbit3 rabbitmq:3-management
```


Providing spring.multirabbit properties will automatically initialize ConnectionFactories, ContainerFactories & RabbitAdmins.
It must be able to create beans for multiple brokers even without Listeners for those brokers. One use-case is to only produce
events on those brokers, which implies only in sending to them.

Enabling MultiRabbitListenerAnnotationBeanPostProcessor will execute the underlying logic of RabbitListenerAnnotationBeanPostProcessor,
and will connect the declarables produced to the proper RabbitAdmins that should declare them.

But if MultiRabbitListenerAnnotationBeanPostProcessor is not initialized AND there are spring.multirabbit beans, that might
be the case that the containerFactories exist but the declarabls are not the declared by the proper RabbitAdmins