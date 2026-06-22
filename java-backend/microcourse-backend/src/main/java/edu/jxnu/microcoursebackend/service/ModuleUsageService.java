package edu.jxnu.microcoursebackend.service;

import edu.jxnu.microcoursebackend.pojo.ModuleUsageEventRequest;

import java.util.List;
import java.util.Map;

public interface ModuleUsageService {
    void record(Long userId, ModuleUsageEventRequest request);

    Map<String, List<?>> summary(Long userId);
}
