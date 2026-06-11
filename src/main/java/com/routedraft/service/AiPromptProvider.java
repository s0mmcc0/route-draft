package com.routedraft.service;

import org.springframework.core.io.Resource;

public interface AiPromptProvider<REQ> {
    Resource getPromptResource(String promptPath); // 프롬프트 파일 반환
    String getUserPrompt(REQ request);    // 유저 프롬프트 문자열 반환
}
