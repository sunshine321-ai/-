package edu.jxnu.microcoursebackend.service;

import edu.jxnu.microcoursebackend.pojo.Bookmark;
import edu.jxnu.microcoursebackend.pojo.BookmarkSaveRequest;

import java.util.List;

public interface BookmarkService {
    List<Bookmark> list(Long userId);
    Bookmark create(Long userId, BookmarkSaveRequest request);
    void delete(Long userId, Long id);
    void clear(Long userId);
}
