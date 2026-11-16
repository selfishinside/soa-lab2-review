package ru.itmo.soa.hr.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        HrWebConfiguration.class,
        WorkersClientConfiguration.class
})
public class HrConfiguration {
}
