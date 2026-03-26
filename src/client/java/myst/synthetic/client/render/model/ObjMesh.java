package myst.synthetic.client.render.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.ToIntFunction;
import net.minecraft.client.renderer.texture.OverlayTexture;

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

    public float[] getNormal(int index) {
        return normals.get(index);
    }

    public float[] getFaceNormal(Face face) {
        FaceVertex[] verts = face.vertices();

        float[] p0 = positions.get(verts[0].vertexIndex());
        float[] p1 = positions.get(verts[1].vertexIndex());
        float[] p2 = positions.get(verts[2].vertexIndex());

        float ax = p1[0] - p0[0];
        float ay = p1[1] - p0[1];
        float az = p1[2] - p0[2];

        float bx = p2[0] - p0[0];
        float by = p2[1] - p0[1];
        float bz = p2[2] - p0[2];

        float nx = ay * bz - az * by;
        float ny = az * bx - ax * bz;
        float nz = ax * by - ay * bx;

        float length = (float)Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (length == 0.0F) {
            return new float[] {0.0F, 1.0F, 0.0F};
        }

        return new float[] {nx / length, ny / length, nz / length};
    }

    public void emit(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            ToIntFunction<Face> lightResolver
    ) {
        for (Face face : faces) {
            int packedLight = lightResolver.applyAsInt(face);
            FaceVertex[] verts = face.vertices();
            float[] faceNormal = getFaceNormal(face);

            // Triangle 1: 0, 1, 2
            emitVertex(pose, consumer, verts[0], packedLight, faceNormal);
            emitVertex(pose, consumer, verts[1], packedLight, faceNormal);
            emitVertex(pose, consumer, verts[2], packedLight, faceNormal);

            // Triangle 2: 0, 2, 3
            emitVertex(pose, consumer, verts[0], packedLight, faceNormal);
            emitVertex(pose, consumer, verts[2], packedLight, faceNormal);
            emitVertex(pose, consumer, verts[3], packedLight, faceNormal);
        }
    }

    private void emitVertex(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            FaceVertex fv,
            int packedLight,
            float[] faceNormal
    ) {
        float[] pos = positions.get(fv.vertexIndex());
        float[] uv = uvs.get(fv.uvIndex());

        float u = uv[0];
        float v = uv[1];

        consumer.addVertex(pose, pos[0], pos[1], pos[2])
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(pose, faceNormal[0], faceNormal[1], faceNormal[2]);
    }
}