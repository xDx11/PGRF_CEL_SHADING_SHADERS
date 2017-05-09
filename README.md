# PGRF_CEL_SHADING_SHADERS
Computer Graphic - Java OpenGL, GLSL - Student project (PGRF3 - FIM - UHK) - Shaders - Cel Shading + Filters

Date: 2016

What is  Cel shading?
[https://en.wikipedia.org/wiki/Cel_shading](https://en.wikipedia.org/wiki/Cel_shading)


## Shader - Per Pixel

	- Model = Options for upper and bottom part
			- GRID
			- Cartesian function 3x
			- Cylinder function 3x
			- Sferic function 3x
      
	- [I] = Change texture = Options for upper and bottom part
		- Pikachu
		- South Park
		- Bricks 1
		- Bricks 2
		- Sand
		- Panel
    
	- Light mode = Options for upper part
			- Position
			- Color
			- Texture
			- Diffusion
			- Blinn-Phong - Half vector
			- Blinn-Phong - Reflect vector
			- Normal mapping
				- only for textures with normal and height map
			- Parallax mapping
				- only for textures with normal and height map
			- Tone shading v1.0
			- Tone shading School version

	- [C] = POST PROCESSING = Options for bottom part
		- Sobel operator for detection edges + Cel shading
		- Depth map
		- Flow boxes?
		- Fish eye
		- Depth map - linearized
		- Blure
		- Grayscale colors for eye
		- Grayscale colors
		- Inverse Colors

	- [N], [M] - Absolute position of camera


### Example options for Cel shading

	- Model:		GRID
	- Light mode:		Texture
	- Post processing:	Sobel operator for detection edges + Cel shading
