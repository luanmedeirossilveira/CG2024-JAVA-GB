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
