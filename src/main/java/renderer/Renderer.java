package renderer;

import loader.*;
import loader.Object;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static renderer.InputHandler.*;

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

        int[] nVertices1 = new int[1];
        int VAO1 = OBJLoader.loadSimpleOBJ(loaderPath + scene.objects.get(0).modelPath, nVertices1);

        int[] nVertices2 = new int[1];
        int VAO2 = OBJLoader.loadSimpleOBJ(loaderPath + scene.objects.get(1).modelPath, nVertices2);

        if (VAO1 == -1 || VAO2 == -1) {
            System.out.println("Falha ao carregar o modelo .obj");
            return;
        }

        // Cria os objetos 3D
        Object obj1 = new Object(VAO1, nVertices1[0], new Vector3f(-1.0f, 0.0f, 0.0f));
        Object obj2 = new Object(VAO2, nVertices2[0], new Vector3f(0.0f, 0.0f, 0.0f));

        List<Object> objects = Arrays.asList(obj1, obj2);

        // Adiciona callback para controle de teclado
        GLFW.glfwSetKeyCallback(loadWindow.getWindowHandle(), (window, key, scancode, action, mods) -> {
            key_callback(cameraPos, key, action, objects);
            moveCamera(cameraPos, key, action);
            // Atualiza seleção de objetos com teclas 1, 2, ...
            if (action == GLFW.GLFW_PRESS) {
                for (int i = 0; i < objects.size(); i++) {
                    if (key == GLFW.GLFW_KEY_1 + i) { // Teclas 1, 2, ...
                        for (Object obj : objects) {
                            obj.setSelected(false); // Desseleciona todos
                        }
                        objects.get(i).setSelected(true); // Seleciona o objeto correspondente
                        System.out.println("Objeto " + (i + 1) + " selecionado.");
                        break;
                    }
                }
            }

            // Aplicar movimentos, rotações e escala no objeto selecionado
            moveSelectedObject(objects, key, action);
            rotateSelectedObject(objects, key, action);
            scaleSelectedObject(objects, key, action);
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
