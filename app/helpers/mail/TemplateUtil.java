package helpers.mail;

import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.StringTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import helpers.mail.freemarker.PlayTemplateLoader;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Map;

/**
 * Template utilities.
 *
 * @author bgamard
 * @author jtremeaux
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
        MultiTemplateLoader templateLoader = getTemplateLoader();
        configuration.setTemplateLoader(templateLoader);
        configuration.setLocalizedLookup(false);
    }

    private static MultiTemplateLoader getTemplateLoader() {
        PlayTemplateLoader playTemplateLoader = new PlayTemplateLoader();
        StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
        return new MultiTemplateLoader(new TemplateLoader[] { stringTemplateLoader, playTemplateLoader });
    }

    /**
     * Render a Freemarker template.
     *
     * @param templateName Template .ftl file name
     * @param params Parameters
     * @return Rendered template
     */
    public static String renderTemplate(String templateName, Map<String, Object> params, Locale locale) {
        try {
            Template template = configuration.getTemplate(templateName);
            StringWriter sb = new StringWriter();
            template.setLocale(locale);
            template.process(params, sb);
            return sb.toString();
        } catch (IOException | TemplateException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Render a Freemarker template.
     *
     * @param templateStr Template content
     * @param params Parameters
     * @return Rendered template
     */
    public static String renderText(String templateStr, Map<String, Object> params, Locale locale) {
        String templateName = "template_" + templateStr.hashCode();
        StringTemplateLoader stringTemplateLoader = (StringTemplateLoader) (((MultiTemplateLoader) configuration.getTemplateLoader()).getTemplateLoader(0));
        stringTemplateLoader.putTemplate(templateName, templateStr);
        stringTemplateLoader.putTemplate(templateName + "_en", templateStr); // Quickfix just in case

        try {
            Template template = configuration.getTemplate(templateName);
            StringWriter sb = new StringWriter();
            template.setLocale(locale);
            template.process(params, sb);
            return sb.toString();
        } catch (IOException | TemplateException e) {
            throw new RuntimeException(e);
        }
    }
}
