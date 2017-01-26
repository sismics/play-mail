package helpers.mail;

import java.io.InputStream;

/**
 * @author jtremeaux
 */
public class InputStreamAttachement {
    public InputStream is;

    public String fileName;

    public String mimeType;

    public String description;

    public InputStreamAttachement(InputStream is, String fileName, String mimeType, String description) {
        this.is = is;
        this.fileName = fileName;
        this.mimeType = mimeType;
        this.description = description;
    }
}
