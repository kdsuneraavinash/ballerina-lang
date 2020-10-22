module io.ballerina.quoter {
    requires java.sql;
    requires gson;
    requires commons.cli;
    requires io.ballerina.parser;
    requires io.ballerina.tools.api;
    exports io.ballerinalang.quoter;
    exports io.ballerinalang.quoter.config;
}
