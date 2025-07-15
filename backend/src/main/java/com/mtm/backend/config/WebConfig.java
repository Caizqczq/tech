package com.mtm.backend.config;

import com.mtm.backend.Interceptor.JwtInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.*;

import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class WebConfig implements WebMvcConfigurer {
    private final JwtInterceptor jwtInterceptor;
    private final LocalFileConfig localFileConfig;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(jwtInterceptor)
//                .addPathPatterns("/**")
//                .excludePathPatterns("/api/auth/login","/api/auth/register");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取绝对路径并确保以 file: 协议开头
        String absolutePath = Paths.get(localFileConfig.getBasePath()).toAbsolutePath().toString();
        String fileLocation = "file:" + absolutePath + "/";
        
        log.info("配置静态资源映射: /files/** -> {}", fileLocation);
        
        // 配置文件访问路径映射
        registry.addResourceHandler("/files/**")
                .addResourceLocations(fileLocation)
                // 设置缓存策略 - 1小时缓存，对于静态文件资源
                .setCacheControl(CacheControl.maxAge(1, TimeUnit.HOURS)
                        .cachePublic()
                        .mustRevalidate())
                // 设置资源链处理，启用资源解析器链
                .resourceChain(true);
        
        log.info("静态资源映射配置完成");
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        // 配置内容协商策略
        configurer
                // 启用路径扩展名策略
                .favorPathExtension(true)
                // 启用参数策略
                .favorParameter(false)
                // 忽略Accept头
                .ignoreAcceptHeader(false)
                // 设置默认内容类型
                .defaultContentType(MediaType.APPLICATION_OCTET_STREAM)
                // 配置媒体类型映射
                .mediaType("pdf", MediaType.APPLICATION_PDF)
                .mediaType("doc", MediaType.valueOf("application/msword"))
                .mediaType("docx", MediaType.valueOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .mediaType("ppt", MediaType.valueOf("application/vnd.ms-powerpoint"))
                .mediaType("pptx", MediaType.valueOf("application/vnd.openxmlformats-officedocument.presentationml.presentation"))
                .mediaType("txt", MediaType.TEXT_PLAIN)
                .mediaType("md", MediaType.TEXT_MARKDOWN)
                .mediaType("mp3", MediaType.valueOf("audio/mpeg"))
                .mediaType("wav", MediaType.valueOf("audio/wav"))
                .mediaType("m4a", MediaType.valueOf("audio/mp4"))
                .mediaType("flac", MediaType.valueOf("audio/flac"))
                .mediaType("jpg", MediaType.IMAGE_JPEG)
                .mediaType("jpeg", MediaType.IMAGE_JPEG)
                .mediaType("png", MediaType.IMAGE_PNG)
                .mediaType("gif", MediaType.IMAGE_GIF);
        
        log.info("内容协商配置完成");
    }
}
