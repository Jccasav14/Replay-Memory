package com.replay.search;

import com.replay.auth.User;
import com.replay.common.ApiResponse;
import com.replay.search.dto.SearchRequest;
import com.replay.search.dto.SearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @PostMapping("/semantic")
    public ResponseEntity<ApiResponse<SearchResponse>> semanticSearch(
            @RequestBody SearchRequest request,
            @AuthenticationPrincipal User currentUser) {
        SearchResponse response = searchService.search(request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
