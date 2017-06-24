package soluz.laazizi.com.soluzia;

/**
 * Created by mo on 10/10/16.
 */
public class mySingleton {

    String pointxy = "";
    public int etat=0;
    public boolean loop=true;


    /**
     * Constructeur privé
     */
    private mySingleton() {
    }

    /**
     * Point d'accès pour l'instance unique du singleton
     */
    public static mySingleton getInstance() {
        return SingletonHolder.instance;
    }

    /**
     * Holder
     */
    private static class SingletonHolder {
        /**
         * Instance unique non préinitialisée
         */
        private final static mySingleton instance = new mySingleton();
    }
}