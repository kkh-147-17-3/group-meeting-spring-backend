package com.sideprj.groupmeeting;

import com.sideprj.groupmeeting.dto.DiscordMessage;
import com.sideprj.groupmeeting.service.DiscordFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiscordMessageService {
    private final DiscordFeignClient client;

    @Async
    public void sendMessage(DiscordMessage message){
        CompletableFuture.runAsync(()->client.sendMessage(message));
    }
}
