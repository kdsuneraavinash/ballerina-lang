package io.ballerina.shell.jupyter.jupyter.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * The kernel spec containing the args for the jupyter server execution.
 *
 * @since 2.0.0
 */
public class KernelSpec {
    @SerializedName("argv")
    @Expose
    private List<String> argv;
    @SerializedName("display_name")
    @Expose
    private String displayName;
    @SerializedName("language")
    @Expose
    private String language;
    @SerializedName("interrupt_mode")
    @Expose
    private String interruptMode;

    public List<String> getArgv() {
        return argv;
    }

    public void setArgv(List<String> argv) {
        this.argv = argv;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getInterruptMode() {
        return interruptMode;
    }

    public void setInterruptMode(String interruptMode) {
        this.interruptMode = interruptMode;
    }
}
