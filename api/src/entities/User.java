package entities;

public class User {
    public int id;
    public  String username;
    public  String password;
    public int role_id;


    public User(int id, String username, String password, int role_id){
        this.id=id;
        this.username=username;
        this.password=password;
        this.role_id=role_id;
    }
}
