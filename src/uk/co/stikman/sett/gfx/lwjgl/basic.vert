//ATTRIB:vertexPosition,vertexUV,vertexColour
#version 150 core

uniform mat4 view;

in vec2 vertexPosition;
in vec2 vertexUV;
in vec4 vertexColour;
//in vec4 vertexNormal;

out vec2 v_uv;
out vec4 v_colour;

void main(void)
{
    gl_Position = view * vec4(vertexPosition, 0.0, 1.0);
   	v_uv = vertexUV;
   	v_colour = vertexColour;
}

