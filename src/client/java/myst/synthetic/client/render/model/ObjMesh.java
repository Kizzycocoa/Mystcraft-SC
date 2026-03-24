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

    private void emitVertex(FaceVertex fv, PoseStack.Pose pose, VertexConsumer consumer, int packedLight) {
        float[] pos = positions.get(fv.vertexIndex());
        float[] uv = uvs.get(fv.uvIndex());
        float[] normal = normals.get(fv.normalIndex());

        float x = pos[0] / 16.0F;
        float y = pos[1] / 16.0F;
        float z = pos[2] / 16.0F;

        float u = uv[0];
        float v = 1.0F - uv[1];

        consumer.addVertex(pose, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(0)
                .setLight(packedLight)
                .setNormal(pose, normal[0], normal[1], normal[2]);
    }

    public void emit(PoseStack.Pose pose, VertexConsumer consumer, int packedLight) {
        for (Face face : faces) {
            FaceVertex[] fv = face.vertices();

            emitVertex(fv[0], pose, consumer, packedLight);
            emitVertex(fv[1], pose, consumer, packedLight);
            emitVertex(fv[2], pose, consumer, packedLight);

            emitVertex(fv[0], pose, consumer, packedLight);
            emitVertex(fv[2], pose, consumer, packedLight);
            emitVertex(fv[3], pose, consumer, packedLight);
        }
    }
}