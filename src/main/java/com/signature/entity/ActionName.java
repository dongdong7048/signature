package com.signature.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@AllArgsConstructor //lombok套件，加入此註解即提供全參數的構造器
@NoArgsConstructor //lombok套件，加入此註解即提供無參數的構造器
@Data //lombok套件，加入此註解即提供set,get的功能
@Component //加入此註解表示由spring管理
@ConfigurationProperties(prefix = "description") //此註解表示會到主配置文件中查找前綴名為description的參數
@PropertySource(value="classpath:application.properties",encoding="UTF-8") //此註解表示此類對應到application.properties
public class ActionName {
    public String SHOW_PDFPAGE;
    public String EXIT_PDFPAGE;
    public String VISIT_SIGNATUREPAD ;
    public String EXIT_SIGNATUREPAD;
    public String SAVE;
    public String SHOW_MERGEDPDF;
    public String EXIT_MERGEDPDF;

}
