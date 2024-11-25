package loader;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = Vector3fAdapterDeserializer.class)
public class Vector3fAdapter {
    public float x;
    public float y;
    public float z;

    public Vector3fAdapter() {}

    public Vector3fAdapter(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public org.joml.Vector3f toVector3f() {
        return new org.joml.Vector3f(x, y, z);
    }
}
