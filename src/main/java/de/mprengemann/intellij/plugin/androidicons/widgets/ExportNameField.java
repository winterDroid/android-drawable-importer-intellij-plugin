package de.mprengemann.intellij.plugin.androidicons.widgets;

import com.intellij.openapi.ui.Messages;
import de.mprengemann.intellij.plugin.androidicons.util.TextUtils;

import javax.swing.*;
import javax.swing.text.DefaultFormatter;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ExportNameField extends JFormattedTextField {

    public ExportNameField() {
        super(new RegexFormatter("[a-z0-9_\\.]+"));
        setInputVerifier(new InputVerifier() {

            public boolean verify(JComponent input) {
                return true;
            }

            public boolean shouldYieldFocus(JComponent input) {
                if (input instanceof JFormattedTextField) {
                    final JFormattedTextField ftf = (JFormattedTextField) input;
                    final JFormattedTextField.AbstractFormatter formatter = ftf.getFormatter();
                    if (formatter != null && !TextUtils.isEmpty(ftf.getText())) {
                        try {
                            formatter.stringToValue(ftf.getText());
                        } catch (final ParseException e) {
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    Messages.showErrorDialog("Please select a valid name for the drawable. Just \"[a-z0-9_.]\" is allowed.",
                                                             "No Valid Name");
                                }
                            });
                        }
                    }
                }
                return true;
            }
        });
    }

    private static class RegexFormatter extends DefaultFormatter {
        private Pattern pattern;

        public RegexFormatter(String pattern) throws PatternSyntaxException {
            super();
            setPattern(Pattern.compile(pattern));
            setOverwriteMode(false);
            setCommitsOnValidEdit(true);
            setAllowsInvalid(false);
        }

        public void setPattern(Pattern pattern) {
            this.pattern = pattern;
        }

        public Pattern getPattern() {
            return pattern;
        }


        public Object stringToValue(String text) throws ParseException {
            Pattern pattern = getPattern();

            if (pattern != null && !TextUtils.isEmpty(text)) {
                Matcher matcher = pattern.matcher(text);
                if (matcher.matches()) {
                    return super.stringToValue(text);
                }
                throw new ParseException("Pattern did not match", 0);
            }
            return text;
        }
    }
}
