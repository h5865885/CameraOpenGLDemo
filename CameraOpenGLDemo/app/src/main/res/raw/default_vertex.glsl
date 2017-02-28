attribute vec4 vPosition;
attribute vec2 inputTextureCoordinate;

//uniform mat4 textureTransform;
varying vec2 textureCoordinate;

void main()
{
	textureCoordinate = inputTextureCoordinate.xy;
	gl_Position = vPosition;
}
