module io.ballerina.shell.jupyter {
    requires io.ballerina.shell;
    requires jupyter.jvm.basekernel;
    requires java.logging;
    requires gson;

    exports io.ballerina.shell.jupyter;
}
