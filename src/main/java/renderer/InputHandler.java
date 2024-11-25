package renderer;

import loader.Object;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class InputHandler {
    public static void key_callback(Vector3f cameraPos, int key, int action, List<Object> objects) {
        if (action == GLFW.GLFW_PRESS) {
            // Seleção de objetos pelo teclado (teclas 1, 2, 3, ...)
            for (int i = 0; i < objects.size(); i++) {
                if (key == GLFW.GLFW_KEY_1 + i) { // Seleciona o objeto correspondente à tecla pressionada
                    for (int j = 0; j < objects.size(); j++) {
                        objects.get(j).setSelected(j == i); // Apenas um objeto é selecionado
                    }
                    System.out.println("Objeto " + (i + 1) + " selecionado.");
                }
            }
        }
    }

    public static void moveCamera(Vector3f cameraPos, int key, int action) {
        if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT) {
            // Controle de movimentação da câmera
            if (key == GLFW.GLFW_KEY_W) cameraPos.add(0.0f, 0.0f, -0.1f); // Para frente
            if (key == GLFW.GLFW_KEY_S) cameraPos.add(0.0f, 0.0f, 0.1f);  // Para trás
            if (key == GLFW.GLFW_KEY_A) cameraPos.add(-0.1f, 0.0f, 0.0f); // Para esquerda
            if (key == GLFW.GLFW_KEY_D) cameraPos.add(0.1f, 0.0f, 0.0f);  // Para direita
            if (key == GLFW.GLFW_KEY_Q) cameraPos.add(0.0f, 0.1f, 0.0f);  // Para cima
            if (key == GLFW.GLFW_KEY_E) cameraPos.add(0.0f, -0.1f, 0.0f); // Para baixo
        }
    }

    public static void moveSelectedObject(List<Object> objects, int key, int action) {
        if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT) {
            Object selectedObj = objects.stream().filter(Object::isSelected).findFirst().orElse(null);

            if (selectedObj != null) {
                Vector3f movement = new Vector3f(0.0f);
                if (key == GLFW.GLFW_KEY_W) movement.add(0.0f, 0.1f, 0.0f); // Para cima
                if (key == GLFW.GLFW_KEY_S) movement.add(0.0f, -0.1f, 0.0f); // Para baixo
                if (key == GLFW.GLFW_KEY_A) movement.add(-0.1f, 0.0f, 0.0f); // Para esquerda
                if (key == GLFW.GLFW_KEY_D) movement.add(0.1f, 0.0f, 0.0f); // Para direita
                if (key == GLFW.GLFW_KEY_Q) movement.add(0.0f, 0.0f, 0.1f); // Para frente
                if (key == GLFW.GLFW_KEY_E) movement.add(0.0f, 0.0f, -0.1f); // Para trás

                selectedObj.translate(movement);
            }
        }
    }

    public static void rotateSelectedObject(List<Object> objects, int key, int action) {
        if (action == GLFW.GLFW_PRESS) {
            Object selectedObj = objects.stream().filter(Object::isSelected).findFirst().orElse(null);

            if (selectedObj != null) {
                if (key == GLFW.GLFW_KEY_R) {
                    selectedObj.rotate((float) Math.toRadians(15), new Vector3f(0, 1, 0)); // Rotação no eixo Y
                }
                if (key == GLFW.GLFW_KEY_T) {
                    selectedObj.rotate((float) Math.toRadians(15), new Vector3f(1, 0, 0)); // Rotação no eixo X
                }
                if (key == GLFW.GLFW_KEY_Y) {
                    selectedObj.rotate((float) Math.toRadians(15), new Vector3f(0, 0, 1)); // Rotação no eixo Z
                }
            }
        }
    }

    public static void scaleSelectedObject(List<Object> objects, int key, int action) {
        if (action == GLFW.GLFW_PRESS) {
            Object selectedObj = objects.stream().filter(Object::isSelected).findFirst().orElse(null);

            if (selectedObj != null) {
                if (key == GLFW.GLFW_KEY_EQUAL) {
                    selectedObj.scale(new Vector3f(1.1f, 1.1f, 1.1f)); // Aumenta 10% da escala
                }
                if (key == GLFW.GLFW_KEY_MINUS) {
                    selectedObj.scale(new Vector3f(0.9f, 0.9f, 0.9f)); // Reduz 10% da escala
                }
            }
        }
    }
}
