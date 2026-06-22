package edu.jxnu.microcoursebackend.service;

import edu.jxnu.microcoursebackend.pojo.ScreenshotNote;

import java.math.BigDecimal;
import java.util.List;

public interface ScreenshotNoteService {
    ScreenshotNote save(Long userId, String imageData, BigDecimal videoTime);

    List<ScreenshotNote> listByUserId(Long userId);

    ScreenshotNote update(Long userId, Long id, String note, String aiAnalysis);

    void delete(Long userId, Long id);
}
