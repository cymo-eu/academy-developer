package eu.cymo.scenario_1.adapter.persistence.user;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserEntityRepository extends JpaRepository<UserEntity, String> {

}
