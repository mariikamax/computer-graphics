package com.cgvsu.render_engine;

public class RenderSettings {
    private boolean drawWireframe = false;
    private boolean useTexture = false;
    private boolean useLighting = false;
    private boolean fillPolygons = true;
    private boolean useZBufferForWireframe = false;

    public RenderSettings() {}

    public boolean isDrawWireframe() { return drawWireframe; }
    public void setDrawWireframe(boolean drawWireframe) {
        this.drawWireframe = drawWireframe;
    }

    public boolean isUseTexture() { return useTexture; }
    public void setUseTexture(boolean useTexture) {
        this.useTexture = useTexture;
    }

    public boolean isUseLighting() { return useLighting; }
    public void setUseLighting(boolean useLighting) {
        this.useLighting = useLighting;
    }

    public boolean isFillPolygons() { return fillPolygons; }
    public void setFillPolygons(boolean fillPolygons) {
        this.fillPolygons = fillPolygons;
    }

    public boolean isUseZBufferForWireframe() { return useZBufferForWireframe; }
    public void setUseZBufferForWireframe(boolean useZBufferForWireframe) {
        this.useZBufferForWireframe = useZBufferForWireframe;
    }
}