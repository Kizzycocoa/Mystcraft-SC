package myst.synthetic.client.render.model;

import net.minecraft.resources.Identifier;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public final class ObjMeshLoader {

    private ObjMeshLoader() {
    }

    public static ObjMesh load(Identifier id) {
        try {
            var stream = ObjMeshLoader.class.getResourceAsStream(
                    "/assets/" + id.getNamespace() + "/" + id.getPath()
            );

            if (stream == null) {
                throw new RuntimeException("OBJ not found: " + id);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

            List<float[]> positions = new ArrayList<>();
            List<float[]> uvs = new ArrayList<>();
            List<float[]> normals = new ArrayList<>();
            List<ObjMesh.Face> faces = new ArrayList<>();

            reader.lines().forEach(line -> {
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) {
                    return;
                }

                if (line.startsWith("v ")) {
                    String[] parts = line.split("\\s+");
                    positions.add(new float[] {
                            Float.parseFloat(parts[1]),
                            Float.parseFloat(parts[2]),
                            Float.parseFloat(parts[3])
                    });
                    return;
                }

                if (line.startsWith("vt ")) {
                    String[] parts = line.split("\\s+");
                    uvs.add(new float[] {
                            Float.parseFloat(parts[1]),
                            Float.parseFloat(parts[2])
                    });
                    return;
                }

                if (line.startsWith("vn ")) {
                    String[] parts = line.split("\\s+");
                    normals.add(new float[] {
                            Float.parseFloat(parts[1]),
                            Float.parseFloat(parts[2]),
                            Float.parseFloat(parts[3])
                    });
                    return;
                }

                if (line.startsWith("f ")) {
                    String[] parts = line.split("\\s+");

                    if (parts.length != 5) {
                        throw new RuntimeException("Only quad faces are supported right now: " + line);
                    }

                    ObjMesh.FaceVertex[] faceVertices = new ObjMesh.FaceVertex[4];

                    for (int i = 1; i < 5; i++) {
                        String[] refs = parts[i].split("/");

                        int vertexIndex = Integer.parseInt(refs[0]) - 1;
                        int uvIndex = Integer.parseInt(refs[1]) - 1;
                        int normalIndex = Integer.parseInt(refs[2]) - 1;

                        faceVertices[i - 1] = new ObjMesh.FaceVertex(vertexIndex, uvIndex, normalIndex);
                    }

                    faces.add(new ObjMesh.Face(faceVertices));
                }
            });

            return new ObjMesh(positions, uvs, normals, faces);

        } catch (Exception e) {
            throw new RuntimeException("Failed to load OBJ: " + id, e);
        }
    }
}