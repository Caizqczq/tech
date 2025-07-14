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

package com.tech.backend.controller;

import java.util.List;
import java.util.Map;
import java.util.Set;


import com.tech.backend.service.BaseService;
import com.tech.backend.service.ChatService;
import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@Tag(name = "Chat APIs")
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ChatController {

	private final ChatService chatService;

	private final BaseService baseService;


	/**
	 * 发送指定参数以获取模型响应。
	 * 1. 当发送的 prompt 为空时，返回错误信息。
	 * 2. 发送模型参数时，允许为空。如果该参数有值且在模型配置列表中，则调用对应模型；否则返回错误。
	 *    如果模型参数为空，则设置为默认模型 qwen-plus。
	 * 3. chatId 为聊天记忆，由前端传递，类型为 Object，且不能重复。
	 */
	@PostMapping("/chat")
	@Operation(summary = "DashScope Flux Chat")
	public Flux<String> chat(
			HttpServletResponse response,
			@Validated @RequestBody String prompt,
			@RequestHeader(value = "model", required = false) String model,
			@RequestHeader(value = "chatId", required = false, defaultValue = "spring-ai-alibaba-playground-chat") String chatId
	) {

		Set<Map<String, String>> dashScope = baseService.getDashScope();
		List<String> modelName = dashScope.stream()
				.flatMap(map -> map.keySet().stream().map(map::get))
				.distinct()
				.toList();

		if (StringUtils.hasText(model)) {
			if (!modelName.contains(model)) {
				return Flux.just("Input model not support.");
			}
		}
		else {
			model = DashScopeApi.ChatModel.QWEN_PLUS.getModel();
		}

		response.setCharacterEncoding("UTF-8");
		return chatService.chat(chatId, model, prompt);
	}

	@PostMapping("/deep-thinking/chat")
	public Flux<String> deepThinkingChat(
			HttpServletResponse response,
			@Validated @RequestBody String prompt,
			@RequestHeader(value = "model", required = false) String model,
			@RequestHeader(value = "chatId", required = false, defaultValue = "spring-ai-alibaba-playground-deepthink-chat") String chatId
	) {

		Set<Map<String, String>> dashScope = baseService.getDashScope();
		List<String> modelName = dashScope.stream()
				.flatMap(map -> map.keySet().stream().map(map::get))
				.distinct()
				.toList();

		if (StringUtils.hasText(model)) {
			if (!modelName.contains(model)) {
				return Flux.just("Input model not support.");
			}
		}
		else {
			model = DashScopeApi.ChatModel.QWEN_PLUS.getModel();
		}

		response.setCharacterEncoding("UTF-8");
		return chatService.deepThinkingChat(chatId, model, prompt);
	}

}
