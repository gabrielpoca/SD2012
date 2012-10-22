
package stockexchange;


class MiddleMan extends Thread {

    Database database;
    private boolean run;

    public MiddleMan(Database database) {
        this.database = database;
        run = true;
    }

    public void run() {
        while (run) {
            
        }
    }

    public void end() {
        run = false;
    }
}