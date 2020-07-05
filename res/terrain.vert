//ATTRIB:vertexPosition,vertexUV,vertexColour,tfact,txt,vertexNormal,shadowUV,isRoad
#version 460 core

uniform mat4 view;
uniform mat4 model;
uniform mat4 proj;
uniform vec3 offset;

in vec3 vertexPosition;
in vec2 vertexUV;
in vec4 vertexColour;
in vec3 vertexNormal;
in vec4 tfact;
in vec4 txt;
in vec2 shadowUV;
in float isRoad;

out vec4 v_colour;
out vec4 v_tfact;
out vec4 v_txt;
out vec2 v_uv;
out vec2 v_uvRoad;
out vec2 v_smb;
out vec3 v_normal;
out vec3 viewDir;
out float v_isRoad;

void main(void)
{
    gl_Position = proj * view * model * vec4(vertexPosition + offset, 1.0);
   	v_colour = vertexColour;
   	v_tfact = tfact;
   	v_txt = txt;
   	v_uv = vertexUV;
   	v_smb = shadowUV;
   	v_normal = vertexNormal;
   	v_isRoad = isRoad;
   	viewDir = normalize((view * vec4(1, 0, 0, 1)).xyz);
}

