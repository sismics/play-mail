package play.libs.email;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.Email;
import play.Logger;
import play.utils.ImmediateFuture;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Future;

/**
 * @author jtremeaux
 */
public class MockMailSystem implements MailSystem {

    /**
     * Email address -> List of emails
     */
    static Map<String, LinkedList<String>> emailsMap = new HashMap<>();

    @Override
    public Future<Boolean> sendMessage(Email email) {
        try {
            final StringBuilder content = new StringBuilder();
            Properties props = new Properties();
            props.put("mail.smtp.host", "myfakesmtpserver.com");

            Session session = Session.getInstance(props);
            email.setMailSession(session);

            email.buildMimeMessage();

            MimeMessage msg = email.getMimeMessage();
            msg.saveChanges();

            String body = getContent(msg);

            content.append("From Mock Mailer\n\tNew email received by");


            content.append("\n\tFrom: ").append(email.getFromAddress().getAddress());
            content.append("\n\tReplyTo: ").append(email.getReplyToAddresses().get(0).getAddress());

            addAddresses(content, "To",  email.getToAddresses());
            addAddresses(content, "Cc",  email.getCcAddresses());
            addAddresses(content, "Bcc", email.getBccAddresses());

            content.append("\n\tSubject: ").append(email.getSubject());
            content.append("\n\t").append(body);

            content.append("\n");
            Logger.info(content.toString());

            for (Object add : email.getToAddresses()) {
                content.append(", ").append(add);
                addEmail(((InternetAddress) add).getAddress(), content.toString());
            }

        } catch (Exception e) {
            Logger.error(e, "error sending mock email");
        }
        return new ImmediateFuture();
    }

    private void addEmail(String address, String email) {
        emailsMap
                .computeIfAbsent(address, k -> new LinkedList<>())
                .push(email);
    }


    private static String getContent(Part message) throws MessagingException,
            IOException {

        if (message.getContent() instanceof String) {
            return message.getContentType() + ": " + message.getContent() + " \n\t";
        } else if (message.getContent() != null && message.getContent() instanceof Multipart) {
            Multipart part = (Multipart) message.getContent();
            String text = "";
            for (int i = 0; i < part.getCount(); i++) {
                BodyPart bodyPart = part.getBodyPart(i);
                if (!Message.ATTACHMENT.equals(bodyPart.getDisposition())) {
                    text += getContent(bodyPart);
                } else {
                    text += "attachment: \n" +
                   "\t\t name: " + (StringUtils.isEmpty(bodyPart.getFileName()) ? "none" : bodyPart.getFileName()) + "\n" +
                   "\t\t disposition: " + bodyPart.getDisposition() + "\n" +
                   "\t\t description: " +  (StringUtils.isEmpty(bodyPart.getDescription()) ? "none" : bodyPart.getDescription())  + "\n\t";
                }
            }
            return text;
        }
        if (message.getContent() != null && message.getContent() instanceof Part) {
            if (!Message.ATTACHMENT.equals(message.getDisposition())) {
                return getContent((Part) message.getContent());
            } else {
                return "attachment: \n" +
                       "\t\t name: " + (StringUtils.isEmpty(message.getFileName()) ? "none" : message.getFileName()) + "\n" +
                       "\t\t disposition: " + message.getDisposition() + "\n" +
                       "\t\t description: " + (StringUtils.isEmpty(message.getDescription()) ? "none" : message.getDescription()) + "\n\t";
            }
        }

        return "";
    }


    private static void addAddresses(final StringBuilder content,
            String header, List<?> ccAddresses) {
        if (ccAddresses != null && !ccAddresses.isEmpty()) {
            content.append("\n\t").append(header).append(": ");
            for (Object add : ccAddresses) {
                content.append(add).append(", ");
            }
            removeTheLastComma(content);
        }
    }

    private static void removeTheLastComma(final StringBuilder content) {
        content.delete(content.length() - 2, content.length());
    }


    public String popMessageReceivedBy(String address) {
        LinkedList<String> emailList = emailsMap.get(address);
        if (emailList == null) {
            return null;
        }
        String email = emailList.pop();
        if (emailList.isEmpty()) {
            emailsMap.put(address, null);
        }
        return email;
    }

    public void reset(){
        emailsMap.clear();
    }
}
