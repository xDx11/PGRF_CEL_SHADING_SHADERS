#version 330
in vec2 texCoord;
layout (location=0) out vec4 outEdgeDepth;
layout (location=1) out vec4 outEdgeNormal;
layout (location=2) out vec4 outEdgeColor;
layout (location=3) out vec4 outCelShading;
uniform sampler2D texNormal;
uniform sampler2D texDepth;
uniform sampler2D texColor;
uniform mat3 G[2] = mat3[](
	mat3( 1.0, 2.0, 1.0, 0.0, 0.0, 0.0, -1.0, -2.0, -1.0 ),
	mat3( 1.0, 0.0, -1.0, 2.0, 0.0, -2.0, 1.0, 0.0, -1.0 )
);


void main(void)
{
	mat3 I;
	float cnv[2];
	vec3 sample;
	vec4 baseColor;
	baseColor = texture2D(texColor,texCoord);
	
	/* SOBELUV OPERATOR POUZIT Z CIZIHO ZDROJE */
	/* ZDROJ: https://github.com/spite/Wagner/blob/master/fragment-shaders/sobel2-fs.glsl */
	/* fetch the 3x3 neighbourhood and use the RGB vector's length as intensity value */
	for (int i=0; i<3; i++)
	for (int j=0; j<3; j++) {
		sample = texelFetch( texNormal, ivec2(gl_FragCoord) + ivec2(i-1,j-1), 0 ).rgb;
		
		I[i][j] = length(sample); 
	}
	
	/* calculate the convolution values for all the masks */
	for (int i=0; i<2; i++) {
		float dp3 = dot(G[i][0], I[0]) + dot(G[i][1], I[1]) + dot(G[i][2], I[2]);
		cnv[i] = dp3 * dp3; 
	}
	vec4 edgeNormal = vec4(sqrt(cnv[0]*cnv[0]+cnv[1]*cnv[1]));
	float g1 = sqrt(cnv[0]*cnv[0]+cnv[1]*cnv[1]);
	g1 = smoothstep(0.2, 0.8, g1);
	
				
				/* fetch the 3x3 neighbourhood and use the RGB vector's length as intensity value */
				for (int i=0; i<3; i++)
				for (int j=0; j<3; j++) {
					sample = texelFetch( texColor, ivec2(gl_FragCoord) + ivec2(i-1,j-1), 0 ).rgb;
					I[i][j] = length(sample); 
					//I[i][j] = texture2D(texColor,texCoord);
				}
				
				/* calculate the convolution values for all the masks */
				for (int i=0; i<2; i++) {
					float dp3 = dot(G[i][0], I[0]) + dot(G[i][1], I[1]) + dot(G[i][2], I[2]);
					cnv[i] = dp3 * dp3; 
				}
				float g2 = sqrt(cnv[0]*cnv[0]+cnv[1]*cnv[1]);
				g2 = smoothstep(0.2, 0.8, g2);
				vec4 edgeColor = vec4(sqrt(cnv[0]*cnv[0]+cnv[1]*cnv[1]));					
	
	
	outEdgeNormal = edgeNormal;
	outEdgeColor = edgeColor;	
	outEdgeDepth = mix(edgeNormal, edgeColor, 0.7);
	vec4 edges = outEdgeDepth;
	
			int Rgb = int(baseColor.r * 0xffff); 
			int rGb = int(baseColor.g * 0xffff);
			int rgB = int(baseColor.b * 0xffff);
			
			Rgb = Rgb / 256;
			rGb = rGb / 256;
			rgB = rgB / 256;
			
			baseColor.r = baseColor.r * Rgb  / 255;
			baseColor.g = baseColor.g * rGb  / 255;
			baseColor.b = baseColor.b * rgB  / 255;	
	
	vec3 eColor = vec3(0.0, 0.0, 0.0);
	vec4 edgesN = vec4(mix(baseColor.xyz, eColor*edgeColor.xyz, g1), 1.);
	vec4 edgesC = vec4(mix(baseColor.xyz, eColor*edgeNormal.xyz, g2), 1.);
	edges = edgesN*0.5 + edgesC*0.5;

	outCelShading = edges;
	
}
