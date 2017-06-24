package soluz.laazizi.com.soluzia;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManagerService;
import java.io.IOException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import soluz.laazizi.com.soluz.R;

public class MainActivity extends Activity implements ImainLoop {
    public static final byte COMMAND_MA = 'A';
    public static final byte COMMAND_MB = 'B';
    private static final String I2C_DEVICE_NAME = "I2C1";
    // I2C Slave Address
    private static final int I2C_ADDRESS = 8;
    private static final String TAG = "test variable pour log";
    private I2cDevice mDevice;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private Servo mServo;

    private static final String LEDok = "BCM6";
    private Gpio mLedGpiook;
    private static final String LEDerror = "BCM5";
    private Gpio mLedGpioerror;
    private PeripheralManagerService manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Soluz");

        try {
            manager = new PeripheralManagerService(); //init manager pour i2c/pwm

            mServo = new Servo("PWM0");
            mServo.setAngleRange(0, 180);
            mServo.setEnabled(true);
            // test pour pwn si dispo
            List<String> portList = manager.getPwmList();
            if (portList.isEmpty()) {
                Log.i("list*********", "No PWM port available on this device.");
            } else {
                Log.i("listmmmmmmmmmmmmmm", "List of available ports: " + portList);
            }
            // test pour I2C si existe
            List<String> deviceList = manager.getI2cBusList();
            if (deviceList.isEmpty()) {
                Log.i(TAG, "No I2C bus available on this device.");
            } else {
                Log.i(TAG, "List of available devices: " + deviceList);
            }

            List<String> portListgpio = manager.getGpioList();
            if (portList.isEmpty()) {
                Log.i(TAG, "No GPIO port available on this device.");
            } else {
                Log.i(TAG, "List of available ports: " + portListgpio);
            }

            mDevice = manager.openI2cDevice(I2C_DEVICE_NAME, I2C_ADDRESS);


        } catch (IOException e) {
            Log.w(TAG, "Unable to access I2C device", e);
        }
        try {
            mLedGpiook = manager.openGpio(LEDok);
            mLedGpiook.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            mLedGpioerror = manager.openGpio(LEDerror);
            mLedGpioerror.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);



        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }
        DatabaseReference refservo = database.getReference("Soluz");
        refservo.child("users").child("mohamed").child("commande").child("power").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                if (dataSnapshot.exists()) {
                    String a = dataSnapshot.getValue().toString();
                    try {

                        mServo.setAngle(Double.parseDouble(dataSnapshot.getValue().toString()));

                        Log.i("je suis dans 1", String.valueOf(Double.parseDouble(dataSnapshot.getValue().toString())));
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.i("je suis dans 1 erreur", e.toString());
                    }
                } else {

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mainLoop loop = new mainLoop(getApplicationContext());
        //callback dans public String call(std_msgs.String message)
        loop.register(this);
    }


    public void sendTOArduino(I2cDevice device, int address, byte motor, int value) throws IOException {
        byte[] buffer = new byte[2];
        buffer[0] = motor;
        buffer[1] = (byte) value;
        device.writeRegBuffer(address, buffer, buffer.length);
    }

    public void getFromArduino(I2cDevice device, int address) throws IOException {
        // Read one register from slave
        byte[] b = new byte[7]; // 7 byte from arduino i2c voir le fichier ino
        device.readRegBuffer(address, b, b.length);
        char who = (char) (b[0] & 0xff); // pour test send char to arduino pour control arduino

        double valtemp = b[1] << 8 | b[2] & 0xff;
        double valhum = b[3] << 8 | b[4] & 0xff;
        double vallum = b[5] << 8 | b[6] & 0xff;

        Log.i(TAG, "il y a temperature =  " + valtemp / 100);
        Log.i(TAG, "il y a humidity =  " + valhum / 100);
        Log.i(TAG, "il y a : lumiere = " + vallum);
        Log.i(TAG, "il y a : time = " + getCurrentTime());
        if (getCurrentTime().length() == 14) { //check if date = taille de yyyymmddhhmmss
            writeNewUser("mohamed", valtemp / 100, valhum / 100, vallum, getCurrentTime());
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mServo != null) {
            try {
                mServo.close();
                mServo = null;
            } catch (IOException e) {
                Log.w(TAG, "Unable to close I2C device", e);
            }
        }
        if (mDevice != null) {
            try {
                mDevice.close();
                mDevice = null;
            } catch (IOException e) {
                Log.w(TAG, "Unable to close I2C device", e);
            }
        }
    }

    public void writeBuffer(I2cDevice device, byte[] buffer) {
        try {
            int count = 0;
            device.write(buffer, buffer.length);

            Log.d(TAG, "Wrote " + count + " bytes over I2C.");
        } catch (IOException e) {
            Log.w(TAG, "Unable to close I2C device", e);
        }

    }
    public static String getCurrentTime() {
        //date output format
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmms");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
        Calendar cal = Calendar.getInstance();
        return dateFormat.format(cal.getTime());
    }//

    private void writeNewUser(String client, double temperature, double humidite, double lumiere, String date
    ) {
        User user = new User(client, temperature, humidite, lumiere, date);

        myRef.child("users").child(client).child("historique").child("list").child(date.toUpperCase()).setValue(user);
    }
    @Override // loop pour la com ave arduino and firebase
    public void loop() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int i = 0;
                    int call = 0;
                    while (true) {
                        sendTOArduino(mDevice, I2C_ADDRESS, COMMAND_MA, 11);
                        Thread.sleep(5000);

                        getFromArduino(mDevice, I2C_ADDRESS);
                        mLedGpiook.setValue(true);
                        mLedGpioerror.setValue(false);

                        Thread.sleep(5000);
                        mLedGpiook.setValue(false);
                        i++;
                    }
                } catch (IOException e) {
                    Log.w(TAG, "Unable to access I2C device", e);
                    try {
                        mLedGpioerror.setValue(true);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
