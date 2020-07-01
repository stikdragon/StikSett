#version 150 core

uniform vec4 colour;

in vec4 v_colour;
out vec4 fragcolour;

void main(void)
{
	fragcolour = v_colour * colour;
}