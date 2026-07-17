package com.mybook.mybook.note.biz.service.impl;

import com.mybook.framework.common.response.Response;
import com.mybook.mybook.note.biz.service.NoteService;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class NoteServiceImpl implements NoteService {
    @Resource
    ThreadPoolTaskExecutor threadPoolTaskExecutor;

//    @Override
    @SneakyThrows
    public Response<?> findNoteDetail(){
        CompletableFuture<Object> userResultFuture = CompletableFuture
                .supplyAsync(() -> Integer.valueOf(1), threadPoolTaskExecutor);
        CompletableFuture<String> contentResultFuture = CompletableFuture.completedFuture(null);
        if (Objects.equals(Integer.valueOf(1), Integer.valueOf(1))) {
            contentResultFuture = CompletableFuture
                    .supplyAsync(() -> String.valueOf(1), threadPoolTaskExecutor);
        }
        CompletableFuture<String> finalContentResultFuture = contentResultFuture;
        CompletableFuture<Object> resultFuture = CompletableFuture
                .allOf(userResultFuture, contentResultFuture)
                .thenApply(s -> {
                   Object findUserByIdRspDTO = userResultFuture.join();
                   String content = finalContentResultFuture.join();

                   Integer noteType = Integer.valueOf(1);
                   String imgUrisStr = String.valueOf(1);
                   List<String> imgUris = null;
                   if (Objects.equals(noteType, 0)
                        && !imgUrisStr.isBlank()){
                       imgUris = List.of(imgUrisStr.split(","));
                   }
                   return Integer.valueOf(1);

                });
        Object findNoteDetailRspVO = resultFuture.get();

        return Response.success(findNoteDetailRspVO);
    }
}
