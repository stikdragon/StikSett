//ATTRIB:vertexPosition,vertexColour,vertexNormal
#version 460 core

uniform mat4 view;
uniform mat4 model;
uniform mat4 proj;
uniform vec3 globalLight;

in vec3 vertexPosition;
in vec4 vertexColour;
in vec2 vertexUV;

out vec4 v_colour;
out vec2 v_uv;

void main(void)
{
    gl_Position = proj * view * model * vec4(vertexPosition, 1.0);
   	v_colour = vertexColour;
   	v_uv = vertexUV;
}
