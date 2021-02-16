package com.signature.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Component
@ConfigurationProperties(prefix = "description")
@PropertySource(value="classpath:application.properties",encoding="UTF-8")
public class ActionName {
    public String SHOW_PDFPAGE;
    public String EXIT_PDFPAGE;
    public String VISIT_SIGNATUREPAD ;
    public String EXIT_SIGNATUREPAD;
    public String SAVE;
    public String SHOW_MERGEDPDF;
    public String EXIT_MERGEDPDF;

}
