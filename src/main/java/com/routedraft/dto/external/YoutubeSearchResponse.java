package com.routedraft.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * YouTube Data API v3 (/search) 응답 JSON을 바인딩하기 위한 DTO 데이터 구조
 * 구글 응답의 계층형 트리 구조(items -> id, snippet -> thumbnails)와 1:1로 매핑
 */
public record YoutubeSearchResponse(
    List<SearchResultItem> items
) {
    /**
     * 검색된 개별 동영상 정보 객체
     */
    public record SearchResultItem(
        IdInfo id,          // 동영상의 고유 식별자 정보 (ID)
        SnippetInfo snippet // 동영상의 제목, 설명, 썸네일 등 메타데이터 정보
    ) {}

    /**
     * 유튜브 리소스의 ID 정보를 담는 객체
     */
    public record IdInfo(
        String videoId
    ) {}

    public record SnippetInfo(
        String title,               // 유튜브 동영상 실제 업로드 제목
        ThumbnailsInfo thumbnails   // 크기별/화질별 썸네일 이미지 모음 객체
    ) {}

    /**
     * 썸네일 해상도별 그룹 객체
     */
    public record ThumbnailsInfo(
        // JSON의 "high" 키값을 자바 변수명 규격인 highResolution으로 매핑 (고화질 썸네일)
        @JsonProperty("high") ThumbnailDetail highResolution
    ) {}

    /**
     * 최종 썸네일 이미지의 세부 정보 객체
     */
    public record ThumbnailDetail(
        String url // 고화질 이미지 주소 URL
    ) {}
}