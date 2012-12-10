package log;

import java.io.File;
import java.util.ArrayList;

public class ObjectLog {

    private String keyName;
    private FileLog file;

    private ArrayList<String> messages;

    public ObjectLog(String keyName, String filename) throws Exception {
        if(keyName.equals(""))
            throw new Exception("The key name is empty!");
        this.keyName = keyName;
        file = new FileLog(new File(filename));
        messages = new ArrayList<String>();
    }


    public ObjectLog(String keyName) throws Exception {
        if(keyName.equals(""))
            throw new Exception("The key name is empty!");
        this.keyName = keyName;
        messages = new ArrayList<String>();
        file = null;
    }

    public void logMessage(String s) {
        this.log("["+keyName+"] "+s);
    }

    public void logError(String s) {
        this.log("[" + keyName + "] " + s);
    }

    public void log(Object object) {
        this.log(object.toString());
    }

    public void log(Object object, String s) {
        this.log("[" + object.getClass().getName() + "] " + s);
    }

    public void dumpToFile() throws Exception {
        if(file == null)
            throw new Exception("Missing paramater file in constructor!");

        for(String msg : messages) {
            file.writeLine(msg);
        }
    }

    private void log(String s) {
        messages.add(s);
        System.out.println(s);
    }

}
