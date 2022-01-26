package doctor.utility;

import java.util.regex.Pattern;

import com.jfoenix.validation.base.ValidatorBase;

import javafx.beans.DefaultProperty;
import javafx.scene.control.TextInputControl;

@DefaultProperty(value = "icon")
public class RegexValidator extends ValidatorBase {

    private String regexPattern;

    public RegexValidator(String message) {
        super(message);
    }

    public RegexValidator() {

    }

    private Pattern regexPatternCompiled;

    @Override
    protected void eval() {
        if (srcControl.get() instanceof TextInputControl) {
            evalTextInputField();
        }
    }

    private void evalTextInputField() {
        TextInputControl textField = (TextInputControl) srcControl.get();
        String text = (textField.getText() == null) ? "" : textField.getText(); // Treat null like empty string

        if (regexPatternCompiled.matcher(text).matches()) {
            hasErrors.set(false);
        } else {
            hasErrors.set(true);
        }
    }

    public void setRegexPattern(String regexPattern) {
        this.regexPattern = regexPattern;
        this.regexPatternCompiled = Pattern.compile(regexPattern);
    }

    public String getRegexPattern() {
        return regexPattern;
    }
}
