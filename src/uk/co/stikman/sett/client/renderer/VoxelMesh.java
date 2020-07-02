package uk.co.stikman.sett.client.renderer;

import uk.co.stikman.sett.VoxelModel;
import uk.co.stikman.sett.VoxelPalette;
import uk.co.stikman.sett.gfx.PolyMesh;
import uk.co.stikman.utils.math.Vector3;
import uk.co.stikman.utils.math.Vector4;

public class VoxelMesh {
	private PolyMesh				mesh;
	private VoxelModel				voxmodel;
	private VoxelPalette			palette;

	private static final Vector3[]	NORMALS	= new Vector3[] { new Vector3(0, -1, 0), new Vector3(0, 1, 0), new Vector3(1, 0, 0), new Vector3(-1, 0, 0), new Vector3(0, 0, 1), new Vector3(0, 0, -1) };

	public VoxelMesh(GameView gameView, VoxelModel voxmodel) {

		//
		// generate mesh for this, we need to work out which faces are visible
		//
		this.voxmodel = voxmodel;
		this.palette = gameView.getGame().getVoxelPalette();
		

		mesh = gameView.getWindow().createPolyMesh();
		for (int z = 0; z < voxmodel.getSizeZ(); ++z) {
			for (int y = 0; y < voxmodel.getSizeY(); ++y) {
				for (int x = 0; x < voxmodel.getSizeX(); ++x) {
					int vox = voxmodel.get(x, y, z);
					if (vox == -1)
						continue;
					Vector4 colour = palette.get(vox);
					outputFace(voxmodel, 0, x, y, z, 0, -1, 0, colour);
					outputFace(voxmodel, 1, x, y, z, 0, 1, 0, colour);
					outputFace(voxmodel, 2, x, y, z, 1, 0, 0, colour);
					outputFace(voxmodel, 3, x, y, z, -1, 0, 0, colour);
					outputFace(voxmodel, 4, x, y, z, 0, 0, 1, colour);
					outputFace(voxmodel, 5, x, y, z, 0, 0, -1, colour);
				}
			}
		}

		float ox = voxmodel.getSizeX() / 2.0f;
		float oy = voxmodel.getSizeY() / 2.0f;
		mesh.offsetVerts(-ox, -oy, 0);
		mesh.scaleVerts(0.05f);
	}

	private void outputFace(VoxelModel model, int face, int x, int y, int z, int dx, int dy, int dz, Vector4 colour) {
		int vox = model.get(x + dx, y + dy, z + dz);
		if (vox != -1) // face is occluded by another voxel 
			return;

		int n0, n1, n2, n3;

		switch (face) {
		case 0:
			n0 = mesh.addVert(x + 0, y + 0, z + 0, 0, 0, NORMALS[face], colour);
			n1 = mesh.addVert(x + 0, y + 0, z + 1, 1, 0, NORMALS[face], colour);
			n2 = mesh.addVert(x + 1, y + 0, z + 1, 1, 1, NORMALS[face], colour);
			n3 = mesh.addVert(x + 1, y + 0, z + 0, 0, 1, NORMALS[face], colour);
			break;
		case 1:
			n0 = mesh.addVert(x + 1, y + 1, z + 0, 0, 0, NORMALS[face], colour);
			n1 = mesh.addVert(x + 1, y + 1, z + 1, 1, 0, NORMALS[face], colour);
			n2 = mesh.addVert(x + 0, y + 1, z + 1, 1, 1, NORMALS[face], colour);
			n3 = mesh.addVert(x + 0, y + 1, z + 0, 0, 1, NORMALS[face], colour);
			break;
		case 2:
			n0 = mesh.addVert(x + 1, y + 0, z + 0, 0, 0, NORMALS[face], colour);
			n1 = mesh.addVert(x + 1, y + 0, z + 1, 1, 0, NORMALS[face], colour);
			n2 = mesh.addVert(x + 1, y + 1, z + 1, 1, 1, NORMALS[face], colour);
			n3 = mesh.addVert(x + 1, y + 1, z + 0, 0, 1, NORMALS[face], colour);
			break;
		case 3:
			n0 = mesh.addVert(x + 0, y + 1, z + 0, 0, 0, NORMALS[face], colour);
			n1 = mesh.addVert(x + 0, y + 1, z + 1, 1, 0, NORMALS[face], colour);
			n2 = mesh.addVert(x + 0, y + 0, z + 1, 1, 1, NORMALS[face], colour);
			n3 = mesh.addVert(x + 0, y + 0, z + 0, 0, 1, NORMALS[face], colour);
			break;
		case 4:
			n0 = mesh.addVert(x + 0, y + 0, z + 1, 0, 0, NORMALS[face], colour);
			n1 = mesh.addVert(x + 0, y + 1, z + 1, 1, 0, NORMALS[face], colour);
			n2 = mesh.addVert(x + 1, y + 1, z + 1, 1, 1, NORMALS[face], colour);
			n3 = mesh.addVert(x + 1, y + 0, z + 1, 0, 1, NORMALS[face], colour);
			break;
		case 5:
			n0 = mesh.addVert(x + 1, y + 0, z + 0, 0, 0, NORMALS[face], colour);
			n1 = mesh.addVert(x + 1, y + 1, z + 0, 1, 0, NORMALS[face], colour);
			n2 = mesh.addVert(x + 0, y + 1, z + 0, 1, 1, NORMALS[face], colour);
			n3 = mesh.addVert(x + 0, y + 0, z + 0, 0, 1, NORMALS[face], colour);
			break;
		default:
			return; // what
		}
		mesh.addTri(n0, n1, n2);
		mesh.addTri(n0, n2, n3);
	}

	public void render() {
		mesh.render(0);
	}
}
