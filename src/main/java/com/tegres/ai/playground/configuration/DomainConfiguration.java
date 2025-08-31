package com.tegres.ai.playground.configuration;

import com.tegres.ai.playground.AiPlaygroundApplication;
import com.tegres.ai.playground.service.DomainService;
import com.tegres.ai.playground.service.Stub;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
@ComponentScan(basePackageClasses = { AiPlaygroundApplication.class },
    includeFilters = {
        @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = { DomainService.class }),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = { Stub.class })
})
public class DomainConfiguration {
}
