package io.ballerina.shell.jupyter.jupyter.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * A single kernel spec data entry containing a resource directory
 * and the spec data.
 *
 * @since 2.0.0
 */
public class KernelSpecEntry {
    @SerializedName("resource_dir")
    @Expose
    private String resourceDir;
    @SerializedName("spec")
    @Expose
    private KernelSpec kernelSpec;

    public String getResourceDir() {
        return resourceDir;
    }

    public void setResourceDir(String resourceDir) {
        this.resourceDir = resourceDir;
    }

    public KernelSpec getKernelSpec() {
        return kernelSpec;
    }

    public void setKernelSpec(KernelSpec kernelSpec) {
        this.kernelSpec = kernelSpec;
    }
}
