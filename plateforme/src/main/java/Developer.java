import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Developer implements Account {
    private static final Scanner SCANNER = new Scanner(System.in);
    ArrayList<GameType> TAGLIST = new ArrayList<>(Arrays.asList(GameType.values()));

    // Attributs des comptes utilisateurs
    private String name;
    private String email;
    private String password;
    private ArrayList<Game> gameList;

    public Developer(String name,String email,String password) {
        this.name=name;
        this.email=email;
        this.password=password;
    }

    public boolean uploadGame(){
        try{
            System.out.println("Quel est le nom du jeu que vous souhaitez ajouter ?");
            String gameName= SCANNER.nextLine();
            System.out.println("Quels sont les tags associés au jeu parmi les suivant ?");
            for (GameType gameType : TAGLIST) {
                System.out.println("\n- " + gameType.toString());
            }
            String gameTags= SCANNER.nextLine();

            // Parcour du résultat String pour remplacer les caractères en dehors de l'alphabet par des espaces
            StringBuilder cleanTagsStr = new StringBuilder();
            for(int i=0;i<gameTags.length();i++){
                if (gameTags.charAt(i)<'A' || gameTags.charAt(i)>'z' ) {
                    cleanTagsStr.append(' ');
                } else if (gameTags.charAt(i)<='Z' || gameTags.charAt(i)>='a') {
                    cleanTagsStr.append(Character.toUpperCase(gameTags.charAt(i)));
                }else  {
                    cleanTagsStr.append(' ');
                }
            }
            // Parcours des différentes parties du String grâce au .split(" ")
            ArrayList<GameType> gameTypes = new ArrayList<GameType>();
            String[] tags = cleanTagsStr.toString().split(" ");
            for(String tag:tags){
                // Parcours de la liste des tags pour voir s'il s'agit bien d'un tag possible
                for(GameType type : TAGLIST){
                    if(type.toString().equals(tag)){
                        // Ajout du tag
                        gameTypes.add(type);
                    }
                }
            }

            System.out.println("Quel est le prix du jeu ?");
            String gamePrice= SCANNER.nextLine();

            GameType[] convertGameTags = gameTypes.toArray(new GameType[gameTypes.size()]);
            new Game(gameName, convertGameTags,this.name,Double.parseDouble(gamePrice));
            return true;
        } catch (Exception e) {
            System.out.println("Error uploading game");
            System.out.println(e.getMessage());
            return false;
        }
    }
}
