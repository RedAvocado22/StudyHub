package com.studyhub.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "posts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, columnDefinition = "NVARCHAR(100)")
    private String title;
    private String content;
    private String status;
    @Column(name = "created_at",updatable = false)
    LocalDateTime createdAt;
    @Column(name = "updated_at")
    LocalDateTime updatedAt;
    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;
    @OneToMany(mappedBy = "post",cascade = CascadeType.ALL)
    @Singular
    @ToString.Exclude
    private List<Comment> comments;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Setting category;

    @PrePersist
    protected void onCreate() {
        createdAt=LocalDateTime.now();
        updatedAt=createdAt;
    }
    @PreUpdate
    protected void onUpdate() {
        updatedAt=LocalDateTime.now();
    }
}
