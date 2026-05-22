package com.mybook.mybook.auth;

import cn.hutool.json.JSONUtil;
import com.mybook.mybook.auth.domain.dataobject.UserDO;
import com.mybook.mybook.auth.domain.mapper.UserDOMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
public class MyBookAuthApplicationTests {

    @Resource
    UserDOMapper userDOMapper;

    @Test
    void testSelect(){
        UserDO userDO = userDOMapper.selectByPrimaryKey(4L);
        log.info("User {}", JSONUtil.toJsonStr(userDO));
    }
}
