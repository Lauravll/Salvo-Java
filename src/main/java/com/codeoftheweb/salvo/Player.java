package com.codeoftheweb.salvo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.*;

import static java.util.stream.Collectors.toList;

@Entity
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;
    private String email;
    private String password;

    @OneToMany( mappedBy="player", fetch=FetchType.EAGER)
    private Set<GamePlayer> gamePlayers  = new HashSet<>();

    @OneToMany( mappedBy="player", fetch=FetchType.EAGER)
    private Set<Score> scores  = new HashSet<>();

    public Player() {}

    public Player(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void addGamePlayer(GamePlayer gamePlayer) {
        gamePlayer.setPlayer(this);
        gamePlayers.add(gamePlayer);
    }

    public void addScore(Score score) {
        score.setPlayer(this);
        scores.add(score);
    }

    public Set<Score> getScores() {
        return scores;
    }
/*
    public Score getScore(Game game) {
        return
        scores
                .stream()
                .filter(score -> score.getGame().getId() == game.getId()).findAny()
                .orElse(null);
    }
*/
    //2) Método -> Retorna todos los scores del player
    public float getScore(){
        return getWins(this.getScores())*1
                + getTies(this.getScores())*((float)0.5)
                + getLosses(this.getScores())*0;
    }

    //3) Método -> Retorna todos los wins del player
    public float getWins(Set<Score> scores){
        return scores
                .stream()
                .filter(score -> score.getScore() == 1)
                .count();
    }

    //4) Método -> Retorna todos los ties del player
    public float getTies(Set<Score> scores){
        return scores
                .stream()
                .filter(score -> score.getScore() == (float)0.5)
                .count();
    }

    //5) Método -> Retorna todos los losses del player
    public float getLosses(Set<Score> scores){
        return scores
                .stream()
                .filter(score -> score.getScore() == 0)
                .count();
    }

    public void setScores(Set<Score> scores) {
        this.scores = scores;
    }

    @JsonIgnore
    public List<Game> getGames() {
        return gamePlayers.stream().map(sub -> sub.getGame()).collect(toList());
    }

    @JsonIgnore
    public List<Game> getGamess() {
        return gamePlayers.stream().map(sub -> sub.getGame()).collect(toList());
    }


    @Override
    public String toString() {
        return "Player{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}

