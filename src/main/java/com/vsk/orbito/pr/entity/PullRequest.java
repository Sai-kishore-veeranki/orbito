package com.vsk.orbito.pr.entity;

import com.vsk.orbito.pr.enums.PRStatus;
import com.vsk.orbito.project.entity.Project;
import com.vsk.orbito.task.entity.Task;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "pull_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PullRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    // which branch this PR is from
    @Column(nullable = false)
    private String sourceBranch;

    // which branch this PR merges into
    @Column(nullable = false)
    private String targetBranch;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PRStatus status = PRStatus.OPEN;

    // who raised this PR
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    // which project this PR belongs to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    // optional — linked task that gets closed on merge
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linked_task_id")
    private Task linkedTask;

    // reviewers assigned to this PR
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "pr_reviewers",
            joinColumns = @JoinColumn(name = "pr_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private Set<User> reviewers = new HashSet<>();

    // reviewers who approved
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "pr_approvals",
            joinColumns = @JoinColumn(name = "pr_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private Set<User> approvals = new HashSet<>();

    @Column
    private LocalDateTime mergedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merged_by_id")
    private User mergedBy;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}