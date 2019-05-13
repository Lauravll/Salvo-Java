package com.codeoftheweb.salvo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@SpringBootApplication
public class SalvoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalvoApplication.class, args);
	}

	//Marca tareas prioritarias para el springboot, va invocando secuencialmetne clases . Por eso las entity los carga primero y los trata como tabla, y los autowired que asume que estan relacionados con otros objetos
	//Antes de que arranque la ejecucion
	@Bean
	public CommandLineRunner initData(PlayerRepository playerRepository, GameRepository gameRepository, GamePlayerRepository gamePlayerRepository, ShipRepository shipRepository, SalvoRepository salvoRepository ){

		return (args) -> {

			Player p1 = new Player("j.bauer@ctu.gov", "p1");
			Player p2 = new Player ("c.obrian@ctu.gov", "p2");
			Player p3 = new Player ("c.oconan@ctu.gov", "p3");
			playerRepository.save(p1);
			playerRepository.save(p2);
			playerRepository.save(p3);

			Date date = new Date();
			Date newDate = Date.from(date.toInstant().plusSeconds(3600 ));
			Date newDate2 = Date.from(date.toInstant().plusSeconds(7200));

			Game g = new Game(date);
			Game g2 = new Game(newDate);
			Game g3 = new Game(newDate2);

			gameRepository.save(g);
			gameRepository.save(g2);
			gameRepository.save(g3);

			GamePlayer gamePlayer = new GamePlayer(p1, g, date);
			GamePlayer gamePlayer2 = new GamePlayer(p2, g, date);
			GamePlayer gamePlayer3 = new GamePlayer(p3, g2, date);
			GamePlayer gamePlayer4 = new GamePlayer(p2, g2, newDate);

			gamePlayerRepository.save(gamePlayer);
			gamePlayerRepository.save(gamePlayer2);
			gamePlayerRepository.save(gamePlayer3);
			gamePlayerRepository.save(gamePlayer4);

			List<String> shipLocations1 = new ArrayList<>();
			shipLocations1.add("H4");
			shipLocations1.add("H3");

			List<String> shipLocations2 = new ArrayList<>();
			shipLocations2.add("A2");
			shipLocations2.add("B2");
			shipLocations2.add("C2");
			shipLocations2.add("D2");

			List<String> shipLocations3 = new ArrayList<>();
			shipLocations3.add("A1");
			shipLocations3.add("B1");

			List<String> shipLocations4 = new ArrayList<>();
			shipLocations4.add("H");
			shipLocations4.add("I1");
			shipLocations4.add("J1");


			Ship s = new Ship("Patrol Boat", gamePlayer2, shipLocations1);
			Ship s2 = new Ship("Submarine", gamePlayer, shipLocations2);
			Ship s3 = new Ship("Destroyer", gamePlayer2, shipLocations3);
			Ship s4 = new Ship("Carrier", gamePlayer, shipLocations4);


			shipRepository.save(s);
			shipRepository.save(s2);
			shipRepository.save(s3);
			shipRepository.save(s4);

			//agrego barco
			gamePlayer.addShip(s3);
			gamePlayer2.addShip(s2);
			gamePlayer3.addShip(s);
			gamePlayer4.addShip(s4);


			List<String> salvoLocations = new ArrayList<>();
			salvoLocations.add("H");

			List<String> salvoLocations2 = Arrays.asList("A2", "A3");

			List<String> salvoLocations3 = new ArrayList<>();
			salvoLocations3.add("A1");

			//Juego 1
			Salvo salvo = new Salvo(1, gamePlayer2, salvoLocations );
			Salvo salvo2 = new Salvo(2, gamePlayer, salvoLocations2 );
			Salvo salvo3 = new Salvo(2, gamePlayer, salvoLocations3 );

			//Juego 2
			Salvo salvo4 = new Salvo(2, gamePlayer3, salvoLocations2 );

			salvoRepository.save(salvo);
			salvoRepository.save(salvo2);
			salvoRepository.save(salvo3);
			salvoRepository.save(salvo4);


		};

	}

}
