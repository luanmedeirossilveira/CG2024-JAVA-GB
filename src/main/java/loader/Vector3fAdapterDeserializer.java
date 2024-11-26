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
