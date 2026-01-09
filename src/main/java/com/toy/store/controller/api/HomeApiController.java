package com.toy.store.controller.api;

import com.toy.store.dto.ApiResponse;
import com.toy.store.mapper.HomeMapper;
import com.toy.store.model.payload.GameFeedItem;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class HomeApiController {

    private final HomeMapper homeMapper;

    @GetMapping("/feed")
    public ResponseEntity<ApiResponse<List<GameFeedItem>>> getFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        int offset = page * size;
        List<GameFeedItem> feed = homeMapper.getGameFeed(offset, size);

        return ResponseEntity.ok(ApiResponse.ok(feed));
    }
}
