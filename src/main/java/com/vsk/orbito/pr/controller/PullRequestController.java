package com.vsk.orbito.pr.controller;

import com.vsk.orbito.dto.response.ApiResponse;
import com.vsk.orbito.pr.dto.*;
import com.vsk.orbito.pr.enums.PRStatus;
import com.vsk.orbito.pr.service.PullRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pull-requests")
@RequiredArgsConstructor
@Tag(name = "Pull Requests",
        description = "PR creation, review, merge and comments")
public class PullRequestController {

    private final PullRequestService prService;

    // ─── PR ENDPOINTS ─────────────────────────────────────────

    @PostMapping
    @Operation(summary = "Raise a new pull request")
    public ResponseEntity<ApiResponse<PRResponse>> create(
            @Valid @RequestBody CreatePRRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        PRResponse response = prService
                .createPR(request, userDetails.getUsername());
        return ResponseEntity.ok(
                ApiResponse.success("Pull request created", response));
    }

    @PostMapping("/{prId}/reviewers/{reviewerId}")
    @Operation(summary = "Assign a reviewer to a PR")
    public ResponseEntity<ApiResponse<PRResponse>> assignReviewer(
            @PathVariable Long prId,
            @PathVariable Long reviewerId,
            @AuthenticationPrincipal UserDetails userDetails) {

        PRResponse response = prService
                .assignReviewer(prId, reviewerId,
                        userDetails.getUsername());
        return ResponseEntity.ok(
                ApiResponse.success("Reviewer assigned", response));
    }

    @PostMapping("/{prId}/approve")
    @Operation(summary = "Approve a PR")
    public ResponseEntity<ApiResponse<PRResponse>> approve(
            @PathVariable Long prId,
            @AuthenticationPrincipal UserDetails userDetails) {

        PRResponse response = prService
                .approvePR(prId, userDetails.getUsername());
        return ResponseEntity.ok(
                ApiResponse.success("PR approved", response));
    }

    @PostMapping("/{prId}/request-changes")
    @Operation(summary = "Request changes on a PR")
    public ResponseEntity<ApiResponse<PRResponse>> requestChanges(
            @PathVariable Long prId,
            @AuthenticationPrincipal UserDetails userDetails) {

        PRResponse response = prService
                .requestChanges(prId, userDetails.getUsername());
        return ResponseEntity.ok(
                ApiResponse.success("Changes requested", response));
    }

    @PostMapping("/{prId}/merge")
    @Operation(summary = "Merge a PR — auto closes linked task")
    public ResponseEntity<ApiResponse<PRResponse>> merge(
            @PathVariable Long prId,
            @AuthenticationPrincipal UserDetails userDetails) {

        PRResponse response = prService
                .mergePR(prId, userDetails.getUsername());
        return ResponseEntity.ok(
                ApiResponse.success("PR merged successfully", response));
    }

    @PostMapping("/{prId}/close")
    @Operation(summary = "Close a PR without merging")
    public ResponseEntity<ApiResponse<PRResponse>> close(
            @PathVariable Long prId,
            @AuthenticationPrincipal UserDetails userDetails) {

        PRResponse response = prService
                .closePR(prId, userDetails.getUsername());
        return ResponseEntity.ok(
                ApiResponse.success("PR closed", response));
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Get all PRs in a project — paginated")
    public ResponseEntity<ApiResponse<Page<PRResponse>>> getByProject(
            @PathVariable Long projectId,
            @RequestParam(required = false) PRStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(
                page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<PRResponse> prs = prService
                .getPRsByProject(projectId, status, pageable);
        return ResponseEntity.ok(
                ApiResponse.success("Pull requests fetched", prs));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get PR by ID")
    public ResponseEntity<ApiResponse<PRResponse>> getById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("PR fetched", prService.getById(id)));
    }

    // ─── COMMENT ENDPOINTS ────────────────────────────────────

    @PostMapping("/{prId}/comments")
    @Operation(summary = "Add a comment or reply to a PR")
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(
            @PathVariable Long prId,
            @Valid @RequestBody CreateCommentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        CommentResponse response = prService
                .addComment(prId, request, userDetails.getUsername());
        return ResponseEntity.ok(
                ApiResponse.success("Comment added", response));
    }

    @GetMapping("/{prId}/comments")
    @Operation(summary = "Get all comments for a PR — threaded")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getComments(
            @PathVariable Long prId) {

        return ResponseEntity.ok(
                ApiResponse.success("Comments fetched",
                        prService.getComments(prId)));
    }

    @PatchMapping("/comments/{commentId}")
    @Operation(summary = "Edit your own comment")
    public ResponseEntity<ApiResponse<CommentResponse>> editComment(
            @PathVariable String commentId,
            @RequestBody CreateCommentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        CommentResponse response = prService
                .editComment(commentId, request.getContent(),
                        userDetails.getUsername());
        return ResponseEntity.ok(
                ApiResponse.success("Comment updated", response));
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "Delete your own comment")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable String commentId,
            @AuthenticationPrincipal UserDetails userDetails) {

        prService.deleteComment(commentId, userDetails.getUsername());
        return ResponseEntity.ok(
                ApiResponse.success("Comment deleted", null));
    }
}