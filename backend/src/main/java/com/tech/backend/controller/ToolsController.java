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


import com.tech.backend.entity.result.Result;
import com.tech.backend.entity.tools.ToolCallResp;
import com.tech.backend.service.ToolsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;



@RestController
@Tag(name = "Tool Calling APIs")
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ToolsController {

	private final ToolsService functionService;


	/**
	 * http://127.0.0.1:8080/api/v1/tool-call?prompt="使用百度翻译将隐私计算翻译为英文"
	 * 触发百度翻译：使用百度翻译将隐私计算翻译为英文
	 * 触发百度地图：使用百度地图查找杭州市的银行 ATM 机信息 or 使用百度地图查找杭州的信息
	 */
	@GetMapping("/tool-call")
	@Operation(summary = "DashScope ToolCall Chat")
	public Result<ToolCallResp> chat(
			@Validated @RequestParam("prompt") String prompt
	) {

		return Result.success(functionService.chat(prompt));
	}

}
