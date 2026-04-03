package com.vsk.orbito.pr.repository;

import com.vsk.orbito.pr.entity.PullRequest;
import com.vsk.orbito.pr.enums.PRStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PullRequestRepository
        extends JpaRepository<PullRequest, Long> {

    // all PRs in a project
    Page<PullRequest> findByProjectId(
            Long projectId, Pageable pageable);

    // filter by status
    Page<PullRequest> findByProjectIdAndStatus(
            Long projectId, PRStatus status, Pageable pageable);

    // PRs raised by a user
    List<PullRequest> findByAuthorId(Long authorId);

    // PRs where user is a reviewer
    @Query("SELECT pr FROM PullRequest pr " +
            "JOIN pr.reviewers r WHERE r.id = :userId")
    List<PullRequest> findByReviewerId(Long userId);

    // count open PRs in a project — for dashboard
    long countByProjectIdAndStatus(Long projectId, PRStatus status);
}