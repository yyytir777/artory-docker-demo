package com.example.demo.domain.comment.entity;

import com.example.demo.domain.common.BaseEntity;
import com.example.demo.domain.member.entity.Member;
import com.example.demo.domain.story.entity.Story;
import jakarta.persistence.*;
import lombok.*;



@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    private String commentSatisfactionLevel;

    @Column(length = 100000)
    private String commentContext;

    // 댓글 삭제 시 해당 댓글을 유지할 것인지
    private Boolean isDeleted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id")
    private Story story;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;
}