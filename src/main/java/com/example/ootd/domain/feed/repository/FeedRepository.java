package com.example.ootd.domain.feed.repository;

import com.example.ootd.domain.feed.entity.Feed;
import com.example.ootd.domain.feed.repository.custom.CustomFeedRepository;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedRepository extends JpaRepository<Feed, UUID>, CustomFeedRepository {

}
