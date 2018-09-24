package model;

public class Field {
    private int value; //   0 = "nicht belegt"    1 = "Kreis"   2 = "Kreuz"


    public Field(){
        value = 0;
    }

    public int getValue() {
        return value;
    }

    public void setCross() {
        this.value = 2;
    }

    public void setCircle() {
        this.value = 1;
    }

    public boolean isEmpty(){
        if(value == 0){
            return true;
        }
        return false;
    }
}
