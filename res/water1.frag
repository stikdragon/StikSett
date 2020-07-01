//
// this is mostly from 
//   https://www.shadertoy.com/view/MdlXz8
// with a few modifications to mix with a texture, etc
// Thanks to Dave_Hoskins, and joltz0r before him
// 

#version 460 core

uniform float time;
uniform sampler2D txt;

in vec4 v_colour;
in vec2 v_uv;

out vec4 fragcolour;

#define TAU 6.28318530718
#define MAX_ITER 5

void main (void)
{
    // uv should be the 0-1 uv of texture...
	vec2 uv = v_uv;
	
    vec2 p = mod(uv*TAU, TAU)-250.0;
	vec2 i = vec2(p);
	float c = 1.0;
	float inten = .005;

	for (int n = 0; n < MAX_ITER; n++) 
	{
		float t = time * 0.3 * (1.0 - (3.5 / float(n+1)));
		i = p + vec2(cos(t - i.x) + sin(t + i.y), sin(t - i.y) + cos(t + i.x));
		c += 1.0/length(vec2(p.x / (sin(i.x+t)/inten),p.y / (cos(i.y+t)/inten)));
	}
	c /= float(MAX_ITER);
	c = 1.17-pow(c, 1.4);
	vec3 colour = vec3(pow(abs(c), 8.0));
    colour = clamp(colour + vec3(0.0, 0.35, 0.5), 0.0, 1.0);
	fragcolour = vec4(colour, 1.0 - max(colour.r, 0.4));
    fragcolour.rgb = mix(fragcolour.rgb, texture2D(txt, uv * 0.4 - (time / 13)).rgb, 0.7);
}

