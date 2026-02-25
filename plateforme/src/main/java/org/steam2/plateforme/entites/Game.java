package org.steam2.plateforme.entites;

import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/*
@author : Jules
 */
@Table(name = "jeux")
@Getter
@Setter
public class Game {

    private static int numberOfGame;

    private int id;
    private String name;
    private GameType[] types;
    private String developer;
    private double price;

    public Game (String name, String developer, double price) {
        this.id = ++numberOfGame;
        this.name = name;
        this.types= new GameType[]{};
        this.developer = developer;
        this.price = price;
    }
    public Game (String name, GameType[] types, String developer, double price) {
        this.id = ++numberOfGame;
        this.name = name;
        this.types = types;
        this.developer = developer;
        this.price = price;
    }

    public Game(String solitaire, GameType gameType, String developer, double price) {
    }

    @Override
    public String toString() {
        int longestLine = 0;
        int currentLength;

        String []listElement = {"","Id","Type","Dev","Price"};
        String []listVariable = {name,String.valueOf(id),this.types.toString(),developer,String.valueOf(price)};

        for(int i=0;i<listElement.length;i++){
            currentLength = (listElement[i]+" "+listVariable[i]).length();
            if(currentLength>longestLine){
                longestLine = currentLength;
            }
        }

        StringBuilder output = new StringBuilder();
        for(int c = 0; c < longestLine; c++){
            output.append("*");
        }

        StringBuilder temp;
        for(int i=0;i<listElement.length;i++){
            temp = new StringBuilder("\n|" + listElement[i]);

            while(temp.length()<longestLine-listVariable[i].length()){
                temp.append(" ");
            }

            temp.append(listVariable[i]);
            temp.append("|");
            output.append(temp);
        }
        output.append("\n");
        for(int c = 0; c < longestLine; c++){
            output.append("*");
        }
        return output.toString();
    }
}