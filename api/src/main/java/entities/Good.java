package entities;

public class Good {

    public int id;
    public int group_id;
    public  String name;
    public  String description;
    public  String producer;
    public int quantity;
    public float price;

    public Good(int id, int group_id, String name, String description, String producer, int quantity, float price){
        this.id=id;
        this.group_id=group_id;
        this.name=name;
        this.description=description;
        this.producer=producer;
        this.quantity=quantity;
        this.price=price;
    }

}
