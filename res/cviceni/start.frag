#version 330
in vec3 position;
in vec3 positionBack;
in vec3 normal;
in vec3 transNormal;
in vec3 lightDirection;
in vec3 ldForReflect;
in vec3 viewDirection;
in vec3 spotDirection;
in vec2 texCoord;
in float dist;
layout (location=0) out vec4 outColor0;
layout (location=1) out vec4 outColor1;
layout (location=2) out vec4 outNormal;
uniform int lightMode;
uniform int reflectorMode;
uniform sampler2D tex;
uniform sampler2D texNormal;
uniform sampler2D texHeight;
vec3 ld;
vec3 ldR;
vec3 nd;
vec3 vd;
float NdotL;
vec3 halfVector;
float NdotH;
vec4 color_base;
vec4 totalAmbient; 
vec4 totalDiffuse;  
vec4 totalSpecular;
vec4 color0 = vec4(0.8, 0.0, 0.0, 1.0); // Material Color:         
vec4 color1 = vec4(0.0, 0.0, 0.0, 1.0); // Silhouette Color:		         
vec4 color2 = vec4(0.8, 0.0, 0.0, 1.0); // Specular Color:
void main() {

					//material 
	vec4 mambient = vec4(1.0,0.7,0.7, 1.0); 
	vec4 mdiffuse = vec4(0.7,0.7,0.7, 1.0); 
	vec4 mspecular = vec4(1.0,1.0,1.0, 1.0); 
					//svìtlo 
	vec4 lambient = vec4(0.2,0.2,0.2, 1.0); 
	vec4 ldiffuse = vec4(0.8,0.8,0.8, 1.0); 
	vec4 lspecular = vec4(1.0,1.0,1.0, 1.0);
	
					//utlum svetla
	float constantAttenuation = 1.0;
	float linearAttenuation = 0.001;
	//float quadraticAttenuation = 0.001;
	float quadraticAttenuation = 0.0008;
	float att = 1.0 / (constantAttenuation + linearAttenuation*dist + quadraticAttenuation*dist*dist);
	
					// charakteristiky zrcadlove slozky a reflektoru
	float specularPower = 32.0;			
	float spotCutOff = 0.95; // 0.99
	float cutOff = 0.91;
	
	switch(lightMode){
		case 1: //Position			
			outColor0 = vec4(normalize(position.xyz), 1.0);						
			break;
		case 2: //Color			
			vec3 pos = normalize(position.xyz);					
			outColor0 = vec4((pos.x+1)/2,(pos.y+1)/2, (pos.z+1)/2, 1.0);			// prevest z <-1, 1> na <0, 1>
			break;
		case 3: //Normal						
			color_base = texture2D(tex, texCoord);			
			outColor0 = color_base;
			break;
		case 4:	//Diffuse						
			outColor0 = color0;
			float diffuse = max(dot(normalize(lightDirection),normalize(transNormal)),0.0);
			   if (diffuse < 0.5) diffuse =0.5;
			   if (diffuse > 0.5) diffuse =0.7;
			   if (diffuse > 0.8) diffuse =0.9;
			   if (diffuse > 0.95) diffuse =1.0;
			outColor0 *= diffuse;
			break;
		case 5: // Blinn-Phong - Half vector
			ld = normalize(lightDirection); 
			nd = normalize(transNormal);
			vd = normalize(viewDirection);	
			NdotL = max(dot(ld,nd),0.0);
			
			vec3 halfVector = normalize( ld + vd);
			float NdotH = max(0.0 , dot( nd, halfVector));						 					
			
			totalAmbient = mambient * lambient;
			totalDiffuse = NdotL * color0 * ldiffuse; 
			
			totalSpecular =  color2 * lspecular * (pow(NdotH, 4.0*specularPower));
			
			if(reflectorMode == 1){
				float spotEffect = dot(normalize(spotDirection), (-ld));				
				float epsilon = spotCutOff - cutOff;
				float intensity = clamp((spotEffect - spotCutOff) / epsilon, 0.0, 1.0);					
				if (spotEffect > spotCutOff){	
					outColor0 = totalAmbient + intensity * att * (totalDiffuse + totalSpecular);
					float diffuse2 = max(dot(normalize(lightDirection),normalize(transNormal)),0.0);
					   if (diffuse2 < 0.5) diffuse2 =0.5;
					   if (diffuse2 > 0.5) diffuse2 =0.7;
					   if (diffuse2 > 0.8) diffuse2 =0.9;
					   if (diffuse2 > 0.95) diffuse2 =1.0;
					outColor0 *=diffuse;
				} else {
					outColor0 = totalAmbient;
				}
			} else {
				outColor0 = totalAmbient + att * (totalDiffuse + totalSpecular);
			}
			break;		
		case 6: // Blinn-Phong - Reflect vector
			ld = normalize(lightDirection); 
			nd = normalize(transNormal);
			vd = normalize(viewDirection);	
			NdotL = max(dot(ld, nd),0.0);
			
			//vec3 reflection=reflect(-ld, nd);			
			//vec3 reflection=normalize((( 2.0 * nd) * NdotL) - ld);
			vec3 reflection=normalize((( 2.0 * dot(ld, nd) * nd)) - ld);						
			float RdotV = max(0.0 , dot( vd, reflection));			
			
			totalAmbient = mambient * lambient;
			totalDiffuse = NdotL * mdiffuse * ldiffuse; 
			totalSpecular =  mspecular * lspecular * (pow(RdotV, specularPower));
			
			if(reflectorMode == 1){
				float spotEffect = dot(normalize(spotDirection), (-ld));				
				float epsilon = spotCutOff - cutOff;
				float intensity = clamp((spotEffect - spotCutOff) / epsilon, 0.0, 1.0);					
				if (spotEffect > spotCutOff){	
					outColor0 = totalAmbient + intensity * att * (totalDiffuse + totalSpecular);
				} else {
					outColor0 = totalAmbient;
				}
			} else {
				outColor0 = totalAmbient + att * (totalDiffuse + totalSpecular);
			}
			break;
		case 7: // Normal Mapping				
			vec3 lnormal = normalize(texture2D( texNormal, texCoord).xyz * 2.0 - 1.0);
			vec4 color_base = texture2D(tex, texCoord);			
			
			ld = normalize(lightDirection);
			ldR = normalize(ldForReflect); 			
			vd = normalize(viewDirection);						
				
			NdotL = max(dot(ld,lnormal),0.0);
			
			halfVector = normalize( ld + vd);
			NdotH = max(0.0 , dot( lnormal, halfVector));						 											
			
			totalAmbient = lambient * color_base;
			totalDiffuse = NdotL * ldiffuse * color_base; 
			totalSpecular =  lspecular * (pow(NdotH, 20.0));
			
			if(reflectorMode == 1){
				float spotEffect = dot(normalize(spotDirection), (-ldR));				
				float epsilon = spotCutOff - cutOff;
				float intensity = clamp((spotEffect - spotCutOff) / epsilon, 0.0, 1.0);					
				if (spotEffect > spotCutOff){	
					outColor0 = totalAmbient + intensity * att * (totalDiffuse + totalSpecular);
				} else {
					outColor0 = totalAmbient;
				}
			} else {
				outColor0 = totalAmbient + att * (totalDiffuse + totalSpecular);
			}			
			break;
		case 8: // Parallax mapping			
			ld = normalize(lightDirection); 			
			vd = normalize(viewDirection);
			ldR = normalize(ldForReflect);
			
			vec2 cBumbSize = vec2(0.04, -0.02);						
			cBumbSize = vec2(0.02, -0.02);			
			float height = texture2D(texHeight, texCoord).r;
			height = height * cBumbSize.x + cBumbSize.y;
			
			vec2 texUV = texCoord.xy + vd.xy * height;
			vec4 color_base2 = texture2D(tex, texUV); 
			vec3 lnormal2 = normalize(texture2D( texNormal, texUV).xyz * 2.0 - 1.0);
																		
				
			NdotL = max(dot(ld,lnormal2),0.0);
			
			halfVector = normalize( ld + vd);
			NdotH = max(0.0 , dot( lnormal2, halfVector));						 					
			
			totalAmbient = lambient * color_base2;
			totalDiffuse = NdotL * ldiffuse * color_base2; 
			totalSpecular =  lspecular * (pow(NdotH, 30.0));
			
			if(reflectorMode == 1){
				float spotEffect = dot(normalize(spotDirection), (-ldR));				
				float epsilon = spotCutOff - cutOff;
				float intensity = clamp((spotEffect - spotCutOff) / epsilon, 0.0, 1.0);					
				if (spotEffect > spotCutOff){	
					outColor0 = totalAmbient + intensity * att * (totalDiffuse + totalSpecular);
				} else {
					outColor0 = totalAmbient;
				}
			} else {
				outColor0 = totalAmbient + att * (totalDiffuse + totalSpecular);
			}
			break;
		case 9: //TOON SHADING ver 1
			ld = normalize(lightDirection); 
			nd = normalize(transNormal);
			vd = normalize(viewDirection);
			halfVector = normalize(ld + vd);	
			float sil = max(dot(nd,vd), 0.0);
			if (sil < 0.3) 
			{
				outColor0 = color1;
			}
			else 
			 {
			   outColor0 = color0;
			   float diffuse = max(dot(nd,ld),0.0);
			   if (diffuse < 0.5) outColor0 *=0.8;
			   float spec = pow(max(dot(nd,halfVector),0.0), 4.0*specularPower);
			   if (spec < 0.2) outColor0 *= 0.8;
			   else outColor0 = color2;			   
			   
			 }			
			break;
			
		case 10: //TOON SHADING ver 2.0
			
			ld = normalize(lightDirection); 
			nd = normalize(transNormal);
			vd = normalize(viewDirection);
			halfVector = normalize(ld + vd);	
			float sil2 = max(dot(nd,vd), 0.0);
			if (sil2 < 0.3) 
			{
				outColor0 = color1;
			}
			else 
			 {
			   outColor0 = color0;
			   float diffuse = max(dot(nd,ld),0.0);
			   if (diffuse < 0.5) diffuse =0.5;
			   if (diffuse > 0.5) diffuse =0.7;
			   if (diffuse > 0.8) diffuse =0.9;
			   if (diffuse > 0.95) diffuse =1.0;
			   outColor0 *=diffuse;
			   float spec = pow(max(dot(nd,halfVector),0.0), 4.0*specularPower);
			   if (spec < 0.2) outColor0 *= 0.8;
			   else outColor0 = color2;			   			   
			 }			
			break;
	}
	
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
			outColor1 = sobel;
		
			outNormal = vec4(normalize(normal), 1.0);
			

		
	
} 
