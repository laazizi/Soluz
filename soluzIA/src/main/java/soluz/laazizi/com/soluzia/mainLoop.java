package soluz.laazizi.com.soluzia;

import android.content.Context;

/**
 * Created by mo on 11/12/16.
 */

public class mainLoop implements ImainLoop {
    private   Context _c;
    private ImainLoop _ImainLoop;
    private mySingleton mydata = mySingleton.getInstance();
    public mainLoop(Context c) {
        _c=c;
    }
    public void register(ImainLoop callback) {

        _ImainLoop=callback;
        this.start();
    }

    private void start() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mydata.loop){

                    try {

                        Thread.sleep(5000);
                        _ImainLoop.loop();

                    } catch (Exception e) {
                        System.out.println("loop **************" + e);
                    }
                }


            }

        }).start();

    }

    @Override
    public void loop() {
        System.out.println("fin loop **************");


    }
}