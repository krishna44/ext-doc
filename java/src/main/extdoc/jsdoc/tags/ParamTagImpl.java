package extdoc.jsdoc.tags;

/**
 * User: Andrey Zubkov
 * Date: 31.10.2008
 * Time: 1:04:07
 */
public class ParamTagImpl extends TagImpl{

    private String paramType;
    private String paramName;
    private String paramDescription;
    private boolean optional;

    public ParamTagImpl(String name, String text) {
        super(name, text);
        String[] str = divideAtWhite(text, 3);
        paramType = removeBrackets(str[0]);
        paramName = str[1];
        optional = isOptional(str[2]);
        paramDescription = optional?cutOptional(str[2]):str[2];
    }

    public String getParamType() {
        return paramType;
    }

    public String getParamName() {
        return paramName;
    }

    public String getParamDescription() {
        return paramDescription;
    }

    public boolean isOptional() {
        return optional;
    }
}
