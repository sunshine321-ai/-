package edu.jxnu.microcoursebackend.mapper;

import edu.jxnu.microcoursebackend.pojo.DailyUsageSummary;
import edu.jxnu.microcoursebackend.pojo.ModuleUsageSummary;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ModuleUsageMapper {
    @Insert("""
            insert into module_usage_events(user_id, module_key, event_type, duration_seconds)
            values(#{userId}, #{moduleKey}, #{eventType}, #{durationSeconds})
            """)
    void insert(@Param("userId") Long userId,
                @Param("moduleKey") String moduleKey,
                @Param("eventType") String eventType,
                @Param("durationSeconds") Integer durationSeconds);

    @Select("""
            select module_key,
                   sum(case when event_type='page_view' then 1 else 0 end) as visit_count,
                   sum(duration_seconds) as duration_seconds,
                   max(created_at) as last_visited_at
            from module_usage_events
            where user_id=#{userId}
            group by module_key
            order by module_key
            """)
    List<ModuleUsageSummary> summarizeModules(Long userId);

    @Select("""
            select date(created_at) as usage_date,
                   module_key,
                   sum(case when event_type='page_view' then 1 else 0 end) as visit_count,
                   sum(duration_seconds) as duration_seconds
            from module_usage_events
            where user_id=#{userId} and created_at >= current_date - interval 6 day
            group by date(created_at), module_key
            order by usage_date, module_key
            """)
    List<DailyUsageSummary> summarizeLastSevenDays(Long userId);
}
