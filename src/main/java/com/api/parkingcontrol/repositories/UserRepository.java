package com.api.parkingcontrol.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.api.parkingcontrol.models.UserModel;

@Repository
public interface UserRepository  extends JpaRepository<UserModel, Long>{
	
	Optional<UserModel> findByUsername(String username);

}
