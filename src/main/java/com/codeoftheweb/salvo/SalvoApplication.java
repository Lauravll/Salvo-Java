package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import java.util.logging.Logger;


@SpringBootApplication
public class SalvoApplication {

	private static final Logger LOGGER = Logger.getLogger(SalvoApplication.class.getName());

	@Autowired
	PasswordEncoder passwordEncoder;

	public static void main(String[] args) {
		SpringApplication.run(SalvoApplication.class, args);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	//Marca tareas prioritarias para el springboot, va invocando secuencialmetne clases . Por eso las entity los carga primero y los trata como tabla, y los autowired que asume que estan relacionados con otros objetos
	//Antes de que arranque la ejecucion
	//turn an instance of a bean (clase normal). Spring save the instance for autowired injection.
	@Bean
	//tells Spring to run initData() and saves the value returned. initData() creates and returns a lambda expression
	//he return type of initData() is CommandLineRunner. CommandLineRunner is a functional interface, i.e., an interface with just one method, run()
	public CommandLineRunner initData(PlayerRepository playerRepository, GameRepository gameRepository, GamePlayerRepository gamePlayerRepository, ShipRepository shipRepository, SalvoRepository salvoRepository, ScoreRepository scoreRepository){

		return (args) -> {
/*
			Player player1 = new Player("j.bauer@ctu.gov", passwordEncoder.encode("123"));
			Player player2 = new Player ("c.obrian@ctu.gov", passwordEncoder.encode("123"));
			Player player3 = new Player ("usuario3@ctu.gov", passwordEncoder.encode("123"));
			playerRepository.save(player1);
			playerRepository.save(player2);
			playerRepository.save(player3);

			Date date = new Date();
			Date newDate = Date.from(date.toInstant().plusSeconds(3600 ));
			Date newDate2 = Date.from(date.toInstant().plusSeconds(7200));

			Game game = new Game(date);
			Game game2 = new Game(newDate);
			Game game3 = new Game(newDate2);

			gameRepository.save(game);
			gameRepository.save(game2);
			gameRepository.save(game3);

			GamePlayer gamePlayer = new GamePlayer(player1, game, date);
			GamePlayer gamePlayer2 = new GamePlayer(player2, game, date);
			GamePlayer gamePlayer3 = new GamePlayer(player3, game2, date);
			GamePlayer gamePlayer4 = new GamePlayer(player2, game2, newDate);
			GamePlayer gamePlayer5 = new GamePlayer(player2, game3, date);

			gamePlayerRepository.save(gamePlayer);
			gamePlayerRepository.save(gamePlayer2);
			gamePlayerRepository.save(gamePlayer3);
			gamePlayerRepository.save(gamePlayer4);
			gamePlayerRepository.save(gamePlayer5);

			List<String> patrolBoatLocations = new ArrayList<>();
			patrolBoatLocations.add("A1");
			patrolBoatLocations.add("B1");

			List<String> destroyerLocations = new ArrayList<>();
			destroyerLocations.add("G2");
			destroyerLocations.add("G3");
			destroyerLocations.add("G1");

			List<String> submarineLocations = new ArrayList<>();
			submarineLocations.add("E10");
			submarineLocations.add("F10");
			submarineLocations.add("G10");

			List<String> battleshipLocations = new ArrayList<>();
			battleshipLocations.add("A10");
			battleshipLocations.add("B10");
			battleshipLocations.add("C10");
			battleshipLocations.add("D10");

			List<String> carrierLocations = new ArrayList<>();
			carrierLocations.add("J1");
			carrierLocations.add("J2");
			carrierLocations.add("J3");
			carrierLocations.add("J4");
			carrierLocations.add("J5");

			Ship ship = new Ship("PatrolBoat", gamePlayer, patrolBoatLocations);
			Ship ship2 = new Ship("Submarine", gamePlayer, submarineLocations);
			Ship ship3 = new Ship("Destroyer", gamePlayer, destroyerLocations);
			Ship ship4 = new Ship("Carrier", gamePlayer, carrierLocations);
			Ship ship5 = new Ship("Battleship", gamePlayer, battleshipLocations);

			List<String> patrolBoatLocations2 = Arrays.asList("A1", "B1");
			List<String> submarineLocations2 = Arrays.asList("E10", "F10", "G10");
			List<String> destroyerLocations2 = Arrays.asList("H10", "I10", "J10");
			List<String> carrierLocations2 = Arrays.asList("J1", "J2", "J3", "J4", "J5");
			List<String> battleshipLocations2 = Arrays.asList("A10", "B10", "C10", "D10");

			Ship shipb = new Ship("PatrolBoat", gamePlayer2, patrolBoatLocations2);
			Ship shipb2 = new Ship("Submarine", gamePlayer2, submarineLocations2);
			Ship shipb3 = new Ship("Destroyer", gamePlayer2, destroyerLocations2);
			Ship shipb4 = new Ship("Carrier", gamePlayer2, carrierLocations2);
			Ship shipb5 = new Ship("Battleship", gamePlayer2, battleshipLocations2);

			shipRepository.save(ship);
			shipRepository.save(ship2);
			shipRepository.save(ship3);
			shipRepository.save(ship4);
			shipRepository.save(ship5);
			shipRepository.save(shipb);
			shipRepository.save(shipb2);
			shipRepository.save(shipb3);
			shipRepository.save(shipb4);
			shipRepository.save(shipb5);


			List<String> salvoLocationsp1 = Arrays.asList("J3", "J2", "J1", "J4", "J5");
			List<String> salvoLocations2p1 = Arrays.asList("H2", "H1", "I2", "I3", "I1");
			/*
			List<String> salvoLocations3 = new ArrayList<>();
			salvoLocations3.add("A1");
			*/
/*
			List<String> salvoLocations3p1 = Arrays.asList("G2", "G3", "G1");
			List<String> salvoLocations4p1 = Arrays.asList("F4", "F3", "F2", "F1");

			List<String> salvoLocationsp2 = Arrays.asList("A1","B1", "A10", "B10", "C10");
			List<String> salvoLocations2p2 = Arrays.asList("D10", "E10", "F10", "G10", "H10");
			List<String> salvoLocations3p2 = Arrays.asList("G5", "F5");
			List<String> salvoLocations4p2 = Arrays.asList("J1", "J2", "J3", "J4", "J5");



			//Juego 1
			Salvo salvo1 = new Salvo(1, gamePlayer2, salvoLocationsp1 );
			Salvo salvo2 = new Salvo(2, gamePlayer2, salvoLocations2p1 );
			Salvo salvo3 = new Salvo(3, gamePlayer2, salvoLocations3p1 );
			Salvo salvo4 = new Salvo(4, gamePlayer2, salvoLocations4p1 );
			Salvo salvo5 = new Salvo(1, gamePlayer, salvoLocationsp2 );
			Salvo salvo6 = new Salvo(2, gamePlayer, salvoLocations2p2 );
			Salvo salvo7 = new Salvo(3, gamePlayer, salvoLocations3p2 );
			Salvo salvo8 = new Salvo(4, gamePlayer, salvoLocations4p2 );


			//Juego 2
			//Salvo salvo4 = new Salvo(2, gamePlayer3, salvoLocations2 );

			salvoRepository.save(salvo1);
			salvoRepository.save(salvo2);
			salvoRepository.save(salvo3);
			salvoRepository.save(salvo4);
			salvoRepository.save(salvo5);
			salvoRepository.save(salvo6);
			salvoRepository.save(salvo7);
			salvoRepository.save(salvo8);

			Date dates = new Date();
			Score score = new Score( (float) 0.5, dates, player1, game );
			Score score2 = new Score((float)1, dates, player2, game);
			Score score3 = new Score(0, dates, player1, game);

			scoreRepository.save(score);
			scoreRepository.save(score2);
			scoreRepository.save(score3);

			player1.addScore(score);
			player2.addScore(score2);
			player1.addScore(score3);



*/
			//System.out.println(playerRepository.findByEmail("c.obrian@ctu.gov"));

		};

	}

}

//Clase no pública ->  Java lets you define multiple classes in one file, but only one class can be public.
//give Spring a method to get a user's password and roles are stored in the Player repository
//should appear after and completely outside of the definition of the Application class.
@Configuration
class WebSecurityConfiguration extends GlobalAuthenticationConfigurerAdapter {

	@Autowired
	PlayerRepository playerRepository;

	@Override
	public void init(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(inputName-> {
			Player player = playerRepository.findByEmail(inputName);
			//System.out.println(player);
			if (player != null) {
				return new User(player.getEmail(), player.getPassword(), //User : clase predefinida de Spring
						AuthorityUtils.createAuthorityList("USER")); //Le da un rol
			} else {
				throw new UsernameNotFoundException("Unknown user: " + inputName);
			}
		});
	}
}

@EnableWebSecurity
@Configuration
class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
				//.antMatchers("/api/games").permitAll()

				.antMatchers( "/web/games_3.html").permitAll()
				.antMatchers( "/web/**").permitAll()
				.antMatchers( "/api/games.").permitAll()
				.antMatchers( "/api/players").permitAll()
				.antMatchers( "/api/game_view/*").hasAuthority("USER")
				.antMatchers( "/rest/*").denyAll()
				.anyRequest().permitAll();


		http.formLogin()
				.usernameParameter("username")
				.passwordParameter("password")
				.loginPage("/api/login");

		http.logout().logoutUrl("/api/logout");

		//para la bd
		http.headers().frameOptions().sameOrigin();


		// turn off checking for CSRF tokens
		http.csrf().disable();

		// if user is not authenticated, just send an authentication failure response
		http.exceptionHandling().authenticationEntryPoint((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

		// if login is successful, just clear the flags asking for authentication
		http.formLogin().successHandler((req, res, auth) -> clearAuthenticationAttributes(req));

		// if login fails, just send an authentication failure response
		http.formLogin().failureHandler((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

		// if logout is successful, just send a success response
		http.logout().logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler());
	}

	private void clearAuthenticationAttributes(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
		}

	}
}

