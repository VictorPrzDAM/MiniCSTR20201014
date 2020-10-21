package tfc.mini.cstr;

public class Seguimiento {
    private String dato;
    private String idImagen;
    private String imageURL;
    public Seguimiento() {
        //empty constructor needed
    }

    public Seguimiento(String dato) {
        this.dato = dato;
    }

    public String getIdImagen() {
        return idImagen;
    }

    public void setIdImagen(String idImagen) {
        this.idImagen = idImagen;
    }

    public Seguimiento(String dato, String imagen_uri ) {
        this.dato = dato;
        this.idImagen = imagen_uri;

    }

    public void setDato(String dato) {
        this.dato = dato;
    }

    public String getDato() {
        return  dato;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
}
