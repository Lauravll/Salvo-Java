package com.codeoftheweb.salvo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

//adds a RESTful interface to Spring
//make it easy to send instances of Java classes to browsers and other web clients in JavaScript Object Notation (JSON) format
@RepositoryRestResource
//JpaRepository: turn an extension of CrudRepository. CRUD stands for the most common operations all databases need to support
public interface GamePlayerRepository extends JpaRepository<GamePlayer, Long> {

    GamePlayer findByPlayer(@Param("player") Player player);

}

