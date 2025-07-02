package com.example.ootd.domain.user.repository;

import com.example.ootd.domain.user.User;
import com.example.ootd.domain.user.dto.UserSearchCondition;
import java.util.List;

public interface CustomUserRepository {

  List<User> searchUserOfCondition(UserSearchCondition condition);
}
