package edu.jxnu.microcoursebackend.service.impl;

import edu.jxnu.microcoursebackend.mapper.ScreenshotNoteMapper;
import edu.jxnu.microcoursebackend.pojo.ScreenshotNote;
import edu.jxnu.microcoursebackend.service.ScreenshotNoteService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
public class ScreenshotNoteServiceImpl implements ScreenshotNoteService {
    private final ScreenshotNoteMapper screenshotNoteMapper;
    private final Path screenshotDirectory;

    public ScreenshotNoteServiceImpl(
            ScreenshotNoteMapper screenshotNoteMapper,
            @Value("${app.storage.root:storage}") String storageRoot
    ) {
        this.screenshotNoteMapper = screenshotNoteMapper;
        this.screenshotDirectory = Path.of(storageRoot).toAbsolutePath().normalize().resolve("screenshots");
    }

    @Override
    public ScreenshotNote save(Long userId, String imageData, BigDecimal videoTime) {
        String encoded = imageData.contains(",") ? imageData.substring(imageData.indexOf(',') + 1) : imageData;
        byte[] imageBytes;
        try {
            imageBytes = Base64.getDecoder().decode(encoded);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("截图数据不是有效的 Base64 图片", exception);
        }

        String filename = UUID.randomUUID() + ".png";
        try {
            Files.createDirectories(screenshotDirectory);
            Files.write(screenshotDirectory.resolve(filename), imageBytes);
        } catch (IOException exception) {
            throw new IllegalStateException("截图文件保存失败", exception);
        }

        ScreenshotNote screenshotNote = new ScreenshotNote();
        screenshotNote.setUserId(userId);
        screenshotNote.setImageUrl("/files/screenshots/" + filename);
        screenshotNote.setVideoTime(videoTime == null ? BigDecimal.ZERO : videoTime);
        screenshotNote.setNote("");
        screenshotNoteMapper.insert(screenshotNote);
        return screenshotNote;
    }

    @Override
    public List<ScreenshotNote> listByUserId(Long userId) {
        return screenshotNoteMapper.findByUserId(userId);
    }

    @Override
    public ScreenshotNote update(Long userId, Long id, String note, String aiAnalysis) {
        ScreenshotNote current = screenshotNoteMapper.findByIdAndUserId(id, userId);
        if (current == null) {
            throw new IllegalArgumentException("截图笔记不存在");
        }
        screenshotNoteMapper.updateContent(
                id,
                userId,
                note == null ? "" : note,
                aiAnalysis == null ? current.getAiAnalysis() : aiAnalysis
        );
        return screenshotNoteMapper.findByIdAndUserId(id, userId);
    }

    @Override
    public void delete(Long userId, Long id) {
        ScreenshotNote screenshotNote = screenshotNoteMapper.findByIdAndUserId(id, userId);
        if (screenshotNote == null) {
            return;
        }
        screenshotNoteMapper.deleteByIdAndUserId(id, userId);
        try {
            Files.deleteIfExists(screenshotDirectory.resolve(Path.of(screenshotNote.getImageUrl()).getFileName()));
        } catch (IOException exception) {
            throw new IllegalStateException("截图文件删除失败", exception);
        }
    }
}
