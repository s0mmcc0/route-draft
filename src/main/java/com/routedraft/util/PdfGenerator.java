package com.routedraft.util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.routedraft.entity.Lesson;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.awt.Color;

@Component
public class PdfGenerator {

    private static final int PAGE_MARGIN = 40;
    private static final int TITLE_FONT_SIZE = 18;
    private static final int SUBTITLE_FONT_SIZE = 12;
    private static final int BODY_FONT_SIZE = 10;
    private static final int HEADER_FONT_SIZE = 10;
    private static final Color HEADER_BG_COLOR = new Color(245, 245, 245);

    public byte[] generateLessonPlan(Lesson lesson) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, PAGE_MARGIN, PAGE_MARGIN, PAGE_MARGIN, PAGE_MARGIN);

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // 1. 폰트 초기화
            // 폰트 파일을 바이트 배열로 직접 읽어들여 컨텍스트 격리 문제를 원천 방어
            ClassPathResource fontResource = new ClassPathResource("fonts/NanumGothic.ttf");
            byte[] fontBytes = fontResource.getInputStream().readAllBytes();
            BaseFont bf = BaseFont.createFont("NanumGothic.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, fontBytes, null);

            Font titleFont = new Font(bf, TITLE_FONT_SIZE, Font.BOLD, Color.BLACK);
            Font subtitleFont = new Font(bf, SUBTITLE_FONT_SIZE, Font.BOLD, Color.DARK_GRAY);
            Font bodyFont = new Font(bf, BODY_FONT_SIZE, Font.NORMAL, Color.BLACK);
            Font headerFont = new Font(bf, HEADER_FONT_SIZE, Font.BOLD, Color.BLACK);

            // 2. 타이틀 배치
            Paragraph title = new Paragraph(lesson.getLessonTitle() != null ? lesson.getLessonTitle() : "수업 교수·학습 과정안", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(25);
            document.add(title);

            // 3. [표 1] 수업 개요 정보 표 (4열 구조)
            PdfPTable infoTable = new PdfPTable(4);
            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new int[]{15, 35, 15, 35});
            infoTable.setSpacingAfter(20);

            addCell(infoTable, "과목명", headerFont, true);
            addCell(infoTable, lesson.getSubject(), bodyFont, false);
            addCell(infoTable, "대상 학년", headerFont, true);
            addCell(infoTable, lesson.getGrade() + " (" + lesson.getSchoolLevel() + ")", bodyFont, false);

            addCell(infoTable, "수업 형태", headerFont, true);
            addCell(infoTable, lesson.getLessonStyle(), bodyFont, false);
            addCell(infoTable, "수업 시간", headerFont, true);
            addCell(infoTable, lesson.getDuration() + "분 (학생 수: " + lesson.getStudentCount() + "명)", bodyFont, false);

            addCell(infoTable, "학습 환경", headerFont, true);
            addCell(infoTable, lesson.getEnvironment(), bodyFont, false);
            addCell(infoTable, "성취 기준", headerFont, true);
            addCell(infoTable, lesson.getAchievementStandard(), bodyFont, false);

            document.add(infoTable);

            // 4. 학습 목표 섹션 (쉼표 기준 줄바꿈 분리 처리)
            document.add(new Paragraph("■ 학습 목표", subtitleFont));
            String objectivesText = lesson.getLearningObjectives() != null ? lesson.getLearningObjectives().replace(",", "\n") : "";
            Paragraph objectives = new Paragraph(objectivesText, bodyFont);
            objectives.setSpacingBefore(8);
            objectives.setSpacingAfter(20);
            objectives.setIndentationLeft(10);
            document.add(objectives);

            // 5. [표 2] 교수·학습 활동 흐름 표
            document.add(new Paragraph("■ 교수·학습 활동 흐름", subtitleFont));
            
            PdfPTable flowTable = new PdfPTable(2);
            flowTable.setWidthPercentage(100);
            flowTable.setWidths(new int[]{20, 80});
            flowTable.setSpacingBefore(8);
            flowTable.setSpacingAfter(20);

            // 테이블 헤더
            addCell(flowTable, "단계", headerFont, true);
            addCell(flowTable, "교수·학습 활동 상세 내용", headerFont, true);

            // 컬럼 매핑
            addCell(flowTable, "도입", headerFont, true);
            addCell(flowTable, lesson.getIntroContent(), bodyFont, false);

            addCell(flowTable, "전개", headerFont, true);
            addCell(flowTable, lesson.getDevContent(), bodyFont, false);

            addCell(flowTable, "정리", headerFont, true);
            addCell(flowTable, lesson.getConclContent(), bodyFont, false);

            document.add(flowTable);

            // 6. 참고 자료 섹션 (심화/보충 및 유튜브 데이터 결합)
            document.add(new Paragraph("■ 맞춤형 격차 해소 및 동기유발 자료", subtitleFont));
            
            StringBuilder sb = new StringBuilder();
            sb.append("[상위권 심화] ").append(lesson.getAdvancedTopic()).append("\n : ").append(lesson.getAdvancedActivity()).append("\n\n");
            sb.append("[하위권 보충] 난이도(").append(lesson.getRemedialDifficulty()).append(")\n : ").append(lesson.getRemedialAssignment()).append("\n\n");
            
            if (lesson.getYoutubeVideoTitle() != null) {
                sb.append("[실시간 연동 영상]\n - 영상 제목: ").append(lesson.getYoutubeVideoTitle()).append("\n - 바로가기 링크: ").append(lesson.getYoutubeSearchUrl());
            }

            Paragraph footerAssets = new Paragraph(sb.toString(), bodyFont);
            footerAssets.setSpacingBefore(8);
            footerAssets.setIndentationLeft(10);
            document.add(footerAssets);

            document.close();
        } catch (DocumentException | IOException e) {
            throw new RuntimeException("PDF 렌더링 엔진 내부 결함 발생", e);
        }

        return out.toByteArray();
    }

    private void addCell(PdfPTable table, String text, Font font, boolean isHeader) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setPadding(8);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        if (isHeader) {
            cell.setBackgroundColor(HEADER_BG_COLOR);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        } else {
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        }
        table.addCell(cell);
    }
}