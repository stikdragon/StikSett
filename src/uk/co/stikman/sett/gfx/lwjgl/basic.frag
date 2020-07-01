#version 150 core

uniform sampler2D txt;

uniform vec4 colour;

in vec2 v_uv;
in vec4 v_colour;
out vec4 fragcolour;

void main(void)
{
	fragcolour = texture2D(txt, v_uv) * v_colour * colour;
}