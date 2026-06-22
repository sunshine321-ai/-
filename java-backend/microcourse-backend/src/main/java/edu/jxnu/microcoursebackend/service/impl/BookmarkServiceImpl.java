package edu.jxnu.microcoursebackend.service.impl;

import edu.jxnu.microcoursebackend.mapper.BookmarkMapper;
import edu.jxnu.microcoursebackend.pojo.Bookmark;
import edu.jxnu.microcoursebackend.pojo.BookmarkSaveRequest;
import edu.jxnu.microcoursebackend.service.BookmarkService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookmarkServiceImpl implements BookmarkService {
    private final BookmarkMapper mapper;

    public BookmarkServiceImpl(BookmarkMapper mapper) {
        this.mapper = mapper;
    }

    public List<Bookmark> list(Long userId) {
        return mapper.findByUserId(userId);
    }

    public Bookmark create(Long userId, BookmarkSaveRequest request) {
        Bookmark item = new Bookmark();
        item.setUserId(userId);
        item.setVideoTime(request.videoTime());
        item.setLabel(request.label());
        mapper.insert(item);
        return mapper.findOne(item.getId(), userId);
    }

    public void delete(Long userId, Long id) {
        mapper.delete(id, userId);
    }

    public void clear(Long userId) {
        mapper.deleteAll(userId);
    }
}
