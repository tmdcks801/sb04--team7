package com.example.ootd.domain.feed.repository;

import com.example.ootd.domain.feed.entity.FeedLike;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedLikeRepository extends JpaRepository<FeedLike, UUID> {

}
