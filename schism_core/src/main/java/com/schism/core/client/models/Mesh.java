package com.schism.core.client.models;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import com.schism.core.Log;

import java.util.List;

public class Mesh implements IHasWeight
{
    protected final String name;
    protected final Index[] indices;
    protected final Vertex[] vertices;
    protected final UV[] uvs;
    protected final Normal[] normals;

    public Mesh(String name, List<Index> indices, List<Vertex> vertices, List<UV> uvs, List<Normal> normals)
    {
        this.name = name;
        this.indices = indices.toArray(new Index[0]);
        this.vertices = vertices.toArray(new Vertex[0]);
        this.uvs = uvs.toArray(new UV[0]);
        this.normals = normals.toArray(new Normal[0]);
    }

    /**
     * Gets the name of this mesh.
     * @return The mesh name.
     */
    public String name()
    {
        return this.name;
    }

    /**
     * Renders this mesh.
     * @param vertexConsumer The vertex consumer to render with.
     * @param poseMatrix A nullable pose matrix.
     * @param normalMatrix A nullable normal matrix.
     */
    public void render(VertexConsumer vertexConsumer,  Matrix4f poseMatrix, Matrix3f normalMatrix)
    {
        for (Index index : this.indices) {
            try {
                Vertex vertex = this.vertices[index.vertex()];
                UV uv = this.uvs[index.uv()];
                Normal normal = this.normals[index.normal()];

                if (poseMatrix != null) {
                    vertexConsumer.vertex(poseMatrix, vertex.x(), vertex.y(), vertex.z());
                } else {
                    vertexConsumer.vertex(vertex.x(), vertex.y(), vertex.z());
                }
                vertexConsumer.color(255, 255, 255, 255);
                vertexConsumer.uv(uv.u(), uv.v());
                vertexConsumer.overlayCoords(0, 10);
                vertexConsumer.uv2(1);
                if (normalMatrix != null) {
                    vertexConsumer.normal(normalMatrix, normal.x(), normal.y(), normal.z());
                } else{
                    vertexConsumer.normal(normal.x(), normal.y(), normal.z());
                }
                vertexConsumer.endVertex();
            } catch (Exception e) {
                Log.error("model", "Error rendering index: " + index + " Available: vertices=" + this.vertices.length + " uvs=" + this.uvs.length + " normals=" + this.normals.length);
            }
        }
    }
}
