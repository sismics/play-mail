package play.libs.email;

import play.Play;

class AppMailSystemFactory extends AbstractMailSystemFactory {

    private static final MailSystem MOCK_MAIL_SYSTEM = new MockMailSystem();
    private static final MailSystem PRODUCTION_MAIL_SYSTEM  = new ProductionMailSystem();

    @Override
    public MailSystem currentMailSystem() {
        if (Play.useDefaultMockMailSystem()) {
            return MOCK_MAIL_SYSTEM;
        } else {
            return PRODUCTION_MAIL_SYSTEM;
        }
    }

}
