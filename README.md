Main.java
```java
import loader.Window;
import renderer.Renderer;

public class Main {
    public static void main(String[] args) {
        // Inicializa janela e shaders
        Window window = new Window(800, 600, "Visualizador 3D");
        window.init();

        // Carrega os objetos .obj
        Renderer renderer = new Renderer();
        renderer.init(window);
    }
}
```

Renderer.java
```java
package renderer;

import loader.*;
import loader.Object;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static renderer.InputHandler.key_callback;

public class Renderer {
    private static Vector3f cameraPos;
    private static Vector3f cameraUp;
    private static float angle;
    private static Vector3f objectPosition;

    // Pontos de controle da curva Bézier
    private final Vector3f p0 = new Vector3f(0, 0, 0);
    private final Vector3f p1 = new Vector3f(1, 2, 0);
    private final Vector3f p2 = new Vector3f(2, -1, 0);
    private final Vector3f p3 = new Vector3f(3, 0, 0);

    public Renderer() {
        cameraPos = new Vector3f(0.0f, 0.0f, 5.0f);
        cameraUp = new Vector3f(0.0f, 1.0f, 0.0f);
        objectPosition = new Vector3f(0.0f, 0.0f, 0.0f);
        angle = 0.0f;
    }

    public void init(Window loadWindow) {
        // Carrega os objetos .obj
        int[] nVertices1 = new int[1];
        Scene scene;
        String loaderPath = "C:\\Users\\Luanm\\Documents\\demo\\CG2024-java-GB\\src\\main\\resources\\";

        try {
            scene = SceneLoader.load(loaderPath + "scenes\\scene.json");
        } catch (IOException e) {
            System.err.println("Erro ao carregar a configuração da cena: " + e.getMessage());
            return;
        }

        // Configura câmera
        cameraPos = scene.camera.position.toVector3f();
        new Matrix4f().perspective(
                (float) Math.toRadians(scene.camera.fov),
                800f / 600f,
                scene.camera.near,
                scene.camera.far
        );

        int VAO1 = OBJLoader.loadSimpleOBJ(loaderPath + scene.objects.get(0).modelPath, nVertices1);

        int[] nVertices2 = new int[1];
        int VAO2 = OBJLoader.loadSimpleOBJ(loaderPath + scene.objects.get(1).modelPath, nVertices2);

        if (VAO1 == -1 || VAO2 == -1) {
            System.out.println("Falha ao carregar o modelo .obj");
            return;
        }

        // Cria os objetos 3D
        Object obj1 = new Object(VAO1, nVertices1[0], new Vector3f(-1.0f, 0.0f, 0.0f));
        Object obj2 = new Object(VAO2, nVertices2[0], new Vector3f(0.0f, 0.0f, 0.0f)); // Objeto animado

        List<Object> objects = Arrays.asList(obj1, obj2);

        GLFW.glfwSetKeyCallback(loadWindow.getWindowHandle(), (window, key, scancode, action, mods) -> {
            key_callback(cameraPos, key, action, objects);
        });

        Matrix4f projectionMatrix = new Matrix4f().perspective((float) Math.toRadians(45), 800f / 600f, 0.1f, 100f);

        Vector3f lightPos = new Vector3f(2.0f, 2.0f, 2.0f);
        Vector3f lightColor = new Vector3f(1.0f, 1.0f, 1.0f);

        float ka = 0.1f;
        float kd = 0.7f;
        float ks = 0.5f;
        float q = 32.0f;

        GL11.glDisable(GL11.GL_CULL_FACE);

        ShaderUtils shader = null;
        while (!GLFW.glfwWindowShouldClose(loadWindow.getWindowHandle())) {
            GLFW.glfwPollEvents();
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

            shader = new ShaderUtils(loaderPath + "shaders\\vertex_phong.glsl",
                    loaderPath + "shaders\\fragment_phong.glsl");
            shader.use();

            Matrix4f viewMatrix = new Matrix4f().lookAt(cameraPos, new Vector3f(0, 0, 0), cameraUp);

            shader.setUniform("view", viewMatrix);
            shader.setUniform("projection", projectionMatrix);
            shader.setUniform("lightPos", lightPos);
            shader.setUniform("lightColor", lightColor);
            shader.setUniform("cameraPos", cameraPos);
            shader.setUniform("ka", ka);
            shader.setUniform("kd", kd);
            shader.setUniform("ks", ks);
            shader.setUniform("q", q);

            // Atualiza a posição do objeto 2 com base na curva Bézier
            float t = (System.currentTimeMillis() % 5000) / 5000.0f; // Tempo normalizado entre 0 e 1
            for (Object obj : objects) {
                if (obj.hasAnimation()) {
                    Vector3f newPosition = bezier(
                            t,
                            obj.getAnimation().points.get(0).toVector3f(),
                            obj.getAnimation().points.get(1).toVector3f(),
                            obj.getAnimation().points.get(2).toVector3f(),
                            obj.getAnimation().points.get(3).toVector3f()
                    );
                    obj.translate(newPosition);
                }
                shader.setUniform("model", obj.modelMatrix);
                shader.setUniform("ka", obj.material.ka);
                shader.setUniform("kd", obj.material.kd);
                shader.setUniform("ks", obj.material.ks);

                GL30.glBindVertexArray(obj.VAO);
                GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, obj.nVertices);
            }
            Vector3f newPosition = bezier(t, p0, p1, p2, p3);
            obj2.modelMatrix.identity().translate(newPosition).scale(0.8f); // Atualiza a matriz de transformação

            // Renderiza os objetos
            render(objects, shader, angle, objectPosition, viewMatrix);

            GL30.glBindVertexArray(0);
            GLFW.glfwSwapBuffers(loadWindow.getWindowHandle());
        }

        assert shader != null;
        shader.cleanup();
        loadWindow.cleanup();
    }

    private Vector3f bezier(float t, Vector3f p0, Vector3f p1, Vector3f p2, Vector3f p3) {
        float u = 1 - t;
        float tt = t * t;
        float uu = u * u;
        float uuu = uu * u;
        float ttt = tt * t;

        Vector3f point = new Vector3f();
        point.set(p0).mul(uuu)
                .add(new Vector3f(p1).mul(3 * uu * t))
                .add(new Vector3f(p2).mul(3 * u * tt))
                .add(new Vector3f(p3).mul(ttt));
        return point;
    }

    public static void render(List<Object> objects, ShaderUtils shader, float angle, Vector3f objectPosition, Matrix4f viewMatrix) {
        for (Object obj : objects) {
            shader.setUniform("model", obj.modelMatrix);
            GL30.glBindVertexArray(obj.VAO);
            GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, obj.nVertices);
        }
    }

}
```

OBJLoader.java
```java
package loader;

import java.io.*;
import java.nio.FloatBuffer;
import java.util.*;
import org.joml.*;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

public class OBJLoader {
    public static int loadSimpleOBJ(String filePath, int[] nVertices) {
        List<Vector3f> vertices = new ArrayList<>();
        List<Vector2f> texCoords = new ArrayList<>();
        List<Vector3f> normals = new ArrayList<>();
        List<Float> vBuffer = new ArrayList<>();

        Vector3f color = new Vector3f(1.0f, 0.0f, 0.0f);

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split("\\s+");
                switch (tokens[0]) {
                    case "v":
                        vertices.add(new Vector3f(
                                Float.parseFloat(tokens[1]),
                                Float.parseFloat(tokens[2]),
                                Float.parseFloat(tokens[3])
                        ));
                        break;
                    case "vt":
                        texCoords.add(new Vector2f(
                                Float.parseFloat(tokens[1]),
                                Float.parseFloat(tokens[2])
                        ));
                        break;
                    case "vn":
                        normals.add(new Vector3f(
                                Float.parseFloat(tokens[1]),
                                Float.parseFloat(tokens[2]),
                                Float.parseFloat(tokens[3])
                        ));
                        break;
                    case "f":
                        for (int i = 1; i < tokens.length; i++) {
                            String[] parts = tokens[i].split("/");
                            int vi = Integer.parseInt(parts[0]) - 1;
                            int ti = Integer.parseInt(parts[1]) - 1;
                            int ni = Integer.parseInt(parts[2]) - 1;

                            // Posição
                            Vector3f vertex = vertices.get(vi);
                            vBuffer.add(vertex.x);
                            vBuffer.add(vertex.y);
                            vBuffer.add(vertex.z);

                            // Cor
                            vBuffer.add(color.x);
                            vBuffer.add(color.y);
                            vBuffer.add(color.z);

                            // Coordenada de textura
                            Vector2f texCoord = texCoords.get(ti);
                            vBuffer.add(texCoord.x);
                            vBuffer.add(texCoord.y);

                            // Normal
                            Vector3f normal = normals.get(ni);
                            vBuffer.add(normal.x);
                            vBuffer.add(normal.y);
                            vBuffer.add(normal.z);
                        }
                        break;
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar o arquivo OBJ: " + e.getMessage());
            return -1;
        }

        nVertices[0] = vBuffer.size() / 11;

        FloatBuffer buffer = MemoryUtil.memAllocFloat(vBuffer.size());
        for (Float f : vBuffer) {
            buffer.put(f);
        }
        buffer.flip();

        int VAO = GL30.glGenVertexArrays();
        int VBO = GL30.glGenBuffers();

        GL30.glBindVertexArray(VAO);

        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, VBO);
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, buffer, GL30.GL_STATIC_DRAW);

        GL30.glVertexAttribPointer(0, 3, GL30.GL_FLOAT, false, 11 * Float.BYTES, 0);
        GL30.glEnableVertexAttribArray(0);

        GL30.glVertexAttribPointer(1, 3, GL30.GL_FLOAT, false, 11 * Float.BYTES, 3 * Float.BYTES);
        GL30.glEnableVertexAttribArray(1);

        GL30.glVertexAttribPointer(2, 2, GL30.GL_FLOAT, false, 11 * Float.BYTES, 6 * Float.BYTES);
        GL30.glEnableVertexAttribArray(2);

        GL30.glVertexAttribPointer(3, 3, GL30.GL_FLOAT, false, 11 * Float.BYTES, 8 * Float.BYTES);
        GL30.glEnableVertexAttribArray(3);

        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);

        MemoryUtil.memFree(buffer);

        return VAO;
    }
}
```

InputHandler.java
```java
package renderer;

import loader.Object;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class InputHandler {
    public static void key_callback(Vector3f cameraPos, int key, int action, List<Object> objects) {
        if (action == org.lwjgl.glfw.GLFW.GLFW_PRESS) {
            for (int i = 0; i < objects.size(); i++) {
                if (key == GLFW.GLFW_KEY_1 + i) { // Teclas 1, 2, 3, ... para selecionar objetos
                    for (int j = 0; j < objects.size(); j++) {
                        objects.get(j).setSelected(j == i); // Apenas um objeto é selecionado
                    }
                    System.out.println("Objeto " + (i + 1) + " selecionado.");
                }
            }
        }

        // Chama o método de movimento
        moveSelectedObject(objects, key, action);

        // Chama o método de rotação
        rotateSelectedObject(objects, key, action);

        // Chama o método de escala
        scaleSelectedObject(objects, key, action);
    }

    public static void moveSelectedObject(List<Object> objects, int key, int action) {
        if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT) {
            // Encontre o objeto selecionado
            Object selectedObj = objects.stream().filter(Object::isSelected).findFirst().orElse(null);

            if (selectedObj != null) {
                // Crie um vetor de movimento
                Vector3f movement = new Vector3f(0.0f);

                // Movimentos básicos
                if (key == GLFW.GLFW_KEY_W) movement.add(0.0f, 0.1f, 0.0f); // Para cima
                if (key == GLFW.GLFW_KEY_S) movement.add(0.0f, -0.1f, 0.0f); // Para baixo
                if (key == GLFW.GLFW_KEY_A) movement.add(-0.1f, 0.0f, 0.0f); // Para a esquerda
                if (key == GLFW.GLFW_KEY_D) movement.add(0.1f, 0.0f, 0.0f); // Para a direita
                if (key == GLFW.GLFW_KEY_Q) movement.add(0.0f, 0.0f, 0.1f); // Para frente
                if (key == GLFW.GLFW_KEY_E) movement.add(0.0f, 0.0f, -0.1f); // Para trás

                // Atualize a posição do objeto com a movimentação
                selectedObj.translate(movement);
            }
        }
    }

    public static void rotateSelectedObject(List<Object> objects, int key, int action) {
        if (action == GLFW.GLFW_PRESS) {
            // Encontre o objeto selecionado
            Object selectedObj = objects.stream().filter(Object::isSelected).findFirst().orElse(null);

            if (selectedObj != null) {
                // Se pressionar a tecla "R", rota 15 graus no eixo Y
                if (key == GLFW.GLFW_KEY_R) {
                    selectedObj.rotate((float) Math.toRadians(15), new Vector3f(0, 1, 0)); // Rotação no eixo Y
                }
            }
        }
    }

    public static void scaleSelectedObject(List<Object> objects, int key, int action) {
        if (action == GLFW.GLFW_PRESS) {
            // Encontre o objeto selecionado
            Object selectedObj = objects.stream().filter(Object::isSelected).findFirst().orElse(null);

            if (selectedObj != null) {
                // Se pressionar a tecla "+", aumenta a escala
                if (key == GLFW.GLFW_KEY_EQUAL) {
                    selectedObj.scale(new Vector3f(1.1f, 1.1f, 1.1f)); // Aumenta 10% da escala
                }
                // Se pressionar a tecla "-", diminui a escala
                if (key == GLFW.GLFW_KEY_MINUS) {
                    selectedObj.scale(new Vector3f(0.9f, 0.9f, 0.9f)); // Reduz 10% da escala
                }
            }
        }
    }


}
```

ShaderUtils.java
```java
package renderer;

import org.lwjgl.opengl.GL20;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.FloatBuffer;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

public class ShaderUtils {
    private final Integer programId;

    public ShaderUtils(String vertexPath, String fragmentPath) {
        programId = GL20.glCreateProgram();
        int vertexShaderId = loadShader(vertexPath, GL20.GL_VERTEX_SHADER);
        int fragmentShaderId = loadShader(fragmentPath, GL20.GL_FRAGMENT_SHADER);

        GL20.glAttachShader(programId, vertexShaderId);
        GL20.glAttachShader(programId, fragmentShaderId);

        GL20.glLinkProgram(programId);

        if (GL20.glGetProgrami(programId, GL20.GL_LINK_STATUS) == GL20.GL_FALSE) {
            throw new RuntimeException("Erro ao iniciar shader: " + GL20.glGetProgramInfoLog(programId));
        }

        GL20.glDetachShader(programId, vertexShaderId);
        GL20.glDetachShader(programId, fragmentShaderId);
        GL20.glDeleteShader(vertexShaderId);
        GL20.glDeleteShader(fragmentShaderId);
    }

    private int loadShader(String path, int type) {
        StringBuilder source = new StringBuilder();
        try {
            Files.lines(Paths.get(path)).forEach(line -> source.append(line).append("\n"));
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível ler o arquivo do shader: " + path, e);
        }

        int shaderId = GL20.glCreateShader(type);
        GL20.glShaderSource(shaderId, source);
        GL20.glCompileShader(shaderId);

        if (GL20.glGetShaderi(shaderId, GL20.GL_COMPILE_STATUS) == GL20.GL_FALSE) {
            throw new RuntimeException("Erro ao compilar o shader: " + path + "\n" + GL20.glGetShaderInfoLog(shaderId));
        }

        return shaderId;
    }

    public void use() {
        GL20.glUseProgram(programId);
    }

    public void setUniform(String name, Matrix4f value) {
        // Define uma matriz 4x4 como variável uniforme
        int location = GL20.glGetUniformLocation(programId, name);
        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        value.get(buffer);
        GL20.glUniformMatrix4fv(location, false, buffer);
    }

    public void setUniform(String name, float value) {
        int location = GL20.glGetUniformLocation(programId, name);
        GL20.glUniform1f(location, value);
    }

    public void setUniform(String name, Vector3f value) {
        int location = GL20.glGetUniformLocation(programId, name);
        GL20.glUniform3f(location, value.x, value.y, value.z);
    }

    public void cleanup() {
        // Exclui o programa shader ao final
        GL20.glDeleteProgram(programId);
    }
}
```

Object.java
```java
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
```

Scene.java
```java
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

```

SceneLoader.java
```java
package loader;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

public class SceneLoader {
    public static Scene load(String path) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File(path), Scene.class);
    }
}
```

Window.java
```java
package loader;


import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.glfw.GLFWVidMode;

public class Window {
    private long window;
    private final int width;
    private final int height;
    private final String title;

    public Window(int width, int height, String title) {
        this.width = width;
        this.height = height;
        this.title = title;
    }

    public void init() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Não foi possível inicializar o GLFW");
        }

        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GL11.GL_TRUE);

        window = GLFW.glfwCreateWindow(width, height, title, 0, 0);
        if (window == 0) {
            throw new RuntimeException("Erro ao criar a janela GLFW");
        }

        GLFWVidMode videoMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
        if (videoMode != null) {
            int monitorLargura = videoMode.width();
            int monitorAltura = videoMode.height();
            GLFW.glfwSetWindowPos(window,
                    (monitorLargura - width) / 2,
                    (monitorAltura - height) / 2);
        }

        GLFW.glfwMakeContextCurrent(window);
        GL.createCapabilities();

        GLFW.glfwSwapInterval(1);

        GL11.glViewport(0, 0, width, height);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    public void cleanup() {
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
    }

    public long getWindowHandle() {
        return window;
    }
}
```

Vector3fAdapter.java
```java
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
```

Vector3fAdapterDeserializer.java
```java
package loader;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class Vector3fAdapterDeserializer extends JsonDeserializer<Vector3fAdapter> {
    @Override
    public Vector3fAdapter deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        // Lê o array JSON [x, y, z]
        float[] coords = p.readValueAs(float[].class);
        if (coords.length != 3) {
            throw new IOException("Esperado um array de 3 valores para Vector3f, mas recebido: " + coords.length);
        }
        return new Vector3fAdapter(coords[0], coords[1], coords[2]);
    }
}
```

scenes.json
```json
{
  "objects": [
    {
      "modelPath": "models\\Suzannes\\Suzanne.obj",
      "position": [0.0, 0.0, 0.0],
      "scale": [1.0, 1.0, 1.0],
      "rotation": [0.0, 0.0, 0.0],
      "animation": {
        "type": "bezier",
        "points": [
          [0.0, 0.0, 0.0],
          [1.0, 2.0, 0.0],
          [2.0, -1.0, 0.0],
          [3.0, 0.0, 0.0]
        ]
      }
    },
    {
      "modelPath": "models\\Navezinha\\Nave.obj",
      "position": [0.0, 0.0, 0.0],
      "scale": [1.0, 1.0, 1.0],
      "rotation": [0.0, 0.0, 0.0],
      "animation": {
        "type": "bezier",
        "points": [
          [0.0, 0.0, 0.0],
          [1.0, 2.0, 0.0],
          [2.0, -1.0, 0.0],
          [3.0, 0.0, 0.0]
        ]
      }
    }
  ],
  "lights": [
    {
      "position": [2.0, 2.0, 2.0],
      "color": [1.0, 1.0, 1.0]
    }
  ],
  "camera": {
    "position": [0.0, 0.0, 5.0],
    "fov": 45,
    "near": 0.1,
    "far": 100.0
  }
}
```

vertex_phong.glsl
```glsl
#version 330 core
layout(location = 0) in vec3 position;
layout(location = 1) in vec3 normal;

out vec3 FragPos;
out vec3 Normal;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

void main() {
    FragPos = vec3(model * vec4(position, 1.0)); // Posição do fragmento no mundo
    Normal = mat3(transpose(inverse(model))) * normal; // Normal transformada
    gl_Position = projection * view * vec4(FragPos, 1.0);
}
```

fragment_phong.glsl
```glsl
#version 330 core
in vec3 FragPos;
in vec3 Normal;

uniform vec3 lightPos;
uniform vec3 lightColor;
uniform vec3 cameraPos;
uniform float ka;
uniform float kd;
uniform float ks;
uniform float q;

out vec4 FragColor;

void main() {
    // Ambiente
    vec3 ambient = ka * lightColor;

    // Difusa
    vec3 norm = normalize(Normal);
    vec3 lightDir = normalize(lightPos - FragPos);
    float diff = max(dot(norm, lightDir), 0.0);
    vec3 diffuse = kd * diff * lightColor;

    // Especular
    vec3 viewDir = normalize(cameraPos - FragPos);
    vec3 reflectDir = reflect(-lightDir, norm);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), q);
    vec3 specular = ks * spec * lightColor;

    vec3 result = ambient + diffuse + specular;
    FragColor = vec4(result, 1.0);
}
```