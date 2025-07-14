/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tech.backend.service;

import java.util.Objects;

import com.alibaba.cloud.ai.advisor.DocumentRetrievalAdvisor;
import com.tech.backend.advisor.ReasoningContentAdvisor;
import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.api.DashScopeResponseFormat;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetrieverOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;



@Service
@Slf4j
public class ChatService {


	private final ChatClient chatClient;

	private final DashScopeApi dashscopeApi;

	@Value("${spring.ai.alibaba.playground.bailian.index-name:default-index}")
	private String indexName;

	private final PromptTemplate deepThinkPromptTemplate;

	private final ReasoningContentAdvisor reasoningContentAdvisor;

	private DocumentRetrievalAdvisor retrievalAdvisor;

	public ChatService(
			DashScopeApi dashscopeApi,
			SimpleLoggerAdvisor simpleLoggerAdvisor,
			MessageChatMemoryAdvisor messageChatMemoryAdvisor,
			@Qualifier("dashscopeChatModel") ChatModel chatModel,
			@Qualifier("systemPromptTemplate") PromptTemplate systemPromptTemplate,
			@Qualifier("deepThinkPromptTemplate") PromptTemplate deepThinkPromptTemplate
	) {
		this.dashscopeApi = dashscopeApi;
		this.chatClient = ChatClient.builder(chatModel)
				.defaultSystem(
					systemPromptTemplate.getTemplate()
				).defaultAdvisors(
						simpleLoggerAdvisor,
						messageChatMemoryAdvisor
				).build();

		this.deepThinkPromptTemplate = deepThinkPromptTemplate;
		this.reasoningContentAdvisor = new ReasoningContentAdvisor(1);
	}

	@PostConstruct
	public void init(){
		this.retrievalAdvisor = new DocumentRetrievalAdvisor(
				new DashScopeDocumentRetriever(
						dashscopeApi,
						DashScopeDocumentRetrieverOptions.builder()
								.withIndexName(indexName)
								.build()
				)
		);
	}

	public Flux<String> chat(String chatId, String model, String prompt) {

		log.debug("chat model is: {}", model);

		// check if model == "deepseek-r1", output reasoning content.
		if (Objects.equals("deepseek-r1", model)) {
			// add reasoning content advisor.
			chatClient.prompt().advisors(reasoningContentAdvisor);
		}
		var runtimeOptions = DashScopeChatOptions.builder()
				.withModel(model)
				.withTemperature(0.8)
				.withResponseFormat(DashScopeResponseFormat.builder()
						.type(DashScopeResponseFormat.Type.TEXT)
						.build()
				).build();

		return chatClient.prompt()
				.options(runtimeOptions)
				.user(prompt)
				.advisors(memoryAdvisor -> memoryAdvisor
						.param(ChatMemory.CONVERSATION_ID, chatId)
				)
				.advisors(retrievalAdvisor)
				.stream()
				.content();
	}

	public Flux<String> deepThinkingChat(String chatId, String model, String prompt) {

		return chatClient.prompt()
				.options(DashScopeChatOptions.builder()
						.withModel(model)
						.withTemperature(0.8)
						.withResponseFormat(DashScopeResponseFormat.builder()
								.type(DashScopeResponseFormat.Type.TEXT)
								.build()
						).build()
				).system(deepThinkPromptTemplate.getTemplate())
				.user(prompt)
				.advisors(memoryAdvisor -> memoryAdvisor
						.param(ChatMemory.CONVERSATION_ID, chatId)
				)
				.advisors(retrievalAdvisor)
				.stream()
				.content();
	}
}
