#version 150 core

uniform sampler2D txt;

in vec4 v_colour;
in vec2 v_uv;

out vec4 fragcolour;


void main(void)
{
	fragcolour = texture2D(txt, v_uv) * v_colour;
}