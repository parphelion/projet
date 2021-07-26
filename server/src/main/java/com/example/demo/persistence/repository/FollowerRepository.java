package com.example.demo.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.persistence.models.Follower;
import com.example.demo.persistence.models.User;

@Repository
public interface FollowerRepository extends JpaRepository<Follower, Long> {

	@Modifying
	@Query(value = "DELETE f.* FROM  follower AS f CROSS JOIN utilisateur AS u ON f.utilisateur2_id = u.id WHERE u.username=:username AND f.utilisateur1_id=:user", nativeQuery = true)
	int unfollow(@Param("user") User user, @Param("username") String username);
	
	@Query("SELECT COUNT(f)>0 from Follower f WHERE f.utilisateur1 = :user1 AND f.utilisateur2 = :user2")
	boolean isFollowed(@Param ("user1") User currentUser, @Param("user2") User browsedUser);
	
	@Query(value = "SELECT COUNT(utilisateur2_id) FROM Follower f CROSS JOIN utilisateur u on f.utilisateur2_id = u.id WHERE u.username= :username", nativeQuery=true)
	Long countFollowers(@Param ("username") String username);
}
