package templatepkg;

import io.ballerinalang.quoter.test.TemplateCode;
import io.ballerina.compiler.syntax.tree.*;

public class TemplateCodeImpl implements TemplateCode {
    @Override
    public Node getNode() {
        return %s;
    }
}
