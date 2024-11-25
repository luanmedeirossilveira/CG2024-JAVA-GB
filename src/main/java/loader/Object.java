package loader;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;

public class Object {
    public int VAO;
    public int nVertices;
    public Vector3f position;
    public Matrix4f modelMatrix;
    public Material material;
    public Animation animation;
    private boolean selected = false;

    public Object(int VAO, int nVertices, Vector3f position) {
        this.VAO = VAO;
        this.nVertices = nVertices;
        this.position = position;
        this.modelMatrix = new Matrix4f();
        this.material = new Material(); // Valores padrão do material
    }

    // Getters e setters
    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public Animation getAnimation() {
        return animation;
    }

    public void setAnimation(Animation animation) {
        this.animation = animation;
    }

    // Métodos de transformação
    public void translate(Vector3f translation) {
        this.modelMatrix.translate(translation);
    }

    public void rotate(float angle, Vector3f axis) {
        this.modelMatrix.rotate(angle, axis);
    }

    public void scale(Vector3f scaleFactor) {
        this.modelMatrix.scale(scaleFactor);
    }

    // Atualiza a matriz de transformação com base nas propriedades atuais
    public void updateModelMatrix() {
        this.modelMatrix.identity();
        this.modelMatrix.translate(position);
    }

    public boolean hasAnimation() {
        return animation != null && animation.points != null && !animation.points.isEmpty();
    }

    // Material do objeto
    public static class Material {
        public float ka = 0.1f; // Coeficiente ambiental
        public float kd = 0.7f; // Coeficiente difuso
        public float ks = 0.5f; // Coeficiente especular

        public Material() {}

        public Material(float ka, float kd, float ks) {
            this.ka = ka;
            this.kd = kd;
            this.ks = ks;
        }
    }

    // Dados de animação (trajetória)
    public static class Animation {
        public String type;
        public List<Vector3fAdapter> points;

        public Animation(String type, List<Vector3fAdapter> points) {
            this.type = type;
            this.points = points;
        }
    }
}
