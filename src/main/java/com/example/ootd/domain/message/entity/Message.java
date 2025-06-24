package com.example.ootd.domain.message.entity;

import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
@Table(
    name = "messages",
    indexes = {
        @Index(name = "index", columnList = "sender, receiver")
    })
public class Message {

  @Id
  @Column(columnDefinition = "id", nullable = false, updatable = false)
  private UUID id;
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "sender", nullable = false)
  private User sender;
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "receiver", nullable = false)
  private User receiver;
  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;
  @Column(length = 255)
  private String contents;


  @Builder
  private Message(User sender, User receiver, String contents) {
    this.id = UUID.randomUUID();
    this.sender = sender;
    this.receiver = receiver;
    this.contents = contents;
  }

  public static Message createMessage(User sender, User receiver, String contents) {
    return Message.builder()
        .sender(sender)
        .receiver(receiver)
        .contents(contents)
        .build();
  }

}
