package com.mtm.backend.enums;

import lombok.Getter;

@Getter
public enum ConversationScenario {
    TEACHING_ADVICE("teaching_advice", "教学建议"),
    CONTENT_ANALYSIS("content_analysis", "内容分析"),
    WRITING_ASSISTANCE("writing_assistance", "写作辅助"),
    GENERAL_CHAT("general_chat", "通用聊天");

    private final String code;
    private final String description;

    ConversationScenario(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static ConversationScenario fromCode(String code) {
        for (ConversationScenario scenario : values()) {
            if (scenario.code.equals(code)) {
                return scenario;
            }
        }
        throw new IllegalArgumentException("Unknown scenario code: " + code);
    }
}