package com.lycanitesmobs.client.obj;

import com.lycanitesmobs.LycanitesMobs;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.*;
import org.apache.commons.io.output.ByteArrayOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class ObjModel {
	public String filename;
	public List<ObjPart> objParts = new ArrayList<>();

    public ObjModel(ResourceLocation resourceLocation) {
		this.filename = resourceLocation.getPath();
        String path = resourceLocation.toString();
        try {
			InputStream inputStream = Minecraft.getInstance().getResourceManager().getResource(resourceLocation).getInputStream();
            String content = new String(read(inputStream), "UTF-8");
            String startPath = path.substring(0, path.lastIndexOf('/') + 1);
            HashMap<ObjPart, IndexedModel> map = new OBJLoader().loadModel(startPath, content);
			this.objParts.clear();
            Set<ObjPart> keys = map.keySet();
            Iterator<ObjPart> it = keys.iterator();
            while(it.hasNext()) {
                ObjPart objPart = it.next();
                Mesh mesh = new Mesh();
                objPart.mesh = mesh;
                this.objParts.add(objPart);
                map.get(objPart).toMesh(mesh);
            }
        }
        catch(Exception e) {
			LycanitesMobs.logWarning("", "Unable to load model: " + resourceLocation);
            e.toString();
        }
    }

	protected byte[] read(InputStream resource) throws IOException {
		int i;
		byte[] buffer = new byte[65565];
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		while((i = resource.read(buffer, 0, buffer.length)) != -1)
		{
			out.write(buffer,0,i);
		}
		out.flush();
		out.close();
		return out.toByteArray();
	}

	public Vector3f getNormal(Vector3f p1, Vector3f p2, Vector3f p3) {
		Vector3f output = new Vector3f();

		// Calculate Edges:
		Vector3f calU = new Vector3f(p2.x() - p1.x(), p2.y() - p1.y(), p2.z() - p1.z());
		Vector3f calV = new Vector3f(p3.x() - p1.x(), p3.y() - p1.y(), p3.z() - p1.z());

		// Cross Edges
		output.set(
				calU.y() * calV.z() - calU.z() * calV.y(),
				calU.z() * calV.x() - calU.x() * calV.z(),
				calU.x() * calV.y() - calU.y() * calV.x()
		);

		output.normalize(); // normalize()
		return output;
	}

    public void renderAll(IVertexBuilder vertexBuilder, Matrix3f matrix3f, Matrix4f matrix4f, int brightness, int fade, Vector4f color, Vector2f textureOffset) {
        Collections.sort(this.objParts, (a, b) -> {
			Vector3d v = Minecraft.getInstance().getCameraEntity().position();
			double aDist = v.distanceTo(new Vector3d(a.center.x(), a.center.y(), a.center.z()));
			double bDist = v.distanceTo(new Vector3d(b.center.x(), b.center.y(), b.center.z()));
			return Double.compare(aDist, bDist);
		});
        for(ObjPart objPart : this.objParts) {
            this.renderPart(vertexBuilder, matrix3f, matrix4f, brightness, fade, objPart, color, textureOffset);
        }
    }

    public void renderPartGroup(IVertexBuilder vertexBuilder, Matrix3f matrix3f, Matrix4f matrix4f, int brightness, int fade, Vector4f color, Vector2f textureOffset, String group) {
        for(ObjPart objPart : this.objParts) {
            if(objPart.getName().equals(group)) {
                renderPart(vertexBuilder, matrix3f, matrix4f, brightness, fade, objPart, color, textureOffset);
            }
        }
    }

    public void renderPart(IVertexBuilder vertexBuilder, Matrix3f matrix3f, Matrix4f matrix4f, int brightness, int fade, ObjPart objPart, Vector4f color, Vector2f textureOffset) {
		// Mesh data:
		if(objPart.mesh == null) {
			return;
		}
		int[] indices = objPart.mesh.indices;
		Vertex[] vertices = objPart.mesh.vertices;
		if(objPart.mesh.normals == null) {
			objPart.mesh.normals = new Vector3f[indices.length];
		}

    	// Invalid Builder:
    	if(vertexBuilder == null) {
    		return;
		}

    	// Build:
		for(int i = 0; i < indices.length; i += 3) {

			// Normal:
			Vector3f normal = objPart.mesh.normals[i];
			if(normal == null) {
				normal = this.getNormal(vertices[indices[i]].getPos(), vertices[indices[i + 1]].getPos(), vertices[indices[i + 2]].getPos());
				objPart.mesh.normals[i] = normal;
			}

			for(int iv = 0; iv < 3; iv++) {
				Vertex v = objPart.mesh.vertices[indices[i + iv]];
				vertexBuilder
						.vertex(matrix4f, v.getPos().x(), v.getPos().y(), v.getPos().z())
						.color(color.x(), color.y(), color.z(), color.w())
						.uv(v.getTexCoords().x + (textureOffset.x * 0.01f), 1f - (v.getTexCoords().y + (textureOffset.y * 0.01f)))
						.overlayCoords(0, 10 - fade)
						.uv2(brightness)
						.normal(matrix3f, normal.x(), normal.y(), normal.z())
						.endVertex();
			}
		}
    }
}
