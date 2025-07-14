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

package com.tech.backend.config.handler;

import com.tech.backend.entity.result.Result;
import com.tech.backend.exception.AIException;
import com.tech.backend.exception.AppException;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public Result<?> handleValidationExceptions(
			MethodArgumentNotValidException ex
	) {
		return Result.failed("Invalid params: " + ex.getMessage());
	}

	@ExceptionHandler(AIException.class)
	public Result<?> handleSAAAIExceptions(
			MethodArgumentNotValidException ex
	) {
		return Result.failed("Spring AI Alibaba Exception: " + ex.getMessage());
	}

	@ExceptionHandler(AppException.class)
	public Result<?> handleSAAAppExceptions(
			MethodArgumentNotValidException ex
	) {
		return Result.failed("Spring AI Alibaba Playground Exception: " + ex.getMessage());
	}

}
