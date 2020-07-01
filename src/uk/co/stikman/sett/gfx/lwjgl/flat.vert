//ATTRIB:vertexPosition,vertexUV,vertexColour
#version 150 core

uniform mat4 view;

in vec2 vertexPosition;
in vec4 vertexColour;

out vec4 v_colour;

void main(void)
{
    gl_Position = view * vec4(vertexPosition, 0.0, 1.0);
   	v_colour = vertexColour;
}

