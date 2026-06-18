package com.routedraft.service.infrastructure;

import com.routedraft.config.YoutubeConfig;
import com.routedraft.dto.external.YoutubeSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Component
@RequiredArgsConstructor
public class YoutubeClient {

    private final YoutubeConfig youtubeConfig;

    /**
     * 특정 채널 내에서 키워드로 영상을 검색하여 상위 1개의 영상 정보를 반환
     * @param channelName AI가 추천한 유튜브 채널명 (예: EBS 다큐프라임)
     * @param keyword AI가 추출한 수업 핵심 키워드 (예: 제어구조)
     */
    public YoutubeSearchResponse.SearchResultItem searchVideo(String channelName, String keyword) {
        String query = (channelName + " " + keyword).trim();
        
        if (query.isEmpty()) {
            log.warn("유튜브 검색어가 비어있음.");
            return null;
        }
        
        String apiKey = youtubeConfig.getApiKey();

        try {
            YoutubeSearchResponse response = youtubeConfig.youtubeWebClient().get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search")
                            .queryParam("key", apiKey)
                            .queryParam("part", "snippet")
                            // .queryParam("type", "video")
                            // .queryParam("videoEmbeddable", "false") // 퍼가기(인앱 재생) 가능 여부
                            .queryParam("maxResults", 1)           // 최상위 1개만 조회
                            .queryParam("q", query)
                            .build())
                    .retrieve()
                    .bodyToMono(YoutubeSearchResponse.class)
                    .block();

            if (response != null && response.items() != null && !response.items().isEmpty()) {
                return response.items().get(0);
            }
        }catch (WebClientResponseException e) {
            log.error("구글 API 응답 에러 코드: {}, 내용: {}", e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("일반 예외 발생: {}", e.getMessage(), e);
        }
        
        return null;
    }
}