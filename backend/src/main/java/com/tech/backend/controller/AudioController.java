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
import com.tech.backend.service.AudioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@RestController
@Tag(name = "Audio APIs")
@RequestMapping("/api/v1/")
@RequiredArgsConstructor
public class AudioController {

	private final AudioService audioService;


	/**
	 * used to convert audio to text output
	 */
	@PostMapping("/audio2text")
	@Operation(summary = "DashScope Audio Transcription")
	public Result<String> audioToText(
			@Validated @RequestParam("audio") MultipartFile audio
	) throws IOException {

		if (audio.isEmpty()) {
			return Result.failed("No audio file provided");
		}

		return Result.success(audioService.audio2text(audio));
	}

	/**
	 * used to convert text into speech output
	 */
	@GetMapping("/text2audio")
	@Operation(summary = "DashScope Speech Synthesis")
	public Result<byte[]> textToAudio(
			@Validated @RequestParam("prompt") String prompt
	) {

		return Result.success("not implemented yet".getBytes());

		// byte[] audioData = audioService.text2audio(prompt);

		// test to verify that the audio data is empty
		// try (FileOutputStream fos = new FileOutputStream("tmp/audio/test-audio.wav")) {
		// 	fos.write(audioData);
		// } catch (IOException e) {
		// 	return Result.failed("Test save audio file: " + e.getMessage());
		// }
		//
		// return Result.success(audioData);
	}

}
