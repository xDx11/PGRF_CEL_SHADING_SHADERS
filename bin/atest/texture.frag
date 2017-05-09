#version 330
in vec2 texCoord;
out vec4 outColor2;
uniform sampler2D tex;

void main() {
	outColor2 = vec4(0.0, 0.0, 0.0, 1.0);
	
			vec4 top         = texture2D(tex, vec2(texCoord.x, texCoord.y + 1.0 / 500.0));
			vec4 bottom      = texture2D(tex, vec2(texCoord.x, texCoord.y - 1.0 / 500.0));			
			vec4 left        = texture2D(tex, vec2(texCoord.x - 1.0 / 500.0, texCoord.y));
			vec4 right       = texture2D(tex, vec2(texCoord.x + 1.0 / 500.0, texCoord.y));
			vec4 topLeft     = texture2D(tex, vec2(texCoord.x - 1.0 / 500.0, texCoord.y + 1.0 / 500.0));
			vec4 topRight    = texture2D(tex, vec2(texCoord.x + 1.0 / 500.0, texCoord.y + 1.0 / 500.0));
			vec4 bottomLeft  = texture2D(tex, vec2(texCoord.x - 1.0 / 500.0, texCoord.y - 1.0 / 500.0));
			vec4 bottomRight = texture2D(tex, vec2(texCoord.x + 1.0 / 500.0, texCoord.y - 1.0 / 500.0));
			vec4 sx = -topLeft - 2 * left - bottomLeft + topRight   + 2 * right  + bottomRight;
			vec4 sy = -topLeft - 2 * top  - topRight   + bottomLeft + 2 * bottom + bottomRight;
			vec4 sobel = sqrt(sx * sx + sy * sy);
			outColor2 = sobel;
	
} 