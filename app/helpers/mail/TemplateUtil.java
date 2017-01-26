package helpers.mail;

import freemarker.cache.StringTemplateLoader;
import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Map;

/**
 * Template utilities.
 *
 * @author bgamard
 */
public class TemplateUtil {
    /**
     * Thread-safe Freemarker configuration.
     */
    private static Configuration configuration;
    static {
        configuration = new Configuration(Configuration.VERSION_2_3_23);
        configuration.setDefaultEncoding("UTF-8");
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setLogTemplateExceptions(false);
    }

    /**
     * Render a Freemarker template.
     *
     * @param templateStr Template content
     * @param params Parameters
     * @return Rendered template
     */
    public static String render(String templateStr, Map<String, Object> params, Locale locale) {
        StringTemplateLoader templateLoader = new StringTemplateLoader();
        templateLoader.putTemplate("template", templateStr);
        configuration.setTemplateLoader(templateLoader);

        try {
            Template template = configuration.getTemplate("template");
            StringWriter sb = new StringWriter();
            template.setLocale(locale);
            template.process(params, sb);
            return sb.toString();
        } catch (IOException | TemplateException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean validateTemplate(String templateStr) {
        try {
            StringTemplateLoader templateLoader = new StringTemplateLoader();
            templateLoader.putTemplate("template", templateStr);
            configuration.setTemplateLoader(templateLoader);
            configuration.getTemplate("template");
        } catch (ParseException e) {
            return false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }
}
