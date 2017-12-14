package play.libs.email;

import org.apache.commons.mail.Email;
import play.libs.AppMail;

import java.util.concurrent.Future;

class ProductionMailSystem implements MailSystem {

    @Override
    public Future<Boolean> sendMessage(Email email) {
        email.setMailSession(AppMail.getSession());
        return AppMail.sendMessage(email);
    }

}
