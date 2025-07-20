package com.mtm.backend.service;

import com.mtm.backend.model.DTO.PPTGenerationDTO;
import com.mtm.backend.service.impl.PPTGenerationServiceImpl;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * PPT生成服务测试
 */
public class PPTGenerationServiceTest {

    public static void main(String[] args) throws IOException {
        PPTGenerationServiceImpl service = new PPTGenerationServiceImpl();
        
        // 创建测试请求
        PPTGenerationDTO request = new PPTGenerationDTO();
        request.setTopic("Java编程基础");
        request.setSubject("计算机科学");
        request.setCourseLevel("初级");
        request.setSlideCount(5);
        request.setStyle("简洁");
        
        // 模拟AI生成的内容
        String aiContent = """
                【幻灯片1】Java简介
                Java是一种面向对象的编程语言
                具有跨平台特性
                广泛应用于企业级开发
                
                【幻灯片2】Java基本语法
                变量声明和初始化
                数据类型：基本类型和引用类型
                运算符和表达式
                控制流语句
                
                【幻灯片3】面向对象编程
                类和对象的概念
                封装、继承、多态
                构造方法和方法重载
                访问修饰符
                
                【幻灯片4】常用类库
                String类的使用
                集合框架：List、Set、Map
                异常处理机制
                输入输出流
                
                【幻灯片5】总结
                Java是强大的编程语言
                掌握基础语法很重要
                多练习编程实例
                持续学习新特性
                """;
        
        try {
            // 生成PPT文件
            byte[] pptBytes = service.generatePPTFile(aiContent, request);
            
            // 保存到文件进行验证
            String fileName = "test_generated.pptx";
            try (FileOutputStream fos = new FileOutputStream(fileName)) {
                fos.write(pptBytes);
                System.out.println("PPT文件生成成功：" + fileName);
                System.out.println("文件大小：" + pptBytes.length + " bytes");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
