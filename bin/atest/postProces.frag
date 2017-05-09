#version 330
in vec2 texCoord;
layout (location=0) out vec4 outColor;
layout (location=1) out vec4 outColor1;
layout (location=2) out vec4 outColor2;
uniform sampler2D textureBack;
uniform sampler2D texture;
uniform sampler2D textureDepth;
uniform int switchShaderPost;
uniform float time;
vec3 color;
const float PI = 3.1415926535;

float LinearizeDepth(vec2 uv)
{
  float n = 0.1; 				// camera z near
  float f = 100.0; 				// camera z far
  float z = texture2D(textureDepth, uv).x;
  return (2.0 * n) / (f + n - z * (f - n));	
}

/* Tato verze sobelova operatoru pro detekci hran je pouzita z nejakeho tutorialu na netu. Bohuzel zpetne nemohu dohledat, odkud jsem to pouzil */
vec4 sobelDetection(){
			vec4 top         = texture2D(texture, vec2(texCoord.x, texCoord.y + 1.0 / 500.0));
			vec4 bottom      = texture2D(texture, vec2(texCoord.x, texCoord.y - 1.0 / 500.0));			
			vec4 left        = texture2D(texture, vec2(texCoord.x - 1.0 / 500.0, texCoord.y));
			vec4 right       = texture2D(texture, vec2(texCoord.x + 1.0 / 500.0, texCoord.y));
			vec4 topLeft     = texture2D(texture, vec2(texCoord.x - 1.0 / 500.0, texCoord.y + 1.0 / 500.0));
			vec4 topRight    = texture2D(texture, vec2(texCoord.x + 1.0 / 500.0, texCoord.y + 1.0 / 500.0));
			vec4 bottomLeft  = texture2D(texture, vec2(texCoord.x - 1.0 / 500.0, texCoord.y - 1.0 / 500.0));
			vec4 bottomRight = texture2D(texture, vec2(texCoord.x + 1.0 / 500.0, texCoord.y - 1.0 / 500.0));
			vec4 sx = -topLeft - 2 * left - bottomLeft + topRight   + 2 * right  + bottomRight;
			vec4 sy = -topLeft - 2 * top  - topRight   + bottomLeft + 2 * bottom + bottomRight;
			return sqrt(sx * sx + sy * sy);
}

void main() {

	switch(switchShaderPost){
		case 1:
			//float delta = 10.0/512.0;
			float delta = 0.003; // HODNOTA ROZMAZANI
			color = 4.0*texture2D(texture, texCoord).rgb; 
			color += 2*texture2D(texture, fract(texCoord+vec2(delta, 0))).rgb; 
			color += 2*texture2D(texture, fract(texCoord+vec2(-delta, 0))).rgb; 
			color += 2*texture2D(texture, fract(texCoord+vec2(0, -delta))).rgb; 
			color += 2*texture2D(texture, fract(texCoord+vec2(0, delta))).rgb; 
			color += texture2D(texture, fract(texCoord+vec2(delta, delta))).rgb; 
			color += texture2D(texture, fract(texCoord+vec2(delta, -delta))).rgb; 
			color += texture2D(texture, fract(texCoord+vec2(-delta, -delta))).rgb; 
			color += texture2D(texture, fract(texCoord+vec2(-delta, delta))).rgb; 
			outColor = vec4(color/16.0,1.0);
			break;
		case 2: // grayscale by eye
			color = texture2D(texture, texCoord).rgb;
			float avg = 0.299*color.r+0.587*color.b+0.114*color.g; 	
			outColor = vec4(vec3(avg),1.0); 
			break;
		case 3: // poor grayscale
			color = texture2D(texture, texCoord).rgb;
			float avg2 = (color.r + color.g + color.b) / 3.0; 	
			outColor = vec4(vec3(avg2),1.0); 
			break;
		case 4: // inverse
			color = texture2D(texture, texCoord).rgb;
			vec4 whiteColor = vec4(1.0);			 
			outColor = whiteColor - vec4(color,1.0);
			break;
		case 5: // sobel
			
			float g = length(sobelDetection());
			g = smoothstep(0.2, 0.8, g);									
			vec4 colorZaklad = vec4(texture2D(texture, texCoord).rgb,1.0);
			
			int r2 = int(colorZaklad.r * 0xffff); 
			int g2 = int(colorZaklad.g * 0xffff);
			int b2 = int(colorZaklad.b * 0xffff);
			
			r2 = r2 / 256;
			g2 = g2 / 256;
			b2 = b2 / 256;
			
			colorZaklad.r = colorZaklad.r * r2  / 255;
			colorZaklad.g = colorZaklad.g * g2  / 255;
			colorZaklad.b = colorZaklad.b * b2  / 255;						
			
			vec4 edges = vec4(mix(colorZaklad.xyz, vec3(0.0, 0.0, 0.0), g), 1.);											
			outColor = edges;
			break;
		case 6: // depth map
			color = texture2D(textureDepth, texCoord).rgb;
			outColor = vec4(color, 1.0);
			break;
		case 7: // Boxes
			color = texture2D(texture, texCoord + 0.005*vec2( sin(time+500.0*texCoord.x), cos(time+500.0*texCoord.y))).rgb;
			outColor = vec4(color, 1.0);
			break;
		case 8: // fish eye
			 float aperture = 178.0;
			 float apertureHalf = 0.5 * aperture * (PI / 180.0);
			 float maxFactor = sin(apertureHalf);
			 
			 vec2 uv;
			 vec2 xy = 2.0 * texCoord.xy - 1.0;
			 float d = length(xy);
			 if (d < (2.0-maxFactor))
			 {
			 	  d = length(xy * maxFactor);
	              float z = sqrt(1.0 - d * d);
			      float r = atan(d, z) / PI;
			      float phi = atan(xy.y, xy.x);
			    
   			      uv.x = r * cos(phi) + 0.5;
			      uv.y = r * sin(phi) + 0.5;
			  }
			 else
			 {
			   uv = texCoord.xy;
			 }
			vec4 c = texture2D(texture, uv);
			outColor = c;
			break;
		case 9: // linearized depth map
					  
			float d2;		
			d2 = LinearizeDepth(texCoord);			
			outColor = vec4(d2, d2, d2, 1.0);
			break;
		case 10: // BASIC STATE											 	
			outColor = vec4(texture2D(texture, texCoord).rgb,1.0);
			break;
	}			
	vec4 sobel = sobelDetection();
	outColor1 = sobel;
		
	float d3;		
	d3 = LinearizeDepth(texCoord);			
	outColor2 = vec4(d3, d3, d3, 1.0);
	
	
	 
} 
