package log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TimeObjectLog extends ObjectLog {

    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public TimeObjectLog(String keyName, String folder) throws Exception {
        super(keyName, folder);
    }

    private void log(String s) {
        Calendar cal = Calendar.getInstance();
        super.log("["+dateFormat.format(cal.getTime())+"] "+s);
    }

}
