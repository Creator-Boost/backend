package com.creatorboost.chat_service.repository;

import com.creatorboost.chat_service.entity.Status;
import com.creatorboost.chat_service.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserRepository  extends MongoRepository<User, String> {
    List<User> findAllByStatus(Status status);
}
