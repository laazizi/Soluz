package soluz.laazizi.com.soluzia;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by mo on 16/06/17.
 */

@IgnoreExtraProperties
public class User {

    public String client;
    public double temperature;
    public double humidite;
    public double lumiere;
    public String date;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String client, double temperature, double humidite, double lumiere,String date
    ) {
        this.client = client;
        this.temperature = temperature;
        this.humidite = humidite;
        this.lumiere = lumiere;
        this.date = date;
    }

}