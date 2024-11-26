package loader;

import java.util.List;

public class Scene {
    public List<SceneObject> objects;
    public List<Light> lights;
    public Camera camera;

    public static class SceneObject {
        public String modelPath;
        public Vector3fAdapter position;
        public Vector3fAdapter scale;
        public Vector3fAdapter rotation;
        public Animation animation;
    }

    public static class Light {
        public Vector3fAdapter position;
        public Vector3fAdapter color;
    }

    public static class Camera {
        public Vector3fAdapter position;
        public float fov;
        public float near;
        public float far;
    }

    public static class Animation {
        public String type;
        public List<Vector3fAdapter> points;
    }
}
