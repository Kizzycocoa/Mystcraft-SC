package myst.synthetic.client.render.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import java.util.List;

public final class ObjMesh {

    public record FaceVertex(int vertexIndex, int uvIndex, int normalIndex) {
    }

    public record Face(FaceVertex[] vertices) {
    }

    private final List<float[]> positions;
    private final List<float[]> uvs;
    private final List<float[]> normals;
    private final List<Face> faces;

    public ObjMesh(
            List<float[]> positions,
            List<float[]> uvs,
            List<float[]> normals,
            List<Face> faces
    ) {
        this.positions = positions;
        this.uvs = uvs;
        this.normals = normals;
        this.faces = faces;
    }

    public void emit(PoseStack.Pose pose, VertexConsumer consumer, int packedLight) {
        for (Face face : faces) {
            for (FaceVertex fv : face.vertices()) {
                float[] pos = positions.get(fv.vertexIndex());
                float[] uv = uvs.get(fv.uvIndex());
                float[] normal = normals.get(fv.normalIndex());

                float u = uv[0];
                float v = 1.0F - uv[1];

                consumer.addVertex(pose, pos[0], pos[1], pos[2])
                        .setColor(255, 255, 255, 255)
                        .setUv(u, v)
                        .setOverlay(0)
                        .setLight(packedLight)
                        .setNormal(pose, normal[0], normal[1], normal[2]);
            }
        }
    }
}