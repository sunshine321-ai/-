package edu.jxnu.microcoursebackend.service.impl;

import edu.jxnu.microcoursebackend.mapper.ModuleUsageMapper;
import edu.jxnu.microcoursebackend.pojo.DailyUsageSummary;
import edu.jxnu.microcoursebackend.pojo.ModuleUsageEventRequest;
import edu.jxnu.microcoursebackend.pojo.ModuleUsageSummary;
import edu.jxnu.microcoursebackend.service.ModuleUsageService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ModuleUsageServiceImpl implements ModuleUsageService {
    private static final Set<String> MODULES = Set.of(
            "video", "study", "ai_tutor", "playground", "showcase_3d", "data_viz", "calculator"
    );
    private static final Set<String> EVENT_TYPES = Set.of("page_view", "duration");
    private final ModuleUsageMapper mapper;

    public ModuleUsageServiceImpl(ModuleUsageMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void record(Long userId, ModuleUsageEventRequest request) {
        if (!MODULES.contains(request.moduleKey())) {
            throw new IllegalArgumentException("不支持的模块：" + request.moduleKey());
        }
        if (!EVENT_TYPES.contains(request.eventType())) {
            throw new IllegalArgumentException("不支持的使用事件：" + request.eventType());
        }
        int duration = request.durationSeconds() == null ? 0 : Math.min(request.durationSeconds(), 86400);
        mapper.insert(userId, request.moduleKey(), request.eventType(), duration);
    }

    @Override
    public Map<String, List<?>> summary(Long userId) {
        List<ModuleUsageSummary> modules = mapper.summarizeModules(userId);
        List<DailyUsageSummary> daily = mapper.summarizeLastSevenDays(userId);
        return Map.of("modules", modules, "daily", daily);
    }
}
