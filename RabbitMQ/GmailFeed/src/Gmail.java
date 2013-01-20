import com.googlecode.gmail4j.GmailClient;
import com.googlecode.gmail4j.GmailConnection;
import com.googlecode.gmail4j.GmailMessage;
import com.googlecode.gmail4j.http.HttpGmailConnection;
import com.googlecode.gmail4j.rss.RssGmailClient;
import com.googlecode.gmail4j.util.LoginDialog;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.List;

public class Gmail {

    private static final String TASK_QUEUE_NAME = "task_queue";

    public static void main(String[] args) throws IOException {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);
        GmailClient client = new RssGmailClient();
        GmailConnection gmail_connection = new HttpGmailConnection(LoginDialog.getInstance().show("Enter Gmail Login"));
        client.setConnection(gmail_connection);

        List<GmailMessage> messages = client.getUnreadMessages();
        for (GmailMessage message : messages) {
            channel.basicPublish("", TASK_QUEUE_NAME, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getSubject().getBytes());
            System.out.println(" [x] Sent '" + message.getSubject() + "'");
        }

        channel.close();
        connection.close();
    }
}
