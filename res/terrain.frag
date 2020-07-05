#version 460 core

uniform vec4 colour;
uniform sampler2D txt;
uniform sampler2D shadmap;
uniform vec3 globalLight;

in vec4 v_tfact;
in vec4 v_txt;
in vec4 v_colour;
in vec2 v_uv;
in vec2 v_smb;
in vec3 v_normal;
in vec3 viewDir;
in float v_isRoad;

out vec4 fragcolour;

vec2 offset(int idx) {
	vec2 v = vec2(idx % 4, idx / 4);
	return v / 4.0;
}

void main(void) {	
	//
	// divide the incoming UVs into one of 16
	// regions.  The offset() function should 
	// take an integer from 0..15 and return 
	// the offset to that region in the map
	//
	vec2 uv = v_uv / 4.0;

	//
	// The four texture regions involved at 
	// this vertex are encoded in vec4 t_txt.  The same
	// values are stored at all vertices, so this doesn't 
	// vary across the triangle
	//
	int ia = int(v_txt.x + 0.5);
	int ib = int(v_txt.y + 0.5);
	int ic = int(v_txt.z + 0.5);
	int id = int(v_txt.w + 0.5);
	
	//
	// Use those indices in the offset function to get the
	// texture sample at that point
	//		
	vec4 ca = texture2D(txt, uv + offset(ia)); 
	vec4 cb = texture2D(txt, uv + offset(ib)); 
	vec4 cc = texture2D(txt, uv + offset(ic)); 
	vec4 cd = texture2D(txt, uv + offset(id)); 
	vec4 croad = texture2D(txt, uv + offset(15)); // road texture is always slot 15
	
	//
	// Merge them with the four factors stored in vec4 v_tfact.
	// These vary for each vertex
	//
	fragcolour = (ca * v_tfact.x 
			   + cb * v_tfact.y 
			   + cc * v_tfact.z 
			   + cd * v_tfact.w) * v_colour;			   
		   
	vec4 smb = texture2D(shadmap, v_smb);
	float mu = max(0, dot(globalLight, -v_normal) + 0.5);
	
	vec3 reflectDir = reflect(-globalLight, v_normal);  
	float spec = max(pow(max(dot(viewDir, reflectDir), 0.0), 16), 0);
	mu += spec * 0.19;
	fragcolour.rgb = mu * fragcolour.rgb * (0.5 + 0.5 * smb.r);
	float m = v_isRoad * croad.a;
	fragcolour.rgb = mix(fragcolour.rgb, croad.rgb, m);
}


