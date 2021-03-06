package io.ballerina.shell.jupyter.jupyter.json;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * The json serialization class for spec map returned by
 * the jupyter kernelspec list.
 *
 * @since 2.0.0
 */
public class KernelSpecJson {
    @SerializedName("kernelspecs")
    @Expose
    private Map<String, KernelSpecEntry> kernelSpecDataMap;

    public Map<String, KernelSpecEntry> getKernelSpecDataMap() {
        return kernelSpecDataMap;
    }

    public void setKernelSpecDataMap(Map<String, KernelSpecEntry> kernelspecs) {
        this.kernelSpecDataMap = kernelspecs;
    }
}
