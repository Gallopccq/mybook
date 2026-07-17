package com.mybook.mybook.kv.biz;

import com.mybook.framework.common.util.JsonUtils;
import com.mybook.mybook.kv.biz.domain.dataobject.NoteContentDO;
import com.mybook.mybook.kv.biz.domain.repository.NoteContentRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;
import java.util.UUID;

@SpringBootTest
@Slf4j
class CassandraTests {

    @Resource
    private NoteContentRepository noteContentRepository;

    /**
     * 测试插入数据
     */
    @Test
    void testInsert() {
        NoteContentDO noteContentDO = NoteContentDO.builder()
                .id(UUID.randomUUID())
                .content("笔记插入")
                .build();
        noteContentRepository.save(noteContentDO);
    }

    @Test
    void testSelect(){
        Optional<NoteContentDO> optional = noteContentRepository.findById(UUID.fromString("13701b12-b070-4fc2-abcb-f8f9737ef403"));
        optional.ifPresent(noteContentDO -> log.info("查询结果：{}", JsonUtils.toJsonString(noteContentDO)));
    }

    @Test
    void testDelete(){
        noteContentRepository.deleteById(UUID.fromString("13701b12-b070-4fc2-abcb-f8f9737ef403"));
    }
}
