#version 330
in vec3 inPosition;
out vec2 texCoord;
uniform mat4 viewMat;
uniform mat4 projMat;
uniform int functionType;
float zetko;
float PI = 3.141592653589793238462643383279;

float getZetko(float x, float y){
	return cos(sqrt(x*x + y*y));
}

vec3 getNavySailCartesianFunc2(vec2 uv){
	uv -= 0.5; 		// zmena na <-0.5, 0.5>
	uv *= 2;		// zmena na <-1.0, 1.0>
	
	float s = uv.x;
	float t = uv.y;
	vec3 positSail;
	positSail.x = cos(s)*cos(s)+cos(t)*cos(s)*8;
	positSail.y = sin(s)+cos(t)*sin(s)*2;
	positSail.z = sin(t)*cos(s)*3;
	positSail = positSail*2;
	return positSail;
}

vec3 getOwnCartesianFunc3(vec2 uv){
	float s = uv.x * 2 * PI;
	float t = uv.y * 2 * PI;
	vec3 pos;
	pos.x = cos(s) + cos(t) * cos(s);
	pos.y = sin(s) + cos(t) * sin(s);
	pos.z = sin(t);
	pos *= 2;	
	return pos;
}

vec3 getCosCartesianFunc1(vec2 uv){
	vec3 positCos;
	uv -= 0.5; 		// zmena na <-0.5, 0.5>
	uv *= 2;		// zmena na <-1.0, 1.0>	
	uv *= 10;
	zetko = getZetko(uv.x, uv.y);
	positCos = vec3(uv.x, uv.y, zetko);
	return positCos;
}

vec3 cylinderFunc1(vec2 uv){
	float t = uv.x * 2 * PI;
	float s = uv.y * 2 * PI;
	
	float R = t;
	float azimuth = s;
	float v = 2 * sin(t);
	
	uv.x = R * cos(azimuth);
	uv.y = R * sin(azimuth);
	zetko = v;
	
	vec3 posit = vec3(uv.x, uv.y, zetko);
	return posit;
}

vec3 cylinderFunc2(vec2 uv){
	uv.x *= 2 * PI;
	uv.y *= 2 * PI;
	float t = uv.x; 
	float s = uv.y;			
	
	float R = (1+max(sin(t),0))*0.5*t;
	float azimuth = s;
	float v = 3-t;
	
	uv.x = R * cos(azimuth);
	uv.y = R * sin(azimuth);
	zetko = v;

	vec3 posit = vec3(uv.x, uv.y, zetko);
	return posit;
}

vec3 cylinderFunc3(vec2 uv){		
	uv.x *= 2*PI;
	uv.x -= PI;
	uv.y *= 2*PI;
	float t = uv.x; 
	float s = uv.y;	
		
	float R = sin(t)-cos(t);
	float azimuth = s+sin(t)-cos(t);
	float v = t+sin(t);
	
	uv.x = -R * cos(azimuth);
	uv.y = R * sin(azimuth);
	zetko = v;

	vec3 posit = vec3(uv.x, uv.y, zetko);
	return posit;
}

vec3 sfericFunc1(vec2 uv){
	float t = uv.x * PI;
	float s = uv.y * 2 * PI;
	float R = 6;
	float azimuth = s;
	float zenit = t;
	
	uv.x = R * sin(zenit) * cos(azimuth);
	uv.y = R * sin(zenit) * sin(azimuth);
	zetko = R * cos(zenit);
	vec3 posit = vec3(uv.x,uv.y,zetko);
	return posit;			
}

vec3 sfericFunc2(vec2 uv){
	float t = uv.x * PI;
	float s = uv.y * 2 * PI;
	float R = 3 + cos(4*s);
	float azimuth = s;
	float zenit = t;
	
	uv.x = R * sin(zenit) * cos(azimuth);
	uv.y = R * sin(zenit) * sin(azimuth);
	zetko = R * cos(zenit);
	vec3 posit = vec3(uv.x,uv.y,zetko);
	return posit;
}

vec3 sfericFunc3(vec2 uv){
	float t = uv.x * 4;
	t -= 1;
	float s = uv.y * 2 * PI;
	float R = t;
	float zenit = -0.5*s*cos(t);
	float azimuth = 2*s*t*0.3*sin(t);
	
	uv.x = R * sin(zenit) * cos(azimuth);
	uv.y = R * sin(zenit) * sin(azimuth);
	zetko = R * cos(zenit);
	vec3 posit = vec3(uv.x,uv.y,zetko);
	return posit;
}

vec3 function(vec2 uv){
	vec3 posit;
	switch(functionType){
		case 1:
			posit = 10*vec3(uv.x, uv.y, 0.0);
			break;
		case 2:
			posit = getCosCartesianFunc1(uv);
			break;
		case 3:
			posit = getNavySailCartesianFunc2(uv);
			break;
		case 4:
			posit = getOwnCartesianFunc3(uv);
			break;		
		case 5:
			posit = cylinderFunc1(uv);
			break;
		case 6:
			posit = cylinderFunc2(uv);
			break;
		case 7:
			posit = cylinderFunc3(uv);
			break;
		case 8:
			posit = sfericFunc1(uv);
			break;		
		case 9:
			posit = sfericFunc2(uv);
			break;
		case 10:
			posit = sfericFunc3(uv);
			break;
	}		
	return posit;
}

void main() {	
	vec2 backPos = vec2(inPosition);
	backPos = backPos*1.01;
	vec3 positionBack = function(backPos);	
	texCoord = vec2(inPosition);		
		
	gl_Position = projMat * viewMat * vec4(positionBack.x, positionBack.y, positionBack.z, 1.0);
} 
