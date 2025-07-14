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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.tech.backend.entity.mcp.McpServer;
import com.tech.backend.entity.result.Result;
import com.tech.backend.entity.tools.ToolCallResp;
import com.tech.backend.exception.AppException;
import com.tech.backend.mcp.McpServerContainer;
import com.tech.backend.service.McpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;



@RestController
@Tag(name = "MCP APIs")
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class McpController {

	private final McpService mcpService;


	/**
	 * 内部接口不应该直接被 web 请求！
	 */
	@GetMapping("/inner/mcp")
	@Operation(summary = "DashScope MCP Chat")
	public Result<ToolCallResp> chat(
			@Validated @RequestParam("prompt") String prompt
	) {

		return Result.success(mcpService.chat(prompt));
	}

	@GetMapping("/mcp-list")
	@Operation(summary = "MCP List")
	public Result<List<McpServer>> mcpList() {

		return Result.success(McpServerContainer.getAllServers());
	}

	@PostMapping("/mcp-run")
	@Operation(summary = "MCP Run")
	public Result<ToolCallResp> mcpRun(
			@Validated @RequestParam("id") String id,
			@Validated @RequestParam("prompt") String prompt,
			@RequestParam(value = "envs", required = false) String envs
	) {

		Map<String, String> env = new HashMap<>();
		if (StringUtils.hasText(envs)) {
			for (String entry : envs.split(",")) {
				String[] keyValue = entry.split("=");
				if (keyValue.length == 2) {
					env.put(keyValue[0], keyValue[1]);
				}
			}
		}

		try {
			return Result.success(mcpService.run(id, env, prompt));
		}
		catch (IOException e) {
			throw new AppException(e.getMessage());
		}
	}
}

