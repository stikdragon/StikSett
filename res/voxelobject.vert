//ATTRIB:vertexPosition,vertexColour,vertexNormal
#version 460 core

uniform mat4 view;
uniform mat4 model;
uniform mat4 proj;
uniform vec3 offset;
uniform vec3 globalLight;

in vec3 vertexPosition;
in vec4 vertexColour;
in vec3 vertexNormal;
in vec2 vertexUV;

out vec4 v_colour;
out vec3 v_normal;
out vec2 v_uv;


void main(void)
{
	mat3 nm = transpose(inverse(mat3(view * model)));
	vec3 norm = normalize(nm * vertexNormal);

    gl_Position =  proj * view * (model * vec4(vertexPosition, 1.0) + vec4(offset, 0.0));
    float mu = max(0, dot(globalLight, -norm)) + 0.5;
   	v_colour = vec4(vertexColour.xyz * mu, 1.0);
   	v_uv = vertexUV;
}

