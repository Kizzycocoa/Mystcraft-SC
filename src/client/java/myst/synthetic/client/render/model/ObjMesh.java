package myst.synthetic.client.render.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.ToIntFunction;
import net.minecraft.client.renderer.texture.OverlayTexture;

import java.util.List;

public final class ObjMesh {

    public record FaceVertex(int vertexIndex, int uvIndex, int normalIndex) {
    }

    public record Face(String objectName, FaceVertex[] vertices) {
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

    public void emit(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            ToIntFunction<Face> lightResolver
    ) {
        for (Face face : faces) {
            int packedLight = lightResolver.applyAsInt(face);

            for (FaceVertex fv : face.vertices()) {
                float[] pos = positions.get(fv.vertexIndex());
                float[] uv = uvs.get(fv.uvIndex());

                float u = uv[0];
                float v = uv[1];

                consumer.addVertex(pose, pos[0], pos[1], pos[2])
                        .setColor(255, 255, 255, 255)
                        .setUv(u, v)
                        .setOverlay(OverlayTexture.NO_OVERLAY)
                        .setLight(packedLight)
                        .setNormal(pose, 0.0F, 1.0F, 0.0F);
            }
        }
    }
}