package edu.jxnu.microcoursebackend;

import edu.jxnu.microcoursebackend.pojo.StudyNote;
import edu.jxnu.microcoursebackend.pojo.ScreenshotNote;
import edu.jxnu.microcoursebackend.pojo.BookmarkSaveRequest;
import edu.jxnu.microcoursebackend.pojo.LearningProgressSaveRequest;
import edu.jxnu.microcoursebackend.pojo.ModuleUsageEventRequest;
import edu.jxnu.microcoursebackend.pojo.WrongQuestionSaveRequest;
import edu.jxnu.microcoursebackend.service.BookmarkService;
import edu.jxnu.microcoursebackend.service.ChatMessageService;
import edu.jxnu.microcoursebackend.service.FlashcardProgressService;
import edu.jxnu.microcoursebackend.service.LearningProgressService;
import edu.jxnu.microcoursebackend.service.ModuleUsageService;
import edu.jxnu.microcoursebackend.service.ScreenshotNoteService;
import edu.jxnu.microcoursebackend.service.StudyNoteService;
import edu.jxnu.microcoursebackend.service.UserAchievementService;
import edu.jxnu.microcoursebackend.service.WrongQuestionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class MicrocourseBackendApplicationTests {
    @Autowired
    private StudyNoteService studyNoteService;

    @Autowired
    private ScreenshotNoteService screenshotNoteService;

    @Autowired
    private WrongQuestionService wrongQuestionService;

    @Autowired
    private LearningProgressService learningProgressService;

    @Autowired
    private ModuleUsageService moduleUsageService;

    @Autowired
    private BookmarkService bookmarkService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private FlashcardProgressService flashcardProgressService;

    @Autowired
    private UserAchievementService userAchievementService;

    @Test
    void contextLoads() {
    }

    @Test
    @Transactional
    void savesAndLoadsStudyNote() {
        StudyNote saved = studyNoteService.save(1L, "MyBatis integration test");

        assertNotNull(saved.getId());
        assertEquals("MyBatis integration test", studyNoteService.getByUserId(1L).getContent());
    }

    @Test
    @Transactional
    void savesScreenshotFileAndRecord() throws IOException {
        String image = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=";
        ScreenshotNote saved = screenshotNoteService.save(1L, image, BigDecimal.valueOf(12.5));
        Path savedFile = Path.of("storage", saved.getImageUrl().replace("/files/", ""));

        try {
            assertNotNull(saved.getId());
            assertEquals(BigDecimal.valueOf(12.5), saved.getVideoTime());
            assertEquals(true, Files.exists(savedFile));
        } finally {
            Files.deleteIfExists(savedFile);
        }
    }

    @Test
    @Transactional
    void savesRemainingMysqlBusinessData() {
        var wrong = wrongQuestionService.create(1L,
                new WrongQuestionSaveRequest("测试题目", "[\"A\",\"B\"]", "A", "B", "测试解析"));
        var progress = learningProgressService.save(1L,
                new LearningProgressSaveRequest("test-chapter", BigDecimal.valueOf(75), false,
                        "{\"answers\":2}", 125L));
        var bookmark = bookmarkService.create(1L,
                new BookmarkSaveRequest(BigDecimal.valueOf(8.5), "测试书签"));
        chatMessageService.clear(1L, "study");
        chatMessageService.append(1L, "study", "user", "测试问题");
        chatMessageService.append(1L, "study", "assistant", "测试回答");

        assertNotNull(wrong.getId());
        assertEquals("[\"A\",\"B\"]", wrong.getOptionsJson());
        assertEquals("test-chapter", progress.getChapterKey());
        assertEquals("{\"answers\": 2}", progress.getDetailJson());
        assertEquals(125L, progress.getDurationSeconds());
        assertNotNull(bookmark.getId());
        assertEquals(2, chatMessageService.list(1L, "study").size());
    }

    @Test
    @Transactional
    void recordsAndSummarizesWholeSiteUsage() {
        moduleUsageService.record(1L, new ModuleUsageEventRequest("calculator", "page_view", 0));
        moduleUsageService.record(1L, new ModuleUsageEventRequest("calculator", "duration", 42));

        var modules = moduleUsageService.summary(1L).get("modules");
        assertTrue(modules.stream().anyMatch(item -> {
            var summary = (edu.jxnu.microcoursebackend.pojo.ModuleUsageSummary) item;
            return "calculator".equals(summary.getModuleKey())
                    && summary.getVisitCount() >= 1
                    && summary.getDurationSeconds() >= 42;
        }));
    }

    @Test
    @Transactional
    void storesFlashcardMasteryInMysql() {
        flashcardProgressService.clear(1L);
        flashcardProgressService.markMastered(1L, 3);

        assertEquals(List.of(3), flashcardProgressService.list(1L));
    }

    @Test
    @Transactional
    void storesAchievementsInMysql() {
        var achievements = userAchievementService.unlock(1L, "first_viz");

        assertTrue(achievements.stream().anyMatch(item -> "first_viz".equals(item.getAchievementKey())));
    }

}
