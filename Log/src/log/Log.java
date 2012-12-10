package log;


import java.util.ArrayList;


public class Log {

    private ArrayList<ObjectLog> objects;
    private String folder;

    public Log(String folder) {
        objects = new ArrayList<ObjectLog>();
        this.folder = folder;
    }

    public ObjectLog createNewObject(String keyName) {
        ObjectLog obj = null;
        try {
            obj = new ObjectLog(keyName, getFileWithPath(keyName));
            objects.add(obj);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return obj;
    }

    public TimeObjectLog creaTimeObjectLog(String keyName) {
        TimeObjectLog obj = null;
        try {
            obj = new TimeObjectLog(keyName, getFileWithPath(keyName));
            objects.add(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }

    public void dumpAllObjects() {
        for(ObjectLog entry : objects) {
            try {
                entry.dumpToFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String getFileWithPath(String filename) {
        return folder+"/"+filename;
    }

}
