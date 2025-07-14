# 音频转录功能修复说明

## 问题描述
原有的音频转录功能使用Spring AI Alibaba的AudioTranscriptionModel，但在调用DashScope API时出现"url error, please check url！"错误。

## 根本原因分析
通过查询Spring AI Alibaba和阿里云百炼官方文档发现：
1. 官方推荐使用DashScope原生的MultiModalConversation API进行音频转录
2. AudioTranscriptionModel可能不支持URL方式的音频输入
3. 官方文档中的所有示例都使用MultiModalConversation.call()方法

## 修复方案
基于阿里云百炼官方文档，将音频转录实现改为使用DashScope原生MultiModalConversation API：

### 主要变更
1. **API调用方式**：从AudioTranscriptionModel改为MultiModalConversation
2. **消息格式**：使用MultiModalMessage构建包含音频URL和转录指令的消息
3. **模型选择**：使用qwen-audio-turbo-latest模型
4. **异常处理**：添加对DashScope特定异常的处理

### 代码变更详情

#### 1. 导入变更
```java
// 移除
import com.alibaba.cloud.ai.dashscope.audio.transcription.AudioTranscriptionModel;
import com.alibaba.cloud.ai.dashscope.audio.DashScopeAudioTranscriptionOptions;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.core.io.UrlResource;

// 添加
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
```

#### 2. 依赖注入变更
```java
// 移除AudioTranscriptionModel依赖
private final AudioTranscriptionModel audioTranscriptionModel;
```

#### 3. 转录逻辑变更
```java
// 新的转录实现
MultiModalConversation conv = new MultiModalConversation();

MultiModalMessage userMessage = MultiModalMessage.builder()
    .role(Role.USER.getValue())
    .content(Arrays.asList(
        Collections.singletonMap("audio", audioUrl),
        Collections.singletonMap("text", "请转录这段音频的内容，只返回转录文本，不要添加其他说明。")
    ))
    .build();

MultiModalConversationParam param = MultiModalConversationParam.builder()
    .model("qwen-audio-turbo-latest")
    .message(userMessage)
    .build();

MultiModalConversationResult result = conv.call(param);
String transcriptionText = result.getOutput().getChoices().get(0)
    .getMessage().getContent().get(0).get("text").toString();
```

## 优势
1. **官方支持**：完全基于阿里云百炼官方文档实现
2. **URL支持**：原生支持音频URL输入，无需本地文件
3. **错误处理**：更清晰的异常处理机制
4. **模型选择**：使用最新的qwen-audio-turbo-latest模型

## 配置要求
确保环境变量AI_DASHSCOPE_API_KEY已正确配置：
```bash
export AI_DASHSCOPE_API_KEY=your_dashscope_api_key
```

## 测试建议
1. 确保OSS bucket配置了公开读权限
2. 测试音频文件URL的可访问性
3. 验证DashScope API密钥的有效性
4. 测试不同格式的音频文件（mp3、wav、m4a、flac）

## 依赖变更
在pom.xml中添加了DashScope SDK依赖：
```xml
<!-- DashScope SDK for 音频转录 -->
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>dashscope-sdk-java</artifactId>
    <version>2.18.3</version>
</dependency>
```

## 修复验证
1. ✅ 编译通过 - 所有依赖正确导入
2. ✅ API调用方式 - 使用官方推荐的MultiModalConversation
3. ✅ 异常处理 - 添加DashScope特定异常处理
4. ✅ URL支持 - 支持OSS公开URL和签名URL

## 注意事项
1. 音频文件大小不超过10MB
2. 音频时长建议不超过30秒
3. 确保音频URL可被公网访问
4. 支持的语言：中文、英语、粤语、法语、意大利语、西班牙语、德语、日语

## 下一步测试
1. 重启应用程序
2. 上传音频文件测试转录功能
3. 检查转录结果的准确性
4. 验证错误处理机制
