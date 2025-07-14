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

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.tech.backend.exception.AppException;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;



@Service
@Slf4j
public class SummarizerService {


	private final ChatClient chatClient;

	public SummarizerService(
			SimpleLoggerAdvisor simpleLoggerAdvisor,
			MessageChatMemoryAdvisor messageChatMemoryAdvisor,
			@Qualifier("dashscopeChatModel") ChatModel chatModel,
			@Qualifier("summarizerPromptTemplate") PromptTemplate docsSummaryPromptTemplate
	) {

		this.chatClient = ChatClient.builder(chatModel)
				.defaultOptions(
						DashScopeChatOptions.builder().withModel("deepseek-r1").build()
				).defaultSystem(
						docsSummaryPromptTemplate.getTemplate()
				).defaultAdvisors(
						messageChatMemoryAdvisor,
						simpleLoggerAdvisor
				).build();
	}

	/**
	 * Docs Summary not has chat memory.
	 */
	public Flux<String> summary(MultipartFile file, String url) {

		String text = getText(url, file);
		if (!StringUtils.hasText(text)) {
			return Flux.error(new AppException("Invalid file content"));
		}

		return chatClient.prompt()
				.user("Summarize the document")
				.user(text)
				.stream().content();
	}

	private String getText(String url, MultipartFile file) {

		if (Objects.nonNull(file)) {

			log.debug("Reading file content form MultipartFile");
			List<Document> documents = new TikaDocumentReader(file.getResource()).get();
			return documents.stream()
					.map(Document::getFormattedContent)
					.collect(Collectors.joining("\n\n"));
		}

		if (StringUtils.hasText(url)) {
			log.debug("Reading file content form url");
			List<Document> documents = new TikaDocumentReader(url).get();
			return documents.stream()
					.map(Document::getFormattedContent)
					.collect(Collectors.joining("\n\n"));
		}

		return "";
	}

}
