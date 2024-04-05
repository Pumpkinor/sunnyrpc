package org.sunny.sunnyrpccore.annotation;

import org.springframework.context.annotation.Import;
import org.sunny.sunnyrpccore.config.ConsumerConfig;
import org.sunny.sunnyrpccore.config.ProviderConfig;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Import({ProviderConfig.class, ConsumerConfig.class})
// 不想同时导入客户端和服务端怎么办 加个参数如何控制
public @interface EnableSunnyRPC {

}
