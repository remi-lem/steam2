public class Test {
    public static void main(String[] args) {
        Game jeu1 = new Game("2048","Jules Couture",4.99);
        Game jeu2 = new Game("Solitaire", GameType.SIMULATION,"Jules Couture",9.99);

        System.out.println(jeu1);
        System.out.println(jeu2);
    }
}
