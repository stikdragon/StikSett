//ATTRIB:vertexPosition,vertexColour,vertexNormal,vertexOverrideColour,vertexOcclude
#version 460 core

uniform mat4 view;
uniform mat4 model;
uniform mat4 proj;
uniform vec3 offset;
uniform vec3 globalLight;
uniform vec3 overrideColour;

in vec3 vertexPosition;
in vec4 vertexColour;
in vec3 vertexNormal;
in vec2 vertexUV;
in float vertexOcclude; // 0 is fully occluded, 1 is not occluded

//
// 1 or 0 if this is a "special" colour that should be 
// multiplied by vertexOverrideColour
//
in float vertexOverrideColour;

out vec4 v_colour;
out vec3 v_normal;
out vec2 v_uv;


void main(void)
{
	mat3 nm = transpose(inverse(mat3(model)));
	vec3 norm = normalize(nm * vertexNormal);

    gl_Position =  proj * view * (model * vec4(vertexPosition, 1.0) + vec4(offset, 0.0));
    float mu = max(0, dot(globalLight, -norm)) + 0.5;
    mu *= (1.0 - exp(-3.5 * vertexOcclude - 0.6));
    vec3 ovr = vertexColour.xyz * overrideColour.xyz;
    vec3 c = mix(vertexColour.xyz, ovr, vertexOverrideColour);

   	v_colour = vec4(c * mu, 1.0);
   	v_uv = vertexUV;
}

