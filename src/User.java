/**
 * This User class only has the username field in this example.
 * You can add more attributes such as the user's shopping cart items.
 */
public class User {

    private final String email;

    public User(String email) {
        this.email = email;
    }
    public String getEmail(){
        return this.email;
    }

}