package com.vsk.orbito.pr.repository;

import com.vsk.orbito.pr.document.PRComment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PRCommentRepository
        extends MongoRepository<PRComment, String> {

    // get all top-level comments for a PR
    // parentCommentId == null means it is a top-level comment
    List<PRComment> findByPrIdAndParentCommentIdIsNull(Long prId);

    // get all replies to a specific comment
    List<PRComment> findByParentCommentId(String parentCommentId);

    // count total comments on a PR
    long countByPrId(Long prId);

    // delete all comments when a PR is deleted
    void deleteByPrId(Long prId);
}