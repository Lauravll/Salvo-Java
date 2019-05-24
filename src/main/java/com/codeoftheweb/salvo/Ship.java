package com.codeoftheweb.salvo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

@Entity
public class Ship {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;
    private String type;

    @ManyToOne( fetch = FetchType.EAGER)
    @JoinColumn(name="gamePlayer_id")
    private GamePlayer gameplayer;

    @ElementCollection
    //Estoy agregando una columna, es mejor el mismo nombre para luego no estar diferenciando.
    @Column(name="locations")
    private List<String> locations = new ArrayList<>();

    public Ship(){}

    public Ship(String type, GamePlayer gameplayer, List<String> locations) {
        this.type = type;
        this.gameplayer = gameplayer;
        this.locations = locations;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @JsonIgnore
    public GamePlayer getGameplayer() {
        return gameplayer;
    }

    public void setGameplayer(GamePlayer gameplayer) {
        this.gameplayer = gameplayer;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getLocations() {
        return locations;
    }

    public void setLocations(List<String> locations) {
        this.locations = locations;
    }

    @Override
    public String toString() {
        return "Ship{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", gameplayer=" + gameplayer +
                ", locations=" + locations +
                '}';
    }
}
