package templatepkg;

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerinalang.quoter.test.TemplateCode;

public class TemplateCodeImpl implements TemplateCode {
    @Override
    public Node getNode() {
        return %s;
    }
}
