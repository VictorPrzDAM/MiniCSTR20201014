package tfc.mini.cstr;

public class Seguimiento {
    private String dato;
    public Seguimiento() {
        //empty constructor needed
    }
    public Seguimiento(String dato) {
        this.dato = dato;
    }

    public void setDato(String dato) {
        this.dato = dato;
    }

    public String getDato() {
        return  dato;
    }
}
