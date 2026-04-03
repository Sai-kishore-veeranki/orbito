package com.vsk.orbito.pr.service;

import com.vsk.orbito.event.OrbitoEventPublisher;
import com.vsk.orbito.exception.ResourceNotFoundException;
import com.vsk.orbito.pr.document.PRComment;
import com.vsk.orbito.pr.dto.*;
import com.vsk.orbito.pr.entity.PullRequest;
import com.vsk.orbito.pr.enums.PRStatus;
import com.vsk.orbito.pr.repository.PRCommentRepository;
import com.vsk.orbito.pr.repository.PullRequestRepository;
import com.vsk.orbito.project.entity.Project;
import com.vsk.orbito.project.repository.ProjectRepository;
import com.vsk.orbito.repository.UserRepository;
import com.vsk.orbito.task.entity.Task;
import com.vsk.orbito.task.enums.TaskStatus;
import com.vsk.orbito.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PullRequestService {

    private final PullRequestRepository prRepository;
    private final PRCommentRepository commentRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final OrbitoEventPublisher eventPublisher;

    // ─── PR OPERATIONS ────────────────────────────────────────

    @Transactional
    public PRResponse createPR(
            CreatePRRequest request, String authorEmail) {

        Project project = projectRepository
                .findById(request.getProjectId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Project not found"));

        User author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        Task linkedTask = null;
        if (request.getLinkedTaskId() != null) {
            linkedTask = taskRepository
                    .findById(request.getLinkedTaskId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Task not found"));
        }

        PullRequest pr = PullRequest.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .sourceBranch(request.getSourceBranch())
                .targetBranch(request.getTargetBranch())
                .project(project)
                .author(author)
                .linkedTask(linkedTask)
                .status(PRStatus.OPEN)
                .build();

        PullRequest saved = prRepository.save(pr);
        log.info("PR created: {} by {}", saved.getId(), authorEmail);
        return toResponse(saved);
    }

    @Transactional
    public PRResponse assignReviewer(
            Long prId, Long reviewerId, String requesterEmail) {

        PullRequest pr = getPROrThrow(prId);
        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Reviewer not found"));

        pr.getReviewers().add(reviewer);
        return toResponse(prRepository.save(pr));
    }

    @Transactional
    public PRResponse approvePR(Long prId, String reviewerEmail) {
        PullRequest pr = getPROrThrow(prId);

        User reviewer = userRepository.findByEmail(reviewerEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        // only assigned reviewers can approve
        boolean isReviewer = pr.getReviewers()
                .stream()
                .anyMatch(r -> r.getEmail().equals(reviewerEmail));

        if (!isReviewer) {
            throw new IllegalArgumentException(
                    "You are not an assigned reviewer for this PR");
        }

        pr.getApprovals().add(reviewer);
        pr.setStatus(PRStatus.APPROVED);
        log.info("PR {} approved by {}", prId, reviewerEmail);
        return toResponse(prRepository.save(pr));
    }

    @Transactional
    public PRResponse requestChanges(Long prId, String reviewerEmail) {
        PullRequest pr = getPROrThrow(prId);

        boolean isReviewer = pr.getReviewers()
                .stream()
                .anyMatch(r -> r.getEmail().equals(reviewerEmail));

        if (!isReviewer) {
            throw new IllegalArgumentException(
                    "You are not an assigned reviewer for this PR");
        }

        pr.setStatus(PRStatus.CHANGES_REQUESTED);
        log.info("Changes requested on PR {} by {}", prId, reviewerEmail);
        return toResponse(prRepository.save(pr));
    }

    // the most important method — merge does 3 things in one transaction
    @Transactional
    public PRResponse mergePR(Long prId, String mergerEmail) {
        PullRequest pr = getPROrThrow(prId);

        // only APPROVED PRs can be merged
        if (pr.getStatus() != PRStatus.APPROVED) {
            throw new IllegalArgumentException(
                    "PR must be APPROVED before merging. " +
                            "Current status: " + pr.getStatus());
        }

        User merger = userRepository.findByEmail(mergerEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        // 1. mark PR as merged
        pr.setStatus(PRStatus.MERGED);
        pr.setMergedAt(LocalDateTime.now());
        pr.setMergedBy(merger);

        // 2. if linked task exists — auto close it
        if (pr.getLinkedTask() != null) {
            Task task = pr.getLinkedTask();
            task.setStatus(TaskStatus.DONE);
            taskRepository.save(task);
            log.info("Task {} auto-closed on PR merge", task.getId());
        }

        PullRequest saved = prRepository.save(pr);
        log.info("PR {} merged by {}", prId, mergerEmail);

        // 3. Kafka event will be published here in Week 5
        // eventPublisher.publishPRMergedEvent(saved);
//        eventPublisher.publishPRMerged(PRMergedEvent.builder()
//                .prId(saved.getId())
//                .prTitle(saved.getTitle())
//                .authorEmail(saved.getAuthor().getEmail())
//                .authorName(saved.getAuthor().getName())
//                .mergedByName(merger.getName())
//                .projectName(saved.getProject().getName())
//                .linkedTaskId(saved.getLinkedTask() != null
//                        ? saved.getLinkedTask().getId() : null)
//                .build());

        return toResponse(saved);
    }

    @Transactional
    public PRResponse closePR(Long prId, String requesterEmail) {
        PullRequest pr = getPROrThrow(prId);

        if (pr.getStatus() == PRStatus.MERGED) {
            throw new IllegalArgumentException(
                    "Cannot close an already merged PR");
        }

        pr.setStatus(PRStatus.CLOSED);
        return toResponse(prRepository.save(pr));
    }

    public Page<PRResponse> getPRsByProject(
            Long projectId, PRStatus status, Pageable pageable) {

        if (status != null) {
            return prRepository
                    .findByProjectIdAndStatus(projectId, status, pageable)
                    .map(this::toResponse);
        }
        return prRepository
                .findByProjectId(projectId, pageable)
                .map(this::toResponse);
    }

    public PRResponse getById(Long id) {
        return toResponse(getPROrThrow(id));
    }

    // ─── COMMENT OPERATIONS ───────────────────────────────────

    public CommentResponse addComment(
            Long prId, CreateCommentRequest request, String authorEmail) {

        // verify PR exists
        getPROrThrow(prId);

        User author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        PRComment comment = PRComment.builder()
                .prId(prId)
                .authorId(author.getId())
                .authorName(author.getName())
                .content(request.getContent())
                .lineNumber(request.getLineNumber())
                .parentCommentId(request.getParentCommentId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        PRComment saved = commentRepository.save(comment);

        // get PR details for the event
        PullRequest pr = getPROrThrow(prId);
//        eventPublisher.publishPRCommentAdded(PRCommentAddedEvent.builder()
//                .prId(prId)
//                .prTitle(pr.getTitle())
//                .commentContent(request.getContent())
//                .commenterName(author.getName())
//                .prAuthorEmail(pr.getAuthor().getEmail())
//                .prAuthorId(pr.getAuthor().getId())
//                .build());

        log.info("Comment added to PR {} by {}", prId, authorEmail);
        return toCommentResponse(saved);
    }

    public List<CommentResponse> getComments(Long prId) {
        // get top-level comments
        List<PRComment> topLevel = commentRepository
                .findByPrIdAndParentCommentIdIsNull(prId);

        // for each top-level comment, attach its replies
        return topLevel.stream()
                .map(comment -> {
                    CommentResponse response = toCommentResponse(comment);
                    List<CommentResponse> replies = commentRepository
                            .findByParentCommentId(comment.getId())
                            .stream()
                            .map(this::toCommentResponse)
                            .toList();
                    response.setReplies(replies);
                    return response;
                })
                .toList();
    }

    @Transactional
    public CommentResponse editComment(
            String commentId, String newContent, String editorEmail) {

        PRComment comment = commentRepository.findById(commentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Comment not found"));

        User editor = userRepository.findByEmail(editorEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        // only the author can edit their own comment
        if (!comment.getAuthorId().equals(editor.getId())) {
            throw new IllegalArgumentException(
                    "You can only edit your own comments");
        }

        comment.setContent(newContent);
        comment.setEdited(true);
        comment.setUpdatedAt(LocalDateTime.now());

        return toCommentResponse(commentRepository.save(comment));
    }

    public void deleteComment(String commentId, String requesterEmail) {
        PRComment comment = commentRepository.findById(commentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Comment not found"));

        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        if (!comment.getAuthorId().equals(requester.getId())) {
            throw new IllegalArgumentException(
                    "You can only delete your own comments");
        }

        commentRepository.delete(comment);
    }

    // ─── HELPERS ──────────────────────────────────────────────

    private PullRequest getPROrThrow(Long id) {
        return prRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Pull Request not found"));
    }

    private PRResponse toResponse(PullRequest pr) {
        int commentCount = (int) commentRepository.countByPrId(pr.getId());

        Set<String> reviewerNames = pr.getReviewers()
                .stream()
                .map(User::getName)
                .collect(Collectors.toSet());

        Set<String> approvalNames = pr.getApprovals()
                .stream()
                .map(User::getName)
                .collect(Collectors.toSet());

        return PRResponse.builder()
                .id(pr.getId())
                .title(pr.getTitle())
                .description(pr.getDescription())
                .sourceBranch(pr.getSourceBranch())
                .targetBranch(pr.getTargetBranch())
                .status(pr.getStatus())
                .authorName(pr.getAuthor().getName())
                .projectName(pr.getProject().getName())
                .linkedTaskTitle(pr.getLinkedTask() != null
                        ? pr.getLinkedTask().getTitle() : null)
                .reviewerNames(reviewerNames)
                .approvalNames(approvalNames)
                .commentCount(commentCount)
                .createdAt(pr.getCreatedAt())
                .mergedAt(pr.getMergedAt())
                .build();
    }

    private CommentResponse toCommentResponse(PRComment c) {
        return CommentResponse.builder()
                .id(c.getId())
                .prId(c.getPrId())
                .authorName(c.getAuthorName())
                .content(c.getContent())
                .lineNumber(c.getLineNumber())
                .parentCommentId(c.getParentCommentId())
                .replies(List.of()) // replies attached separately
                .isEdited(c.isEdited())
                .createdAt(c.getCreatedAt())
                .build();
    }
}