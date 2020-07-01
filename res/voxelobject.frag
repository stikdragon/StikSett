#version 460 core

#define PI 3.14159

uniform vec4 colour;


in vec4 v_colour;
in vec2 v_uv;

out vec4 fragcolour;

void main(void) {	
	
	// give each voxel a subtle border
   // float fx = 0.02*(1.0/cos(v_uv.x*PI-PI/2.0)-1.0);
    //float fy = 0.02*(1.0/cos(v_uv.y*PI-PI/2.0)-1.0);
   // float f = clamp(max(fx, fy), 0.0, 1.0);	
	
	//fragcolour = vec4(v_colour.xyz * (1.0-f), 1.0);
	fragcolour = v_colour;
}


