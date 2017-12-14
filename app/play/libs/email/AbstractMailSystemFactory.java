package play.libs.email;

public abstract class AbstractMailSystemFactory {

    public static final AbstractMailSystemFactory DEFAULT = new AppMailSystemFactory();

    public abstract MailSystem currentMailSystem();

}
