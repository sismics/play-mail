package helpers.mail.freemarker;

import freemarker.cache.TemplateLoader;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import play.Play;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * Load freemarker templates from Play assets.
 *
 * @author jtremeaux
 */
public class PlayTemplateLoader implements TemplateLoader {
    public static String getFileAsText(File file) {
        try {
            return FileUtils.readFileToString(file, Charsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object findTemplateSource(String s) throws IOException {
        return Play.getFile(s);
    }

    @Override
    public long getLastModified(Object o) {
        return ((File) o).lastModified();
    }

    @Override
    public Reader getReader(Object o, String s) throws IOException {
        return new StringReader(getFileAsText((File) o));
    }

    @Override
    public void closeTemplateSource(Object o) throws IOException {
        // NOP
    }
}
