import lombok.Getter;

import java.util.Date;


public class Player {
    String username;
    String firstName;
    String lastName;
    Date birthDay;
    final GameLibrary library;
    @Getter
    private float wallet;

    public Player(String username, String lastName, String firstName, Date birthDay) {
        this.birthDay = birthDay;
        this.lastName = lastName;
        this.firstName = firstName;
        this.username = username;
        library = new GameLibrary(this);
        wallet = 0f;
    }

    /**
     * Add amount to the player wallet
     * @param amount the amount to add
     * @return the now wallet value
     * @throws IllegalArgumentException if the amount is negative
     */
    public float rechargeWallet(float amount) throws IllegalArgumentException{
        if (amount <= 0) throw new IllegalArgumentException("Can't add negative or null amont");
        wallet += amount;
        return wallet;
    }
}
