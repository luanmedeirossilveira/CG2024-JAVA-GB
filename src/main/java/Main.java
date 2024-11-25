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