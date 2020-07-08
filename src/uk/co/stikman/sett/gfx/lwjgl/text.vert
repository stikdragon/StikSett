//ATTRIB:vertexPosition,vertexUV,vertexColour,vertexNormal
#version 150 core

uniform mat4 proj;
uniform vec4 colourOverrideColour;
uniform float colourOverride;
uniform vec2 offset;

in vec2 vertexPosition;
in vec2 vertexUV;
in vec4 vertexColour;
in vec4 vertexNormal;

out vec4 v_colour;
out vec2 v_uv;


void main(void)
{
    gl_Position =  proj * vec4(vertexPosition + offset, 0.0, 1.0);
   	v_colour = mix(vertexColour, colourOverrideColour, colourOverride);
   	v_uv = vertexUV;
}

