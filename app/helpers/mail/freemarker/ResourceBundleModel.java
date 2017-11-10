package helpers.mail.freemarker;

import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import play.i18n.Messages;

import java.util.*;

/**
 * Overload {@link freemarker.ext.beans.ResourceBundleModel}
 * to allow uniform processing of single quotes.
 *
 * @author bgamard
 */
public class ResourceBundleModel extends freemarker.ext.beans.ResourceBundleModel {

    /**
     * Constructor.
     *
     * @param bundle
     * @param wrapper
     */
    public ResourceBundleModel(ResourceBundle bundle, BeansWrapper wrapper) {
        super(bundle, wrapper);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Object exec(List arguments) throws TemplateModelException {
        // Must have at least one argument - the key
        if (arguments.size() < 1)
            throw new TemplateModelException("No message key was specified");
        // Read it
        Iterator it = arguments.iterator();
        String key = unwrap((TemplateModel) it.next()).toString();
        try {
            // Copy remaining arguments into an Object[]
            int args = arguments.size() - 1;
            Object[] params = new Object[args];
            for (int i = 0; i < args; ++i)
                params[i] = unwrap((TemplateModel) it.next());

            // Invoke format
            return new StringModel(format(key, params), wrapper);
        } catch (MissingResourceException e) {
            throw new TemplateModelException("No such key: " + key);
        } catch (Exception e) {
            throw new TemplateModelException(e.getMessage());
        }
    }

    public String format(String key, Object[] params) throws MissingResourceException {
        Locale locale = getBundle().getLocale();
        String message = ((ResourceBundle) object).getString(key);
        return Messages.formatString(locale, message, params);
    }
}
