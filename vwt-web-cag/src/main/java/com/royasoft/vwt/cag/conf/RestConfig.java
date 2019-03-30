/*
 * Copyright 2004-2017 上海若雅软件系统有限公司
 */
package com.royasoft.vwt.cag.conf;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Source;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * 初始化负载均衡控件
 * 
 * @author: xutf
 */
@Configuration
public class RestConfig {

    /**
     * 生产环境
     * 
     * @return RestTemplate
     */
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = getRestTemplate();
        return restTemplate;
    }


    /**
     * 得到一个RestTemplate ,处理中文乱码问题
     * 
     * @return RestTemplate
     */
    private RestTemplate getRestTemplate() {
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        ByteArrayHttpMessageConverter byteArrayHttpMessageConverter = new ByteArrayHttpMessageConverter();
        StringHttpMessageConverter stringHttpMessageConverter = new StringHttpMessageConverter(Charset.forName("UTF-8"));
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        MappingJackson2XmlHttpMessageConverter mappingJackson2XmlHttpMessageConverter = new MappingJackson2XmlHttpMessageConverter();
        ResourceHttpMessageConverter resourceHttpMessageConverter = new ResourceHttpMessageConverter();
        SourceHttpMessageConverter<Source> sourceHttpMessageConverter = new SourceHttpMessageConverter<Source>();
        AllEncompassingFormHttpMessageConverter formHttpMessageConverter = new AllEncompassingFormHttpMessageConverter();
        messageConverters.add(byteArrayHttpMessageConverter);
        messageConverters.add(stringHttpMessageConverter);
        messageConverters.add(resourceHttpMessageConverter);
        messageConverters.add(sourceHttpMessageConverter);
        List<HttpMessageConverter<?>> partConverters = new ArrayList<>();
        partConverters.add(byteArrayHttpMessageConverter);
        partConverters.add(stringHttpMessageConverter);
        partConverters.add(resourceHttpMessageConverter);
        partConverters.add(sourceHttpMessageConverter);
        partConverters.add(mappingJackson2HttpMessageConverter);
        partConverters.add(mappingJackson2XmlHttpMessageConverter);
        formHttpMessageConverter.setPartConverters(partConverters);
        messageConverters.add(formHttpMessageConverter);
        RestTemplate restTemplate = new RestTemplate(messageConverters);
        return restTemplate;
    }
}
