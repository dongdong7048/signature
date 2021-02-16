package com.signature.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.swing.filechooser.FileSystemView;

@Configuration
public class MyWebAppConfiguration extends WebMvcConfigurerAdapter {
    private String desktop_temp_path =  FileSystemView.getFileSystemView().getHomeDirectory().toString()+"\\temp\\";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

//        /**
//         * @Description: 對檔案的路徑進行配置,建立一個虛擬路徑/Path/** ，即只要在<img src="/temp/picName.jpg" />便可以直接引用圖片
//         *這是圖片的物理路徑 "file:/+本地圖片的地址"
//         * @Date： Create in 14:08 2017/12/20
//         */   registry.addResourceHandler("/temp/**").addResourceLocations(desktop_temp_path);
//        super.addResourceHandlers(registry);
//        //file:/E:/WebPackage/IdeaProjects/shiroLearn/src/main/resources/static/
    }
}
