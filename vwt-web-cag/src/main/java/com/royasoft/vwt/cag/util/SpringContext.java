package com.royasoft.vwt.cag.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
@Component
public class SpringContext implements ApplicationContextAware {

    private static ApplicationContext context;

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    public static Object getBean(String name) throws BeansException {
        return context.getBean(name);
    }

    public static <T> T getBean(Class<T> clazz) throws BeansException {
        return context.getBean(clazz);
    }

    public static <T> T getBean(String name, Class<T> clazz) throws BeansException {
        return context.getBean(name, clazz);
    }
}